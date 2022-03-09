package com.imunyu.raft.model;

public class LogEntry {

    private byte[] data;


    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
