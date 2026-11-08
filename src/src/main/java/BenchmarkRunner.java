import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public final class BenchmarkRunner {
    private static final int RUNS = 5, WARMUPS = 2;
    private static final int[] SMALL = {3, 6, 9, 12};
    private static final int[] LARGE = {128, 256, 512, 1024, 2048};
    private static final int[] SEARCH = {4096, 8192, 16384, 32768, 65536};
    private static volatile long sink;

    private static class Run {
        final String family;
        final String algorithm;
        final String input;
        final int n;
        final int m;
        final int run;
        final double timeMs;
        final long result;
        final Counters.Snapshot counters;

        Run(String family, String algorithm, String input, int n, int m, int run, double timeMs, long result, Counters.Snapshot counters) {
            this.family = family;
            this.algorithm = algorithm;
            this.input = input;
            this.n = n;
            this.m = m;
            this.run = run;
            this.timeMs = timeMs;
            this.result = result;
            this.counters = counters;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 1) throw new IllegalArgumentException("Usage: java -jar assignment4-1.0.jar [results-directory]");
        Path output = Path.of(args.length == 0 ? "results" : args[0]);
        Files.createDirectories(output);
        List<Run> rows = new ArrayList<>();
        try (BufferedWriter csv = Files.newBufferedWriter(output.resolve("results.csv"), StandardCharsets.UTF_8)) {
            csv.write("family,algorithm,input,n,m,run,seed,time_ms,state_evaluations,char_comparisons,text_comparisons,lps_comparisons,peak_int_slots,result\n");
            List<EditDistance> edits = List.of(new RecursiveEditDistance(), new TableEditDistance(), new TwoRowEditDistance());
            for (String type : List.of("identical", "random", "disjoint")) {
                for (int n : SMALL) measureEdit(edits, type, n, csv, rows);
                for (int n : LARGE) measureEdit(edits.subList(1, 3), type, n, csv, rows);
            }
            List<StringSearch> searches = List.of(new NaiveSearch(), new KmpSearch());
            for (String type : List.of("random", "adversarial", "immediate_mismatch")) {
                for (int n : SEARCH) {
                    InputGenerator.Pair input = InputGenerator.search(type, n);
                    List<Integer> expected = oracle(input.first, input.second);
                    for (StringSearch algorithm : searches) {
                        for (int warm = 0; warm < WARMUPS; warm++) sink = algorithm.search(input.first, input.second).hashCode();
                        for (int run = 1; run <= RUNS; run++) {
                            long start = System.nanoTime();
                            List<Integer> actual = algorithm.search(input.first, input.second);
                            long elapsed = System.nanoTime() - start;
                            if (!actual.equals(expected)) throw new IllegalStateException("Wrong search result: " + algorithm.name());
                            sink = actual.hashCode();
                            record(csv, rows, new Run("search", algorithm.name(), type, n, input.second.length(), run,
                                    elapsed / 1_000_000.0, actual.size(), algorithm.counters()));
                        }
                    }
                    System.out.println("Search: " + type + ", n=" + n + ", m=" + input.second.length());
                }
            }
        }
        writeSummary(output.resolve("summary.csv"), rows);
        System.out.println("Saved " + rows.size() + " measured runs to " + output.toAbsolutePath());
    }

    private static void measureEdit(List<EditDistance> algorithms, String type, int n,
                                    BufferedWriter csv, List<Run> rows) throws IOException {
        InputGenerator.Pair input = InputGenerator.edit(type, n);
        int expected = new TableEditDistance().distance(input.first, input.second);
        for (EditDistance algorithm : algorithms) {
            for (int warm = 0; warm < WARMUPS; warm++) sink = algorithm.distance(input.first, input.second);
            for (int run = 1; run <= RUNS; run++) {
                long start = System.nanoTime();
                int result = algorithm.distance(input.first, input.second);
                long elapsed = System.nanoTime() - start;
                if (result != expected) throw new IllegalStateException("Wrong distance: " + algorithm.name());
                sink = result;
                record(csv, rows, new Run("edit", algorithm.name(), type, n, n, run,
                        elapsed / 1_000_000.0, result, algorithm.counters()));
            }
        }
        System.out.println("Edit: " + type + ", n=" + n);
    }

    private static List<Integer> oracle(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        for (int pos = text.indexOf(pattern); pos >= 0; pos = text.indexOf(pattern, pos + 1)) matches.add(pos);
        return matches;
    }

    private static void record(BufferedWriter csv, List<Run> rows, Run row) throws IOException {
        Counters.Snapshot c = row.counters;
        csv.write(String.format(Locale.ROOT, "%s,%s,%s,%d,%d,%d,%d,%.6f,%d,%d,%d,%d,%d,%d%n",
                row.family, row.algorithm, row.input, row.n, row.m, row.run, InputGenerator.SEED,
                row.timeMs, c.stateEvaluations, c.charComparisons, c.textComparisons, c.lpsComparisons,
                c.peakIntSlots, row.result));
        csv.flush();
        rows.add(row);
    }

    private static Map<String, List<Run>> groups(List<Run> rows) {
        Map<String, List<Run>> groups = new LinkedHashMap<>();
        for (Run row : rows) {
            String key = row.family + "," + row.algorithm + "," + row.input + "," + row.n + "," + row.m;
            if (!groups.containsKey(key)) {
                groups.put(key, new ArrayList<>());
            }
            groups.get(key).add(row);
        }
        return groups;
    }

    private static double[] sortedTimes(List<Run> rows) {
        double[] times = new double[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            times[i] = rows.get(i).timeMs;
        }
        Arrays.sort(times);
        return times;
    }

    private static void writeSummary(Path path, List<Run> rows) throws IOException {
        try (BufferedWriter csv = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            csv.write("family,algorithm,input,n,m,median_time_ms,min_time_ms,max_time_ms,state_evaluations,char_comparisons,peak_int_slots,result\n");
            for (Map.Entry<String, List<Run>> entry : groups(rows).entrySet()) {
                List<Run> runs = entry.getValue();
                Run first = runs.get(0);
                for (Run run : runs) {
                    if (run.counters.stateEvaluations != first.counters.stateEvaluations
                            || run.counters.charComparisons != first.counters.charComparisons
                            || run.counters.textComparisons != first.counters.textComparisons
                            || run.counters.lpsComparisons != first.counters.lpsComparisons
                            || run.counters.peakIntSlots != first.counters.peakIntSlots || run.result != first.result)
                        throw new IllegalStateException("Nondeterministic work counters");
                }
                double[] times = sortedTimes(runs);
                csv.write(String.format(Locale.ROOT, "%s,%.6f,%.6f,%.6f,%d,%d,%d,%d%n", entry.getKey(),
                        times[times.length / 2], times[0], times[times.length - 1], first.counters.stateEvaluations,
                        first.counters.charComparisons, first.counters.peakIntSlots, first.result));
            }
        }
    }
}
