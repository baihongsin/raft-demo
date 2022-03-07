package com.imunyu.raft.transport;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class RpcProtostuffCodec implements RpcCodec {

    private final Schema<RpcInternalWrapper> wrapperSchema = RuntimeSchema.getSchema(RpcInternalWrapper.class);

    @Override
    public byte[] encode(RpcInternalWrapper data) {
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        byte[] res;
        try {
            res = ProtostuffIOUtil.toByteArray(data, wrapperSchema, buffer);
        } finally {
            buffer.clear();
        }
        return res;
    }

    @Override
    public RpcInternalWrapper decode(byte[] data) {
        RpcInternalWrapper wrapper = wrapperSchema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, wrapper, wrapperSchema);
        return wrapper;
    }

}
