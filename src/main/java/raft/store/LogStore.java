package raft.store;

import raft.model.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogStore implements Store {

    private long lowIndex;
    private long highIndex;

    private final Map<Long, Log> logMap = new ConcurrentHashMap<>();

    private final Map<String, String> kvMap = new ConcurrentHashMap<>();

    private final Map<String, Long> kvLongMap = new ConcurrentHashMap<>();

    @Override
    public long firstIndex() {
        return 0;
    }

    @Override
    public long lastIndex() {
        return 0;
    }

    @Override
    public Log getLog(long index) {
        return null;
    }

    @Override
    public void storeLog(Log log) {

    }

    @Override
    public void storeLogs(Log[] logs) {

    }

    @Override
    public void deleteRange(long min, long max) {

    }

    @Override
    public void set(String key, String val) {

    }

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public void setLong(String key, long val) {

    }

    @Override
    public long getLong(String key) {
        return 0;
    }
}
