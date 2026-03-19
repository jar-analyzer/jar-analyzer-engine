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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * CLI 命令行参数定义
 */
@Parameters(commandDescription = "Build SQLite database from JAR files")
public class EngineBuildCmd {
    @Parameter(names = {"--jar", "-j"}, description = "JAR file or directory path (required)")
    public String jarPath;

    @Parameter(names = {"--rt"}, description = "rt.jar path (optional, for JDK class analysis)")
    public String rtJarPath;

    @Parameter(names = {"--temp", "-t"}, description = "Temp directory for extraction (default: jar-analyzer-temp)")
    public String tempDir = "jar-analyzer-temp";

    @Parameter(names = {"--black-list", "-b"}, description = "Class/package black list text or file path")
    public String classBlackList;

    @Parameter(names = {"--white-list", "-w"}, description = "Class/package white list text or file path")
    public String classWhiteList;

    @Parameter(names = {"--black-list-file"}, description = "Class/package black list file path")
    public String classBlackListFile;

    @Parameter(names = {"--white-list-file"}, description = "Class/package white list file path")
    public String classWhiteListFile;

    @Parameter(names = {"--quick", "-q"}, description = "Quick mode (method calls only, skip inheritance/string/spring)")
    public boolean quickMode = false;

    @Parameter(names = {"--fix-class"}, description = "Fix class names using FixClassVisitor")
    public boolean fixClass = false;

    @Parameter(names = {"--inner-jars"}, description = "Parse nested JARs inside JAR files")
    public boolean jarsInJar = false;

    @Parameter(names = {"--no-fix-impl"}, description = "Disable automatic method implementation fix (not recommended)")
    public boolean noFixMethodImpl = false;

    @Parameter(names = {"--help", "-h"}, help = true, description = "Show help message")
    public boolean help;
}
