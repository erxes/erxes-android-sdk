package com.newmedia.erxeslibrary.helper;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

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
        String color, textColor;
        color = js.getString("color");
        textColor = js.getString("textColor");
        dataManager.setData(DataManager.COLOR, color);
        dataManager.setData(DataManager.TEXTCOLOR, textColor);
        if (color != null)
            config.colorCode = Color.parseColor(color);
        else {
            config.colorCode = Color.parseColor("#5629B6");
        }
        if (textColor != null) {
            try {
                config.textColorCode = Color.parseColor(textColor);
            } catch (Exception e) {
                e.printStackTrace();
                config.textColorCode = config.getInColor(config.colorCode);
            }
        } else {
            config.textColorCode = config.getInColor(config.colorCode);
        }
        dataManager.setData("wallpaper", js.getString("wallpaper"));
        config.wallpaper = js.getString("wallpaper");
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
//        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setBackgroundDrawableResource(R.color.black_10);
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
            Configuration config = new Configuration(context.getResources().getConfiguration());
            config.setLocale(new Locale(language));

            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        }
    }

    public static Resources getLocalizedResources(Context context, String language) {
        if (!TextUtils.isEmpty(language)) {
            Configuration config = context.getResources().getConfiguration();
            config = new Configuration(config);
            config.setLocale(new Locale(language));
            Context localizedContext = context.createConfigurationContext(config);
            return localizedContext.getResources();
        }
        return context.getResources();
    }

    public static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a");
}
