package github.javaguide.config;

import github.javaguide.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * When the server  is closed, do something such as unregister all services
 *
 * @author shuang.kou
 * @createTime 2020年06月04日 13:11:00
 */
@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll() {
        log.info("addShutdownHook for clearAll");
        // try {
        //     InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
        //     CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
        // } catch (UnknownHostException ignored) {
        // }
        Runtime.getRuntime().addShutdownHook(new Thread(ThreadPoolFactoryUtil::shutDownAllThreadPool));
    }
}
