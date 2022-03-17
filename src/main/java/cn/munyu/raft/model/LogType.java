package cn.munyu.raft.model;

public class LogType {

    /**
     * 应用在状态机
     */
    public static final int LogCommand = 1;

    /**
     * 心跳日志，用来确保领导者
     */
    public static final int LogNoop = 2;

    /**
     *
     */
    public static final int LogAddPeerDeprecated = 3;

    public static final int LogRemovePeerDeprecated = 4;

    /**
     * 确保所有运行操作应用到状态机。和LogNoop类似，不是在提交后返回，而是在状态机确认后返回
     * 否则，可能操作可能提交，但是没有应用到状态机。
     */
    public static final int LogBarrier = 5;

    /**
     * 建立一个成员变换的配置信息。当一个服务器被添加，移除时创建。
     */
    public static final int LogConfiguration = 6;
}
