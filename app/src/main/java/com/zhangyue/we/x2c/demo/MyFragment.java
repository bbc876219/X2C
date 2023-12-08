package com.zhangyue.we.x2c.demo;

import android.app.Fragment;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.bbc876219.lib.xml2code.Xml2CodeHelper;
import com.bbc876219.lib.xml2code.annotation.Xml2Code;


/**
 * @author:chengwei 2018/8/28
 * @description
 */
@Xml2Code(layouts = "fragmetn_layout")
public class MyFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return Xml2CodeHelper.inflate(inflater.getContext(), R.layout.fragmetn_layout, container, false);
    }

}
