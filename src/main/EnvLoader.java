package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EnvLoader {
    private static final String ENV_FILE = ".env";
    private static final Map<String, String> FILE_VALUES = loadFileValues();

    private EnvLoader() {}

    public static String get(String key) {
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }
        String fileValue = FILE_VALUES.get(key);
        return fileValue == null ? "" : fileValue.trim();
    }

    private static Map<String, String> loadFileValues() {
        Map<String, String> values = new HashMap<>();
        Path envPath = Path.of(ENV_FILE);
        if (!Files.exists(envPath)) {
            return values;
        }

        try {
            List<String> lines = Files.readAllLines(envPath);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int separatorIndex = line.indexOf('=');
                if (separatorIndex <= 0) {
                    continue;
                }
                String key = line.substring(0, separatorIndex).trim();
                String value = line.substring(separatorIndex + 1).trim();
                values.put(key, stripWrappingQuotes(value));
            }
        } catch (IOException e) {
            return values;
        }

        return values;
    }

    private static String stripWrappingQuotes(String value) {
        if (value.length() >= 2) {
            boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
            boolean singleQuoted = value.startsWith("'") && value.endsWith("'");
            if (doubleQuoted || singleQuoted) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
