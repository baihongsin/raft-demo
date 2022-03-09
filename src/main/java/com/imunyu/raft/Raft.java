package com.imunyu.raft;

/**
 * 启动任意一组服务器后，通过加入的方式来组建集群
 * 当集群有2个节点，通过随机选举来，选择一个来设置为一个主，一个从
 *      选举方式：互相发送网络请求，传递一个随机数，谁的大谁是主
 * 当集群有3个以及以上数量的节点，使用raft协议来确定主节点来管理并同步集群
 *
 * 必须先解决集群启动问题
 * 然后解决选举问题
 * 然后是日志复制
 * 最后是整体的安全保证
 *
 * 首先，当一个节点加入一个集群，需要通过集群中一个节点来获取整个集群的状态
 * 节点列表，各个节点的状态，哪个节点是领导者
 * 如果当前节点存在领导者，则当前节点状态变更为follower，然后开始计时
 *
 *
 *
 */
public class Raft {

    public static void main(String[] args) {
        String addr = "127.0.0.1:8080";

        Node node = new Node();
        node.startup(8080);
        node.join(addr);

        Node node2 = new Node();
        node2.startup(8081);
        node2.join(addr);

        Node node3 = new Node();
        node3.startup(8082);
        node3.join(addr);
    }
}
