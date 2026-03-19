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

import me.n1ar4.jar.analyzer.analyze.spring.SpringService;
import me.n1ar4.jar.analyzer.core.*;
import me.n1ar4.jar.analyzer.core.asm.FixClassVisitor;
import me.n1ar4.jar.analyzer.core.asm.StringClassVisitor;
import me.n1ar4.jar.analyzer.core.reference.ClassReference;
import me.n1ar4.jar.analyzer.core.reference.MethodReference;
import me.n1ar4.jar.analyzer.engine.log.LogManager;
import me.n1ar4.jar.analyzer.engine.log.Logger;
import me.n1ar4.jar.analyzer.engine.utils.*;
import me.n1ar4.jar.analyzer.entity.ClassFileEntity;
import org.objectweb.asm.ClassReader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Engine Build Runner - core analysis pipeline without any GUI dependency.
 * Extracted from CoreRunner, driven by EngineConfig.
 */
public class EngineBuildRunner {
    private static final Logger logger = LogManager.getLogger();

    public static void run(EngineConfig config) {
        ProgressCallback callback = config.getProgressCallback();
        if (callback == null) {
            callback = ProgressCallback.CONSOLE;
        }

        // Clear corrupted files tracking
        AnalyzeEnv.corruptedFiles.clear();

        // Setup JarUtil with black/white list
        JarUtil.setBlackListText(config.getClassBlackList());
        JarUtil.setWhiteListText(config.getClassWhiteList());
        JarUtil.setInputFileText(config.getJarPath().toAbsolutePath().toString());

        // Setup AnalyzeEnv
        AnalyzeEnv.jarsInJar = config.isJarsInJar();

        Path jarPath = config.getJarPath();
        Path rtJarPath = config.getRtJarPath();
        boolean fixClass = config.isFixClass();
        boolean quickMode = config.isQuickMode();

        Map<String, Integer> jarIdMap = new HashMap<>();
        List<ClassFileEntity> cfs;

        callback.onProgress(10);

        if (Files.isDirectory(jarPath)) {
            logger.info("input is a dir");
            callback.onInfo("input is a dir");
            List<String> files = DirUtil.GetFiles(jarPath.toAbsolutePath().toString());
            if (rtJarPath != null) {
                files.add(rtJarPath.toAbsolutePath().toString());
                callback.onInfo("analyze with rt.jar file");
            }
            callback.onStats("totalJar", String.valueOf(files.size()));
            for (String s : files) {
                if (s.toLowerCase().endsWith(".jar") ||
                        s.toLowerCase().endsWith(".war")) {
                    DatabaseManager.saveJar(s);
                    jarIdMap.put(s, DatabaseManager.getJarId(s).getJid());
                }
            }
            cfs = CoreUtil.getAllClassesFromJars(files, jarIdMap);
        } else {
            logger.info("input is a jar file");
            callback.onInfo("input is a jar");
            List<String> jarList = new ArrayList<>();
            if (rtJarPath != null) {
                jarList.add(rtJarPath.toAbsolutePath().toString());
                callback.onInfo("analyze with rt.jar file");
            }
            jarList.add(jarPath.toAbsolutePath().toString());
            callback.onStats("totalJar", String.valueOf(jarList.size()));
            for (String s : jarList) {
                DatabaseManager.saveJar(s);
                jarIdMap.put(s, DatabaseManager.getJarId(s).getJid());
            }
            cfs = CoreUtil.getAllClassesFromJars(jarList, jarIdMap);
        }

        // Fix class names
        for (ClassFileEntity cf : cfs) {
            String className = cf.getClassName();
            if (!fixClass) {
                int i = className.indexOf("classes");
                if (className.contains("BOOT-INF") || className.contains("WEB-INF")) {
                    className = className.substring(i + 8);
                }
                cf.setClassName(className);
            } else {
                Path parPath = Paths.get(config.getTempDir());
                FixClassVisitor cv = new FixClassVisitor();
                try {
                    ClassReader cr = new ClassReader(cf.getFile());
                    cr.accept(cv, EngineConst.AnalyzeASMOptions);
                } catch (IndexOutOfBoundsException e) {
                    if (!StackMapFrameHandler.handleParseException(cf.getFile(), cv,
                            cf.getJarName() + "!" + cf.getClassName(),
                            logger, "fix class name", e)) {
                        throw e;
                    }
                }
                Path path = parPath.resolve(Paths.get(cv.getName()));
                File file = path.toFile();
                if (!file.getParentFile().mkdirs()) {
                    logger.error("fix class mkdirs error");
                }
                className = file.getPath() + ".class";
                try {
                    IOUtil.copy(new ByteArrayInputStream(cf.getFile()),
                            new FileOutputStream(className));
                } catch (FileNotFoundException ignored) {
                    logger.error("fix path copy bytes error");
                }
                cf.setClassName(className);
                cf.setPath(Paths.get(className));
            }
        }

        callback.onProgress(15);
        AnalyzeEnv.classFileList.addAll(cfs);
        logger.info("get all class");
        callback.onInfo("get all class");
        DatabaseManager.saveClassFiles(AnalyzeEnv.classFileList);

        callback.onProgress(20);
        DiscoveryRunner.start(AnalyzeEnv.classFileList, AnalyzeEnv.discoveredClasses,
                AnalyzeEnv.discoveredMethods, AnalyzeEnv.classMap,
                AnalyzeEnv.methodMap, AnalyzeEnv.stringAnnoMap);
        DatabaseManager.saveClassInfo(AnalyzeEnv.discoveredClasses);

        callback.onProgress(25);
        DatabaseManager.saveMethods(AnalyzeEnv.discoveredMethods);

        callback.onProgress(30);
        logger.info("analyze class finish");
        callback.onInfo("analyze class finish");
        callback.onStats("totalClass", String.valueOf(DatabaseManager.getTotalClassCount()));
        callback.onStats("totalMethod", String.valueOf(DatabaseManager.getTotalMethodCount()));

        for (MethodReference mr : AnalyzeEnv.discoveredMethods) {
            ClassReference.Handle ch = mr.getClassReference();
            if (AnalyzeEnv.methodsInClassMap.get(ch) == null) {
                List<MethodReference> ml = new ArrayList<>();
                ml.add(mr);
                AnalyzeEnv.methodsInClassMap.put(ch, ml);
            } else {
                List<MethodReference> ml = AnalyzeEnv.methodsInClassMap.get(ch);
                ml.add(mr);
                AnalyzeEnv.methodsInClassMap.put(ch, ml);
            }
        }

        callback.onProgress(35);
        MethodCallRunner.start(AnalyzeEnv.classFileList, AnalyzeEnv.methodCalls);
        callback.onProgress(40);

        if (!quickMode) {
            AnalyzeEnv.inheritanceMap = InheritanceRunner.derive(AnalyzeEnv.classMap);
            callback.onProgress(50);
            logger.info("build inheritance");
            callback.onInfo("build inheritance");

            Map<MethodReference.Handle, Set<MethodReference.Handle>> implMap =
                    InheritanceRunner.getAllMethodImplementations(
                            AnalyzeEnv.inheritanceMap, AnalyzeEnv.methodMap);
            DatabaseManager.saveImpls(implMap);
            callback.onProgress(60);

            if (config.isFixMethodImpl()) {
                for (Map.Entry<MethodReference.Handle, Set<MethodReference.Handle>> entry :
                        implMap.entrySet()) {
                    MethodReference.Handle k = entry.getKey();
                    Set<MethodReference.Handle> v = entry.getValue();
                    HashSet<MethodReference.Handle> calls = AnalyzeEnv.methodCalls.get(k);
                    if (calls != null) {
                        calls.addAll(v);
                    }
                }
            } else {
                logger.warn("enable fix method impl/override is recommend");
            }

            DatabaseManager.saveMethodCalls(AnalyzeEnv.methodCalls);
            callback.onProgress(70);
            logger.info("build extra inheritance");
            callback.onInfo("build extra inheritance");

            for (ClassFileEntity file : AnalyzeEnv.classFileList) {
                try {
                    StringClassVisitor dcv = new StringClassVisitor(
                            AnalyzeEnv.strMap, AnalyzeEnv.classMap, AnalyzeEnv.methodMap);
                    ClassReader cr = new ClassReader(file.getFile());
                    cr.accept(dcv, EngineConst.AnalyzeASMOptions);
                } catch (IndexOutOfBoundsException e) {
                    if (!StackMapFrameHandler.handleParseException(file,
                            new StringClassVisitor(AnalyzeEnv.strMap,
                                    AnalyzeEnv.classMap, AnalyzeEnv.methodMap),
                            logger, "string analysis", e)) {
                        logger.error("string analyze error: {}", e.toString());
                    }
                } catch (Exception ex) {
                    logger.error("string analyze error: {}", ex.toString());
                }
            }

            callback.onProgress(80);
            DatabaseManager.saveStrMap(AnalyzeEnv.strMap, AnalyzeEnv.stringAnnoMap);

            SpringService.start(AnalyzeEnv.classFileList, AnalyzeEnv.controllers,
                    AnalyzeEnv.classMap, AnalyzeEnv.methodMap);
            DatabaseManager.saveSpringController(AnalyzeEnv.controllers);

            OtherWebService.start(AnalyzeEnv.classFileList,
                    AnalyzeEnv.interceptors,
                    AnalyzeEnv.servlets, AnalyzeEnv.filters, AnalyzeEnv.listeners);
            DatabaseManager.saveSpringInterceptor(AnalyzeEnv.interceptors);
            DatabaseManager.saveServlets(AnalyzeEnv.servlets);
            DatabaseManager.saveFilters(AnalyzeEnv.filters);
            DatabaseManager.saveListeners(AnalyzeEnv.listeners);

            callback.onProgress(90);
        } else {
            callback.onProgress(70);
            DatabaseManager.saveMethodCalls(AnalyzeEnv.methodCalls);
        }

        logger.info("build database finish");
        callback.onInfo("build database finish");

        long fileSizeBytes = new File(EngineConst.dbFile).length();
        String fileSizeMB = String.format("%.2f MB", (double) fileSizeBytes / (1024 * 1024));
        callback.onStats("dbSize", fileSizeMB);

        callback.onProgress(100);

        // Report corrupted files
        if (!AnalyzeEnv.corruptedFiles.isEmpty()) {
            callback.onWarn("corrupted files count: " + AnalyzeEnv.corruptedFiles.size());
            for (String fileInfo : AnalyzeEnv.corruptedFiles) {
                callback.onWarn("corrupted: " + fileInfo);
            }
        }

        // GC
        AnalyzeEnv.classFileList.clear();
        AnalyzeEnv.discoveredClasses.clear();
        AnalyzeEnv.discoveredMethods.clear();
        AnalyzeEnv.methodsInClassMap.clear();
        AnalyzeEnv.classMap.clear();
        AnalyzeEnv.methodMap.clear();
        AnalyzeEnv.methodCalls.clear();
        AnalyzeEnv.strMap.clear();
        if (!quickMode) {
            if (AnalyzeEnv.inheritanceMap != null) {
                AnalyzeEnv.inheritanceMap.getInheritanceMap().clear();
                AnalyzeEnv.inheritanceMap.getSubClassMap().clear();
            }
        }
        AnalyzeEnv.controllers.clear();
        System.gc();
    }
}
