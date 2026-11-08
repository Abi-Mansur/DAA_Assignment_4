import java.util.Arrays;
import java.util.List;

public class ReferenceChecker {
    public static void main(String[] args) {
        System.out.println("Edit Distance");
        List<EditDistance> editAlgorithms = List.of(
                new RecursiveEditDistance(), new TableEditDistance(), new TwoRowEditDistance());

        for (EditDistance algorithm : editAlgorithms) {
            checkDistance(algorithm, "kitten", "sitting", 3);
            checkDistance(algorithm, "flaw", "lawn", 2);
            checkDistance(algorithm, "intention", "execution", 5);
            checkDistance(algorithm, "", "abc", 3);
            checkDistance(algorithm, "abc", "abc", 0);
        }

        System.out.println("\nKMP prefix arrays");
        KmpSearch kmp = new KmpSearch();
        checkLps(kmp, "AAACAAAA", new int[]{0, 1, 2, 0, 1, 2, 3, 3});
        checkLps(kmp, "ABABCABAB", new int[]{0, 0, 1, 2, 0, 1, 2, 3, 4});

        System.out.println("\nString Search");
        for (StringSearch algorithm : List.of(new NaiveSearch(), kmp)) {
            checkSearch(algorithm, "ABABDABACDABABCABAB", "ABABCABAB", List.of(10));
            checkSearch(algorithm, "AAAAA", "AA", List.of(0, 1, 2, 3));
            checkSearch(algorithm, "ABABABA", "ABA", List.of(0, 2, 4));
        }

        System.out.println("\nAll reference checks passed.");
    }

    private static void checkDistance(EditDistance algorithm, String a, String b, int expected) {
        String label = algorithm.name() + " distance(\"" + a + "\", \"" + b + "\")";
        check(label, expected, algorithm.distance(a, b));
    }

    private static void checkLps(KmpSearch algorithm, String pattern, int[] expected) {
        check("KMP lps(\"" + pattern + "\")",
                Arrays.toString(expected), Arrays.toString(algorithm.lps(pattern)));
    }

    private static void checkSearch(StringSearch algorithm, String text, String pattern, List<Integer> expected) {
        String label = algorithm.name() + " search(\"" + text + "\", \"" + pattern + "\")";
        check(label, expected, algorithm.search(text, pattern));
    }

    private static void check(String label, Object expected, Object actual) {
        String result = label + " = " + actual + " (expected " + expected + ")";
        if (!expected.equals(actual)) {
            throw new AssertionError(result);
        }
        System.out.println(result + " [OK]");
    }
}
