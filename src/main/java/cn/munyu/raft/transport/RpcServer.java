package cn.munyu.raft.transport;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcServer extends ChannelInitializer<SocketChannel> {

    private static final Logger log = LoggerFactory.getLogger(RpcServer.class);
    private static final int DEFAULT_PORT = 8080;
    private final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(RpcConst.SERVER_THREAD_POOL_NAME).build();
    private final ThreadPoolExecutor threadPool =
            new ThreadPoolExecutor(10, 100, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), threadFactory);

    private final RpcRegistry rpcRegistry = new RpcRegistry();
    private final RpcCodec rpcCodec = new RpcProtostuffCodec();
    private final RpcFuture<ChannelFuture> startFuture = new RpcFuture<>();

    private final String host;
    private final int port;

    public RpcServer() {
        this(DEFAULT_PORT);
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("heartbeat", new IdleStateHandler(0, 0, RpcConst.BEAT_TIMEOUT, TimeUnit.SECONDS));
        pipeline.addLast("encoder", new RpcEncoder(rpcCodec));
        pipeline.addLast("decoder", new RpcDecoder(rpcCodec));
        pipeline.addLast("server", new RpcServerHandler(rpcRegistry));
    }

    public RpcServer(int port) {
        this.host = getHost();
        this.port = port;
    }


    public RpcServer(String host, int port) {
        this.host = host;
        this.port = port;
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
        rpcRegistry.addService(service);
    }

    public void start() {
        log.info("server start {}:{}", host, port);
        threadPool.execute(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup(threadFactory);
            EventLoopGroup workerGroup = new NioEventLoopGroup(threadFactory);

            try {
                ServerBootstrap bootstrap = new ServerBootstrap()
                        .group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(this)
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);

                InetSocketAddress socketAddress = new InetSocketAddress(host, port);
                ChannelFuture channelFuture = bootstrap.bind(socketAddress).sync();
                log.info("server bind port:{}", port);
                startFuture.done(channelFuture);
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


}
