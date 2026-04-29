package main;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenRouterAIService implements AIService {
    private static final String ENDPOINT = "https://openrouter.ai/api/v1/chat/completions";
    private static final String DEFAULT_MODEL = "openrouter/free";
    private static final String EXTERNAL_SOURCE_LABEL = "OpenRouter external AI";
    private static final String FALLBACK_SOURCE_LABEL = "Local valuation engine (fallback)";
    private static final Pattern PRICE_PATTERN = Pattern.compile(
        "(?i)\"(?:price|estimated_price|estimatedPrice)\"\\s*:\\s*([0-9]+(?:\\.[0-9]{1,2})?)"
    );
    private static final Pattern REASON_PATTERN = Pattern.compile(
        "(?i)\"reason\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\""
    );
    private static final Pattern LABELED_PRICE_PATTERN = Pattern.compile(
        "(?i)(?:price|estimated price|estimate|value)\\D{0,12}\\$?([0-9]+(?:\\.[0-9]{1,2})?)"
    );
    private static final Pattern CURRENCY_PATTERN = Pattern.compile(
        "\\$\\s*([0-9]+(?:\\.[0-9]{1,2})?)"
    );

    private final AIService fallbackService;
    private final HttpClient httpClient;
    private final String apiKey;
    private final List<String> models;

    private static final class OpenRouterException extends IOException {
        private OpenRouterException(String message) {
            super(message);
        }
    }

    public OpenRouterAIService(AIService fallbackService) {
        this(
            fallbackService,
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build(),
            EnvLoader.get("OPENROUTER_API_KEY"),
            readModels()
        );
    }

    OpenRouterAIService(
        AIService fallbackService,
        HttpClient httpClient,
        String apiKey,
        List<String> models
    ) {
        this.fallbackService = fallbackService;
        this.httpClient = httpClient;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.models = models == null || models.isEmpty() ? List.of(DEFAULT_MODEL) : models;
    }

    @Override
    public EvaluationResult evaluate(Phone phone) {
        EvaluationResult fallbackResult = fallbackService.evaluate(phone);
        String fallbackExplanation = fallbackResult.getExplanation();
        String retrievalContext = fallbackResult.getRetrievalContext();
        double fallbackPrice = phone.getEstimatedPrice();

        if (apiKey.isEmpty()) {
            return new EvaluationResult(
                fallbackExplanation,
                FALLBACK_SOURCE_LABEL + ": API key not configured (.env or environment variable required)",
                false,
                retrievalContext
            );
        }

        List<String> attempted = new ArrayList<>();
        String lastError = "API unavailable";

        for (int i = 0; i < models.size(); i++) {
            String model = models.get(i);
            attempted.add(model);
            try {
                String responseBody = callOpenRouter(phone, fallbackPrice, fallbackExplanation, retrievalContext, model);
                String assistantContent = extractAssistantContent(responseBody);
                if (assistantContent.isEmpty()) {
                    lastError = "empty API response from " + model;
                    continue;
                }

                Double aiPrice = extractPrice(assistantContent);
                String aiReason = extractReason(assistantContent);

                if (aiReason.isEmpty()) {
                    lastError = "invalid API explanation format from " + model;
                    continue;
                }

                if (aiPrice == null) {
                    lastError = "API missing price from " + model;
                    continue;
                }

                phone.setEstimatedPrice(aiPrice);
                return new EvaluationResult(
                    aiReason + " Estimated with OpenRouter using vector-retrieved references.",
                    EXTERNAL_SOURCE_LABEL + ": " + model,
                    true,
                    retrievalContext
                );
            } catch (IOException | InterruptedException | RuntimeException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    lastError = summarizeError(e);
                    break;
                }
                lastError = summarizeError(e);
            }
        }

        phone.setEstimatedPrice(fallbackPrice);
        return new EvaluationResult(
            fallbackExplanation,
            FALLBACK_SOURCE_LABEL + ": " + lastError + " | attempted " + String.join(", ", attempted),
            false,
            retrievalContext
        );
    }

    private String callOpenRouter(
        Phone phone,
        double fallbackPrice,
        String fallbackExplanation,
        String retrievalContext,
        String model
    )
        throws IOException, InterruptedException {
        String payload = buildPayload(phone, fallbackPrice, fallbackExplanation, retrievalContext, model);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ENDPOINT))
            .timeout(Duration.ofSeconds(45))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .header("HTTP-Referer", "https://github.com/")
            .header("X-OpenRouter-Title", "Phone Evaluation CLI")
            .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new OpenRouterException(
                "HTTP " + response.statusCode() + " - " + compactBody(response.body())
            );
        }
        return response.body();
    }

    private String buildPayload(
        Phone phone,
        double fallbackPrice,
        String fallbackExplanation,
        String retrievalContext,
        String model
    ) {
        String systemPrompt = "You estimate used phone resale prices in the United States."
            + " Consider model, storage, damage, and the retrieved reference phones."
            + " Return only a valid JSON object with keys price and reason."
            + " Do not include markdown, code fences, or any extra text."
            + " Example: {\"price\":699.00,\"reason\":\"...\"}.";

        String userPrompt = String.format(
            Locale.US,
            "Phone model: %s\nStorage: %dGB\nDamage: %s\nVector database retrieval:\n%s\nLocal baseline price: $%.2f\nLocal baseline reasoning: %s",
            phone.getModel(),
            phone.getStorage(),
            phone.getDamageDescription(),
            retrievalContext,
            fallbackPrice,
            fallbackExplanation
        );

        return "{"
            + "\"model\":\"" + escapeJson(model) + "\","
            + "\"temperature\":0.2,"
            + "\"max_tokens\":320,"
            + "\"reasoning\":{\"exclude\":true},"
            + "\"response_format\":{"
            + "\"type\":\"json_schema\","
            + "\"json_schema\":{"
            + "\"name\":\"phone_valuation\","
            + "\"strict\":true,"
            + "\"schema\":{"
            + "\"type\":\"object\","
            + "\"properties\":{"
            + "\"price\":{\"type\":\"number\",\"description\":\"Estimated resale price in USD.\"},"
            + "\"reason\":{\"type\":\"string\",\"description\":\"One concise sentence explaining the estimate.\"}"
            + "},"
            + "\"required\":[\"price\",\"reason\"],"
            + "\"additionalProperties\":false"
            + "}"
            + "}"
            + "},"
            + "\"messages\":["
            + "{\"role\":\"system\",\"content\":\"" + escapeJson(systemPrompt) + "\"},"
            + "{\"role\":\"user\",\"content\":\"" + escapeJson(userPrompt) + "\"}"
            + "]"
            + "}";
    }

    private String extractAssistantContent(String json) {
        int messageIndex = json.indexOf("\"message\"");
        if (messageIndex < 0) {
            return "";
        }
        int contentKeyIndex = json.indexOf("\"content\"", messageIndex);
        if (contentKeyIndex < 0) {
            return "";
        }
        int colonIndex = json.indexOf(':', contentKeyIndex);
        if (colonIndex < 0) {
            return "";
        }
        int startQuote = json.indexOf('"', colonIndex + 1);
        if (startQuote < 0) {
            return "";
        }
        return readJsonString(json, startQuote + 1);
    }

    private String readJsonString(String json, int start) {
        StringBuilder builder = new StringBuilder();
        boolean escaping = false;

        for (int i = start; i < json.length(); i++) {
            char current = json.charAt(i);
            if (escaping) {
                if (current == 'n') {
                    builder.append('\n');
                } else if (current == 't') {
                    builder.append('\t');
                } else if (current == 'r') {
                    builder.append('\r');
                } else if (current == 'u' && i + 4 < json.length()) {
                    String hex = json.substring(i + 1, i + 5);
                    try {
                        builder.append((char) Integer.parseInt(hex, 16));
                    } catch (NumberFormatException e) {
                        builder.append("\\u").append(hex);
                    }
                    i += 4;
                } else {
                    builder.append(current);
                }
                escaping = false;
                continue;
            }

            if (current == '\\') {
                escaping = true;
                continue;
            }
            if (current == '"') {
                return builder.toString();
            }
            builder.append(current);
        }

        return builder.toString();
    }

    private Double extractPrice(String content) {
        String sanitized = sanitizeAssistantContent(content);

        Matcher matcher = PRICE_PATTERN.matcher(sanitized);
        boolean found = matcher.find();
        if (!found) {
            matcher = LABELED_PRICE_PATTERN.matcher(sanitized);
            found = matcher.find();
        }
        if (!found) {
            matcher = CURRENCY_PATTERN.matcher(sanitized);
            found = matcher.find();
        }
        if (!found) {
            return null;
        }
        try {
            return Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String extractReason(String content) {
        String sanitized = sanitizeAssistantContent(content);
        Matcher matcher = REASON_PATTERN.matcher(sanitized);
        if (!matcher.find()) {
            String[] lines = sanitized.split("\\R");
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].toUpperCase(Locale.US).startsWith("REASON:")) {
                    return lines[i].substring("REASON:".length()).trim();
                }
            }
            return sanitized.trim();
        }
        return matcher.group(1)
            .replace("\\n", " ")
            .replace("\\\"", "\"")
            .trim();
    }

    private String sanitizeAssistantContent(String content) {
        String sanitized = content == null ? "" : content.trim();
        if (sanitized.startsWith("```")) {
            sanitized = sanitized.replaceFirst("^```[a-zA-Z0-9_-]*\\s*", "");
            sanitized = sanitized.replaceFirst("\\s*```$", "");
        }
        return sanitized.trim();
    }

    private String escapeJson(String text) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == '\\' || current == '"') {
                escaped.append('\\').append(current);
            } else if (current == '\n') {
                escaped.append("\\n");
            } else if (current == '\r') {
                escaped.append("\\r");
            } else if (current == '\t') {
                escaped.append("\\t");
            } else {
                escaped.append(current);
            }
        }
        return escaped.toString();
    }

    private static List<String> readModels() {
        LinkedHashSet<String> values = new LinkedHashSet<>();

        String configuredList = EnvLoader.get("OPENROUTER_MODELS");
        if (!configuredList.isEmpty()) {
            String[] parts = configuredList.split(",");
            for (int i = 0; i < parts.length; i++) {
                String candidate = parts[i].trim();
                if (!candidate.isEmpty()) {
                    values.add(candidate);
                }
            }
        }

        String configuredSingle = EnvLoader.get("OPENROUTER_MODEL");
        if (!configuredSingle.isEmpty()) {
            values.add(configuredSingle.trim());
        }

        values.add(DEFAULT_MODEL);
        return new ArrayList<>(values);
    }

    private String summarizeError(Exception error) {
        String message = error.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return "API unavailable";
        }
        return compactBody(message);
    }

    private String compactBody(String body) {
        if (body == null) {
            return "API unavailable";
        }
        String compact = body.replace('\n', ' ').replace('\r', ' ').trim();
        if (compact.length() > 120) {
            return compact.substring(0, 120) + "...";
        }
        return compact;
    }
}
