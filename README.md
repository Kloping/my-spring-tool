# Spring式 浓缩式 分支处理 框架

## 声明:

### 一切开发旨在学习，请勿用于非法用途

  <h4>
  <li>此框架由GitHhub: <a href="https://github.com/kloping"> Kloping </a> 开发</li>
  <li>此框架 是完全免费且开放源代码</li>
  <li>且 仅用于 学习和娱乐<u><b><i>禁止用于非法用途</i></b></u>
</h4>

使用Maven pom.xml

```xml

<dependencies>
    <dependency>
        <groupId>io.github.Kloping</groupId>
        <artifactId>SpringTool</artifactId>
        <version>0.6.3-R1</version>
    </dependency>
</dependencies>

```

### 功能列表

- action 字符匹配执行 类似springboot@controller执行方式 不过不需要web 路径改为匹配字符串模式执行
- 自动生成网络访问实现类 类似feign [文档](docs/http.md)
- 自动实例化 实体类 类似springboot@bean
- 自动填充类内字段 类springboot@Autowired
- 可扩展框架

> 应用场景一

QQ官方机器人 Java/JVM/kotlin [SDK](https://github.com/Kloping/qqpd-bot-java)

> 应用场景二

不知名game[a game for Cultivation and Turn](https://github.com/Kloping/mihdp)

