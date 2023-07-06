package github.javaguide.loadbalance.loadbalancer;

import github.javaguide.loadbalance.AbstractLoadBalance;
import github.javaguide.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * refer to dubbo consistent hash load balance: https://github.com/apache/dubbo/blob/2d9583adf26a2d8bd6fb646243a9fe80a77e65d5/dubbo-cluster/src/main/java/org/apache/dubbo/rpc/cluster/loadbalance/ConsistentHashLoadBalance.java
 *
 * @author RicardoZ
 * @createTime 2020年10月20日 18:15:20
 */
@Slf4j
public class ConsistentHashLoadBalance extends AbstractLoadBalance {
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        int identityHashCode = System.identityHashCode(serviceAddresses);
        // build rpc service name by rpcRequest
        String rpcServiceName = rpcRequest.getRpcServiceName();
        ConsistentHashSelector selector = selectors.get(rpcServiceName);
        // check for updates
        if (selector == null || selector.identityHashCode != identityHashCode) {
            // selector.identityHashCode != identityHashCode 则证明该service对应的服务提供者列表发生了变化，需要更新哈希映射
            selectors.put(rpcServiceName, new ConsistentHashSelector(serviceAddresses, 160, identityHashCode));
            selector = selectors.get(rpcServiceName);
        }
        // selector底层的hash映射对应的都是提供同一个服务的provider，因此，需要尽量传入不同的key来调用不同的provider
        return selector.select(rpcServiceName + Arrays.stream(rpcRequest.getParameters()));
    }

    // 基于一致性哈希实习 参考 https://www.xiaolincoding.com/os/8_network_system/hash.html
    static class ConsistentHashSelector {
        // 基于红黑树实现的有序Map集合，其中的Entry基于key的顺序进行排序
        private final TreeMap<Long, String> virtualInvokers;

        private final int identityHashCode;

        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode) {
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;
            // 给每个真实节点创建160个虚拟节点
            for (String invoker : invokers) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    // 尽量使虚拟节点均匀分布
                    byte[] digest = md5(invoker + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        static byte[] md5(String key) {
            // java.security.MessageDigest 单向文本加密 不论输入的文本多长，输出固定长度的hash值
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                // 执行加密计算
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            // 获取加密结果
            return md.digest();
        }

        static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        public String select(String rpcServiceKey) {
            byte[] digest = md5(rpcServiceKey);
            return selectForKey(hash(digest, 0));
        }

        public String selectForKey(long hashCode) {
            // tailMap(myKey, true) 获取TreeMap中key值大于等于myKey的所有项组成的map视图
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();

            // 取一致性哈希逻辑环上的下一个虚拟节点
            if (entry == null) {
                entry = virtualInvokers.firstEntry();
            }

            // 返回真实节点，即 要调用的服务提供者地址
            return entry.getValue();
        }
    }
}
