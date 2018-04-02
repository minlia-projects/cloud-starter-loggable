# MVC层自动记录调试日志（一般用于开发环境）

打印出请求参数与各项请求指标  

## TODO  
添加parameter语言请求参数配置项  

## Done  
Header区域请求头配置项  

## 依赖项目  
cloud-starter-context  

## 添加依赖项:  
```pom
<dependency>
  <groupId>com.minlia.cloud.starter</groupId>
  <artifactId>cloud-starter-loggable</artifactId>
  <version>2.0.0.RELEASE</version>
</dependency>
```

##应用配置项：

application-dev.yml
```yaml
system:
  mvc:
    loggable:
      enabled: true
```

application-dev.properties

```properties
system.mvc.loggable.enabled=true
```