package raft.store;

import raft.model.Log;

public interface Store {

    long firstIndex();

    long lastIndex();

    Log getLog(long index);

    void storeLog(Log log);

    void storeLogs(Log[] logs);

    void deleteRange(long min, long max);

    void set(String key, String val);

    String get(String key);

    void setLong(String key, long val);

    long getLong(String key);
}
