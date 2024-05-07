package aufgabe1;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for parsing the given data to an Ini file
 * STYLE: procedural, because it is static and each method follows a specific task
 */
public class IniFileParser {
    private static Map<String, Map<String, String>> sections = new HashMap<>();

    /**
     * Loads the data from an Ini file to the simulation
     */
    public static void load(@NotNull String filePath) throws IOException {
        String line = "";
        int lineNumber = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String currentSection = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                lineNumber++;
                //noinspection StatementWithEmptyBody
                if (line.isEmpty() || line.startsWith(";") || line.startsWith("#")) {
                    // do nothing
                } else if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1);
                    if (currentSection.isEmpty()) {
                        throw new INISyntaxException("Section name is empty");
                    }
                    sections.put(currentSection, new HashMap<>());
                } else if (currentSection != null && line.contains("=")) {
                    String[] parts = line.split("=");
                    if (parts.length != 2) {
                        throw new INISyntaxException(String.format("Need 2 parts in assignment, got %d", parts.length));
                    }
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    sections.get(currentSection).put(key, value);
                } else {
                    throw new INISyntaxException("Unknown line type");
                }
            }
        } catch (INISyntaxException e) {
            // GOOD: Error message contains information about the location of the error.
            throw new IOException(String.format("Invalid INI Syntax at %s:%d: '%s'", filePath, lineNumber, line), e);
        }
    }

    /**
     * Gets the section data from the specified key
     */
    public static @Nullable String get(String section, String key) {
        Map<String, String> sectionData = sections.get(section);
        if (sectionData != null) {
            return sectionData.get(key);
        }
        return null;
    }

    /**
     * Sets a section with a key and value
     */
    public static void set(String section, String key, String value) {
        Map<String, String> sectionData = sections.computeIfAbsent(section, k -> new HashMap<>());
        sectionData.put(key, value);
    }

    public static void setSections(Map<String, Map<String, String>> sections) {
        IniFileParser.sections = sections;
    }

    /**
     * Saves simulation data to an Ini File
     */
    public static void save(@NotNull String filePath) throws IOException {
        // BAD: No response to possible errors when creating directories or files.
        File file = new File(filePath);
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, Map<String, String>> sectionEntry : sections.entrySet()) {
                String sectionName = sectionEntry.getKey();
                bw.write("[" + sectionName + "]");
                bw.newLine();
                Map<String, String> sectionData = sectionEntry.getValue();
                for (Map.Entry<String, String> keyEntry : sectionData.entrySet()) {
                    String key = keyEntry.getKey();
                    String value = keyEntry.getValue();
                    bw.write(key + " = " + value);
                    bw.newLine();
                }
                bw.newLine();
            }
        }
        sections = new HashMap<>();
    }

    private static class INISyntaxException extends Exception {
        public INISyntaxException(String message) {
            super(message);
        }
    }
}