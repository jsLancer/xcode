> 通过动态编译 Java 类生成 class 字节码执行，避免每次新加Test接口，都要提交打包发布等流程。

### 依赖

```
<dependency>
    <groupId>cn.westlife</groupId>
    <artifactId>dynamic-compile</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 使用

#### 1. Callable 形式
```
public class TestAction implements Callable<String> {
    @Override
    public String call() throws Exception {
        return "hello world";
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
