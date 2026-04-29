package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RAGService implements AIService {
    private static final String SOURCE_LABEL = "Local valuation engine";
    private static final int RETRIEVAL_LIMIT = 3;
    private final PhoneVectorDatabase vectorDatabase = new PhoneVectorDatabase();

    @Override
    public EvaluationResult evaluate(Phone phone) {
        List<PhoneVectorDatabase.SearchResult> retrieved = vectorDatabase.search(phone.getModel(), RETRIEVAL_LIMIT);
        PhoneKnowledgeBase.PhoneReference bestMatch = retrieved.isEmpty() ? null : retrieved.get(0).getReference();

        double basePrice;
        String matchedModel;
        int matchedStorage;
        double matchConfidence;

        if (bestMatch != null) {
            basePrice = bestMatch.getMarketPrice();
            matchedModel = bestMatch.getModel();
            matchedStorage = bestMatch.getStorage();
            matchConfidence = calculateConfidence(phone.getModel(), bestMatch.getModel());
        } else {
            basePrice = fallbackPrice(phone.getModel());
            matchedModel = "generic market reference";
            matchedStorage = phone.getStorage();
            matchConfidence = 0.35;
        }

        double storageAdjustment = calculateStorageAdjustment(phone.getStorage(), matchedStorage);
        DamageAssessment damageAssessment = assessDamage(phone.getDamageDescription());
        double estimatedPrice = clampPrice((basePrice + storageAdjustment) * (1.0 - damageAssessment.penaltyRate));

        phone.setEstimatedPrice(estimatedPrice);

        String explanation = buildExplanation(
            phone,
            matchedModel,
            matchedStorage,
            basePrice,
            storageAdjustment,
            damageAssessment,
            estimatedPrice,
            matchConfidence
        );

        return new EvaluationResult(
            explanation,
            SOURCE_LABEL,
            false,
            vectorDatabase.buildContext(retrieved)
        );
    }

    private int scoreMatch(String input, String reference) {
        if (input.equals(reference)) {
            return 100;
        }
        if (reference.contains(input) || input.contains(reference)) {
            return 80;
        }

        String[] inputTokens = input.split(" ");
        String[] referenceTokens = reference.split(" ");
        int score = 0;

        for (int i = 0; i < inputTokens.length; i++) {
            for (int j = 0; j < referenceTokens.length; j++) {
                if (inputTokens[i].equals(referenceTokens[j])) {
                    score += 15;
                } else if (referenceTokens[j].startsWith(inputTokens[i]) || inputTokens[i].startsWith(referenceTokens[j])) {
                    score += 8;
                }
            }
        }

        if (input.startsWith(reference) || reference.startsWith(input)) {
            score += 10;
        }

        return score;
    }

    private double calculateConfidence(String inputModel, String matchedModel) {
        int score = scoreMatch(normalize(inputModel), normalize(matchedModel));
        return Math.min(0.95, Math.max(0.45, score / 100.0));
    }

    private double calculateStorageAdjustment(int storage, int referenceStorage) {
        int diff = storage - referenceStorage;
        return diff * 0.35;
    }

    private DamageAssessment assessDamage(String damageDescription) {
        String normalized = normalize(damageDescription);
        double penaltyRate = 0.0;
        List<String> notes = new ArrayList<>();

        if (normalized.isEmpty() || normalized.equals("none") || normalized.equals("no damage")) {
            notes.add("No damage reported");
        } else {
            if (containsAny(normalized, "scratch", "scratches", "scuff", "scuffs", "minor")) {
                penaltyRate += 0.07;
                notes.add("minor cosmetic wear");
            }
            if (containsAny(normalized, "crack", "cracked", "screen")) {
                penaltyRate += 0.18;
                notes.add("screen damage");
            }
            if (containsAny(normalized, "battery", "drain", "health")) {
                penaltyRate += 0.12;
                notes.add("battery issue");
            }
            if (containsAny(normalized, "camera", "speaker", "mic", "button", "face id", "touch")) {
                penaltyRate += 0.14;
                notes.add("hardware functionality issue");
            }
            if (containsAny(normalized, "water", "liquid")) {
                penaltyRate += 0.22;
                notes.add("possible liquid damage");
            }
            if (containsAny(normalized, "bent", "dead", "not working", "won't turn on", "wont turn on")) {
                penaltyRate += 0.30;
                notes.add("major structural or power issue");
            }
            if (notes.isEmpty()) {
                penaltyRate += 0.08;
                notes.add("unspecified condition risk");
            }
        }

        if (penaltyRate > 0.65) {
            penaltyRate = 0.65;
        }

        String condition;
        if (penaltyRate <= 0.03) {
            condition = "excellent";
        } else if (penaltyRate <= 0.12) {
            condition = "good";
        } else if (penaltyRate <= 0.28) {
            condition = "fair";
        } else {
            condition = "poor";
        }

        return new DamageAssessment(penaltyRate, condition, notes);
    }

    private boolean containsAny(String text, String... keywords) {
        for (int i = 0; i < keywords.length; i++) {
            if (text.contains(keywords[i])) {
                return true;
            }
        }
        return false;
    }

    private double fallbackPrice(String model) {
        String normalized = normalize(model);
        if (normalized.contains("iphone")) {
            return 500.00;
        }
        if (normalized.contains("galaxy") || normalized.contains("samsung")) {
            return 420.00;
        }
        if (normalized.contains("pixel")) {
            return 360.00;
        }
        return 280.00;
    }

    private String buildExplanation(
        Phone phone,
        String matchedModel,
        int matchedStorage,
        double basePrice,
        double storageAdjustment,
        DamageAssessment damageAssessment,
        double estimatedPrice,
        double confidence
    ) {
        StringBuilder message = new StringBuilder();
        message.append("AI matched your device to ")
            .append(matchedModel)
            .append(" (")
            .append(matchedStorage)
            .append("GB) with about ")
            .append(Math.round(confidence * 100))
            .append("% confidence. ");

        message.append(String.format(Locale.US, "Base market value starts at $%.2f. ", basePrice));

        if (storageAdjustment > 0) {
            message.append(String.format(Locale.US,
                "Storage adds about $%.2f because your phone has %dGB. ",
                storageAdjustment,
                phone.getStorage()
            ));
        } else if (storageAdjustment < 0) {
            message.append(String.format(Locale.US,
                "Storage lowers value by about $%.2f compared with the %dGB reference. ",
                Math.abs(storageAdjustment),
                matchedStorage
            ));
        } else {
            message.append("Storage is in line with the matched reference. ");
        }

        message.append("Condition is estimated as ")
            .append(damageAssessment.condition)
            .append(" based on ");
        message.append(String.join(", ", damageAssessment.notes));
        message.append(". ");
        message.append("Retrieval was grounded on the local vector database. ");
        message.append(String.format(Locale.US,
            "After condition adjustment, the estimated resale price is $%.2f.",
            estimatedPrice
        ));

        return message.toString();
    }

    private double clampPrice(double value) {
        if (value < 25.0) {
            return 25.0;
        }
        return Math.round(value * 100.0) / 100.0;
    }

    private String normalize(String text) {
        return text == null
            ? ""
            : text.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", " ").trim();
    }

    private static final class DamageAssessment {
        private final double penaltyRate;
        private final String condition;
        private final List<String> notes;

        private DamageAssessment(double penaltyRate, String condition, List<String> notes) {
            this.penaltyRate = penaltyRate;
            this.condition = condition;
            this.notes = notes;
        }
    }
}
