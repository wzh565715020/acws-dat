package com.tyyd.framework.dat.taskexecuter.task;

import org.springframework.beans.factory.InitializingBean;

import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.taskexecuter.Result;
import com.tyyd.framework.dat.taskexecuter.runner.TaskRunner;

import java.lang.reflect.Method;

public class MethodInvokingJobRunner implements InitializingBean {

    private Object targetObject;
    private String targetMethod;
    private String shardValue;

    @Override
    public void afterPropertiesSet() throws Exception {

        if (targetObject == null) {
            throw new IllegalArgumentException("targetObject can not be null");
        }
        if (StringUtils.isEmpty(targetMethod)) {
            throw new IllegalArgumentException("targetMethod can not be null");
        }
        if (StringUtils.isEmpty(shardValue)) {
            throw new IllegalArgumentException("shardValue can not be null");
        }

        Class<?> clazz = targetObject.getClass();
        Method[] methods = clazz.getMethods();
        Method method = null;
        if (methods != null && methods.length > 0) {
            for (Method m : methods) {
                if (m.getName().equals(targetMethod)) {
                    if (method != null) {
                        throw new IllegalArgumentException("Duplicate targetMethod can not be found in " + targetObject.getClass().getName());
                    }
                    method = m;
                }
            }
        }

        if (method == null) {
            throw new IllegalArgumentException("targetMethod can not be found in " + targetObject.getClass().getName());
        }

        Class<?> returnType = method.getReturnType();
        if (returnType != Result.class) {
            throw new IllegalArgumentException(clazz.getName() + ":" + method.getName() + " returnType must be " + Result.class.getName());
        }

        final Class<?>[] pTypes = method.getParameterTypes();

        final Method finalMethod = method;

        TaskRunnerHolder.add(shardValue, new TaskRunner() {
            @Override
            public Result run(Task task) throws Throwable {
                if (pTypes == null || pTypes.length == 0) {
                    return (Result) finalMethod.invoke(targetObject);
                }
                Object[] pTypeValues = new Object[pTypes.length];

                for (int i = 0; i < pTypes.length; i++) {
                    if (pTypes[i] == Task.class) {
                        pTypeValues[i] = task;
                    } else {
                        pTypeValues[i] = null;
                    }
                }
                return (Result) finalMethod.invoke(targetObject, pTypeValues);
            }
        });

    }

    public void setTargetObject(Object targetObject) {
        this.targetObject = targetObject;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public void setShardValue(String shardValue) {
        this.shardValue = shardValue;
    }

}
