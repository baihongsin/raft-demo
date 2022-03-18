package raft.transport;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class RpcClientHandler extends ChannelInboundHandlerAdapter implements RpcClientInvoker {

    private static final Logger log = LoggerFactory.getLogger(RpcClientHandler.class);


    private final ConcurrentHashMap<String, RpcFuture<Object>> rpcFutureMap = new ConcurrentHashMap<>();

    private ChannelHandlerContext handlerCtx;

    private final AtomicLong atomicLong = new AtomicLong(1000);

    private final String serviceKey;

    private final RpcClient rpcClient;

    public RpcClientHandler(String serviceKey, RpcClient rpcClient) {
        this.serviceKey = serviceKey;
        this.rpcClient = rpcClient;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.handlerCtx = ctx;
        rpcClient.addInvoker(serviceKey, this);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        rpcClient.removeInvoker(serviceKey);

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
        if (handlerCtx == null) {
            log.error("handler context not init");
            requestFuture.done(null);
        } else {
            ChannelFuture channelFuture = handlerCtx.writeAndFlush(wrapper);
            channelFuture.await();
            if (!channelFuture.isSuccess()) {
                log.error("send request error");
                requestFuture.done(null);
            }
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
            if (rpcResponse == null) {
                return null;
            }
            return rpcResponse.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
