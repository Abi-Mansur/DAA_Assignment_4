public interface EditDistance {
    int distance(String a, String b);
    Counters.Snapshot counters();
    String name();
}