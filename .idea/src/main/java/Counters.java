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

}