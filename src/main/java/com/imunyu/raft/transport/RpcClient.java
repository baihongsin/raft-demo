package com.imunyu.raft.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcClient {

    private final static Logger log = LoggerFactory.getLogger(RpcClient.class);

    private final RpcFuture<ChannelFuture> rpcFuture = new RpcFuture<>();
    private final Map<String, Set<String>> exposeList = new HashMap<>();
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 8, 600, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000));

    private final Map<String, RpcClientInvoker> invokers = new ConcurrentHashMap<>();
    private final Map<String, ChannelFuture> serverChannels = new ConcurrentHashMap<>();

    private final Random random = new Random();
    private String[] serverList = new String[]{};

    private boolean reconnected = false;

    public void setReconnected(boolean reconnected) {
        this.reconnected = reconnected;
    }

    public void addInvoker(String serverKey, RpcClientInvoker invoker) {
        invokers.put(serverKey, invoker);
    }

    public <T> T registerNode(Class<T> interfaceName, String addr) {
        Map<String, Set<String>> registryNode = new HashMap<>();
        HashSet<String> addressList = new HashSet<>();
        addressList.add(addr);
        registryNode.put(interfaceName.getName(), addressList);
        fillNodes(registryNode);
        return createService(interfaceName);
    }

    private void fillNodes(Map<String, Set<String>> nodes) {
        this.exposeList.putAll(nodes);
        Set<String> serverSet = new HashSet<>();
        for (String k : nodes.keySet()) {
            //  和已有的服务器列表集合做对比
            //  查询增加和删除的
            serverSet.addAll(nodes.get(k));
        }
        updateServer(serverSet);
    }

    public RpcClientInvoker select() {
        if (serverList.length == 0) {
            return null;
        }
        int i = random.nextInt(serverList.length);
        String serverKey = serverList[i];
        return invokers.get(serverKey);
    }

    public <T> T createService(Class<T> cls) {
        String name = cls.getName();
        Set<String> serverList = exposeList.get(name);
        if (serverList != null) {
            updateServer(serverList);
        }
        // 选择服务key
        ClassLoader classLoader = cls.getClassLoader();
        return cls.cast(Proxy.newProxyInstance(classLoader, new Class[]{cls}, new RpcProxy(this)));
    }


    private void updateServer(Set<String> serverSet) {
        for (String serverKey: serverChannels.keySet()) {
            if (serverSet.contains(serverKey)) {
                // 保持不变的
                continue;
            }
            // 不包含的关闭链接
            ChannelFuture channelFuture = serverChannels.get(serverKey);
            try {
                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                channelFuture.channel().eventLoop().shutdownGracefully();
            }
            serverChannels.remove(serverKey);
        }
        // 新增处理
        for (String serverKey : serverSet) {
            if (serverChannels.containsKey(serverKey)) {
                continue;
            }
            ChannelFuture connectFuture = connect(serverKey);
            serverChannels.put(serverKey, connectFuture);
        }

        serverList = serverChannels.keySet().toArray(new String[]{});

    }

    private ChannelFuture connect(String addr) {
        String[] address = addr.split(":");
        String ip = address[0];
        int port = Integer.parseInt(address[1]);
        return connect(ip, port).sync();
    }

    private RpcClient connect(String host, int port) {
        log.info("connect");
        threadPoolExecutor.execute(() -> {
            boolean connected = false;
            while (!connected) {
                EventLoopGroup loopGroup = new NioEventLoopGroup();
                Bootstrap bootstrap = new Bootstrap()
                        .group(loopGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();
                                RpcCodec rpcCodec = new RpcProtostuffCodec();
                                pipeline.addLast("heartbeat", new IdleStateHandler(0, 0, Beat.BEAT_TIMEOUT, TimeUnit.SECONDS));
                                pipeline.addLast("encoder", new RpcEncoder(rpcCodec));
                                pipeline.addLast("decoder", new RpcDecoder(rpcCodec));
                                pipeline.addLast("client", new RpcRpcClientHandler(RpcClient.this, host + ":" + port));
                            }
                        });
                try {
                    ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
                    log.info("client connected, {}:{}", host, port);
                    rpcFuture.done(channelFuture);

                    connected = true;
                    channelFuture.channel().closeFuture().sync();
                    if (reconnected) {
                        connected = false;
                    }
                    log.info("client disconnected, {}:{}", host, port);
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                } finally {
                    loopGroup.shutdownGracefully();
                }
            }

        });
        return this;
    }

    public ChannelFuture sync() {
        return rpcFuture.get();
    }

}
