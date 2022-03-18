package raft.model;

public class Log {

    private long index;

    private long term;

    private LogType type;

    private byte[] data;

    private long appendedAt;
}
