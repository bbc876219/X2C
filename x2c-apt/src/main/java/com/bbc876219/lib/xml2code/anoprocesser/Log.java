package com.bbc876219.lib.xml2code.anoprocesser;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * @author bbcl 2023/12/8
 */
public class Log {
    private static Messager sMessager;

    public static void init(Messager msger) {
        sMessager = msger;
    }

    public static void w(String msg) {
        if (sMessager != null) {
            sMessager.printMessage(Diagnostic.Kind.OTHER, msg);
        }
    }

    public static void e(String msg) {
        if (sMessager != null) {
            sMessager.printMessage(Diagnostic.Kind.ERROR, msg);
        }
    }
}
