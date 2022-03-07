package com.imunyu.raft.transport;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;


public class RpcHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(RpcHandler.class);

    private final ServiceRegistry serviceRegistry;


    public RpcHandler(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {

            if (msg instanceof RpcInternalWrapper) {
                RpcInternalWrapper wrapper = (RpcInternalWrapper) msg;
                Object data = wrapper.getData();
                RpcRequest request = (RpcRequest) data;
                String methodName = request.getMethodName();
                Object o = serviceRegistry.getService(request.getClassName());

                Object[] params = request.getParams();
                Class<?>[] paramTypes = request.getParamTypes();
                Method method = o.getClass().getMethod(methodName, paramTypes);
                method.setAccessible(true);
                Object ret = method.invoke(o, params);

                RpcInternalWrapper resp = new RpcInternalWrapper();
                RpcResponse response = new RpcResponse();
                response.setData(ret);
                resp.setReqId(wrapper.getReqId());
                resp.setData(response);
                ctx.writeAndFlush(resp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        String key = address.getAddress().getHostAddress() + ":" +  address.getPort();
        ContextHolder.set(key);
        serviceRegistry.register(key);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ContextHolder.remove();
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        String key = address.getAddress().getHostAddress() + ":" + address.getPort();
        serviceRegistry.unregister(key);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        log.error("exceptionCaught:" + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
        ContextHolder.remove();
    }
}
