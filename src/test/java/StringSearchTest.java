import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class StringSearchTest {
    private List<StringSearch> algorithms() { return List.of(new NaiveSearch(), new KmpSearch()); }

    @Test void referenceLpsArrays() {
        KmpSearch kmp = new KmpSearch();
        assertArrayEquals(new int[]{0, 1, 2, 0, 1, 2, 3, 3}, kmp.lps("AAACAAAA"));
        assertArrayEquals(new int[]{0, 0, 1, 2, 0, 1, 2, 3, 4}, kmp.lps("ABABCABAB"));
        assertArrayEquals(new int[]{}, kmp.lps(""));
        assertArrayEquals(new int[]{0}, kmp.lps("A"));
    }

    @Test void referenceMatchesAndOverlaps() {
        for (StringSearch algorithm : algorithms()) {
            assertEquals(List.of(10), algorithm.search("ABABDABACDABABCABAB", "ABABCABAB"));
            assertEquals(List.of(0, 1, 2, 3), algorithm.search("AAAAA", "AA"));
            assertEquals(List.of(0, 2, 4), algorithm.search("ABABABA", "ABA"));
            assertEquals(List.of(0), algorithm.search("ABC", "ABC"));
            assertEquals(List.of(0, 2), algorithm.search("ABA", "A"));
        }
    }

    @Test void emptyPatternThrows() {
        for (StringSearch algorithm : algorithms()) {
            assertThrows(IllegalArgumentException.class, () -> algorithm.search("ABC", ""));
            assertThrows(IllegalArgumentException.class, () -> algorithm.search("", ""));
        }
    }

    @Test void longerPatternEmptyTextAndNoMatch() {
        for (StringSearch algorithm : algorithms()) {
            assertEquals(List.of(), algorithm.search("AB", "ABC"));
            assertEquals(List.of(), algorithm.search("", "A"));
            assertEquals(List.of(), algorithm.search("ABCD", "ZZ"));
            assertEquals(List.of(), algorithm.search("A".repeat(100), "AAAAAB"));
        }
    }

    @Test void oneHundredRandomTextsAgreeWithEachOtherAndJavaOracle() {
        Random random = new Random(20261109L);
        StringSearch naive = new NaiveSearch(), kmp = new KmpSearch();
        for (int trial = 0; trial < 100; trial++) {
            String text = InputGenerator.randomString(random, random.nextInt(81), "ABCD");
            String pattern = InputGenerator.randomString(random, 1 + random.nextInt(12), "ABCD");
            if (trial % 2 == 0 && !text.isEmpty()) {
                int start = random.nextInt(text.length());
                pattern = text.substring(start, Math.min(text.length(), start + 1 + random.nextInt(8)));
            }
            List<Integer> oracle = new ArrayList<>();
            for (int pos = text.indexOf(pattern); pos >= 0; pos = text.indexOf(pattern, pos + 1)) oracle.add(pos);
            assertEquals(oracle, naive.search(text, pattern), "Naive trial " + trial);
            assertEquals(oracle, kmp.search(text, pattern), "KMP trial " + trial);
        }
    }

    @Test void randomLpsMatchesIndependentBorderDefinition() {
        Random random = new Random(42);
        KmpSearch kmp = new KmpSearch();
        for (int trial = 0; trial < 100; trial++) {
            String p = InputGenerator.randomString(random, random.nextInt(25), "ABC");
            int[] actual = kmp.lps(p);
            for (int i = 0; i < p.length(); i++) {
                int expected = 0;
                for (int len = 1; len <= i; len++) {
                    if (p.substring(0, len).equals(p.substring(i + 1 - len, i + 1))) expected = len;
                }
                assertEquals(expected, actual[i], p + " at " + i);
            }
        }
    }

    @Test void countersIncludePreprocessingAndReset() {
        KmpSearch kmp = new KmpSearch();
        kmp.search("AAAAA", "AA");
        assertEquals(5, kmp.counters().textComparisons);
        assertEquals(1, kmp.counters().lpsComparisons);
        assertEquals(6, kmp.counters().charComparisons);
        kmp.search("B", "A");
        assertEquals(1, kmp.counters().charComparisons);
        assertEquals(0, kmp.counters().lpsComparisons);
    }

    private void assertPairEquals(InputGenerator.Pair a, InputGenerator.Pair b) {
        assertEquals(a.first, b.first);
        assertEquals(a.second, b.second);
    }

    @Test void generatorsAreReproducibleAndPlantMatches() {
        for (String type : List.of("identical", "random", "disjoint")) {
            assertPairEquals(InputGenerator.edit(type, 12), InputGenerator.edit(type, 12));
        }
        for (String type : List.of("random", "adversarial", "immediate_mismatch")) {
            assertPairEquals(InputGenerator.search(type, 4096), InputGenerator.search(type, 4096));
        }
        InputGenerator.Pair pair = InputGenerator.search("random", 4096);
        List<Integer> matches = new KmpSearch().search(pair.first, pair.second);
        assertTrue(matches.contains(4096 / 3));
        assertTrue(matches.contains(2 * 4096 / 3));
    }
}