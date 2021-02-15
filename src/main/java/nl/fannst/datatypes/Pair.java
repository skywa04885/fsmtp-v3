package nl.fannst.datatypes;

public class Pair<U, V> {
    private U m_First;
    private V m_Second;

    public Pair(U first, V second) {
        m_First = first;
        m_Second = second;
    }

    public U getFirst() {
        return m_First;
    }

    public V getSecond() {
        return m_Second;
    }
}