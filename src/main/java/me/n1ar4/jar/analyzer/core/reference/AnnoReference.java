/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core.reference;

import java.util.Objects;

public class AnnoReference {
    private String annoName;
    private Boolean visible;
    private Integer parameter;

    public AnnoReference() {
    }

    public AnnoReference(String annoName) {
        this.annoName = annoName;
    }

    public String getAnnoName() {
        return annoName;
    }

    public void setAnnoName(String annoName) {
        this.annoName = annoName;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Integer getParameter() {
        return parameter;
    }

    public void setParameter(Integer parameter) {
        this.parameter = parameter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnoReference that = (AnnoReference) o;
        return Objects.equals(annoName, that.annoName) &&
                Objects.equals(visible, that.visible) &&
                Objects.equals(parameter, that.parameter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annoName, visible, parameter);
    }

    @Override
    public String toString() {
        return "AnnoReference{" +
                "annoName='" + annoName + '\'' +
                ", visible=" + visible +
                ", parameter=" + parameter +
                '}';
    }
}
