public final class Counters {
    private long states, textComparisons, lpsComparisons, liveIntSlots, peakIntSlots;

    public void reset() {
        states = textComparisons = lpsComparisons = liveIntSlots = peakIntSlots = 0;
    }

    void state() { states++; }
    void compareText() { textComparisons++; }
    void compareLps() { lpsComparisons++; }


    void allocate(int slots) {
        liveIntSlots += slots;
        peakIntSlots = Math.max(peakIntSlots, liveIntSlots);
    }

    public Snapshot snapshot() {
        return new Snapshot(states, textComparisons + lpsComparisons,
                textComparisons, lpsComparisons, peakIntSlots);
    }

    public static class Snapshot {
        final long stateEvaluations;
        final long charComparisons;
        final long textComparisons;
        final long lpsComparisons;
        final long peakIntSlots;

        Snapshot(long stateEvaluations, long charComparisons, long textComparisons, long lpsComparisons, long peakIntSlots) {
            this.stateEvaluations = stateEvaluations;
            this.charComparisons = charComparisons;
            this.textComparisons = textComparisons;
            this.lpsComparisons = lpsComparisons;
            this.peakIntSlots = peakIntSlots;
        }
    }
}