/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.entity;

import me.n1ar4.jar.analyzer.engine.log.LogManager;
import me.n1ar4.jar.analyzer.engine.log.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class ClassFileEntity {
    private static final Logger logger = LogManager.getLogger();
    private int cfId;
    private String className;
    private Path path;
    private String pathStr;
    private String jarName;
    private Integer jarId;

    public int getCfId() {
        return cfId;
    }

    public void setCfId(int cfId) {
        this.cfId = cfId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getPathStr() {
        return pathStr;
    }

    public void setPathStr(String pathStr) {
        this.pathStr = pathStr;
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public Integer getJarId() {
        return jarId;
    }

    public void setJarId(Integer jarId) {
        this.jarId = jarId;
    }

    public ClassFileEntity() {
    }

    public ClassFileEntity(String className, Path path, Integer jarId) {
        this.className = className;
        this.path = path;
        this.jarId = jarId;
    }

    public byte[] getFile() {
        try {
            return Files.readAllBytes(this.path);
        } catch (Exception e) {
            logger.error("get file error: {}", e.toString());
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassFileEntity that = (ClassFileEntity) o;
        return Objects.equals(className, that.className) &&
                Objects.equals(path, that.path) &&
                Objects.equals(jarId, that.jarId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, path, jarId);
    }

    @Override
    public String toString() {
        return "ClassFileEntity{" +
                "cfId=" + cfId +
                ", className='" + className + '\'' +
                ", path=" + path +
                ", pathStr='" + pathStr + '\'' +
                ", jarName='" + jarName + '\'' +
                ", jarId=" + jarId +
                '}';
    }
}
