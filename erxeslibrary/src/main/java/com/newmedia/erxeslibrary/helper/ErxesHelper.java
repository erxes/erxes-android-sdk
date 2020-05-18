package com.newmedia.erxeslibrary.helper;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.model.Messengerdata;
import com.newmedia.erxeslibrary.ui.ErxesActivity;
import com.newmedia.erxeslibrary.utils.DataManager;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ErxesHelper {
    private static DataManager dataManager;
    private static Config config;
    static public int[] backgrounds = {R.drawable.bitmap1, R.drawable.bitmap2, R.drawable.bitmap3, R.drawable.bitmap4};

    public static void Init(Context context) {
        dataManager = DataManager.getInstance(context);
        config = Config.getInstance(context);
    }

    static public void load_uiOptions(Json js) {
        if (js == null)
            return;
        String color;
        color = js.getString("color");
        dataManager.setData(DataManager.COLOR, color);
        if (color != null)
            config.colorCode = Color.parseColor(color);
        else {
            config.colorCode = Color.parseColor("#5629B6");
        }
        dataManager.setData("wallpaper", js.getString("wallpaper"));
        dataManager.setData("videoCallUsageStatus", js.getBoolean("videoCallUsageStatus"));
        config.wallpaper = js.getString("wallpaper");
        config.videoCallUsageStatus = js.getBoolean("videoCallUsageStatus");
    }

    public static void load_messengerData(Json js) {
        if (js == null)
            return;
        config.messengerdata = Messengerdata.convert(js, config.language);
    }

    public static Point display_configure(AppCompatActivity context, View container, String color) {
        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y * 8 / 10;

        context.getWindow().setLayout(width, WindowManager.LayoutParams.MATCH_PARENT);
        Window window = context.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor(color)));
        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);

        if (!(context instanceof ErxesActivity)) {
            container.getLayoutParams().height = height;
            container.requestLayout();
        }
        return size;
    }

    public static void changeLanguage(Context context, String language) {
        if (!TextUtils.isEmpty(language)) {
            Configuration config = new android.content.res.Configuration();
            config.locale = new Locale(language);
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        }
    }

    public static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a");
}
