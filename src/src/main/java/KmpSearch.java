import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class KmpSearch implements StringSearch {
    private final Counters counts = new Counters();


    public int[] lps(String p) {
        Objects.requireNonNull(p, "p");
        counts.reset();
        return buildLps(p);
    }

    private int[] buildLps(String p) {
        int[] lps = new int[p.length()];
        counts.allocate(lps.length);
        int i = 1, length = 0;
        while (i < p.length()) {
            counts.compareLps();
            if (p.charAt(i) == p.charAt(length)) {
                length++;
                lps[i] = length;
                i++;
            } else if (length > 0) {
                length = lps[length - 1]; // Reuse a shorter border; do not increment i yet.
            } else {
                lps[i] = 0;
                i++;
            }
        }
        return lps;
    }

    @Override
    public List<Integer> search(String text, String pattern) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(pattern, "pattern");
        counts.reset();
        if (pattern.isEmpty()) throw new IllegalArgumentException("Pattern must not be empty");
        List<Integer> matches = new ArrayList<>();
        if (pattern.length() > text.length()) return matches;
        int[] lps = buildLps(pattern); // Included in counters and timed search.
        int i = 0, j = 0;
        while (i < text.length()) {
            counts.compareText();
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == pattern.length()) {
                    matches.add(i - j);
                    j = lps[j - 1]; // Preserve a border, so overlapping matches are found.
                }
            } else if (j > 0) {
                j = lps[j - 1]; // The text index i never moves backwards.
            } else {
                i++;
            }
        }
        return matches;
    }

    @Override public Counters.Snapshot counters() { return counts.snapshot(); }
    @Override public String name() { return "KMP"; }
}