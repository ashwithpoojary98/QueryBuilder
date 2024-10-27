package io.github.ashwithpoojary98.compilers;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ConditionsCompilerProvider{

    private final Class<?> compilerType;
    private final Map<String, Method> methodsCache = new HashMap<>();
    private final Object syncRoot = new Object();


    public ConditionsCompilerProvider(Compiler compiler) {
        this.compilerType = compiler.getClass();
    }

    public Method getMethodInfo(Class<?> clauseType, String methodName) {
        // The cache key should take the type and the method name into consideration
        String cacheKey = methodName + "::" + clauseType.getName();

        synchronized (syncRoot) {
            if (methodsCache.containsKey(cacheKey)) {
                return methodsCache.get(cacheKey);
            }

            Method methodInfo = findMethodInfo(clauseType, methodName);
            methodsCache.put(cacheKey, methodInfo);
            return methodInfo;
        }
    }

    private Method findMethodInfo(Class<?> clauseType, String methodName) {
        Method[] methods = compilerType.getDeclaredMethods();
        Method methodInfo = null;

        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                methodInfo = method;
                break;
            }
        }
        Class<?> superClass = compilerType.getSuperclass();
        while (superClass != null) {
            Method[] superMethods = superClass.getDeclaredMethods();
            for (Method method : superMethods) {
                if (method.getName().equals(methodName)) {
                    methodInfo = method;
                    break;
                }
            }
            superClass = superClass.getSuperclass();
        }

        if (methodInfo == null) {
            throw new RuntimeException("Failed to locate a compiler for '" + methodName + "'.");
        }

        if (clauseType.getTypeParameters().length > 0) {
            // Assuming clauseType is a parameterized type, you might need to handle generic types accordingly
            // Note: Java reflection has some limitations with generics, so this may need additional handling
            methodInfo = makeGenericMethod(methodInfo, clauseType);
        }

        return methodInfo;
    }

    private Method makeGenericMethod(Method methodInfo, Class<?> clauseType) {
        // This method would be where you handle making a generic method, if necessary
        // Java's reflection does not support directly making generic methods like C# does
        return methodInfo; // Placeholder for actual implementation
    }
}
