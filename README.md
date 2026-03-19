<p align="center">
  <strong>🔍 Jar Analyzer Engine</strong>
</p>

<p align="center">
  Java 字节码分析引擎，将 JAR 文件解析 SQLite 数据库，用于安全审计代码分析（AI 友好）
</p>

<p align="center">
  <a href="https://github.com/jar-analyzer/jar-analyzer-engine/actions/workflows/maven.yml">
    <img src="https://github.com/jar-analyzer/jar-analyzer-engine/actions/workflows/maven.yml/badge.svg" alt="Build Status" />
  </a>
  <img src="https://img.shields.io/badge/Java-8%2B-orange" alt="Java 8+" />
  <img src="https://img.shields.io/badge/ASM-9.9.1-blue" alt="ASM 9.9.1" />
  <img src="https://img.shields.io/badge/SQLite-3.51-green" alt="SQLite" />
  <a href="https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE">
    <img src="https://img.shields.io/badge/License-MIT-red" alt="License" />
  </a>
</p>

---

> [English Version](README_EN.md)

[更新日志](CHANGLOG.md)

## 📖 简介

**Jar Analyzer Engine** 是 Java 字节码安全分析 GUI 工具 [jar-analyzer](https://github.com/jar-analyzer/jar-analyzer) 的**核心引擎**，现已独立提取为可单独使用的命令行工具和编程库。

> `jar-analyzer` 项目连续 `5` 年更新，共发布 `58` 个版本，成熟稳定可用

引擎基于 **ASM 字节码分析框架**，采用**多阶段流水线架构**，自动完成类发现、方法调用图构建、继承关系分析、字符串提取、Spring 路由识别、JavaWeb 组件识别等工作，并将所有结果输出为**结构化的 SQLite 数据库**。

> 💡 **推荐**：将生成的 SQLite 数据库集成到 `Claude Code` 等 AI 工具进行代码审计，结构化数据可使 AI 分析效率大幅提升

> 🤖 **AI 用户必读**：[DATABASE.md](DATABASE.md) 包含完整的数据库表结构与字段说明，请在使用 AI 分析数据库前将该文件提供给 AI 作为上下文参考

## ✨ 特性亮点

- 🚀 **完整方法调用图** — 追踪所有 `invoke*` 指令（含 Lambda/方法引用），构建精确的 caller → callee 调用关系
- 🧬 **继承关系分析** — 递归构建完整继承树，自动将子类 Override 方法加入调用图，解决多态分析难题
- 🌱 **Spring MVC 识别** — 自动识别 Controller、解析 `@RequestMapping` 家族注解，提取完整 URL 路径
- 🌐 **JavaWeb 组件发现** — 识别 Servlet、Filter、Listener、HandlerInterceptor
- 📝 **字符串常量提取** — 从方法体 `LDC` 指令和注解中提取字符串（SQL、URL、密钥等敏感信息）
- ⚡ **快速模式** — 仅分析类结构和方法调用，跳过耗时分析，适合快速摸底
- 📦 **Spring Boot / WAR 支持** — 嵌套 JAR 解析、类名修正，完美适配 Fat JAR
- 🛡️ **安全防护** — 内置 Zip Slip 路径穿越攻击防御，损坏类文件自动容错
- 🔌 **双模式使用** — 既可作为 CLI 工具独立运行，也可作为 Java 库集成到项目中

## 🚀 快速开始

### 环境要求

| 依赖 | 版本要求 |
|------|----------|
| Java (JDK/JRE) | **8** 或更高 |
| Maven（仅构建时） | 3.6+ |

### 构建

```bash
git clone https://github.com/jar-analyzer/jar-analyzer-engine.git
cd jar-analyzer-engine
mvn clean package -DskipTests
```

构建产物为 fat jar（包含所有依赖），位于 `target/jar-analyzer-engine-1.0.0-jar-with-dependencies.jar`。

### 基本用法

```bash
# 分析单个 JAR 文件
java -jar jar-analyzer-engine.jar --jar /path/to/app.jar

# 分析目录下所有 JAR
java -jar jar-analyzer-engine.jar --jar /path/to/libs/

```

分析完成后将在当前目录生成 SQLite 数据库文件 `jar-analyzer.db`，可使用任何 SQLite 客户端工具查询。分析过程中的临时文件存放在 `jar-analyzer-temp` 目录中，分析完成后可手动删除。

## 📋 命令行参数

### 必填参数

| 参数 | 缩写 | 说明 |
|------|------|------|
| `--jar <path>` | `-j` | **必填**。待分析的 JAR/WAR 文件或包含 JAR 的目录路径 |

### 可选参数

| 参数 | 缩写 | 默认值 | 说明 |
|------|------|--------|------|
| `--rt <path>` | — | 无 | rt.jar 路径，附加 JDK 标准类进行分析 |
| `--quick` | `-q` | `false` | 启用快速模式 |
| `--fix-class` | — | `false` | 启用类名修正模式 |
| `--inner-jars` | — | `false` | 解析 JAR 中嵌套的 JAR |
| `--no-fix-impl` | — | `false` | 禁用方法实现自动修正 |
| `--black-list <text>` | `-b` | 无 | 类/包黑名单（内联文本） |
| `--white-list <text>` | `-w` | 无 | 类/包白名单（内联文本） |
| `--black-list-file <file>` | — | 无 | 从文件读取黑名单 |
| `--white-list-file <file>` | — | 无 | 从文件读取白名单 |
| `--help` | `-h` | — | 显示帮助信息 |

## 📚 参数详解

### `--jar` / `-j`（必填）

指定待分析的输入路径，支持三种形式：

- **单个 JAR 文件**：直接分析该 JAR 包
- **单个 WAR 文件**：自动解压并分析其中的 class 文件
- **目录路径**：递归扫描目录下所有 `.jar` 文件并分析

```bash
# 单个 JAR
java -jar jar-analyzer-engine.jar --jar app.jar

# WAR 文件
java -jar jar-analyzer-engine.jar --jar webapp.war

# 扫描整个 lib 目录
java -jar jar-analyzer-engine.jar --jar /opt/tomcat/webapps/ROOT/WEB-INF/lib/
```

### `--rt`

指定 `rt.jar` 的路径（通常位于 `$JAVA_HOME/jre/lib/rt.jar`），用于将 JDK 标准库的类也纳入分析范围。

这对于需要追踪 JDK 类方法调用链的场景非常有用，例如分析反序列化链时需要知道 `java.util.HashMap` 的方法调用关系。

> **注意**：Java 9+ 已移除 `rt.jar`，该选项主要适用于分析面向 Java 8 的项目。

```bash
java -jar jar-analyzer-engine.jar --jar app.jar --rt /usr/lib/jvm/java-8/jre/lib/rt.jar
```

### `--quick` / `-q`（快速模式）

启用快速模式，**仅执行类发现和方法调用分析**，跳过耗时较长的继承关系构建、字符串提取、Spring 分析和 JavaWeb 组件识别。

适合在只关心直接方法调用关系、不需要完整分析结果时使用，可以显著缩短分析时间。

| 分析阶段 | 标准模式 | 快速模式 |
|----------|:--------:|:--------:|
| JAR 解压与类发现 | ✅ | ✅ |
| 方法调用分析 | ✅ | ✅ |
| 继承关系构建 | ✅ | ❌ |
| 方法实现/Override 修正 | ✅ | ❌ |
| 字符串常量提取 | ✅ | ❌ |
| Spring Controller 分析 | ✅ | ❌ |
| JavaWeb 组件识别 | ✅ | ❌ |

```bash
# 快速分析，仅关注方法调用关系
java -jar jar-analyzer-engine.jar --jar app.jar --quick
```

### `--fix-class`（类名修正模式）

启用后，引擎会使用 ASM 从字节码中读取每个 class 文件的**真实类名**，而不是依赖文件路径推断。

这在以下场景中非常关键：

- **Spring Boot Fat JAR**：class 文件位于 `BOOT-INF/classes/` 目录下，文件路径包含额外前缀
- **WAR 文件**：class 文件位于 `WEB-INF/classes/` 目录下
- **打包工具重新组织目录结构**的情况

```bash
# 分析 Spring Boot Fat JAR 时推荐使用
java -jar jar-analyzer-engine.jar --jar springboot-app.jar --fix-class
```

### `--inner-jars`（嵌套 JAR 解析）

启用后，引擎会递归解析 JAR 包中嵌套的 JAR 文件。

典型应用场景：

- **Spring Boot Fat JAR**：依赖库以 JAR 形式嵌套在 `BOOT-INF/lib/` 中
- **WAR 文件**：第三方依赖以 JAR 形式嵌套在 `WEB-INF/lib/` 中
- **任何包含嵌套 JAR 的打包格式**

```bash
# 分析 Spring Boot 应用（包括嵌套依赖）
java -jar jar-analyzer-engine.jar --jar springboot-app.jar --inner-jars --fix-class
```

### `--no-fix-impl`（禁用方法实现修正）

默认情况下，引擎会在构建方法调用关系时自动处理方法的继承/Override 关系：如果代码调用了父类/接口的方法 A，引擎会自动将所有子类中 Override 的方法 A 也加入调用图。

启用 `--no-fix-impl` 可以禁用这一行为，只保留**字面上的直接调用关系**。

```bash
# 仅记录直接调用，不自动关联子类 override 方法
java -jar jar-analyzer-engine.jar --jar app.jar --no-fix-impl
```

### 黑白名单过滤

通过黑白名单可以控制哪些类参与分析，减少不必要的分析范围，加速分析过程。

#### 名单语法

```text
# 这是注释行
// 这也是注释行
/* 这也是注释行

com.example.service.         # 包级别过滤（以 . 结尾，匹配该包下所有类）
com.example.service.MyClass  # 类级别过滤（精确匹配某个类）
com.a.;com.b.;com.c.Demo    # 分号分隔多项
```

#### 使用方式

**内联文本**（适合简单规则）：

```bash
# 黑名单：排除 test 和 mock 包
java -jar jar-analyzer-engine.jar --jar app.jar --black-list "com.example.test.;com.example.mock."

# 白名单：仅分析 service 和 controller 包
java -jar jar-analyzer-engine.jar --jar app.jar --white-list "com.example.service.;com.example.controller."
```

**文件方式**（适合复杂规则）：

```bash
# 从文件读取名单
java -jar jar-analyzer-engine.jar --jar app.jar --black-list-file blacklist.txt --white-list-file whitelist.txt
```

`blacklist.txt` 示例：

```text
# 排除测试相关
com.example.test.
com.example.mock.
org.junit.

# 排除日志相关
org.slf4j.
ch.qos.logback.
org.apache.logging.
```

#### 过滤逻辑

- 如果配置了**白名单**：只有匹配白名单的类才会被分析
- 如果配置了**黑名单**：匹配黑名单的类会被排除
- 如果同时配置了黑白名单：先匹配白名单，再排除黑名单
- 如果都未配置：分析所有类

## ⚙️ 分析流程

引擎采用多阶段流水线架构，各阶段按顺序执行：

```
输入 (JAR/WAR/CLASS)
     │
     ▼
┌──────────────────────────────┐
│  阶段 0: JAR 解压与过滤       │  解压文件，应用黑白名单
│  (0% - 15%)                  │
└──────────────┬───────────────┘
               ▼
┌──────────────────────────────┐
│  阶段 1: 类发现 (Discovery)   │  提取类/方法/字段/注解信息
│  (15% - 30%)                 │
└──────────────┬───────────────┘
               ▼
┌──────────────────────────────┐
│  阶段 2: 方法调用分析          │  分析方法体中的调用指令
│  (30% - 40%)                 │
└──────────────┬───────────────┘
               ▼
┌──────────────────────────────┐
│  阶段 3: 继承关系构建          │  构建继承树 + 方法实现映射
│  (40% - 70%) [标准模式]       │
└──────────────┬───────────────┘
               ▼
┌──────────────────────────────┐
│  阶段 4: 字符串常量提取        │  提取代码和注解中的字符串
│  (70% - 80%) [标准模式]       │
└──────────────┬───────────────┘
               ▼
┌──────────────────────────────┐
│  阶段 5: Spring 分析          │  识别 Controller/Mapping/参数
│  (80% - 90%) [标准模式]       │
└──────────────┬───────────────┘
               ▼
┌──────────────────────────────┐
│  阶段 6: JavaWeb 组件识别     │  识别 Servlet/Filter/Listener
│  [标准模式]                   │
└──────────────┬───────────────┘
               ▼
          SQLite 数据库
```

### 阶段 0: JAR 解压与过滤

- 解压输入的 JAR/WAR 文件到临时目录
- 应用黑白名单规则过滤不需要分析的类
- 如果启用 `--inner-jars`，递归解压嵌套 JAR
- 如果启用 `--fix-class`，从字节码中读取真实类名
- 自动提取配置文件（`.yml`、`.yaml`、`.properties`、`.xml`、`.json` 等）
- 内置 Zip Slip 路径穿越攻击防御

### 阶段 1: 类发现 (Discovery)

通过 ASM `ClassVisitor` 遍历所有 class 文件，提取：

- **类信息**：类名、父类、接口、访问修饰符、版本号
- **字段信息**：字段名、类型、修饰符、初始值
- **方法信息**：方法名、描述符、是否静态、访问修饰符、行号
- **注解信息**：类级和方法级注解及其参数

### 阶段 2: 方法调用分析

分析每个方法体中的字节码调用指令：

- `invokevirtual` — 虚方法调用
- `invokestatic` — 静态方法调用
- `invokespecial` — 构造方法/super 调用
- `invokeinterface` — 接口方法调用
- `invokedynamic` — Lambda 表达式和方法引用

记录完整的 caller → callee 关系，包括调用的 opcode 类型。

### 阶段 3: 继承关系构建（标准模式）

- 递归构建每个类的**完整继承链**（包括所有祖先类和接口）
- 构建双向映射：子类 → 所有父类，父类 → 所有子类
- 查找方法实现/Override 关系：对每个非静态方法，找到所有子类中同名同描述符的方法
- 如果启用方法实现修正（默认启用），将 Override 方法自动加入调用图

### 阶段 4: 字符串常量提取（标准模式）

- 提取方法体中 `LDC` 指令加载的字符串常量
- 提取方法注解中的字符串值
- 可用于 SQL 语句、URL 路径、密钥等敏感信息检索

### 阶段 5: Spring 分析（标准模式）

识别 Spring MVC 相关组件：

- 识别 `@Controller` / `@RestController` 注解的控制器类
- 解析 `@RequestMapping`、`@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping`、`@PatchMapping` 注解
- 构建完整的 URL 路径（类级 basePath + 方法级 path）
- 提取 `@RequestParam` 参数映射

### 阶段 6: JavaWeb 组件识别（标准模式）

通过父类/接口关系识别传统 JavaWeb 组件：

- **Servlet**：实现 `javax.servlet.Servlet` / `jakarta.servlet.Servlet` 或继承 `HttpServlet`
- **Filter**：实现 `javax.servlet.Filter` / `jakarta.servlet.Filter`
- **Listener**：实现 `ServletContextListener` / `ServletRequestListener` / `HttpSessionListener`
- **Interceptor**：实现 `HandlerInterceptor` / `AsyncHandlerInterceptor` 或继承 `HandlerInterceptorAdapter`

## 🗄️ 输出数据库

引擎输出一个 SQLite 数据库，包含以下核心表：

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `jar_table` | JAR 文件信息 | jar_name, jar_abs_path |
| `class_table` | 类信息 | class_name, super_class_name, is_interface, access |
| `class_file_table` | 类文件路径 | class_name, path_str, jar_name |
| `member_table` | 字段/成员 | member_name, modifiers, type_class_name, class_name |
| `method_table` | 方法信息 | method_name, method_desc, is_static, class_name, line_number |
| `anno_table` | 注解信息 | anno_name, method_name, class_name, visible |
| `interface_table` | 接口实现 | interface_name, class_name |
| `method_call_table` | 方法调用关系 | caller_class/method/desc, callee_class/method/desc, op_code |
| `method_impl_table` | 方法实现/Override | class_name, method_name, impl_class_name |
| `string_table` | 字符串常量 | value, method_name, class_name |
| `spring_controller_table` | Spring 控制器 | class_name |
| `spring_method_table` | Spring Mapping | class_name, method_name, restful_type, path |
| `spring_interceptor_table` | Spring 拦截器 | class_name |
| `java_web_table` | Servlet/Filter/Listener | type_name, class_name |

### SQL 查询示例

以下是一些常用的分析查询，可直接在 SQLite 客户端中执行：

```sql
-- 查找所有 Spring Controller 的 API 路由
SELECT sm.path, sm.restful_type, sm.class_name, sm.method_name
FROM spring_method_table sm
ORDER BY sm.path;

-- 查找某个方法的所有调用者（谁调用了它）
SELECT caller_class_name, caller_method_name
FROM method_call_table
WHERE callee_class_name = 'com/example/service/UserService'
  AND callee_method_name = 'getUser';

-- 查找某个方法调用了哪些方法
SELECT callee_class_name, callee_method_name
FROM method_call_table
WHERE caller_class_name = 'com/example/controller/UserController'
  AND caller_method_name = 'handleRequest';

-- 查找包含敏感字符串的方法（如密码、密钥等）
SELECT class_name, method_name, value
FROM string_table
WHERE value LIKE '%password%'
   OR value LIKE '%secret%'
   OR value LIKE '%token%';

-- 查找所有 Servlet 和 Filter（攻击面枚举）
SELECT type_name, class_name
FROM java_web_table
ORDER BY type_name;

-- 查找实现了某个接口的所有类
SELECT class_name
FROM interface_table
WHERE interface_name = 'java/io/Serializable';

-- 查找方法的继承/Override 关系
SELECT class_name, method_name, method_desc, impl_class_name
FROM method_impl_table
WHERE method_name = 'invoke';
```

## 🎯 典型使用场景

### 1. 分析 Spring Boot 应用

```bash
java -jar jar-analyzer-engine.jar \
  --jar springboot-app.jar \
  --fix-class \
  --inner-jars
```

推荐同时启用 `--fix-class` 和 `--inner-jars`，因为 Spring Boot Fat JAR 将 class 文件放在 `BOOT-INF/classes/` 中，依赖库放在 `BOOT-INF/lib/` 中。

### 2. 分析 Tomcat Web 应用

```bash
java -jar jar-analyzer-engine.jar \
  --jar /opt/tomcat/webapps/myapp/WEB-INF/lib/
```

### 3. 快速分析方法调用链

```bash
java -jar jar-analyzer-engine.jar \
  --jar target.jar \
  --quick
```

### 4. 精确范围分析

```bash
java -jar jar-analyzer-engine.jar \
  --jar app.jar \
  --white-list "com.mycompany.service.;com.mycompany.controller." \
  --black-list "com.mycompany.service.test."
```

### 5. 包含 JDK 类分析（反序列化链挖掘等场景）

```bash
java -jar jar-analyzer-engine.jar \
  --jar app.jar \
  --rt /usr/lib/jvm/java-8-openjdk/jre/lib/rt.jar
```

## 🤖 与 AI 集成进行代码审计

生成的 SQLite 数据库天然适合与 AI 工具结合使用，以下是推荐的工作流：

> 📄 数据库完整表结构请参阅 [DATABASE.md](DATABASE.md)，建议将该文件作为 AI 的上下文输入

### 使用 Claude Code

```bash
# 1. 先用引擎分析目标应用
java -jar jar-analyzer-engine.jar --jar target-app.jar

# 2. 在 Claude Code 中，AI 可以直接查询数据库进行分析
```

在 Claude Code 中，你可以要求 AI 执行以下审计任务：

- **攻击面枚举**：查询所有 Spring Controller 路由和 Servlet/Filter，梳理 Web 入口点
- **调用链追踪**：从危险方法（如 `Runtime.exec`、`ProcessBuilder.start`）反向追踪调用链，发现潜在 RCE
- **反序列化分析**：查找实现 `Serializable` 的类，分析 `readObject` 方法的调用图
- **敏感信息检索**：通过字符串表搜索硬编码密码、API Key、内部 URL 等
- **权限校验审计**：追踪 Controller 方法是否经过认证/授权拦截器

### 示例对话

```
用户: 帮我分析 jar-analyzer.db 中所有的 Web 入口点，并追踪哪些入口点最终会调用到 Runtime.exec

AI: 我来查询数据库进行分析...
    [查询 spring_method_table 获取所有路由]
    [查询 method_call_table 追踪 Runtime.exec 的调用链]
    [关联分析，找出可达路径]
```

## 💻 编程接口

除了 CLI 使用方式，引擎也可以作为库被其他 Java 程序集成：

```java
import me.n1ar4.jar.analyzer.engine.EngineConfig;
import me.n1ar4.jar.analyzer.engine.EngineBuildRunner;
import me.n1ar4.jar.analyzer.engine.ProgressCallback;

// 构建配置
EngineConfig config = new EngineConfig();
config.setJarPath(Paths.get("/path/to/app.jar"));
config.setQuickMode(false);
config.setFixClass(true);
config.setJarsInJar(true);
config.setFixMethodImpl(true);

// 设置进度回调（可选）
config.setProgressCallback(new ProgressCallback() {
    @Override
    public void onProgress(double percent) {
        System.out.printf("进度: %.1f%%\n", percent * 100);
    }
    
    @Override
    public void onMessage(String msg) {
        System.out.println(msg);
    }
});

// 执行分析
EngineBuildRunner.run(config);
```

## 🔧 技术栈

| 依赖 | 版本 | 用途 |
|------|------|------|
| [ASM](https://asm.ow2.io/) | 9.9.1 | Java 字节码分析框架 |
| [MyBatis](https://mybatis.org/) | 3.5.19 | ORM / SQL 映射 |
| [SQLite JDBC](https://github.com/xerial/sqlite-jdbc) | 3.51.3.0 | SQLite 数据库驱动 |
| [Commons DBCP2](https://commons.apache.org/proper/commons-dbcp/) | 2.14.0 | 数据库连接池 |
| [Commons Compress](https://commons.apache.org/proper/commons-compress/) | 1.28.0 | JAR/WAR/ZIP 解压 |
| [JCommander](https://jcommander.org/) | 1.82 | CLI 参数解析 |
| [Hutool](https://hutool.cn/) | 5.8.43 | 工具方法库 |

## 📁 项目结构

```
jar-analyzer-engine/
├── src/main/java/me/n1ar4/jar/analyzer/
│   ├── engine/                      # 引擎入口层
│   │   ├── EngineMain.java          #   CLI 主入口
│   │   ├── EngineBuildRunner.java   #   流水线编排核心
│   │   ├── EngineConfig.java        #   配置类
│   │   ├── EngineBuildCmd.java      #   CLI 参数定义 (JCommander)
│   │   ├── ProgressCallback.java    #   进度回调接口
│   │   ├── EngineConst.java         #   常量定义
│   │   ├── log/                     #   自定义日志系统
│   │   └── utils/                   #   工具类 (JAR 解压、黑白名单等)
│   ├── core/                        # 核心分析层
│   │   ├── AnalyzeEnv.java          #   全局分析环境
│   │   ├── DatabaseManager.java     #   数据库管理 & 持久化
│   │   ├── DiscoveryRunner.java     #   阶段1: 类发现
│   │   ├── MethodCallRunner.java    #   阶段2: 方法调用分析
│   │   ├── InheritanceRunner.java   #   阶段3: 继承关系构建
│   │   ├── InheritanceMap.java      #   继承关系数据结构
│   │   ├── OtherWebService.java     #   阶段6: JavaWeb 组件识别
│   │   ├── asm/                     #   ASM 字节码访问器
│   │   ├── mapper/                  #   MyBatis Mapper 接口 (15个)
│   │   └── reference/               #   核心数据模型
│   ├── entity/                      # 数据库实体类 (18个)
│   └── analyze/spring/              # Spring 框架分析
│       ├── SpringService.java       #   Spring 分析入口
│       └── asm/                     #   Spring 注解 ASM 访问器
├── src/main/resources/
│   ├── mybatis.xml                  # MyBatis 主配置
│   ├── jdbc.properties              # JDBC 连接配置
│   └── mappers/                     # MyBatis SQL 映射文件 (15个)
└── pom.xml                          # Maven 构建配置
```

## ❓ 常见问题

<details>
<summary><b>Q: 分析大型项目时内存不足怎么办？</b></summary>

增大 JVM 堆内存：

```bash
java -Xmx2g -jar jar-analyzer-engine.jar --jar large-app.jar
```

对于特别大的项目，也可以使用 `--quick` 模式减少内存消耗，或通过黑白名单缩小分析范围。

</details>

<details>
<summary><b>Q: 分析 Spring Boot Fat JAR 结果为空？</b></summary>

需要同时启用 `--fix-class` 和 `--inner-jars`：

```bash
java -jar jar-analyzer-engine.jar --jar springboot-app.jar --fix-class --inner-jars
```

Spring Boot Fat JAR 的 class 文件位于 `BOOT-INF/classes/` 中，不启用 `--fix-class` 会导致类名包含 `BOOT-INF.classes.` 前缀。

</details>

<details>
<summary><b>Q: 支持 Java 9+ 模块化 JAR 吗？</b></summary>

支持。引擎使用 ASM 9.9.1，可以处理 Java 8 到 Java 21+ 的 class 文件。但 `--rt` 参数仅适用于 Java 8 的 `rt.jar`，Java 9+ 的模块化 JDK 无需此参数。

</details>

<details>
<summary><b>Q: 是否支持增量分析？</b></summary>

当前版本每次分析会重建整个数据库（覆盖已有文件）。增量分析功能计划在后续版本中支持。

</details>

<details>
<summary><b>Q: 遇到 "StackMapTable" 相关错误怎么办？</b></summary>

引擎内置了损坏类文件的容错处理。如果某个 class 文件的 `StackMapTable` 损坏，引擎会自动降级为 `SKIP_FRAMES` 模式重新解析，不会中断整个分析流程。

</details>

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. **Fork** 本仓库
2. 创建特性分支：`git checkout -b feature/amazing-feature`
3. 提交更改：`git commit -m 'feat: add amazing feature'`
4. 推送分支：`git push origin feature/amazing-feature`
5. 提交 **Pull Request**

### 开发环境

- JDK 8 或更高版本
- Maven 3.6+
- IDE 推荐：IntelliJ IDEA

### 代码规范

- 遵循 Java 标准命名规范
- 新增功能需保持与 Java 8 的兼容性
- 提交信息遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范

## 🔗 相关项目

| 项目 | 说明 |
|------|------|
| [jar-analyzer](https://github.com/jar-analyzer/jar-analyzer) | Java 字节码安全分析 GUI 工具（本引擎的上层应用） |

## 📄 许可证

本项目基于 [MIT](https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE) 许可证开源。

Copyright © 2022-2026 [4ra1n](https://github.com/4ra1n) (Jar Analyzer Team)
