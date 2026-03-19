<p align="center">
  <strong>🔍 Jar Analyzer Engine</strong>
</p>

<p align="center">
  A Java bytecode analysis engine that parses JAR files into a SQLite database for security auditing and code analysis (AI-friendly)
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

> [中文文档](README_CN.md)

## 📖 Introduction

**Jar Analyzer Engine** is the **core engine** of [jar-analyzer](https://github.com/jar-analyzer/jar-analyzer), a Java bytecode security analysis GUI tool. It has been extracted as a standalone command-line tool and programming library.

> The `jar-analyzer` project has been continuously updated for `5` years with `58` releases — mature, stable, and production-ready.

The engine is built on the **ASM bytecode analysis framework** and uses a **multi-stage pipeline architecture** to automatically perform class discovery, method call graph construction, inheritance analysis, string extraction, Spring route identification, JavaWeb component detection, and more. All results are output as a **structured SQLite database**.

> 💡 **Recommended**: Integrate the generated SQLite database with AI tools such as `Claude Code` for code auditing — structured data can significantly boost AI analysis efficiency.

## ✨ Key Features

- 🚀 **Complete Method Call Graph** — Tracks all `invoke*` instructions (including Lambda/method references), building precise caller → callee relationships
- 🧬 **Inheritance Analysis** — Recursively builds the full inheritance tree, automatically adding overridden methods to the call graph to solve polymorphism analysis challenges
- 🌱 **Spring MVC Recognition** — Automatically identifies Controllers and parses `@RequestMapping` family annotations, extracting complete URL paths
- 🌐 **JavaWeb Component Discovery** — Identifies Servlets, Filters, Listeners, and HandlerInterceptors
- 📝 **String Constant Extraction** — Extracts strings from method body `LDC` instructions and annotations (SQL, URLs, keys, and other sensitive information)
- ⚡ **Quick Mode** — Analyzes only class structure and method calls, skipping time-consuming analysis stages for rapid overview
- 📦 **Spring Boot / WAR Support** — Nested JAR parsing and class name correction, perfectly adapted for Fat JARs
- 🛡️ **Security Protection** — Built-in Zip Slip path traversal attack defense with automatic error tolerance for corrupted class files
- 🔌 **Dual-Mode Usage** — Works as both a standalone CLI tool and an embeddable Java library

## 🚀 Quick Start

### Requirements

| Dependency | Version |
|------------|---------|
| Java (JDK/JRE) | **8** or higher |
| Maven (build only) | 3.6+ |

### Build

```bash
git clone https://github.com/jar-analyzer/jar-analyzer-engine.git
cd jar-analyzer-engine
mvn clean package -DskipTests
```

The build artifact is a fat jar (with all dependencies included), located at `target/jar-analyzer-engine-1.0.0-jar-with-dependencies.jar`.

### Basic Usage

```bash
# Analyze a single JAR file
java -jar jar-analyzer-engine.jar --jar /path/to/app.jar

# Analyze all JARs in a directory
java -jar jar-analyzer-engine.jar --jar /path/to/libs/

```

After analysis, a SQLite database file `jar-analyzer.db` will be generated in the current directory and can be queried with any SQLite client tool.

## 📋 Command-Line Arguments

### Required Arguments

| Argument | Short | Description |
|----------|-------|-------------|
| `--jar <path>` | `-j` | **Required**. Path to the JAR/WAR file or directory containing JARs to analyze |

### Optional Arguments

| Argument | Short | Default | Description |
|----------|-------|---------|-------------|
| `--temp <path>` | `-t` | `jar-analyzer-temp` | Temporary extraction directory path |
| `--rt <path>` | — | None | Path to rt.jar for including JDK standard classes in the analysis |
| `--quick` | `-q` | `false` | Enable quick mode |
| `--fix-class` | — | `false` | Enable class name correction mode |
| `--inner-jars` | — | `false` | Parse nested JARs within JARs |
| `--no-fix-impl` | — | `false` | Disable automatic method implementation correction |
| `--black-list <text>` | `-b` | None | Class/package blacklist (inline text) |
| `--white-list <text>` | `-w` | None | Class/package whitelist (inline text) |
| `--black-list-file <file>` | — | None | Read blacklist from file |
| `--white-list-file <file>` | — | None | Read whitelist from file |
| `--help` | `-h` | — | Display help information |

## 📚 Argument Details

### `--jar` / `-j` (Required)

Specifies the input path to analyze, supporting three forms:

- **Single JAR file**: Directly analyzes the JAR package
- **Single WAR file**: Automatically extracts and analyzes the class files within
- **Directory path**: Recursively scans all `.jar` files in the directory

```bash
# Single JAR
java -jar jar-analyzer-engine.jar --jar app.jar

# WAR file
java -jar jar-analyzer-engine.jar --jar webapp.war

# Scan entire lib directory
java -jar jar-analyzer-engine.jar --jar /opt/tomcat/webapps/ROOT/WEB-INF/lib/
```

### `--temp` / `-t`

Specifies the temporary extraction directory. The engine needs to extract class files from JARs/WARs to disk during analysis. The directory can be manually cleaned up after analysis completes.

```bash
java -jar jar-analyzer-engine.jar --jar app.jar --temp /tmp/jar-temp
```

### `--rt`

Specifies the path to `rt.jar` (typically located at `$JAVA_HOME/jre/lib/rt.jar`) to include JDK standard library classes in the analysis scope.

This is useful when you need to trace JDK class method call chains, for example, when analyzing deserialization chains where you need to know the method call relationships of `java.util.HashMap`.

> **Note**: Java 9+ has removed `rt.jar`. This option is primarily intended for analyzing Java 8 targeted projects.

```bash
java -jar jar-analyzer-engine.jar --jar app.jar --rt /usr/lib/jvm/java-8/jre/lib/rt.jar
```

### `--quick` / `-q` (Quick Mode)

Enables quick mode, which **only performs class discovery and method call analysis**, skipping the more time-consuming inheritance tree construction, string extraction, Spring analysis, and JavaWeb component detection.

Best suited when you only care about direct method call relationships and don't need the full analysis results, significantly reducing analysis time.

| Analysis Phase | Standard Mode | Quick Mode |
|---------------|:-------------:|:----------:|
| JAR Extraction & Class Discovery | ✅ | ✅ |
| Method Call Analysis | ✅ | ✅ |
| Inheritance Tree Construction | ✅ | ❌ |
| Method Implementation/Override Correction | ✅ | ❌ |
| String Constant Extraction | ✅ | ❌ |
| Spring Controller Analysis | ✅ | ❌ |
| JavaWeb Component Detection | ✅ | ❌ |

```bash
# Quick analysis, focusing only on method call relationships
java -jar jar-analyzer-engine.jar --jar app.jar --quick
```

### `--fix-class` (Class Name Correction Mode)

When enabled, the engine uses ASM to read each class file's **actual class name** from bytecode, rather than inferring it from the file path.

This is critical in the following scenarios:

- **Spring Boot Fat JAR**: Class files are located under the `BOOT-INF/classes/` directory, with extra path prefixes
- **WAR files**: Class files are located under the `WEB-INF/classes/` directory
- **Build tools that reorganize directory structures**

```bash
# Recommended when analyzing Spring Boot Fat JARs
java -jar jar-analyzer-engine.jar --jar springboot-app.jar --fix-class
```

### `--inner-jars` (Nested JAR Parsing)

When enabled, the engine recursively parses nested JAR files within JAR packages.

Typical use cases:

- **Spring Boot Fat JAR**: Dependencies are nested as JARs under `BOOT-INF/lib/`
- **WAR files**: Third-party dependencies are nested as JARs under `WEB-INF/lib/`
- **Any packaging format containing nested JARs**

```bash
# Analyze Spring Boot application (including nested dependencies)
java -jar jar-analyzer-engine.jar --jar springboot-app.jar --inner-jars --fix-class
```

### `--no-fix-impl` (Disable Method Implementation Correction)

By default, the engine automatically handles method inheritance/override relationships when building the method call graph: if code calls method A on a parent class/interface, the engine automatically adds all overriding implementations of method A from subclasses to the call graph.

Enabling `--no-fix-impl` disables this behavior, keeping only the **literal direct call relationships**.

```bash
# Record only direct calls, without automatically linking subclass override methods
java -jar jar-analyzer-engine.jar --jar app.jar --no-fix-impl
```

### Blacklist & Whitelist Filtering

Blacklists and whitelists allow you to control which classes participate in analysis, reducing unnecessary analysis scope and speeding up the process.

#### List Syntax

```text
# This is a comment line
// This is also a comment line
/* This is also a comment line

com.example.service.         # Package-level filter (ends with ., matches all classes in the package)
com.example.service.MyClass  # Class-level filter (exact match for a specific class)
com.a.;com.b.;com.c.Demo    # Semicolon-separated multiple entries
```

#### Usage

**Inline text** (suitable for simple rules):

```bash
# Blacklist: exclude test and mock packages
java -jar jar-analyzer-engine.jar --jar app.jar --black-list "com.example.test.;com.example.mock."

# Whitelist: only analyze service and controller packages
java -jar jar-analyzer-engine.jar --jar app.jar --white-list "com.example.service.;com.example.controller."
```

**File-based** (suitable for complex rules):

```bash
# Read lists from files
java -jar jar-analyzer-engine.jar --jar app.jar --black-list-file blacklist.txt --white-list-file whitelist.txt
```

`blacklist.txt` example:

```text
# Exclude test-related
com.example.test.
com.example.mock.
org.junit.

# Exclude logging-related
org.slf4j.
ch.qos.logback.
org.apache.logging.
```

#### Filtering Logic

- If a **whitelist** is configured: only classes matching the whitelist are analyzed
- If a **blacklist** is configured: classes matching the blacklist are excluded
- If both are configured: the whitelist is applied first, then the blacklist excludes from the result
- If neither is configured: all classes are analyzed

## ⚙️ Analysis Pipeline

The engine uses a multi-stage pipeline architecture, with stages executed sequentially:

```
Input (JAR/WAR/CLASS)
     │
     ▼
┌──────────────────────────────────┐
│  Stage 0: JAR Extraction &       │  Extract files, apply blacklist/
│  Filtering (0% - 15%)           │  whitelist
└──────────────┬───────────────────┘
               ▼
┌──────────────────────────────────┐
│  Stage 1: Class Discovery        │  Extract class/method/field/
│  (15% - 30%)                     │  annotation info
└──────────────┬───────────────────┘
               ▼
┌──────────────────────────────────┐
│  Stage 2: Method Call Analysis   │  Analyze invocation instructions
│  (30% - 40%)                     │  in method bodies
└──────────────┬───────────────────┘
               ▼
┌──────────────────────────────────┐
│  Stage 3: Inheritance Tree       │  Build inheritance tree + method
│  Construction (40%-70%)          │  implementation mapping
│  [Standard Mode]                 │
└──────────────┬───────────────────┘
               ▼
┌──────────────────────────────────┐
│  Stage 4: String Constant        │  Extract strings from code and
│  Extraction (70%-80%)            │  annotations
│  [Standard Mode]                 │
└──────────────┬───────────────────┘
               ▼
┌──────────────────────────────────┐
│  Stage 5: Spring Analysis        │  Identify Controller/Mapping/
│  (80% - 90%)                     │  parameters
│  [Standard Mode]                 │
└──────────────┬───────────────────┘
               ▼
┌──────────────────────────────────┐
│  Stage 6: JavaWeb Component      │  Identify Servlet/Filter/
│  Detection [Standard Mode]       │  Listener
└──────────────┬───────────────────┘
               ▼
         SQLite Database
```

### Stage 0: JAR Extraction & Filtering

- Extracts input JAR/WAR files to a temporary directory
- Applies blacklist/whitelist rules to filter out unnecessary classes
- If `--inner-jars` is enabled, recursively extracts nested JARs
- If `--fix-class` is enabled, reads actual class names from bytecode
- Automatically extracts configuration files (`.yml`, `.yaml`, `.properties`, `.xml`, `.json`, etc.)
- Built-in Zip Slip path traversal attack defense

### Stage 1: Class Discovery

Traverses all class files using ASM `ClassVisitor`, extracting:

- **Class information**: class name, superclass, interfaces, access modifiers, version
- **Field information**: field name, type, modifiers, initial value
- **Method information**: method name, descriptor, static flag, access modifiers, line number
- **Annotation information**: class-level and method-level annotations with their parameters

### Stage 2: Method Call Analysis

Analyzes bytecode invocation instructions in each method body:

- `invokevirtual` — Virtual method calls
- `invokestatic` — Static method calls
- `invokespecial` — Constructor/super calls
- `invokeinterface` — Interface method calls
- `invokedynamic` — Lambda expressions and method references

Records complete caller → callee relationships, including the invocation opcode type.

### Stage 3: Inheritance Tree Construction (Standard Mode)

- Recursively builds the **complete inheritance chain** for each class (including all ancestor classes and interfaces)
- Builds bidirectional mappings: subclass → all superclasses, superclass → all subclasses
- Finds method implementation/override relationships: for each non-static method, locates all subclass methods with the same name and descriptor
- If method implementation correction is enabled (default), automatically adds overriding methods to the call graph

### Stage 4: String Constant Extraction (Standard Mode)

- Extracts string constants loaded by `LDC` instructions in method bodies
- Extracts string values from method annotations
- Useful for searching SQL statements, URL paths, keys, and other sensitive information

### Stage 5: Spring Analysis (Standard Mode)

Identifies Spring MVC related components:

- Identifies controller classes annotated with `@Controller` / `@RestController`
- Parses `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping` annotations
- Builds complete URL paths (class-level basePath + method-level path)
- Extracts `@RequestParam` parameter mappings

### Stage 6: JavaWeb Component Detection (Standard Mode)

Identifies traditional JavaWeb components through superclass/interface relationships:

- **Servlet**: Implements `javax.servlet.Servlet` / `jakarta.servlet.Servlet` or extends `HttpServlet`
- **Filter**: Implements `javax.servlet.Filter` / `jakarta.servlet.Filter`
- **Listener**: Implements `ServletContextListener` / `ServletRequestListener` / `HttpSessionListener`
- **Interceptor**: Implements `HandlerInterceptor` / `AsyncHandlerInterceptor` or extends `HandlerInterceptorAdapter`

## 🗄️ Output Database

The engine outputs a SQLite database containing the following core tables:

| Table Name | Description | Key Fields |
|------------|-------------|------------|
| `jar_table` | JAR file information | jar_name, jar_abs_path |
| `class_table` | Class information | class_name, super_class_name, is_interface, access |
| `class_file_table` | Class file paths | class_name, path_str, jar_name |
| `member_table` | Fields/Members | member_name, modifiers, type_class_name, class_name |
| `method_table` | Method information | method_name, method_desc, is_static, class_name, line_number |
| `anno_table` | Annotation information | anno_name, method_name, class_name, visible |
| `interface_table` | Interface implementations | interface_name, class_name |
| `method_call_table` | Method call relationships | caller_class/method/desc, callee_class/method/desc, op_code |
| `method_impl_table` | Method implementation/Override | class_name, method_name, impl_class_name |
| `string_table` | String constants | value, method_name, class_name |
| `spring_controller_table` | Spring Controllers | class_name |
| `spring_method_table` | Spring Mappings | class_name, method_name, restful_type, path |
| `spring_interceptor_table` | Spring Interceptors | class_name |
| `java_web_table` | Servlet/Filter/Listener | type_name, class_name |

### SQL Query Examples

Here are some commonly used analysis queries that can be executed directly in any SQLite client:

```sql
-- Find all Spring Controller API routes
SELECT sm.path, sm.restful_type, sm.class_name, sm.method_name
FROM spring_method_table sm
ORDER BY sm.path;

-- Find all callers of a method (who calls it)
SELECT caller_class_name, caller_method_name
FROM method_call_table
WHERE callee_class_name = 'com/example/service/UserService'
  AND callee_method_name = 'getUser';

-- Find all methods called by a specific method
SELECT callee_class_name, callee_method_name
FROM method_call_table
WHERE caller_class_name = 'com/example/controller/UserController'
  AND caller_method_name = 'handleRequest';

-- Find methods containing sensitive strings (passwords, keys, etc.)
SELECT class_name, method_name, value
FROM string_table
WHERE value LIKE '%password%'
   OR value LIKE '%secret%'
   OR value LIKE '%token%';

-- Find all Servlets and Filters (attack surface enumeration)
SELECT type_name, class_name
FROM java_web_table
ORDER BY type_name;

-- Find all classes implementing a specific interface
SELECT class_name
FROM interface_table
WHERE interface_name = 'java/io/Serializable';

-- Find method inheritance/override relationships
SELECT class_name, method_name, method_desc, impl_class_name
FROM method_impl_table
WHERE method_name = 'invoke';
```

## 🎯 Typical Use Cases

### 1. Analyzing a Spring Boot Application

```bash
java -jar jar-analyzer-engine.jar \
  --jar springboot-app.jar \
  --fix-class \
  --inner-jars
```

It is recommended to enable both `--fix-class` and `--inner-jars`, since Spring Boot Fat JARs place class files under `BOOT-INF/classes/` and dependency libraries under `BOOT-INF/lib/`.

### 2. Analyzing a Tomcat Web Application

```bash
java -jar jar-analyzer-engine.jar \
  --jar /opt/tomcat/webapps/myapp/WEB-INF/lib/
```

### 3. Quick Method Call Chain Analysis

```bash
java -jar jar-analyzer-engine.jar \
  --jar target.jar \
  --quick
```

### 4. Precise Scope Analysis

```bash
java -jar jar-analyzer-engine.jar \
  --jar app.jar \
  --white-list "com.mycompany.service.;com.mycompany.controller." \
  --black-list "com.mycompany.service.test."
```

### 5. Analysis Including JDK Classes (Deserialization Chain Mining, etc.)

```bash
java -jar jar-analyzer-engine.jar \
  --jar app.jar \
  --rt /usr/lib/jvm/java-8-openjdk/jre/lib/rt.jar
```

## 🤖 AI Integration for Code Auditing

The generated SQLite database is naturally suited for use with AI tools. Here is the recommended workflow:

### Using Claude Code

```bash
# 1. First, analyze the target application with the engine
java -jar jar-analyzer-engine.jar --jar target-app.jar

# 2. In Claude Code, the AI can directly query the database for analysis
```

In Claude Code, you can ask the AI to perform the following audit tasks:

- **Attack Surface Enumeration**: Query all Spring Controller routes and Servlets/Filters to map web entry points
- **Call Chain Tracing**: Trace backwards from dangerous methods (e.g., `Runtime.exec`, `ProcessBuilder.start`) to discover potential RCE vulnerabilities
- **Deserialization Analysis**: Find classes implementing `Serializable` and analyze the call graph of `readObject` methods
- **Sensitive Information Search**: Search the string table for hardcoded passwords, API keys, internal URLs, etc.
- **Authorization Audit**: Trace whether Controller methods pass through authentication/authorization interceptors

### Example Conversation

```
User: Analyze all web entry points in jar-analyzer.db and trace which ones eventually call Runtime.exec

AI: Let me query the database for analysis...
    [Query spring_method_table to get all routes]
    [Query method_call_table to trace Runtime.exec call chains]
    [Correlate analysis to find reachable paths]
```

## 💻 Programming API

In addition to CLI usage, the engine can also be integrated as a library into other Java programs:

```java
import me.n1ar4.jar.analyzer.engine.EngineConfig;
import me.n1ar4.jar.analyzer.engine.EngineBuildRunner;
import me.n1ar4.jar.analyzer.engine.ProgressCallback;

// Build configuration
EngineConfig config = new EngineConfig();
config.setJarPath(Paths.get("/path/to/app.jar"));
config.setTempDir("temp");
config.setQuickMode(false);
config.setFixClass(true);
config.setJarsInJar(true);
config.setFixMethodImpl(true);

// Set progress callback (optional)
config.setProgressCallback(new ProgressCallback() {
    @Override
    public void onProgress(double percent) {
        System.out.printf("Progress: %.1f%%\n", percent * 100);
    }
    
    @Override
    public void onMessage(String msg) {
        System.out.println(msg);
    }
});

// Run analysis
EngineBuildRunner.run(config);
```

## 🔧 Tech Stack

| Dependency | Version | Purpose |
|------------|---------|---------|
| [ASM](https://asm.ow2.io/) | 9.9.1 | Java bytecode analysis framework |
| [MyBatis](https://mybatis.org/) | 3.5.19 | ORM / SQL mapping |
| [SQLite JDBC](https://github.com/xerial/sqlite-jdbc) | 3.51.3.0 | SQLite database driver |
| [Commons DBCP2](https://commons.apache.org/proper/commons-dbcp/) | 2.14.0 | Database connection pool |
| [Commons Compress](https://commons.apache.org/proper/commons-compress/) | 1.28.0 | JAR/WAR/ZIP extraction |
| [JCommander](https://jcommander.org/) | 1.82 | CLI argument parsing |
| [Hutool](https://hutool.cn/) | 5.8.43 | Utility library |

## 📁 Project Structure

```
jar-analyzer-engine/
├── src/main/java/me/n1ar4/jar/analyzer/
│   ├── engine/                      # Engine entry layer
│   │   ├── EngineMain.java          #   CLI main entry point
│   │   ├── EngineBuildRunner.java   #   Pipeline orchestration core
│   │   ├── EngineConfig.java        #   Configuration class
│   │   ├── EngineBuildCmd.java      #   CLI argument definitions (JCommander)
│   │   ├── ProgressCallback.java    #   Progress callback interface
│   │   ├── EngineConst.java         #   Constants
│   │   ├── log/                     #   Custom logging system
│   │   └── utils/                   #   Utilities (JAR extraction, blacklist/whitelist, etc.)
│   ├── core/                        # Core analysis layer
│   │   ├── AnalyzeEnv.java          #   Global analysis environment
│   │   ├── DatabaseManager.java     #   Database management & persistence
│   │   ├── DiscoveryRunner.java     #   Stage 1: Class discovery
│   │   ├── MethodCallRunner.java    #   Stage 2: Method call analysis
│   │   ├── InheritanceRunner.java   #   Stage 3: Inheritance tree construction
│   │   ├── InheritanceMap.java      #   Inheritance data structure
│   │   ├── OtherWebService.java     #   Stage 6: JavaWeb component detection
│   │   ├── asm/                     #   ASM bytecode visitors
│   │   ├── mapper/                  #   MyBatis Mapper interfaces (15)
│   │   └── reference/               #   Core data models
│   ├── entity/                      # Database entity classes (18)
│   └── analyze/spring/              # Spring framework analysis
│       ├── SpringService.java       #   Spring analysis entry point
│       └── asm/                     #   Spring annotation ASM visitors
├── src/main/resources/
│   ├── mybatis.xml                  # MyBatis main configuration
│   ├── jdbc.properties              # JDBC connection configuration
│   └── mappers/                     # MyBatis SQL mapping files (15)
└── pom.xml                          # Maven build configuration
```

## ❓ FAQ

<details>
<summary><b>Q: Running out of memory when analyzing large projects?</b></summary>

Increase the JVM heap memory:

```bash
java -Xmx2g -jar jar-analyzer-engine.jar --jar large-app.jar
```

For particularly large projects, you can also use `--quick` mode to reduce memory consumption, or narrow the analysis scope using blacklists/whitelists.

</details>

<details>
<summary><b>Q: Empty results when analyzing Spring Boot Fat JARs?</b></summary>

You need to enable both `--fix-class` and `--inner-jars`:

```bash
java -jar jar-analyzer-engine.jar --jar springboot-app.jar --fix-class --inner-jars
```

Spring Boot Fat JAR class files are located under `BOOT-INF/classes/`. Without `--fix-class`, class names will contain the `BOOT-INF.classes.` prefix.

</details>

<details>
<summary><b>Q: Does it support Java 9+ modular JARs?</b></summary>

Yes. The engine uses ASM 9.9.1, which can handle class files from Java 8 to Java 21+. However, the `--rt` parameter only applies to Java 8's `rt.jar` — Java 9+ modular JDKs do not require this parameter.

</details>

<details>
<summary><b>Q: Does it support incremental analysis?</b></summary>

The current version rebuilds the entire database on each analysis run (overwriting existing files). Incremental analysis is planned for a future release.

</details>

<details>
<summary><b>Q: What about "StackMapTable" related errors?</b></summary>

The engine has built-in error tolerance for corrupted class files. If a class file's `StackMapTable` is corrupted, the engine automatically falls back to `SKIP_FRAMES` mode for re-parsing, without interrupting the overall analysis process.

</details>

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork** this repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'feat: add amazing feature'`
4. Push the branch: `git push origin feature/amazing-feature`
5. Submit a **Pull Request**

### Development Environment

- JDK 8 or higher
- Maven 3.6+
- Recommended IDE: IntelliJ IDEA

### Code Standards

- Follow Java standard naming conventions
- New features must maintain compatibility with Java 8
- Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/) specification

## 🔗 Related Projects

| Project | Description |
|---------|-------------|
| [jar-analyzer](https://github.com/jar-analyzer/jar-analyzer) | Java bytecode security analysis GUI tool (the upstream application of this engine) |

## 📄 License

This project is open-sourced under the [MIT](https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE) License.

Copyright © 2022-2026 [4ra1n](https://github.com/4ra1n) (Jar Analyzer Team)
