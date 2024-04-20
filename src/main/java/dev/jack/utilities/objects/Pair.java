package dev.jack.utilities.objects;

public class Pair<F, S> {

    private F first;
    private S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public Pair() {
        this.first = null;
        this.second = null;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }
}
