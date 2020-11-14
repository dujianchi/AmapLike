package com.orange.amaplike.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.collection.LruCache;

import com.orange.amaplike.R;

import cn.dujc.core.util.BitmapUtil;
import cn.dujc.core.util.StringUtil;

public class ImageUtil {

    private static final LruCache<Double, Bitmap> CACHE = new LruCache<>(1024 * 1024);

    public static Bitmap get(Context context, Double limit) {
        Bitmap bitmap = CACHE.get(limit);
        if (bitmap == null) {
            View view = LayoutInflater.from(context).inflate(R.layout.view_limit, null);
            TextView tvLimit = view.findViewById(R.id.tv_limit);
            tvLimit.setText(StringUtil.concat(limit, "m"));
            bitmap = BitmapUtil.viewToBitmap(view);
            CACHE.put(limit, bitmap);
        }
        return bitmap;
    }
}
