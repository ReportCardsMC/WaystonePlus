package xyz.reportcards.waystoneplus.utils.strings;

import java.util.ArrayList;

public class RandomString {

    /**
     * List of words to generate random strings
     */
    private static final String[] WORD_LIST = new String[]{
            "abruptly", "absurd", "abyss", "affix", "askew", "avenue", "awkward", "axiom", "azure", "bagpipes", "bandwagon", "banjo", "bayou", "beekeeper", "bikini", "blitz", "blizzard", "boggle", "bookworm", "cobweb", "clique", "cycle", "daiquiri", "dashboard", "day", "embezzle", "equip", "exodus", "faking", "fishhook", "fixable", "galaxy", "galvanize", "gazebo", "gizmo", "haphazard", "haiku", "haphazard", "hyphen", "icebox", "injury", "ivory", "ivy", "jackpot", "jaundice", "jawbreaker", "kilobyte", "kiosk", "kiwi", "lengths", "lucky", "luxury", "matrix", "micro", "nightclub", "nowadays", "numbskull", "ostracize", "oxygen", "peekaboo", "pixel", "pizazz", "polka", "quartz", "quiz", "rank", "rhythm", "random", "sphinx", "spritz", "squawk", "strength", "stretch", "stronghold",
    };

    /**
     * Generate a random string
     * @param characters The characters to use
     * @param length The length of the string
     * @param exclude The list of strings to exclude
     * @return The generated string
     */
    private static String generateString(String characters, int length, ArrayList<String> exclude) {
        StringBuilder builder = new StringBuilder();
        while (builder.length() <= 2 || exclude.contains(builder.toString())) {
            builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                int index = (int) (characters.length() * Math.random());
                builder.append(characters.charAt(index));
            }
        }
        return builder.toString();
    }

    /**
     * Generate a random string with only letters
     * @param length The length of the string
     * @param exclude The list of strings to exclude
     * @return The generated string
     */
    public static String generateOnlyLetters(int length, ArrayList<String> exclude) {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return generateString(letters, length, exclude);
    }

    /**
     * Generate a random string with only numbers
     * @param length The length of the string
     * @param exclude The list of strings to exclude
     * @return The generated string
     */
    public static String generateOnlyNumbers(int length, ArrayList<String> exclude) {
        String numbers = "0123456789";
        return generateString(numbers, length, exclude);
    }

    /**
     * Generate a random string with words
     * @param length The length of the string
     * @param maxLength The maximum length of the string
     * @param exclude The list of strings to exclude
     * @return The generated string
     */
    public static String generateWithWords(int length, int maxLength, ArrayList<String> exclude) {
        StringBuilder builder = new StringBuilder();
        int attempts = 0;
        while (attempts == 0 || builder.length() > maxLength || exclude.contains(builder.toString())) {
            builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                int index = (int) (WORD_LIST.length * Math.random());
                // Capitalize first letter
                String word = WORD_LIST[index].substring(0, 1).toUpperCase() + WORD_LIST[index].substring(1);
                builder.append(word);
            }

            attempts++;
        }
        return builder.toString();
    }

}
