package raft.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder<RpcInternalWrapper> {

    private final RpcCodec rpcCodec;

    public RpcEncoder(RpcCodec rpcCodec) {
        this.rpcCodec = rpcCodec;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcInternalWrapper msg, ByteBuf out) {
        try {
            byte[] data = rpcCodec.encode(msg);
            out.writeInt(data.length);
            out.writeBytes(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
