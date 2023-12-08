package com.zhangyue.we.x2c.demo.act;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;

import com.bbc876219.lib.xml2code.Xml2CodeHelper;
import com.bbc876219.lib.xml2code.annotation.Xml2Code;

import com.zhangyue.we.x2c.demo.R;

/**
 * @author:chengwei 2018/8/24
 * @description
 */
@Xml2Code(layouts = "activity_main_inter")
public class ActivityX2C extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Xml2CodeHelper.setContentView(this, R.layout.activity_main_inter);

        findViewById(R.id.iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ViewStub stub = (ViewStub) findViewById(R.id.stub);
        stub.setLayoutResource(R.layout.stub_layout);
        stub.inflate();
    }
}
