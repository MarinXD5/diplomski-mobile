package com.example.myapplication;

import android.os.Bundle;

public class AccountActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildLayout(R.layout.activity_account);
    }
}
