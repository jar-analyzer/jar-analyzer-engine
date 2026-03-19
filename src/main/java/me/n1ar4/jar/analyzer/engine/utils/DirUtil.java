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

import me.n1ar4.jar.analyzer.engine.log.LogManager;
import me.n1ar4.jar.analyzer.engine.log.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class DirUtil {
    private static final Logger logger = LogManager.getLogger();

    public static List<String> GetFiles(String path) {
        List<String> filenames = new ArrayList<>();
        getFiles(path, filenames);
        return filenames;
    }

    private static void getFiles(String path, List<String> filenames) {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (File value : files) {
                if (value.isDirectory()) {
                    getFiles(value.getPath(), filenames);
                } else {
                    filenames.add(value.getAbsolutePath());
                }
            }
        } else {
            filenames.add(file.getAbsolutePath());
        }
    }
}
