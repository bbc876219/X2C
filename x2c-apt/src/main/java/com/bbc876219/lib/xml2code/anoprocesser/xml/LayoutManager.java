package com.bbc876219.lib.xml2code.anoprocesser.xml;

import com.bbc876219.lib.xml2code.anoprocesser.FileFilter;
import com.bbc876219.lib.xml2code.anoprocesser.Log;
import com.bbc876219.lib.xml2code.anoprocesser.Util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author bbcl 2018/8/10
 */
public class LayoutManager {

    private static LayoutManager sInstance;
    private File mRootFile;
    private String mPackageName;
    private int mGroupId;
    private Filer mFiler;
    /**
     * key is layoutId, value is javaName
     */
    private HashMap<Integer, String> mMap;
    /**
     * key is layoutName,value is layoutId
     */
    private HashMap<String, Integer> mRJava;
    /**
     * key is layoutName,value is layoutId
     */
    private HashMap<Integer, String> mRJavaId;
    /**
     * key is styleName,value is style
     */
    private HashMap<String, Style> mStyles;
    /**
     * key is layoutName,value is javaName
     */
    private HashMap<String, String> mTranslateMap;

    /**
     * key is attrName,value is attr
     */
    private HashMap<String, Attr> mAttrs;

    /**
     * key is layoutName,value is layout list,like layout-land/main.xml,layout-v23/main.xml
     */
    private HashMap<String, ArrayList<File>> mLayouts;

    private LayoutManager() {
        mMap = new HashMap<>();
        mRJavaId = new HashMap<>();
        mTranslateMap = new HashMap<>();
        mLayouts = new HashMap<>();
    }

    public static LayoutManager instance() {
        if (sInstance == null) {
            synchronized (LayoutManager.class) {
                if (sInstance == null) {
                    sInstance = new LayoutManager();
                }
            }
        }
        return sInstance;
    }

    public void setFiler(Filer filer) {
        this.mLayouts.clear();
        this.mFiler = filer;
        this.mRootFile = getRootFile();
        this.findPackageName();
        this.mRJava = getR();
        this.mAttrs = new Attr2FuncReader(new File(mRootFile, "X2C_CONFIG.xml")).parse();
    }

    public void setGroupId(int groupId) {
        this.mGroupId = groupId;
    }


    public Integer getLayoutId(String layoutName) {
        return mRJava.get(layoutName);
    }

    public String translate(String layoutName) {
        if (mLayouts.size() == 0) {
            mLayouts = scanLayouts(mRootFile);
        }
        String fileName = null;
        Integer layoutId = getLayoutId(layoutName);
        if (mMap.containsKey(layoutId)) {
            fileName = mMap.get(layoutId);
        } else {
            ArrayList<File> layouts = mLayouts.get(layoutName);
            if (layouts != null) {
                Util.sortLayout(layouts);
                ArrayList<String> javaNames = new ArrayList<>();
                for (File file : layouts) {
                    LayoutReader reader = new LayoutReader(file, layoutName, mFiler, mPackageName, mGroupId);
                    fileName = reader.parse();
                    javaNames.add(fileName);
                    mMap.put(layoutId, fileName);
                }

                MapWriter mapWriter = new MapWriter(mGroupId, layouts, javaNames, mFiler);
                mapWriter.write();
            }
        }
        if (fileName != null) {
            mTranslateMap.put(fileName + ".java", layoutName + ".xml");
        }
        return fileName;
    }

    private HashMap<String, ArrayList<File>> scanLayouts(File root) {
        return new FileFilter(root)
                .include("layout")
                .include("layout-land")
                .include("layout-v28")
                .include("layout-v27")
                .include("layout-v26")
                .include("layout-v25")
                .include("layout-v24")
                .include("layout-v23")
                .include("layout-v22")
                .include("layout-v21")
                .include("layout-v20")
                .include("layout-v19")
                .include("layout-v18")
                .include("layout-v17")
                .include("layout-v16")
                .include("layout-v15")
                .include("layout-v14")
                .exclude("build")
                .exclude("java")
                .exclude("libs")
                .exclude("mipmap")
                .exclude("values")
                .exclude("drawable")
                .exclude("anim")
                .exclude("color")
                .exclude("menu")
                .exclude("raw")
                .exclude("xml")
                .filter();
    }

    public Style getStyle(String name) {
        if (name == null) {
            return null;
        }
        if (mStyles == null) {
            mStyles = new HashMap();
            new StyleReader(mRootFile, mStyles).parse();
        }
        return mStyles.get(name);
    }

    public void printTranslate() {
        if (mTranslateMap.size() == 0) {
            return;
        }
        int maxTab = 0;
        int tabCount;
        for (String layoutName : mTranslateMap.values()) {
            tabCount = layoutName.length() / 4 + 1;
            if (tabCount > maxTab) {
                maxTab = tabCount;
            }
        }

        StringBuilder stringBuilder;
        String layoutName;
        for (String javaName : mTranslateMap.keySet()) {
            layoutName = mTranslateMap.get(javaName);
            tabCount = layoutName.length() / 4;
            stringBuilder = new StringBuilder(layoutName);
            if (tabCount < maxTab) {
                for (int j = 0; j < maxTab - tabCount; j++) {
                    stringBuilder.append("\t");
                }
            }
            Log.w(String.format("%s->\t%s", stringBuilder.toString(), javaName));
        }
        mTranslateMap.clear();
    }


    private File getRootFile() {
        try {
            JavaFileObject fileObject = mFiler.createSourceFile("bb");
            String path = URLDecoder.decode(fileObject.toUri().toString(), "utf-8");
            String preFix = "file:/";
            if (path.startsWith(preFix)) {
                path = path.substring(preFix.length() - 1);
            }
            File file = new File(path);
            while (!file.getName().equals("build")) {
                file = file.getParentFile();
            }
            Log.w("getRootFile() called file=" + file.getParentFile().getAbsolutePath());
            return file.getParentFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private void findPackageName() {
        String sep = File.separator;
        File androidMainfest = new File(mRootFile + sep + "src" + sep + "main" + sep + "AndroidManifest.xml");
        Log.w("findPackageName() called androidMainfest=" + androidMainfest.getAbsolutePath());
        Log.w("findPackageName() called androidMainfest.exists=" + androidMainfest.exists());
        if (!androidMainfest.exists()) {
            androidMainfest = new File(mRootFile + sep + "build" + sep + "intermediates" + sep + "manifests"
                    + sep + "full" + sep + "debug" + sep + "AndroidManifest.xml");
            Log.w("findPackageName() called androidMainfest1=" + androidMainfest.getAbsolutePath());
            Log.w("findPackageName() called androidMainfest1.exists=" + androidMainfest.exists());

        }
        SAXParser parser = null;
        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(androidMainfest, new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    super.startElement(uri, localName, qName, attributes);
                    if (qName.equals("manifest")) {
                        mPackageName = attributes.getValue("package");
                        Log.w("findPackageName() called mPackageName=" + mPackageName);
                    }
                }
            });
            parser.reset();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (parser != null) {
                    parser.reset();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

    }

    private HashMap<String, Integer> getR() {
        HashMap<String, Integer> map = new HashMap<>();
        File rFile = getRFile();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(rFile));
            String line;
            boolean layoutStarted = false;
            while ((line = reader.readLine()) != null) {
                if (rFile.getName().endsWith(".txt")){
                    if (line.contains("int layout")){
                        line = line.substring(line.indexOf("layout") + 7).trim();
                        String[] lineSplit = line.split(" ");
                        int id = Integer.decode(lineSplit[1]);
                        Log.w( "getR() called  layout="+lineSplit[0]+"  ,id="+lineSplit[1]);
                        map.put(lineSplit[0], id);
                        mRJavaId.put(id, lineSplit[0]);
                    }

                }else {
                    if (line.contains("public static final class layout")) {
                        layoutStarted = true;
                    } else if (layoutStarted) {
                        if (line.contains("}")) {
                            break;
                        } else {
                            line = line.substring(line.indexOf("int") + 3, line.indexOf(";"))
                                    .replaceAll(" ", "").trim();
                            String[] lineSplit = line.split("=");
                            int id = Integer.decode(lineSplit[1]);
                            map.put(lineSplit[0], id);
                            mRJavaId.put(id, lineSplit[0]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.close(reader);
        }
        return map;
    }

    private File getRFile() {
        String sep = File.separator;
        File rFile = null;
        try {
            String rJavaPath = mPackageName.replace(".", sep) + sep + "R.java";
            Log.w("getRFile() called rJavaPath=" + rJavaPath);
            JavaFileObject filerSourceFile = mFiler.createSourceFile("test");
            String path = filerSourceFile.toUri().getPath();
            Log.w("getRFile() called path=" + path);


            File x2jDir = new File(path.substring(0, path.indexOf("/build/")) + "/build/generated/source/x2j/main/");
            Log.w("getRFile() called x2jDir=" + x2jDir);


            rFile = new File(x2jDir, rJavaPath.replace("R.java", "X2J_R.java"));
            Log.w("getRFile() called rFile=" + rFile.getAbsolutePath());
            Log.w("getRFile() called rFile.exists()=" + rFile.exists());
            if (rFile.exists()) {
                return rFile;
            }



            String basePath = path.replace("/apt/", "/r/")
                    .replace("/kapt/", "/r/")
                    .replace("test.java", "");
            rFile = new File(basePath, rJavaPath);
            Log.w("getRFile() called rFile1=" + rFile.getAbsolutePath());
            Log.w("getRFile() called rFile1.exists()=" + rFile.exists());
            if (!rFile.exists()) {
                Log.w("first R File not found, path1 = " + rFile.getAbsolutePath());
                basePath = basePath
                        .replace(sep, "/")
                        .replace("/generated/ap_generated_sources/", "/generated/not_namespaced_r_class_sources/")
                        .replace("/generated/source/", "/generated/not_namespaced_r_class_sources/")
//                        .replace("/debug/", "Debug/")
//                        .replace("/release/", "Release/")
                        .replace("/out/", "/r/");

                rFile = new File(basePath, rJavaPath);
                Log.w("getRFile() called rFile2=" + rFile.getAbsolutePath());
                Log.w("getRFile() called rFile2.exists()=" + rFile.exists());

            }
            ///Users/lidongjun/project/X2C/app/build/generated/source/kapt/debug/test.java
            ///Users/lidongjun/project/X2C/app/build/intermediates/runtime_symbol_list/debug/R.txt
            String basePath1 = path
                    .replace("generated/source/kapt", "intermediates/runtime_symbol_list")
                    .replace("generated/source/kapt", "intermediates/compile_symbol_list")
                    .replace("test.java", "R.txt");
            rFile = new File(basePath1);
            Log.w("getRFile() called rFile3=" + rFile.getAbsolutePath());
            Log.w("getRFile() called rFile3.exists()=" + rFile.exists());
            if (!rFile.exists()) {
                Log.w("first R File not found, path3 = " + rFile.getAbsolutePath());
                basePath1 = path
                        .replace("generated/source/kapt", "intermediates/compile_symbol_list")
                        .replace("test.java", "R.txt");
                rFile = new File(basePath1);
                Log.w("getRFile() called rFile4=" + rFile.getAbsolutePath());
                Log.w("getRFile() called rFile4.exists()=" + rFile.exists());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (rFile == null || !rFile.exists()) {
            Log.e("X2C not find R.java!!!, path = " + rFile);
        }
        return rFile;
    }

    public HashMap<String, Attr> getAttrs() {
        return mAttrs;
    }

    private boolean isLibrary() {
        File file = new File(mRootFile, "build.gradle");
        BufferedReader fileReader = null;
        boolean isLibrary = true;
        try {
            fileReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = fileReader.readLine()) != null) {
                int indexOfApplication = line.indexOf("'com.android.application'");
                if (indexOfApplication >= 0) {
                    int indexOfNote = line.indexOf("//");
                    if (indexOfNote == -1 || indexOfNote > indexOfApplication) {
                        isLibrary = false;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.close(fileReader);
        }
        return isLibrary;
    }

}
