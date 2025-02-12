package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;

public class TemplatesActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setChildLayout(R.layout.templates_activity);

    }
}
