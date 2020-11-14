package com.orange.amaplike;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import butterknife.OnClick;
import cn.dujc.core.ui.BaseActivity;

public class MainActivity extends BaseActivity {

    @OnClick({R.id.goin_text})
    public void onViewclick(View view) {
        if (view.getId() == R.id.goin_text) {
            startActivity(new Intent(MainActivity.this, RoutePlanActivity.class));
        }
    }

    @Override
    public int getViewId() {
        return R.layout.activity_main;
    }

    @Override
    public void initBasic(Bundle savedInstanceState) {

    }
}
