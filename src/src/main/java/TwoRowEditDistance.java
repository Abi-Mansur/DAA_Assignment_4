import java.util.Objects;

public final class TwoRowEditDistance implements EditDistance {
    private final Counters counts = new Counters();

    @Override
    public int distance(String a, String b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        counts.reset();
        int n = a.length(), m = b.length();
        int[] previous = new int[m + 1];
        counts.allocate(previous.length);
        int[] current = new int[m + 1];
        counts.allocate(current.length);
        for (int j = 0; j <= m; j++) {
            previous[j] = j;
            counts.state();
        }
        for (int i = 1; i <= n; i++) {
            current[0] = i;
            counts.state();
            for (int j = 1; j <= m; j++) {
                counts.state();
                counts.compareText();
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    current[j] = previous[j - 1];
                } else {
                    int replace = previous[j - 1] + 1;
                    int delete = previous[j] + 1;
                    int insert = current[j - 1] + 1;
                    current[j] = Math.min(replace, Math.min(delete, insert));
                }
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[m];
    }

    @Override public Counters.Snapshot counters() { return counts.snapshot(); }
    @Override public String name() { return "TwoRows"; }
}