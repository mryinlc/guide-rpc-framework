package github.javaguide.serialize.protostuff;

import github.javaguide.remoting.dto.RpcRequest;
import org.junit.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author：mryinlc
 * @description:
 * @date: 2023/7/5
 */
public class ProtoStuffSerializerTest {
    @Test
    public void testProtoStuffSerializer() {
        RpcRequest target = RpcRequest.builder().methodName("hello")
                .parameters(new Object[]{"sayhelooloo", "sayhelooloosayhelooloo"})
                .interfaceName("github.javaguide.HelloService")
                .paramTypes(new Class<?>[]{String.class, String.class})
                .requestId(UUID.randomUUID().toString())
                .group("group1")
                .version("version1")
                .build();
        ProtostuffSerializer protostuffSerializer = new ProtostuffSerializer();
        byte[] bytes = protostuffSerializer.serialize(target);
        RpcRequest actual = protostuffSerializer.deserialize(bytes, RpcRequest.class);
        assertEquals(target.getGroup(), actual.getGroup());
        assertEquals(target.getVersion(), actual.getVersion());
        assertEquals(target.getRequestId(), actual.getRequestId());
    }
}
