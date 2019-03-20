package cn.westlife.compile;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author westlife
 * @date 2018/12/6 10:06
 */
public class ClassGenerator {

    private static Map<String, Integer> classCacheMap = new ConcurrentHashMap<>();


    public static Class<?> generate(String classFullName, String code, String classRootDir, String jarFile) throws MalformedURLException, ClassNotFoundException {
        Integer version = classCacheMap.get(classFullName);
        if (version == null) {
            version = 1;
            classCacheMap.put(classFullName, version);
        } else {
            classCacheMap.put(classFullName, ++version);
        }
        String className = classFullName.substring(classFullName.lastIndexOf(".") + 1);

        String newClassName = className + "_" + version;
        code = code.replace(className, newClassName);
        newClassName = classFullName.substring(0, classFullName.lastIndexOf(".") + 1) + newClassName;

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavaFileObject fileObject = new JavaSourceFromString(newClassName, code);

        File root = new File(classRootDir);
        if (!root.exists()) {
            root.mkdirs();
        }

        String jars = getJars(jarFile);
        String libs = getTomcatLibs(root);
        Iterable<String> options = Arrays.asList("-d", classRootDir, "-cp", jars + File.pathSeparator + libs + File.pathSeparator + classRootDir);
        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(fileObject);
        JavaCompiler.CompilationTask task = compiler.getTask(null, null, null, options, null, compilationUnits);

        // 动态编译
        boolean success = task.call();
        if (!success) {
            System.out.println("compile error root path = " + root.getPath());
            return null;
        }
        System.out.println("compile success root path = " + root.getPath());
        URL[] urls = new URL[]{root.toURI().toURL()};
        System.out.println("url = " + urls[0]);
        // 设置父类加载器
        ClassLoader parent = ClassGenerator.class.getClassLoader();
        // WebappClassLoader -> StandardClassLoader -> AppClassLoader -> ExtClassLoader -> BootstrapClassloader
        URLClassLoader classLoader = new URLClassLoader(urls, parent);
        return Class.forName(newClassName, true, classLoader);
    }

    private static String getJars(String jarFile) {
        File file = new File(jarFile);
        if (!file.exists()) {
            System.out.println("jar file not exists");
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (File jar : Objects.requireNonNull(file.listFiles())) {
            builder.append(jar.getPath()).append(File.pathSeparator);
        }

        return builder.toString();
    }

    private static String getTomcatLibs(File root) {
        String tomcatLibPath = root.getParentFile().getParentFile().getParentFile().getParent() + "/lib";
        File file = new File(tomcatLibPath);
        if (!file.exists()) {
            System.out.println("tomcat lib file not exists");
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (File jar : Objects.requireNonNull(file.listFiles())) {
            builder.append(jar.getPath()).append(File.pathSeparator);
        }

        return builder.toString();
    }
}