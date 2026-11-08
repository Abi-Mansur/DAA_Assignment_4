import java.util.Random;

public final class InputGenerator {
    public static final long SEED = 20261108L;
    private static final String ALPHABET = "ABCD";
    private InputGenerator() { }

    public static class Pair {
        final String first;
        final String second;

        Pair(String first, String second) {
            this.first = first;
            this.second = second;
        }
    }

    public static String randomString(Random random, int length, String alphabet) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) result.append(alphabet.charAt(random.nextInt(alphabet.length())));
        return result.toString();
    }

    private static Random stream(String family, String type, int n) {

        long familyPart = (long) family.hashCode() << 32;
        long typePart = (long) type.hashCode() << 1;
        long caseSeed = SEED ^ familyPart ^ typePart ^ n;
        return new Random(caseSeed);
    }

    public static Pair edit(String type, int n) {
        Random random = stream("edit", type, n);
        if (type.equals("identical")) {
            String a = randomString(random, n, ALPHABET);
            return new Pair(a, a);
        }
        if (type.equals("random")) {
            return new Pair(randomString(random, n, ALPHABET), randomString(random, n, ALPHABET));
        }
        if (type.equals("disjoint")) {
            return new Pair("A".repeat(n), "B".repeat(n));
        }
        throw new IllegalArgumentException("Unknown edit input: " + type);
    }

    public static Pair search(String type, int n) {
        int m = n / 64;
        if (m < 1) throw new IllegalArgumentException("Search generator needs n >= 64");
        Random random = stream("search", type, n);
        if (type.equals("random")) {
            StringBuilder text = new StringBuilder(randomString(random, n, ALPHABET));
            String pattern = randomString(random, m, ALPHABET);
            text.replace(n / 3, n / 3 + m, pattern);
            text.replace(2 * n / 3, 2 * n / 3 + m, pattern);
            return new Pair(text.toString(), pattern);
        }
        if (type.equals("adversarial")) {
            return new Pair("A".repeat(n), "A".repeat(m - 1) + "B");
        }
        if (type.equals("immediate_mismatch")) {
            return new Pair("B".repeat(n), "A".repeat(m));
        }
        throw new IllegalArgumentException("Unknown search input: " + type);
    }
}