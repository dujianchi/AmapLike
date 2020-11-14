package com.orange.amaplike;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.multidex.MultiDexApplication;

import java.util.List;

import butterknife.ButterKnife;
import cn.dujc.core.app.Core;
import cn.dujc.core.initializer.content.IRootViewSetup;
import cn.dujc.core.initializer.toolbar.IToolbar;
import cn.dujc.core.ui.IBaseUI;
import cn.dujc.core.ui.StatusBarPlaceholder;

public class App extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Core.init(this
                , new Class[]{ToolbarHelper.class}
                , null
                , null
                , null
                , RootViewSetup.class);
    }

    public static class RootViewSetup implements IRootViewSetup {

        @Override
        public void setup(Object target, View rootView) {
            ButterKnife.bind(target, rootView);
        }
    }

    public static class ToolbarHelper implements IToolbar {

        @Override
        public View normal(ViewGroup parent) {
            return new StatusBarPlaceholder(parent.getContext());
        }

        @Override
        public int statusBarColor(Context context) {
            return R.color.colorPrimary;
        }

        @Override
        public int statusBarMode() {
            return IToolbar.AUTO;
        }

        @Override
        public int toolbarStyle() {
            return IToolbar.LINEAR;
        }

        @Override
        public List<Class<? extends IBaseUI>> include() {
            return null;
        }

        @Override
        public List<Class<? extends IBaseUI>> exclude() {
            return null;
        }
    }
}
