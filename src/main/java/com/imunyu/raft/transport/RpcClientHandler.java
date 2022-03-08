package com.imunyu.raft.transport;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class RpcClientHandler extends ChannelInboundHandlerAdapter implements RpcClientInvoker {

    private static final Logger log = LoggerFactory.getLogger(RpcClientHandler.class);

    private final RpcClient clientManager;

    private final ConcurrentHashMap<String, RpcFuture<Object>> rpcFutureMap = new ConcurrentHashMap<>();

    private ChannelHandlerContext handlerCtx;

    private final String serverKey;

    private final AtomicLong atomicLong = new AtomicLong(1000);

    public RpcClientHandler(RpcClient clientManager, String serverKey) {
        this.clientManager = clientManager;
        this.serverKey = serverKey;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.handlerCtx = ctx;
        clientManager.addInvoker(serverKey, this);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof RpcInternalWrapper) {
            RpcInternalWrapper wrapper = (RpcInternalWrapper) msg;
            Object data = wrapper.getData();
            RpcFuture<Object> requestFuture = rpcFutureMap.get(wrapper.getReqId());
            requestFuture.done(data);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public RpcResponse sendRequest(RpcRequest request) throws InterruptedException {
        long l = atomicLong.addAndGet(1);
        RpcInternalWrapper wrapper = new RpcInternalWrapper();
        wrapper.setReqId(l + "");
        wrapper.setData(request);
        RpcFuture<Object> requestFuture = new RpcFuture<>();
        rpcFutureMap.put(wrapper.getReqId(), requestFuture);
        ChannelFuture channelFuture = handlerCtx.writeAndFlush(wrapper);
        channelFuture.await();
        if (!channelFuture.isSuccess()) {
            log.error("send request error");
        }
        return (RpcResponse) requestFuture.get();
    }

    @Override
    public Object invoke(String className, String methodName, Class<?>[] parameterTypes, Object[] parameters) {
        RpcRequest request = new RpcRequest();
        request.setClassName(className);
        request.setMethodName(methodName);
        request.setParamTypes(parameterTypes);
        request.setParams(parameters);
        try {
            log.debug("rpc request:{}", request);
            long start = System.currentTimeMillis();
            RpcResponse rpcResponse = sendRequest(request);
            log.info("cost mills: {}", System.currentTimeMillis() - start);
            log.debug("rpc response:{}", rpcResponse);

            return rpcResponse.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
