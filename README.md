# start-springcloud
一个 Spring Cloud Alibaba 脚手架项目，使用 Maven 构建


## Docker
- `mysql`：设置root密码 `MYSQL_ROOT_PASSWORD=123456`
- `redis`：默认无密码
- `nacos/nacos-server`： 设置模式 `MODE=standalone`，默认账号密码 `nacos`


## JDK
- 安装 OpenJDK 并配置环境变量：https://openjdk.org/


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


## Spring Boot
- 参考：https://start.spring.io/
- 可以快速创建 web 应用


## Spring Cloud
- 参考：https://github.com/alibaba/spring-cloud-alibaba
- 已集成 Spring Cloud Alibaba 2022.x


## JDBC（Java DataBase Connectivity）
- JDBC Interface 已内置 JDK 中，是一种标准接口
- JDBC Driver 由数据库厂商实现，例如 MySQL Driver 由 Oracle 实现（引入 jar 包）

