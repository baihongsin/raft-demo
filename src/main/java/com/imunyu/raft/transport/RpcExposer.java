package com.imunyu.raft.transport;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcExposer extends ChannelInitializer<SocketChannel> {

    private static final Logger log = LoggerFactory.getLogger(RpcExposer.class);
    private static final int DEFAULT_PORT = 8080;

    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(10, 100, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());


    private final ServiceRegistry serviceRegistry = new ServiceRegistry();


    private final String host;
    private final int port;

    private RegistryConfig registryConfig;

    private final RpcFuture<ChannelFuture> startFuture = new RpcFuture<>();

    public RpcExposer() {
        this(DEFAULT_PORT);
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        RpcCodec rpcCodec = new RpcProtostuffCodec();
        pipeline.addLast("heartbeat", new IdleStateHandler(0, 0, Beat.BEAT_TIMEOUT, TimeUnit.SECONDS));
        pipeline.addLast("encoder", new RpcEncoder(rpcCodec));
        pipeline.addLast("decoder", new RpcDecoder(rpcCodec));
        pipeline.addLast("server", new RpcHandler(serviceRegistry));
    }

    public RpcExposer(int port) {
        this.host = getHost();
        this.port = port;
    }


    public RpcExposer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public RpcExposer(int port, RegistryConfig registryConfig) {
        this.host = getHost();
        this.port = port;
        this.registryConfig = registryConfig;
    }

    public RpcExposer(String host, int port, RegistryConfig registryConfig) {
        this.host = host;
        this.port = port;
        this.registryConfig = registryConfig;
    }

    public String getHost() {
        String host;
        try {
            host = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RpcException("unknown host");
        }
        return host;
    }

    public void addService(Object service) {
        serviceRegistry.addService(service);
    }


    public void start() {
        threadPool.execute(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap bootstrap = new ServerBootstrap()
                        .group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(this)
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);
                ChannelFuture channelFuture = bootstrap.bind(port).sync();
                startFuture.done(channelFuture);
                log.info("server bind port:{}", port);
                channelFuture.addListener((ChannelFutureListener) listener -> {
                    boolean active = listener.channel().isActive();
                    log.info("active:" + active);
                });
                channelFuture.channel().closeFuture().sync();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        });
    }


    public void shutdown() {
        ChannelFuture channelFuture = startFuture.get();
        channelFuture.channel().eventLoop().shutdownGracefully();
        threadPool.shutdown();
        log.info("shutdown, {}:{}", host, port);
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public String serverKey() {
        return host + ":" + port;
    }

}
