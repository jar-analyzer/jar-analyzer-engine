/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.engine.utils;

import me.n1ar4.jar.analyzer.core.AnalyzeEnv;
import me.n1ar4.jar.analyzer.engine.EngineConst;
import me.n1ar4.jar.analyzer.engine.log.LogManager;
import me.n1ar4.jar.analyzer.engine.log.Logger;
import me.n1ar4.jar.analyzer.entity.ClassFileEntity;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Engine version of JarUtil - no GUI dependency.
 * Black/white list texts are passed as parameters instead of reading from MainForm.
 */
@SuppressWarnings("all")
public class JarUtil {
    private static final Logger logger = LogManager.getLogger();

    private static final String META_INF = "META-INF";
    private static final int MAX_PARENT_SEARCH = 20;

    // 黑白名单文本（由引擎设置）
    private static String blackListText;
    private static String whiteListText;
    private static String inputFileText;

    public static void setBlackListText(String text) {
        blackListText = text;
    }

    public static void setWhiteListText(String text) {
        whiteListText = text;
    }

    public static void setInputFileText(String text) {
        inputFileText = text;
    }

    public static final Set<String> CONFIG_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".yml", ".yaml", ".properties", ".xml", ".json", ".conf", ".config", ".ini", ".toml", "web.xml"
    ));

    public static boolean isConfigFile(String fileName) {
        fileName = fileName.toLowerCase();
        for (String ext : CONFIG_EXTENSIONS) {
            if (fileName.endsWith(ext.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static List<ClassFileEntity> resolveNormalJarFile(String jarPath, Integer jarId) {
        try {
            Path tmpDir = Paths.get(EngineConst.tempDir);
            Set<ClassFileEntity> localClassFileSet = new HashSet<>();
            resolve(jarId, jarPath, tmpDir, localClassFileSet);
            return new ArrayList<>(localClassFileSet);
        } catch (Exception e) {
            logger.error("error: {}", e.toString());
        }
        return new ArrayList<>();
    }

    private static boolean shouldRun(String whiteText, String blackText, String saveClass) {
        boolean whiteDoIt = false;

        // Normalize saveClass: strip .class suffix and fix BOOT-INF/WEB-INF paths
        if (saveClass.contains("BOOT-INF") || saveClass.contains("WEB-INF")) {
            int i = saveClass.indexOf("classes");
            if (i >= 0) {
                // e.g. "BOOT-INF/classes/com/example/Foo.class" -> "com/example/Foo"
                saveClass = saveClass.substring(i + 8);
            }
        }
        if (saveClass.endsWith(".class")) {
            saveClass = saveClass.substring(0, saveClass.length() - 6);
        }

        if (whiteText != null && !StringUtil.isNull(whiteText)) {
            ArrayList<String> data = ListParser.parse(whiteText);
            if (data.isEmpty()) {
                whiteDoIt = true;
            } else {
                String className = saveClass;
                for (String s : data) {
                    if (s.endsWith("/")) {
                        if (className.startsWith(s)) {
                            whiteDoIt = true;
                            break;
                        }
                    } else {
                        if (className.equals(s)) {
                            whiteDoIt = true;
                            break;
                        }
                    }
                }
            }
        } else {
            whiteDoIt = true;
        }

        if (!whiteDoIt) {
            return false;
        }

        boolean doIt = true;
        if (blackText != null && !StringUtil.isNull(blackText)) {
            ArrayList<String> data = ListParser.parse(blackText);
            String className = saveClass;
            for (String s : data) {
                if (className.equals(s)) {
                    doIt = false;
                    break;
                }
                if (s.endsWith("/")) {
                    if (className.startsWith(s)) {
                        doIt = false;
                        break;
                    }
                }
            }
        }

        if (!doIt) {
            return false;
        }

        return true;
    }

    private static void resolve(Integer jarId, String jarPathStr, Path tmpDir,
                                Set<ClassFileEntity> localClassFileSet) {
        String text = blackListText;
        String whiteText = whiteListText;
        Path jarPath = Paths.get(jarPathStr);
        if (!Files.exists(jarPath)) {
            logger.error("jar not exist");
            return;
        }
        try {
            if (jarPathStr.toLowerCase(Locale.ROOT).endsWith(".class")) {
                String fileText = inputFileText != null ? inputFileText.trim() : "";
                if (jarPathStr.contains(fileText)) {
                    String backPath = jarPathStr;

                    Path parentPath = jarPath;
                    Path resultPath = null;
                    int index = 0;
                    while ((parentPath = parentPath.getParent()) != null) {
                        Path metaPath = parentPath.resolve("META-INF");
                        if (Files.exists(metaPath)) {
                            resultPath = metaPath;
                            break;
                        }
                        index++;
                        if (index > MAX_PARENT_SEARCH) {
                            break;
                        }
                    }
                    if (resultPath == null) {
                        return;
                    }
                    String finalPath = resultPath.toAbsolutePath().toString();
                    if (!finalPath.contains(fileText)) {
                        return;
                    }
                    if (finalPath.length() < META_INF.length()) {
                        logger.warn("路径长度不足: {}", finalPath);
                        return;
                    }
                    try {
                        jarPathStr = jarPathStr.substring(finalPath.length() - META_INF.length());
                    } catch (StringIndexOutOfBoundsException e) {
                        logger.error("字符串截取错误: jarPathStr={}, finalPath={}", jarPathStr, finalPath);
                        return;
                    }
                    String saveClass = jarPathStr.replace("\\", "/");
                    logger.info("加载 CLASS 文件 {}", saveClass);

                    if (!shouldRun(whiteText, text, saveClass)) {
                        return;
                    }

                    ClassFileEntity classFile = new ClassFileEntity(saveClass, jarPath, jarId);
                    classFile.setJarName("class");
                    localClassFileSet.add(classFile);

                    Path fullPath = tmpDir.resolve(jarPathStr);
                    Path parPath = fullPath.getParent();
                    if (!Files.exists(parPath)) {
                        Files.createDirectories(parPath);
                    }
                    try {
                        Files.createFile(fullPath);
                    } catch (Exception ignored) {
                    }
                    try (InputStream fis = Files.newInputStream(Paths.get(backPath));
                         OutputStream outputStream = Files.newOutputStream(fullPath)) {
                        IOUtil.copy(fis, outputStream);
                    }
                } else {
                    return;
                }
            } else if (jarPathStr.toLowerCase(Locale.ROOT).endsWith(".jar") ||
                    jarPathStr.toLowerCase(Locale.ROOT).endsWith(".war")) {
                ZipFile jarFile = new ZipFile(jarPath);
                Enumeration<? extends ZipArchiveEntry> entries = jarFile.getEntries();
                while (entries.hasMoreElements()) {
                    ZipArchiveEntry jarEntry = entries.nextElement();
                    String jarEntryName = jarEntry.getName();
                    if (jarEntryName.contains("../") || jarEntryName.contains("..\\")) {
                        logger.warn("detect zip slip vulnerability");
                        continue;
                    }
                    Path entryPath = tmpDir.resolve(jarEntryName).toAbsolutePath().normalize();
                    Path tmpDirAbs = tmpDir.toAbsolutePath();
                    if (!entryPath.toString().startsWith(tmpDirAbs.toString())) {
                        logger.warn("detect zip slip vulnerability");
                        continue;
                    }
                    Path fullPath = tmpDir.resolve(jarEntryName);
                    if (!jarEntry.isDirectory()) {
                        if (isConfigFile(jarEntryName)) {
                            Path dirName = fullPath.getParent();
                            if (!Files.exists(dirName)) {
                                Files.createDirectories(dirName);
                            }
                            try {
                                Files.createFile(fullPath);
                            } catch (Exception ignored) {
                            }
                            try (InputStream temp = jarFile.getInputStream(jarEntry);
                                 OutputStream outputStream = Files.newOutputStream(fullPath)) {
                                IOUtil.copy(temp, outputStream);
                            }
                            logger.debug("save config: {}", jarEntryName);
                            continue;
                        }

                        if (!jarEntry.getName().endsWith(".class")) {
                            if (AnalyzeEnv.jarsInJar && jarEntry.getName().endsWith(".jar")) {
                                logger.info("analyze jars in jar: {}", jarEntry.getName());
                                Path dirName = fullPath.getParent();
                                if (!Files.exists(dirName)) {
                                    Files.createDirectories(dirName);
                                }
                                try {
                                    Files.createFile(fullPath);
                                } catch (Exception ignored) {
                                }
                                try (InputStream temp = jarFile.getInputStream(jarEntry);
                                     OutputStream outputStream = Files.newOutputStream(fullPath)) {
                                    IOUtil.copy(temp, outputStream);
                                }
                                doInternal(jarId, fullPath, tmpDir, text, whiteText, localClassFileSet);
                            }
                            continue;
                        }

                        if (!shouldRun(whiteText, text, jarEntry.getName())) {
                            continue;
                        }

                        Path dirName = fullPath.getParent();
                        if (!Files.exists(dirName)) {
                            Files.createDirectories(dirName);
                        }
                        try (InputStream temp = jarFile.getInputStream(jarEntry);
                             OutputStream outputStream = Files.newOutputStream(fullPath)) {
                            IOUtil.copy(temp, outputStream);
                        }
                        ClassFileEntity classFile = new ClassFileEntity(jarEntry.getName(), fullPath, jarId);
                        String splitStr;
                        if (OSUtil.isWindows()) {
                            splitStr = "\\\\";
                        } else {
                            splitStr = "/";
                        }
                        String[] splits = jarPathStr.split(splitStr);
                        classFile.setJarName(splits[splits.length - 1]);
                        localClassFileSet.add(classFile);
                    }
                }
                jarFile.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("error: {}", e.toString());
        }
    }

    private static void doInternal(Integer jarId, Path jarPath, Path tmpDir,
                                   String text, String whiteText,
                                   Set<ClassFileEntity> localClassFileSet) {
        try {
            ZipFile jarFile = new ZipFile(jarPath);
            Enumeration<? extends ZipArchiveEntry> entries = jarFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry jarEntry = entries.nextElement();
                String jarEntryName = jarEntry.getName();
                if (jarEntryName.contains("../") || jarEntryName.contains("..\\")) {
                    logger.warn("detect zip slip vulnerability");
                    continue;
                }
                Path entryPath = tmpDir.resolve(jarEntryName).toAbsolutePath().normalize();
                Path tmpDirAbs = tmpDir.toAbsolutePath();
                if (!entryPath.toString().startsWith(tmpDirAbs.toString())) {
                    logger.warn("detect zip slip vulnerability");
                    continue;
                }
                Path fullPath = tmpDir.resolve(jarEntryName);
                if (!jarEntry.isDirectory()) {
                    if (isConfigFile(jarEntryName)) {
                        Path dirName = fullPath.getParent();
                        if (!Files.exists(dirName)) {
                            Files.createDirectories(dirName);
                        }
                        try {
                            Files.createFile(fullPath);
                        } catch (Exception ignored) {
                        }
                        try (InputStream temp = jarFile.getInputStream(jarEntry);
                             OutputStream outputStream = Files.newOutputStream(fullPath)) {
                            IOUtil.copy(temp, outputStream);
                        }
                        logger.debug("save config: {}", jarEntryName);
                        continue;
                    }

                    if (!jarEntry.getName().endsWith(".class")) {
                        continue;
                    }

                    if (!shouldRun(whiteText, text, jarEntry.getName())) {
                        continue;
                    }

                    Path dirName = fullPath.getParent();
                    if (!Files.exists(dirName)) {
                        Files.createDirectories(dirName);
                    }
                    try (InputStream temp = jarFile.getInputStream(jarEntry);
                         OutputStream outputStream = Files.newOutputStream(fullPath)) {
                        IOUtil.copy(temp, outputStream);
                    }
                    ClassFileEntity classFile = new ClassFileEntity(jarEntry.getName(), fullPath, jarId);
                    String splitStr;
                    if (OSUtil.isWindows()) {
                        splitStr = "\\\\";
                    } else {
                        splitStr = "/";
                    }
                    String[] splits = jarPath.toString().split(splitStr);
                    classFile.setJarName(splits[splits.length - 1]);
                    localClassFileSet.add(classFile);
                }
            }
            jarFile.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("error: {}", e.toString());
        }
    }
}
