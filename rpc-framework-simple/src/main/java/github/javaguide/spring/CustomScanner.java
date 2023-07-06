package github.javaguide.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

/**
 * custom package scanner
 * 继承ClassPathBeanDefinitionScanner可以自定义类扫描器，通过添加过滤器 addIncludeFilter
 * 可以指定哪些类需要被包装为beanDefinition并注册到ioc容器中
 *
 * @author shuang.kou
 * @createTime 2020年08月10日 21:42:00
 */
public class CustomScanner extends ClassPathBeanDefinitionScanner {

    public CustomScanner(BeanDefinitionRegistry registry, Class<? extends Annotation> annoType) {
        super(registry);
        super.addIncludeFilter(new AnnotationTypeFilter(annoType));
    }

    @Override
    public int scan(String... basePackages) {
        return super.scan(basePackages);
    }
}