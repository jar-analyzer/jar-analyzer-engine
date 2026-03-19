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

import com.beust.jcommander.JCommander;
import me.n1ar4.jar.analyzer.engine.log.LogManager;
import me.n1ar4.jar.analyzer.engine.log.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EngineMain {
    private static final Logger logger = LogManager.getLogger();

    @SuppressWarnings("all")
    public static void main(String[] args) {
        System.out.println("=== Jar Analyzer Engine " + EngineConst.version + " ===");
        System.out.println("Build SQLite database from JAR/WAR files");
        System.out.println();

        EngineBuildCmd cmd = new EngineBuildCmd();
        JCommander jc = JCommander.newBuilder()
                .addObject(cmd)
                .build();
        jc.setProgramName("jar-analyzer-engine");

        try {
            jc.parse(args);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            jc.usage();
            System.exit(1);
            return;
        }

        if (cmd.help) {
            jc.usage();
            return;
        }

        if (cmd.jarPath == null || cmd.jarPath.isEmpty()) {
            System.err.println("Error: --jar parameter is required");
            jc.usage();
            System.exit(1);
            return;
        }

        Path jarPath = Paths.get(cmd.jarPath);
        if (!Files.exists(jarPath)) {
            System.err.println("Error: JAR path does not exist: " + cmd.jarPath);
            System.exit(1);
            return;
        }

        // Build EngineConfig from CLI parameters
        EngineConfig config = new EngineConfig();
        config.setJarPath(jarPath);
        config.setQuickMode(cmd.quickMode);
        config.setFixClass(cmd.fixClass);
        config.setJarsInJar(cmd.jarsInJar);
        config.setFixMethodImpl(!cmd.noFixMethodImpl);
        config.setProgressCallback(ProgressCallback.CONSOLE);

        if (cmd.rtJarPath != null && !cmd.rtJarPath.isEmpty()) {
            Path rtPath = Paths.get(cmd.rtJarPath);
            if (Files.exists(rtPath)) {
                config.setRtJarPath(rtPath);
            } else {
                System.err.println("Warning: rt.jar path does not exist: " + cmd.rtJarPath);
            }
        }

        // Handle black list
        if (cmd.classBlackListFile != null && !cmd.classBlackListFile.isEmpty()) {
            try {
                config.setClassBlackList(new String(Files.readAllBytes(
                        Paths.get(cmd.classBlackListFile))));
            } catch (IOException e) {
                System.err.println("Warning: cannot read black list file: " + cmd.classBlackListFile);
            }
        } else if (cmd.classBlackList != null) {
            config.setClassBlackList(cmd.classBlackList);
        }

        // Handle white list
        if (cmd.classWhiteListFile != null && !cmd.classWhiteListFile.isEmpty()) {
            try {
                config.setClassWhiteList(new String(Files.readAllBytes(
                        Paths.get(cmd.classWhiteListFile))));
            } catch (IOException e) {
                System.err.println("Warning: cannot read white list file: " + cmd.classWhiteListFile);
            }
        } else if (cmd.classWhiteList != null) {
            config.setClassWhiteList(cmd.classWhiteList);
        }

        // Print config summary
        System.out.println("Configuration:");
        System.out.println("  JAR Path:      " + config.getJarPath());
        System.out.println("  DB Path:       jar-analyzer.db");
        System.out.println("  Temp Dir:      " + EngineConst.tempDir);
        System.out.println("  Quick Mode:    " + config.isQuickMode());
        System.out.println("  Fix Class:     " + config.isFixClass());
        System.out.println("  Inner JARs:    " + config.isJarsInJar());
        System.out.println("  Fix Impl:      " + config.isFixMethodImpl());
        System.out.println("  Black List:    " + (config.getClassBlackList() != null ? "set" : "none"));
        System.out.println("  White List:    " + (config.getClassWhiteList() != null ? "set" : "none"));
        System.out.println();

        long startTime = System.currentTimeMillis();

        try {
            EngineBuildRunner.run(config);
        } catch (Exception e) {
            logger.error("build failed: {}", e.toString());
            e.printStackTrace();
            System.exit(1);
            return;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println();
        System.out.println("=== Build Complete ===");
        System.out.printf("Time elapsed: %.2f seconds%n", elapsed / 1000.0);
        System.out.println("Database: jar-analyzer.db");
    }
}
