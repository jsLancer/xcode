> 通过动态编译 Java 类生成 class 字节码执行，避免每次新加Test接口，都要提交打包发布等流程。支持Web应用 Jar 包或 War 包两种运行方式。



### 依赖

```
<dependency>
    <groupId>cn.westlife</groupId>
    <artifactId>dynamic-compile</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```



### 原理

借助 Java Compiler API 完成编译，这是JDK6开始提供的标准API，提供了与 javac 对等的编译功能，即动态编译。

##### 1. War 包底层机制

将提交的类编译成字节码文件，交给父类加载器加载（WebappClassLoader），因为同一个类只会被类加载器加载一次，为了支持重复提交执行，对类生成版本号。

##### 2. Jar 包底层机制

参考Arthas实现，使用内存编译，不生成字节码文件（性能更优一点）， 自定义加载加载器负责加载，不交给父加载器加载，每次执行都会重新生成类加载器。



### 使用

#### 1. Callable 形式

代码可以是独立的代码逻辑

```
public class TestAction implements Callable<String> {
    @Override
    public String call() throws Exception {
        return "hello world";
    }
}
```

也可以是包含上下文依赖的逻辑

```
public class CallAction implements Callable<String> {
    @Override
    public String call() throws Exception {
        ActionService actionService = ApplicationContextHolder.getContext().getBean(ActionService.class);
        return actionService.action();
    }
}

```



#### 2. BiFunction 形式

```
public class TestAction implements BiFunction<HttpServletRequest, HttpServletResponse, String> {
    @Override
    public String apply(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        return "hello world";
    }
}
```

#### 代码提交

参数：
- code 代码
- cls 全限类名

```
@PostMapping("/call")
public JSONResult compileExecute(@RequestParam("code") String code,
                                @RequestParam("class") String cls) {
    Object result = DynamicCompile.compileCall(code, cls);
    return JSONResult.okResult(result);
}

@PostMapping("/apply")
public JSONResult compileExecute(@RequestParam("code") String code,
                             @RequestParam("class") String cls,
                             HttpServletRequest request,
                             HttpServletResponse response) {
    Object result = DynamicCompile.compileApply(code, cls, request, response);
    return JSONResult.okResult(result);
}
```
