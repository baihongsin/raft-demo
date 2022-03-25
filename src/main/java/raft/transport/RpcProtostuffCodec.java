package raft.transport;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcProtostuffCodec implements RpcCodec {

    private static final Logger logger = LoggerFactory.getLogger(RpcProtostuffCodec.class);
    private final Schema<RpcInternalWrapper> wrapperSchema = RuntimeSchema.getSchema(RpcInternalWrapper.class);

    @Override
    public byte[] encode(RpcInternalWrapper data) {
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE * 2);
        byte[] res;
        try {
            res = ProtostuffIOUtil.toByteArray(data, wrapperSchema, buffer);
        } catch (Exception e) {
            logger.error("rpc encode error ", e);
            return null;
        } finally {
            buffer.clear();
        }
        return res;
    }

    @Override
    public RpcInternalWrapper decode(byte[] data) {
        try {
            RpcInternalWrapper wrapper = wrapperSchema.newMessage();
            ProtostuffIOUtil.mergeFrom(data, wrapper, wrapperSchema);
            return wrapper;
        } catch (Exception e) {
            logger.error("rpc decode error ", e);
            return null;
        }


    }

}
