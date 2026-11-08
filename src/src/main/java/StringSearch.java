import java.util.List;


public interface StringSearch {
    List<Integer> search(String text, String pattern);
    Counters.Snapshot counters();
    String name();
}