/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.engine;

import java.nio.file.Path;

/**
 * 引擎构建配置
 * 封装了从 JAR 文件/目录构建 SQLite 数据库所需的全部参数
 * 该类不依赖任何 GUI 组件，可在 CLI 和 GUI 模式下共同使用
 */
public class EngineConfig {
    /**
     * JAR 文件或目录路径（必填）
     */
    private Path jarPath;

    /**
     * rt.jar 路径（可选，附加 JDK 类分析）
     */
    private Path rtJarPath;

    /**
     * 临时解压目录（默认 jar-analyzer-temp）
     */
    private String tempDir = EngineConst.tempDir;

    /**
     * 类/包黑名单文本
     * 支持 ListParser 语法：
     * - # // /* 注释
     * - com.test.a. 包级别
     * - com.test.a.Demo 类级别
     * - 分号分隔
     */
    private String classBlackList;

    /**
     * 类/包白名单文本
     */
    private String classWhiteList;

    /**
     * 是否使用快速模式
     * false = 标准模式（继承、字符串、Spring 分析）
     * true = 快速模式（仅方法调用关系）
     */
    private boolean quickMode = false;

    /**
     * 是否修正类名（FixClassVisitor）
     */
    private boolean fixClass = false;

    /**
     * 是否解析 JAR 中嵌套的 JAR
     */
    private boolean jarsInJar = false;

    /**
     * 是否自动处理方法实现（override）
     */
    private boolean fixMethodImpl = true;

    /**
     * 进度回调接口（可选）
     * GUI 模式下用于更新进度条，CLI 模式下用于打印日志
     */
    private ProgressCallback progressCallback;

    public EngineConfig() {
    }

    public Path getJarPath() {
        return jarPath;
    }

    public void setJarPath(Path jarPath) {
        this.jarPath = jarPath;
    }

    public Path getRtJarPath() {
        return rtJarPath;
    }

    public void setRtJarPath(Path rtJarPath) {
        this.rtJarPath = rtJarPath;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public String getClassBlackList() {
        return classBlackList;
    }

    public void setClassBlackList(String classBlackList) {
        this.classBlackList = classBlackList;
    }

    public String getClassWhiteList() {
        return classWhiteList;
    }

    public void setClassWhiteList(String classWhiteList) {
        this.classWhiteList = classWhiteList;
    }

    public boolean isQuickMode() {
        return quickMode;
    }

    public void setQuickMode(boolean quickMode) {
        this.quickMode = quickMode;
    }

    public boolean isFixClass() {
        return fixClass;
    }

    public void setFixClass(boolean fixClass) {
        this.fixClass = fixClass;
    }

    public boolean isJarsInJar() {
        return jarsInJar;
    }

    public void setJarsInJar(boolean jarsInJar) {
        this.jarsInJar = jarsInJar;
    }

    public boolean isFixMethodImpl() {
        return fixMethodImpl;
    }

    public void setFixMethodImpl(boolean fixMethodImpl) {
        this.fixMethodImpl = fixMethodImpl;
    }

    public ProgressCallback getProgressCallback() {
        return progressCallback;
    }

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }
}
