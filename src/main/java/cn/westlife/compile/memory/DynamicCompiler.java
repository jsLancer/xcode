package cn.westlife.compile.memory;

import cn.westlife.compile.JavaSourceFromString;

import javax.tools.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author westlife
 * @date 2019/6/6 15:22
 */
public class DynamicCompiler {

    private final JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
    private final StandardJavaFileManager standardFileManager;
    private final List<String> options = new ArrayList<>();
    private final DynamicClassLoader dynamicClassLoader;

    public DynamicCompiler(ClassLoader classLoader) {
        options.add("-Xlint:unchecked");
        standardFileManager = javaCompiler.getStandardFileManager(null, null, null);
        dynamicClassLoader = new DynamicClassLoader(classLoader);
    }

    public Map<String, Class<?>> build(String className, String source) throws ClassNotFoundException {
        JavaSourceFromString javaSource = new JavaSourceFromString(className, source);

        JavaFileManager fileManager = new DynamicJavaFileManager(standardFileManager, dynamicClassLoader);
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();

        JavaCompiler.CompilationTask task = javaCompiler.getTask(null, fileManager, collector, options, null,
                Arrays.asList(javaSource));

        Boolean success = task.call();
        if (!success) {
            System.out.println("compile error");
            return null;
        }

        return dynamicClassLoader.getClasses();
    }


}
