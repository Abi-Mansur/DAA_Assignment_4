import java.util.Objects;

public final class TableEditDistance implements EditDistance {
    private final Counters counts = new Counters();


    @Override
    public int distance(String a, String b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        counts.reset();
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][];
        for (int i = 0; i <= n; i++) {
            dp[i] = new int[m + 1];
            counts.allocate(dp[i].length);
        }
        for (int j = 0; j <= m; j++) {
            dp[0][j] = j;
            counts.state();
        }
        for (int i = 1; i <= n; i++) {
            dp[i][0] = i;
            counts.state();
            for (int j = 1; j <= m; j++) {
                counts.state();
                counts.compareText();
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int replace = dp[i - 1][j - 1] + 1;
                    int delete = dp[i - 1][j] + 1;
                    int insert = dp[i][j - 1] + 1;
                    dp[i][j] = Math.min(replace, Math.min(delete, insert));
                }
            }
        }
        return dp[n][m];
    }

    @Override public Counters.Snapshot counters() { return counts.snapshot(); }
    @Override public String name() { return "Table"; }
}