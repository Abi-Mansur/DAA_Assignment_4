import java.util.Objects;

public final class RecursiveEditDistance implements EditDistance {
    private final Counters counts = new Counters();


    @Override
    public int distance(String a, String b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        counts.reset();
        return solve(a, b, a.length(), b.length());
    }

    private int solve(String a, String b, int i, int j) {
        counts.state();
        if (i == 0) return j;
        if (j == 0) return i;
        counts.compareText();
        if (a.charAt(i - 1) == b.charAt(j - 1)) return solve(a, b, i - 1, j - 1);
        int delete = solve(a, b, i - 1, j);
        int insert = solve(a, b, i, j - 1);
        int replace = solve(a, b, i - 1, j - 1);
        return 1 + Math.min(delete, Math.min(insert, replace));
    }

    @Override public Counters.Snapshot counters() { return counts.snapshot(); }
    @Override public String name() { return "Recursive"; }
}