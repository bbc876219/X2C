package com.zhangyue.x2c.demo;

import android.app.Activity;
import android.os.Bundle;


import com.bbc876219.lib.xml2code.Xml2CodeHelper;
import com.bbc876219.lib.xml2code.annotation.Xml2Code;


import butterknife.OnClick;
import plugin.zhangyue.com.sub_module.R;

/**
 * @author：chengwei 2018/9/5
 * @description
 */
@Xml2Code(layouts = "sub_activity")
public class ActivitySubModule extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Xml2CodeHelper.setContentView(this, R.layout.sub_activity);
    }

}
