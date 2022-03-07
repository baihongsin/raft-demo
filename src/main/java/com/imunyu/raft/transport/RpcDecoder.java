package com.imunyu.raft.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RpcDecoder extends ByteToMessageDecoder {

    private final RpcCodec rpcCodec;

    public RpcDecoder(RpcCodec rpcCodec) {
        this.rpcCodec = rpcCodec;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] buf = new byte[dataLength];
        in.readBytes(buf);
        RpcInternalWrapper obj = rpcCodec.decode(buf);
        if (obj != null) {
            out.add(obj);
        }
    }
}
