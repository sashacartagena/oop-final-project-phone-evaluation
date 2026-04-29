package main;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class PhoneVectorDatabase {
    private static final int VECTOR_SIZE = 96;
    private static final Path DATABASE_PATH = Path.of("data", "phone_vectors.db");

    private final List<VectorRecord> records;

    public PhoneVectorDatabase() {
        this.records = buildRecords(PhoneKnowledgeBase.getReferences());
        persistDatabase(records);
    }

    public List<SearchResult> search(String query, int limit) {
        double[] queryVector = embed(buildSearchText(query, 0, 0.0));
        String normalizedQuery = normalize(query);
        List<SearchResult> results = new ArrayList<>();

        for (int i = 0; i < records.size(); i++) {
            VectorRecord record = records.get(i);
            double vectorScore = cosineSimilarity(queryVector, record.vector);
            double lexicalScore = lexicalScore(normalizedQuery, normalize(record.reference.getModel()));
            double score = vectorScore + lexicalScore;
            results.add(new SearchResult(record.reference, score));
        }

        results.sort(Comparator.comparingDouble(SearchResult::getScore).reversed());
        if (results.size() > limit) {
            return new ArrayList<>(results.subList(0, limit));
        }
        return results;
    }

    private double lexicalScore(String query, String candidate) {
        if (query.isEmpty() || candidate.isEmpty()) {
            return 0.0;
        }
        if (query.equals(candidate)) {
            return 1.2;
        }
        double score = 0.0;
        if (candidate.contains(query) || query.contains(candidate)) {
            score += 0.45;
        }

        String[] queryTokens = query.split(" ");
        String[] candidateTokens = candidate.split(" ");
        for (int i = 0; i < queryTokens.length; i++) {
            if (queryTokens[i].isEmpty()) {
                continue;
            }
            for (int j = 0; j < candidateTokens.length; j++) {
                if (queryTokens[i].equals(candidateTokens[j])) {
                    score += 0.18;
                } else if (candidateTokens[j].startsWith(queryTokens[i]) || queryTokens[i].startsWith(candidateTokens[j])) {
                    score += 0.08;
                }
            }
        }
        return score;
    }

    public String buildContext(List<SearchResult> results) {
        if (results.isEmpty()) {
            return "No vector matches found in the local phone database.";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Top retrieved phone references:");
        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            PhoneKnowledgeBase.PhoneReference reference = result.reference;
            builder.append("\n")
                .append(i + 1)
                .append(". ")
                .append(reference.getModel())
                .append(" / ")
                .append(reference.getStorage())
                .append("GB / $")
                .append(String.format(Locale.US, "%.2f", reference.getMarketPrice()))
                .append(" / similarity ")
                .append(String.format(Locale.US, "%.3f", result.score));
        }
        return builder.toString();
    }

    private List<VectorRecord> buildRecords(List<PhoneKnowledgeBase.PhoneReference> references) {
        List<VectorRecord> built = new ArrayList<>();
        for (int i = 0; i < references.size(); i++) {
            PhoneKnowledgeBase.PhoneReference reference = references.get(i);
            String text = buildSearchText(reference.getModel(), reference.getStorage(), reference.getMarketPrice());
            built.add(new VectorRecord(reference, text, embed(text)));
        }
        return Collections.unmodifiableList(built);
    }

    private String buildSearchText(String model, int storage, double price) {
        return String.format(
            Locale.US,
            "%s %dGB %.2f Apple Samsung Google OnePlus Motorola used resale phone market reference",
            normalize(model),
            storage,
            price
        );
    }

    private double[] embed(String text) {
        double[] vector = new double[VECTOR_SIZE];
        String normalized = normalize(text);

        for (int i = 0; i < normalized.length(); i++) {
            char current = normalized.charAt(i);
            if (current == ' ') {
                continue;
            }
            int index = Math.abs((current * 31 + i * 17) % VECTOR_SIZE);
            vector[index] += 1.0;
        }

        String[] tokens = normalized.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].isEmpty()) {
                continue;
            }
            int tokenIndex = Math.abs(tokens[i].hashCode() % VECTOR_SIZE);
            vector[tokenIndex] += 2.0;
        }

        normalizeVector(vector);
        return vector;
    }

    private void normalizeVector(double[] vector) {
        double magnitude = 0.0;
        for (int i = 0; i < vector.length; i++) {
            magnitude += vector[i] * vector[i];
        }
        magnitude = Math.sqrt(magnitude);
        if (magnitude == 0.0) {
            return;
        }
        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] / magnitude;
        }
    }

    private double cosineSimilarity(double[] left, double[] right) {
        double sum = 0.0;
        for (int i = 0; i < left.length; i++) {
            sum += left[i] * right[i];
        }
        return sum;
    }

    private void persistDatabase(List<VectorRecord> builtRecords) {
        try {
            Files.createDirectories(DATABASE_PATH.getParent());
            List<String> lines = new ArrayList<>();
            lines.add("brand\tmodel\tstorage\tprice\ttext\tvector");
            for (int i = 0; i < builtRecords.size(); i++) {
                VectorRecord record = builtRecords.get(i);
                lines.add(record.reference.getBrand()
                    + "\t" + record.reference.getModel()
                    + "\t" + record.reference.getStorage()
                    + "\t" + String.format(Locale.US, "%.2f", record.reference.getMarketPrice())
                    + "\t" + record.text
                    + "\t" + joinVector(record.vector));
            }
            Files.write(DATABASE_PATH, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // The app can continue using the in-memory vector store even if persistence fails.
        }
    }

    private String joinVector(double[] vector) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(String.format(Locale.US, "%.6f", vector[i]));
        }
        return builder.toString();
    }

    private String normalize(String text) {
        return text == null
            ? ""
            : text.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", " ").trim();
    }

    public static final class SearchResult {
        private final PhoneKnowledgeBase.PhoneReference reference;
        private final double score;

        private SearchResult(PhoneKnowledgeBase.PhoneReference reference, double score) {
            this.reference = reference;
            this.score = score;
        }

        public PhoneKnowledgeBase.PhoneReference getReference() {
            return reference;
        }

        public double getScore() {
            return score;
        }
    }

    private static final class VectorRecord {
        private final PhoneKnowledgeBase.PhoneReference reference;
        private final String text;
        private final double[] vector;

        private VectorRecord(PhoneKnowledgeBase.PhoneReference reference, String text, double[] vector) {
            this.reference = reference;
            this.text = text;
            this.vector = vector;
        }
    }
}
