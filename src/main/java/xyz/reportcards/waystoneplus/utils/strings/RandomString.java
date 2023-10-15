package xyz.reportcards.waystoneplus.utils.strings;

public class RandomString {

    private static final String[] WORD_LIST = new String[]{
            "abruptly", "absurd", "abyss", "affix", "askew", "avenue", "awkward", "axiom", "azure", "bagpipes", "bandwagon", "banjo", "bayou", "beekeeper", "bikini", "blitz", "blizzard", "boggle", "bookworm", "cobweb", "clique", "cycle", "daiquiri", "dashboard", "day", "embezzle", "equip", "exodus", "faking", "fishhook", "fixable", "galaxy", "galvanize", "gazebo", "gizmo", "haphazard", "haiku", "haphazard", "hyphen", "icebox", "injury", "ivory", "ivy", "jackpot", "jaundice", "jawbreaker", "kilobyte", "kiosk", "kiwi", "lengths", "lucky", "luxury", "matrix", "micro", "nightclub", "nowadays", "numbskull", "ostracize", "oxygen", "peekaboo", "pixel", "pizazz", "polka", "quartz", "quiz", "rank", "rhythm", "random", "sphinx", "spritz", "squawk", "strength", "stretch", "stronghold",
    };

    private static String generateString(String characters, int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (characters.length() * Math.random());
            builder.append(characters.charAt(index));
        }
        return builder.toString();
    }

    public static String generateOnlyLetters(int length) {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return generateString(letters, length);
    }

    public static String generateOnlyNumbers(int length) {
        String numbers = "0123456789";
        return generateString(numbers, length);
    }

    public static String generateLettersAndNumbers(int length) {
        String lettersAndNumbers = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        return generateString(lettersAndNumbers, length);
    }

    public static String generateWithWords(int length, int maxLength) {
        StringBuilder builder = new StringBuilder();
        int attempts = 0;
        while (attempts == 0 || builder.length() > maxLength) {
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
