package com.bbc876219.lib.xml2code.view;

/**
 * @author bbcl 2018/8/23
 */
public interface ITranslator {
    boolean translate(StringBuilder stringBuilder, String key, String value);
    void onAttributeEnd(StringBuilder stringBuilder);
}
