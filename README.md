# start-java
一个 Spring Cloud Alibaba 脚手架项目，使用 Maven 构建


## JDK 8/11/17
- 下载（微软 build）：https://learn.microsoft.com/zh-cn/java/openjdk/download
- 目前 11 占有率最高，超过 8
- 配置环境变量：`JAVA_HOME`，`Path`


## Maven
- IDEA 自带 Maven，也可以手动下载并配置环境变量：https://maven.apache.org/download.cgi
- 查找 Maven 包：https://central.sonatype.com/
- 配置阿里源（重要） `settings.xml` `<mirror>`
```xml
<!-- 阿里云仓库 -->
<mirror>
    <id>alimaven</id>
    <mirrorOf>central</mirrorOf>
    <name>aliyun maven</name>
    <url>http://maven.aliyun.com/nexus/content/repositories/central/</url>
</mirror>
```
- 配置本地仓库 `settings.xml` `<localRepository>`
- 绑定 JDK 版本 `settings.xml` `<profile>`


## Spring Boot/Cloud
- Spring Cloud Alibaba：https://github.com/alibaba/spring-cloud-alibaba.git
- Nepxion Discovery：https://github.com/Nepxion/Discovery.git
- spring脚手架：https://start.spring.io/
- 阿里巴巴脚手架：https://start.aliyun.com/


## JDBC（Java DataBase Connectivity）
- JDBC Interface 已内置 JDK 中，是一种标准接口
- JDBC Driver 由数据库厂商实现，例如 MySQL Driver 由 Oracle 实现（引入 jar 包）


## MySQL
- docker安装：`docker pull mysql`
- docker运行：`docker run -itd --name zxf-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 mysql`
- 默认账号 `root`


## Redis
- docker安装：`docker pull redis`
- docker运行：`docker run -itd --name zxf-redis -p 6379:6379 redis`
- 默认无密码


## Nacos
- docker安装：`docker pull nacos/nacos-server`
- docker运行：`docker run --name zxf-nacos -e MODE=standalone -p 8848:8848 -p 9848:9848 -p 9849:9849 -d nacos/nacos-server`
- 发布配置 http://127.0.0.1:8848/nacos 默认账号密码 `nacos`
- 获取配置 `src/main/resources/application.yml`


### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.6.11/maven-plugin/)
* [MyBatis Framework](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.6.11/reference/htmlsingle/#web)
* [Arthas](https://arthas.gitee.io/index.html)
* [Nacos Configuration](https://spring-cloud-alibaba-group.github.io/github-pages/hoxton/en-us/index.html#_spring_cloud_alibaba_nacos_config)
* [Nacos Service Discovery](https://spring-cloud-alibaba-group.github.io/github-pages/hoxton/en-us/index.html#_spring_cloud_alibaba_nacos_discovery)

### Guides
The following guides illustrate how to use some features concretely:

* [《阿里巴巴Java开发手册》](https://github.com/alibaba/p3c)
* [MyBatis Quick Start](https://github.com/mybatis/spring-boot-starter/wiki/Quick-Start)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Nacos Configuration Example](https://github.com/alibaba/spring-cloud-alibaba/tree/master/spring-cloud-alibaba-examples/nacos-example/nacos-config-example)
* [Nacos Service Discovery Example](https://github.com/alibaba/spring-cloud-alibaba/blob/master/spring-cloud-alibaba-examples/nacos-example/nacos-discovery-example/readme.md)

