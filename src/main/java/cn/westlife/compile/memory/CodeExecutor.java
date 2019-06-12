package cn.westlife.compile.memory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;

/**
 * @author westlife
 * @date 2019/6/6 16:57
 */
public class CodeExecutor {

    public static Object compileCall(String className, String source) throws Throwable {
        ClassLoader classLoader = CodeExecutor.class.getClassLoader();

        DynamicCompiler dynamicCompiler = new DynamicCompiler(classLoader);
        Map<String, Class<?>> classMap = dynamicCompiler.build(className, source);
        Class<?> cls = classMap.get(className);
        if (cls == null) {
            throw new RuntimeException("compile error");
        }
        Object instance = cls.newInstance();
        if (!(instance instanceof Callable)) {
            throw new RuntimeException("only support Callable interface");
        }

        MethodType methodType = MethodType.methodType(Object.class);
        MethodHandle methodHandle = MethodHandles.lookup().findVirtual(cls, "call", methodType);
        return methodHandle.invoke(instance);
    }


    public static <T, U> Object compileApply(String className, String source, T t, U u) throws Throwable {
        ClassLoader classLoader = CodeExecutor.class.getClassLoader();

        DynamicCompiler dynamicCompiler = new DynamicCompiler(classLoader);
        Map<String, Class<?>> classMap = dynamicCompiler.build(className, source);
        Class<?> cls = classMap.get(className);
        if (cls == null) {
            throw new RuntimeException("compile error");
        }

        Object instance = cls.newInstance();
        if (!(instance instanceof BiFunction)) {
            throw new RuntimeException("only support BiFunction interface");
        }

        MethodType methodType = MethodType.methodType(Object.class, Object.class, Object.class);
        MethodHandle methodHandle = MethodHandles.lookup().findVirtual(cls, "apply", methodType);
        return methodHandle.invoke(instance, t, u);
    }

}
