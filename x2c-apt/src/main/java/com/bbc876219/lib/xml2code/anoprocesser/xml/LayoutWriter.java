package com.bbc876219.lib.xml2code.anoprocesser.xml;

import com.squareup.javapoet.x2c.ClassName;
import com.squareup.javapoet.x2c.JavaFile;
import com.squareup.javapoet.x2c.MethodSpec;
import com.squareup.javapoet.x2c.TypeSpec;

import java.io.IOException;
import java.util.TreeSet;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

/**
 * @author  bbcl 2018/8/8
 */
public class LayoutWriter {
    private Filer mFiler;
    private String mName;
    private String mMethodSpec;
    private String mPkgName;
    private String mLayoutCategory;
    private String mLayoutName;
    private TreeSet<String> mImports;

    public LayoutWriter(String methodSpec, Filer filer, String javaName
            , String pkgName
            , String layoutSort
            , String layoutName
            , TreeSet<String> imports) {
        this.mMethodSpec = methodSpec;
        this.mFiler = filer;
        this.mName = javaName;
        this.mPkgName = pkgName;
        this.mLayoutCategory = layoutSort;
        this.mLayoutName = layoutName;
        this.mImports = imports;
    }

    public String write() {

        MethodSpec methodSpec = MethodSpec.methodBuilder("createView")
                .addParameter(ClassName.get("android.content", "Context"), "ctx")
                .addStatement(mMethodSpec)
                .returns(ClassName.get("android.view", "View"))
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .build();

        TypeSpec typeSpec = TypeSpec.classBuilder(mName)
                .addMethod(methodSpec)
                .addSuperinterface(ClassName.get("com.bbc876219.lib.xml2code", "IViewCreator"))
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(String.format("WARN!!! dont edit this file\ntranslate from {@link  %s.R.layout.%s}" +
                        "\nautho bbcl \nemail bbcl@bitvenus.com\n", mPkgName, mLayoutName))
                .build();

        String pkgName = "com.bbc876219.lib.xml2code.layouts";
        if (mLayoutCategory != null && mLayoutCategory.length() > 0) {
            pkgName += ("." + mLayoutCategory);
        }
        JavaFile javaFile = JavaFile.builder(pkgName, typeSpec)
                .addImports(mImports)
                .build();
        try {
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pkgName + "." + mName;
    }
}
