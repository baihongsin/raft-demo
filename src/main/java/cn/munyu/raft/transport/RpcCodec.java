package cn.munyu.raft.transport;

public interface RpcCodec {

    byte[] encode(RpcInternalWrapper wrapper);

    RpcInternalWrapper decode(byte[] data);

}
