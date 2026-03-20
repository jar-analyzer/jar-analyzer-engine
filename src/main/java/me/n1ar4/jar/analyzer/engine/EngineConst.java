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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

/**
 * 引擎核心常量（不包含 GUI 常量）
 */
public interface EngineConst {
    String version = "1.2.0";
    int ASMVersion = Opcodes.ASM9;
    int AnalyzeASMOptions = ClassReader.EXPAND_FRAMES;
    /**
     * Fallback ASM option for handling corrupted StackMapTable class files.
     */
    int FallbackASMOptions = ClassReader.SKIP_FRAMES;
    String dbFile = "jar-analyzer.db";
    String tempDir = "jar-analyzer-temp";
}
