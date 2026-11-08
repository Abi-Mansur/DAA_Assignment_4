import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class EditDistanceTest {
    private List<EditDistance> algorithms() {
        return List.of(new RecursiveEditDistance(), new TableEditDistance(), new TwoRowEditDistance());
    }

    @Test void referenceValues() {
        for (EditDistance algorithm : algorithms()) {
            assertEquals(3, algorithm.distance("kitten", "sitting"), algorithm.name());
            assertEquals(2, algorithm.distance("flaw", "lawn"), algorithm.name());
            assertEquals(5, algorithm.distance("intention", "execution"), algorithm.name());
            assertEquals(3, algorithm.distance("", "abc"), algorithm.name());
            assertEquals(0, algorithm.distance("abc", "abc"), algorithm.name());
        }
    }

    @Test void emptyStringsAndUnequalLengths() {
        for (EditDistance algorithm : algorithms()) {
            assertEquals(0, algorithm.distance("", ""));
            assertEquals(3, algorithm.distance("abc", ""));
            assertEquals(3, algorithm.distance("a", "abcd"));
            assertEquals(3, algorithm.distance("abcd", "a"));
            assertEquals(2, algorithm.distance("Abylai Han", "Abylai Khan")); // Insert K and replace H with h.
            assertEquals(1, algorithm.distance("Abylai han", "Abylai khan"));
        }
    }

    @Test void oneHundredRandomShortPairsAgree() {
        Random random = new Random(20261108L);
        List<EditDistance> algorithms = algorithms();
        for (int trial = 0; trial < 100; trial++) {
            String a = InputGenerator.randomString(random, random.nextInt(7), "ABC");
            String b = InputGenerator.randomString(random, random.nextInt(7), "ABC");
            int expected = algorithms.get(0).distance(a, b);
            for (EditDistance algorithm : algorithms) {
                int actual = algorithm.distance(a, b);
                assertEquals(expected, actual, "trial=" + trial + ", algorithm=" + algorithm.name());
                assertEquals(actual, algorithm.distance(b, a), "symmetry");
                assertTrue(actual >= Math.abs(a.length() - b.length()));
                assertTrue(actual <= Math.max(a.length(), b.length()));
            }
        }
    }

    @Test void countersResetAndReflectStorageTradeoff() {
        EditDistance table = new TableEditDistance(), rows = new TwoRowEditDistance();
        table.distance("ABC", "XY");
        rows.distance("ABC", "XY");
        assertEquals(12, table.counters().stateEvaluations);
        assertEquals(12, rows.counters().stateEvaluations);
        assertEquals(12, table.counters().peakIntSlots);
        assertEquals(6, rows.counters().peakIntSlots);
        for (EditDistance algorithm : algorithms()) {
            algorithm.distance("AA", "BB");
            algorithm.distance("A", "A");
            assertEquals(1, algorithm.counters().charComparisons);
        }
    }
}
