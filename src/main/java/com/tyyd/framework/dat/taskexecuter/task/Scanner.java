package com.tyyd.framework.dat.taskexecuter.task;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.stereotype.Service;

import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.taskexecuter.Result;
import com.tyyd.framework.dat.taskexecuter.runner.TaskRunner;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class Scanner implements DisposableBean, BeanFactoryPostProcessor, BeanPostProcessor{

    private static final Logger LOGGER = LoggerFactory.getLogger(Scanner.class);

    private String[] annotationPackages;

    public void setBasePackage(String annotationPackage) {
        this.annotationPackages = (annotationPackage == null || annotationPackage.length() == 0) ? null
                : Pattern.compile("\\s*[,]+\\s*").split(annotationPackage);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        if (beanFactory instanceof BeanDefinitionRegistry) {
            try {
                // init scanner
                Class<?> scannerClass = Class.forName("org.springframework.context.annotation.ClassPathBeanDefinitionScanner");
                Object scanner = scannerClass.getConstructor(new Class<?>[]{BeanDefinitionRegistry.class, boolean.class}).newInstance(beanFactory, true);
                // add filter
                Class<?> filterClass = Class.forName("org.springframework.core.type.filter.AnnotationTypeFilter");
                Object filter = filterClass.getConstructor(Class.class).newInstance(Service.class);
                Method addIncludeFilter = scannerClass.getMethod("addIncludeFilter", Class.forName("org.springframework.core.type.filter.TypeFilter"));
                addIncludeFilter.invoke(scanner, filter);
                // scan packages
                Method scan = scannerClass.getMethod("scan", String[].class);
                scan.invoke(scanner, new Object[]{annotationPackages});
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {

        Class<?> clazz = bean.getClass();

        if (!isMatchPackage(clazz)) {
            return bean;
        }

        if (!clazz.isAnnotationPresent(Service.class)) {
            return bean;
        }

        Method[] methods = clazz.getMethods();
        if (methods != null && methods.length > 0) {

            for (final Method method : methods) {
                if (method.isAnnotationPresent(TaskRunnerItem.class)) {
                    TaskRunnerItem jobRunnerItem = method.getAnnotation(TaskRunnerItem.class);
                    String shardValue = jobRunnerItem.shardValue();
                    if (StringUtils.isEmpty(shardValue)) {
                        LOGGER.error(clazz.getName() + ":" + method.getName() + " " + TaskRunnerItem.class.getName() + " shardValue can not be null");
                        continue;
                    }
                    Class<?> returnType = method.getReturnType();
                    if (returnType != Result.class) {
                        LOGGER.error(clazz.getName() + ":" + method.getName() + " returnType must be " + Result.class.getName());
                        continue;
                    }

                    final Class<?>[] pTypes = method.getParameterTypes();

                    TaskRunnerHolder.add(shardValue, new TaskRunner() {
                        @Override
                        public Result run(Task task) throws Throwable {
                            if (pTypes == null || pTypes.length == 0) {
                                return (Result) method.invoke(bean);
                            }
                            Object[] pTypeValues = new Object[pTypes.length];

                            for (int i = 0; i < pTypes.length; i++) {
                                if (pTypes[i] == Task.class) {
                                    pTypeValues[i] = task;
                                } else {
                                    pTypeValues[i] = null;
                                }
                            }
                            return (Result) method.invoke(bean, pTypeValues);
                        }
                    });
                }
            }
        }

        return bean;
    }

    @Override
    public void destroy() throws Exception {

    }

    private boolean isMatchPackage(Class<?> clazz) {
        if (annotationPackages == null || annotationPackages.length == 0) {
            return true;
        }
        String beanClassName = clazz.getName();
        for (String pkg : annotationPackages) {
            if (beanClassName.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }
}
