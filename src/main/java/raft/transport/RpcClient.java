package raft.transport;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
import java.util.concurrent.*;

/**
 * rpc客户端包含
 */
public class RpcClient {

    private final static Logger log = LoggerFactory.getLogger(RpcClient.class);

    private final RpcFuture<ChannelFuture> rpcFuture = new RpcFuture<>();
    private final Map<String, List<String>> exposeList = new HashMap<>();
    private final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(RpcConst.CLIENT_THREAD_POOL_NAME).build();
    private final ThreadPoolExecutor threadPoolExecutor
            = new ThreadPoolExecutor(4, 100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000), threadFactory);

    private final Map<String, RpcClientInvoker> invokers = new ConcurrentHashMap<>();
    private final Map<String, ChannelFuture> serverChannels = new ConcurrentHashMap<>();

    private final Random random = new Random();
    private final RpcCodec rpcCodec = new RpcProtostuffCodec();

    private final Map<String, Object> serviceMap = new ConcurrentHashMap<>();


    public void addInvoker(String serverKey, RpcClientInvoker invoker) {
        invokers.put(serverKey, invoker);
        log.info("invoker register, {}", serverKey);
    }

    public void removeInvoker(String serverKey) {
        invokers.remove(serverKey);
        log.info("invoker unregister, {}", serverKey);
    }


    /**
     * 将已经注册并连接成功的地址列表返回
     *
     * @param key 需要访问的接口key
     * @return 有效可用的服务列表
     */
    public List<String> getServerList(String key) {
        List<String> serverList = exposeList.get(key);
        if (serverList.isEmpty()) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (String serverKey : serverList) {
            if (!invokers.containsKey(serverKey)) {
                continue;
            }
            result.add(serverKey);
        }
        return result;
    }


    public RpcClientInvoker select(List<String> serverList) {
        int i = random.nextInt(serverList.size());
        String target = serverList.get(i);
        return invokers.get(target);
    }

    public <T> T createService(Class<T> cls, String addr) {
        String key = cls.getName();
        List<String> addressList = exposeList.computeIfAbsent(key, val -> new ArrayList<>());
        if (!addressList.contains(addr)) {
            addressList.add(addr);
        }
        updateServer(addressList);

        if (serviceMap.containsKey(key)) {
            return cls.cast(serviceMap.get(key));
        }
        ClassLoader classLoader = cls.getClassLoader();
        Object instance = Proxy.newProxyInstance(classLoader, new Class[]{cls}, new RpcProxy(this));
        serviceMap.put(key, instance);
        return cls.cast(instance);
    }

    /**
     * 更新服务
     *
     * @param serverList 需要连接的服务器地址集合
     */
    private void updateServer(List<String> serverList) {
        for (String serverKey : serverChannels.keySet()) {
            if (serverList.contains(serverKey)) {
                continue;
            }
            ChannelFuture channelFuture = serverChannels.get(serverKey);
            channelFuture.channel().eventLoop().shutdownGracefully();
            serverChannels.remove(serverKey);
        }
        for (String serverKey : serverList) {
            if (serverChannels.containsKey(serverKey)) {
                continue;
            }
            ChannelFuture connectFuture = connect(serverKey);
            serverChannels.put(serverKey, connectFuture);
        }
    }

    private ChannelFuture connect(String addr) {
        String[] address = addr.split(":");
        String ip = address[0];
        int port = Integer.parseInt(address[1]);
        return connect(ip, port).sync();
    }


    class HandlerInitializer extends ChannelInitializer<SocketChannel> {

        private final String addrKey;
        private final RpcClient rpcClient;

        public HandlerInitializer(String addrKey, RpcClient rpcClient) {
            this.addrKey = addrKey;
            this.rpcClient = rpcClient;
        }

        @Override
        protected void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("heartbeat", new IdleStateHandler(0, 0, RpcConst.BEAT_TIMEOUT, TimeUnit.SECONDS));
            pipeline.addLast("encoder", new RpcEncoder(rpcCodec));
            pipeline.addLast("decoder", new RpcDecoder(rpcCodec));
            pipeline.addLast("client", new RpcClientHandler(addrKey, rpcClient));
        }
    }

    private RpcClient connect(String host, int port) {
        String addrKey = host + ":" + port;
        RpcClient rpcClient = this;
        threadPoolExecutor.execute(() -> {
            EventLoopGroup loopGroup = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap()
                    .group(loopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new HandlerInitializer(addrKey, rpcClient));
            try {
                long start = System.currentTimeMillis();
                ChannelFuture channelFuture = bootstrap.connect(host, port);
                channelFuture.sync();
                log.info("client connected, {}, cost:{}", addrKey, System.currentTimeMillis() - start);
                rpcFuture.done(channelFuture);
                channelFuture.channel().closeFuture().sync();
                log.info("client disconnected, {}", addrKey);
            } catch (Exception e) {
                log.error("{}", e.getMessage());
            } finally {
                log.info("client shutdown, {}", addrKey);
                loopGroup.shutdownGracefully();
            }

        });
        return this;
    }

    public ChannelFuture sync() {
        return rpcFuture.get();
    }

    public void shutdown() {
        ChannelFuture channelFuture = null;
        try {
            channelFuture = rpcFuture.get(1,  TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (channelFuture != null) {
            channelFuture.channel().eventLoop().shutdownGracefully();
        }
        if (!threadPoolExecutor.isShutdown()) {
            threadPoolExecutor.shutdown();
        }

    }


}
