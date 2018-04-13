# MVC层自动记录调试日志（一般用于开发环境）

打印出请求参数与各项请求指标  

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.minlia.cloud.starter/cloud-starter-loggable/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.minlia.cloud.starter/cloud-starter-loggable/) 
[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt) 
[![Build Status](https://travis-ci.org/minlia-projects/cloud-starter-loggable.svg?branch=master)](https://travis-ci.org/minlia-projects/cloud-starter-loggable)
[![Waffle.io - Columns and their card count](https://badge.waffle.io/minlia-projects/cloud-starter-loggable.svg?columns=all)](https://waffle.io/minlia-projects/cloud-starter-loggable)


## 集成到自已的项目时添加依赖项  
```pom
<dependency>
  <groupId>com.minlia.cloud.starter</groupId>
  <artifactId>cloud-starter-loggable</artifactId>
  <version>2.0.0.RELEASE</version>
</dependency>
```

## 应用配置项：

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