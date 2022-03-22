package raft.model;

public class Log {

    private long index;

    private long term;

    private int type;

    private byte[] data;

    private long appendedAt;

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public long getAppendedAt() {
        return appendedAt;
    }

    public void setAppendedAt(long appendedAt) {
        this.appendedAt = appendedAt;
    }

    @Override
    public String toString() {
        return "Log{" +
                "index=" + index +
                ", term=" + term +
                ", type=" + type +
                ", data=" + new String(data) +
                ", appendedAt=" + appendedAt +
                '}';
    }
}
