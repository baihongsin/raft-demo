package com.imunyu.raft.transport;

public class RpcConst {

    public static final int BEAT_INTERVAL = 30;
    public static final int BEAT_TIMEOUT = 3 * BEAT_INTERVAL;

    public static final String CLIENT_THREAD_POOL_NAME = "client";
    public static final String SERVER_THREAD_POOL_NAME = "server";


}
