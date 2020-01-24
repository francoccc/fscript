package com.franco.util;

/**
 * <h>ReflectionUtil</h>
 *
 * @author franco
 */
public class ReflectionUtil {

    /**
     * 获取方法名字
     *
     * @return
     */
    public static String getCallMethod() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        String method = stack[3].getMethodName();
        return method;
    }

    /**
     * 获取类名+方法
     *
     * @return
     */
    public static String getCallClassMethod() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        String[] classNames = stack[3].getClassName().split("\\.");
        String fullName = classNames[classNames.length - 1] + ":" + stack[3].getMethodName();
        return fullName;
    }


}
