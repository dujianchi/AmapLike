package com.orange.amaplike.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.orange.amaplike.R;

import java.util.Arrays;
import java.util.List;

import cn.dujc.core.ui.BaseDialog;
import cn.dujc.widget.wheelpicker.WheelPicker;

public class PickDialog extends BaseDialog {

    public static interface Callback {
        void onPicked(String name, double val);
    }

    public PickDialog(Context context, Callback callback) {
        super(context);
        mCallback = callback;
    }

    private final Callback mCallback;
    private WheelPicker mWheelPicker0;
    private WheelPicker mWheelPicker1;
    private Button mBtnOk;

    @Override
    public int getViewId() {
        return R.layout.dialog_pick;
    }

    @Override
    public void initBasic(Bundle savedInstanceState) {

        mWheelPicker0 = findViewById(R.id.wheel_picker0);
        mWheelPicker1 = findViewById(R.id.wheel_picker1);
        mBtnOk = findViewById(R.id.btn_ok);

        List<String> list0 = Arrays.asList("北京牌", "北奔重卡", "北汽威旺", "奔驰", "比亚迪");
        mWheelPicker0.setData(list0);
        List<String> list1 = Arrays.asList("1030系列", "北京1041", "北奔V3ET", "NG80", "Actros");
        mWheelPicker1.setData(list1);

        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = list0.get(mWheelPicker0.getCurrentItemPosition());
                s += list1.get(mWheelPicker1.getCurrentItemPosition());
                mCallback.onPicked(s, 4 +
                        (mWheelPicker0.getCurrentItemPosition() + mWheelPicker1.getCurrentItemPosition()) / 10.0);
                dismiss();
            }
        });
    }
}
