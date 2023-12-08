package com.bbc876219.lib.xml2code.anoprocesser;

import com.bbc876219.lib.xml2code.annotation.Xml2Code;
import com.bbc876219.lib.xml2code.anoprocesser.xml.LayoutManager;


import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * @author bbcl 2023/12/8
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.bbc876219.lib.xml2code.annotation.Xml2Code")
public class Xml2CodeProcessor extends AbstractProcessor {

    private int mGroupId = 0;
    private LayoutManager mLayoutMgr;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        Log.init(processingEnvironment.getMessager());
        mLayoutMgr = LayoutManager.instance();
        mLayoutMgr.setFiler(processingEnvironment.getFiler());
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Xml2Code.class);
        //System.out.println( "process() called with: elements = [" + elements.size() + "], roundEnvironment = [" + roundEnvironment + "]");
        TreeSet<String> layouts = new TreeSet<>();
        for (Element element : elements) {
            Xml2Code xml = element.getAnnotation(Xml2Code.class);
            String[] names = xml.layouts();
            for (String name : names) {
                //System.out.println( "process() called with: layouts.add = [" + name + "], real  = [" + (name.substring(name.lastIndexOf(".") + 1)) + "]");

                layouts.add(name.substring(name.lastIndexOf(".") + 1));
            }
        }

        for (String name : layouts) {
            if (mGroupId == 0 && mLayoutMgr.getLayoutId(name) != null) {
                mGroupId = (mLayoutMgr.getLayoutId(name) >> 24);
            }
            //System.out.println( "process().translate called with: mGroupId = [" + mGroupId + "], name = [" + name + "]");
            mLayoutMgr.setGroupId(mGroupId);
            mLayoutMgr.translate(name);
        }

        mLayoutMgr.printTranslate();
        return false;
    }


}
