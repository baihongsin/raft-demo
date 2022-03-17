package cn.munyu.raft.transport;

public class RpcResponse {

    private String error;

    private Object data;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "error='" + error + '\'' +
                ", data=" + data +
                '}';
    }
}
