package github.javaguide.spring;

import github.javaguide.annotation.RpcScan;
import github.javaguide.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * scan and filter specified annotations
 * 通过ImportBeanDefinitionRegistrar接口，可以手动向ioc容器注入bean
 * 该接口的使用参考 <a href="https://zhuanlan.zhihu.com/p/430837566" />
 * 简单来说，只要使用@import接口导入实现类，便可以自动调用其中registerBeanDefinitions方法来注册bean
 *
 * @author shuang.kou
 * @createTime 2020年08月10日 22:12:00
 */
@Slf4j
public class CustomScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    private static final String SPRING_BEAN_BASE_PACKAGE = "github.javaguide";
    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";
    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;

    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        // annotationMetadata 指代的是通过标注@import导入CustomScannerRegistrar的类的信息
        //get the attributes and values ​​of RpcScan annotation
        AnnotationAttributes rpcScanAnnotationAttributes = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(RpcScan.class.getName()));
        String[] rpcScanBasePackages = new String[0];
        if (rpcScanAnnotationAttributes != null) {
            // get the value of the basePackage property
            // 获取@RpcScan注解的 basePackage 属性值
            rpcScanBasePackages = rpcScanAnnotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        if (rpcScanBasePackages.length == 0) {
            // 如果@RpcScan中没有指定basePackage属性，则以 annotationMetadata 对应的类的 package 作为basePackage
            rpcScanBasePackages = new String[]{((StandardAnnotationMetadata) annotationMetadata).getIntrospectedClass().getPackage().getName()};
        }
        // Scan the RpcService annotation
        CustomScanner rpcServiceScanner = new CustomScanner(beanDefinitionRegistry, RpcService.class);
        // Scan the Component annotation
        CustomScanner springBeanScanner = new CustomScanner(beanDefinitionRegistry, Component.class);
        if (resourceLoader != null) {
            rpcServiceScanner.setResourceLoader(resourceLoader);
            springBeanScanner.setResourceLoader(resourceLoader);
        }
        int springBeanAmount = springBeanScanner.scan(SPRING_BEAN_BASE_PACKAGE);
        log.info("springBeanScanner扫描的数量 [{}]", springBeanAmount);
        int rpcServiceCount = rpcServiceScanner.scan(rpcScanBasePackages);
        log.info("rpcServiceScanner扫描的数量 [{}]", rpcServiceCount);

    }

}
