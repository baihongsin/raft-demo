package cn.munyu.raft.transport;

public class ContextHolder {

    public static final InheritableThreadLocal<String> connectionMap = new InheritableThreadLocal<>();

    public static void set(String connId) {
        connectionMap.set(connId);
    }

    public static String get() {
        return connectionMap.get();
    }

    public static void remove() {
        connectionMap.remove();
    }
}
