package com.newmedia.erxeslibrary.helper;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;

import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.newmedia.erxeslibrary.utils.DataManager;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.model.Messengerdata;
import com.newmedia.erxeslibrary.ui.ErxesActivity;
import com.newmedia.erxeslibrary.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ErxesHelper {
    static DataManager dataManager;
    static Config config;
    static public int[] backgrounds = {R.drawable.bitmap1, R.drawable.bitmap2, R.drawable.bitmap3, R.drawable.bitmap4};

    static public final Map<String, Integer> ICON_MAP =
            Collections.unmodifiableMap(new HashMap<String, Integer>() {{
                put("paste", R.drawable.paste);
                put("flag", R.drawable.flag);
                put("laptop", R.drawable.laptop);
                put("alarm", R.drawable.knowledge_alarm);
                put("briefcase", R.drawable.knowledge_briefcase);
                put("cloud-computing", R.drawable.knowledge_cloud_computing);
                put("earthgrid", R.drawable.knowledge_earthgrid);
                put("diagram", R.drawable.knowledge_diagram);
                put("compass", R.drawable.knowledge_compass);
                put("idea", R.drawable.knowledge_idea);
                put("diamond", R.drawable.knowledge_diamond);
                put("piggybank", R.drawable.piggy_bank);
                put("piechart", R.drawable.knowledge_pie_chart);
                put("scale", R.drawable.knowledge_scale);
                put("megaphone", R.drawable.knowledge_megaphone);
                put("tools", R.drawable.knowledge_tools);
                put("umbrella", R.drawable.knowledge_umbrella);
                put("bar-chart", R.drawable.knowledge_bar_chart);
                put("star", R.drawable.knowledge_star);
                put("head-1", R.drawable.knowledge_head);
                put("settings", R.drawable.knowledge_settings);
                put("users", R.drawable.knowledge_users);
                put("paintpalette", R.drawable.knowledge_paint_palette);
                put("stamp", R.drawable.knowledge_stamp);
                put("phone-call", R.drawable.knowledge_phone_call);
                put("magicwand", R.drawable.knowledge_magic_wand);
                put("calculator", R.drawable.knowledge_calculator);
                put("home", R.drawable.knowledge_home);
                put("puzzle", R.drawable.knowledge_puzzle);
                put("medal", R.drawable.knowledge_medal);
                put("calendar", R.drawable.knowledge_calendar);
                put("like", R.drawable.knowledge_like);
                put("book", R.drawable.knowledge_book);
                put("clipboard", R.drawable.clipboard);
                put("computer", R.drawable.knowledge_computer);
                put("folder", R.drawable.knowledge_folder);

            }});

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
        color = js.getString("wallpaper");
        dataManager.setData("wallpaper", color);

    }

    static public void load_messengerData(Json js) {
        if (js == null)
            return;
        dataManager.setMessengerData(js.toString());
        config.messengerdata = Messengerdata.convert(js, config.language);
    }

    static public Point display_configure(AppCompatActivity context, View container, String color) {


        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = (int) (size.y * 0.8);

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
    static public void changeLanguage(Context context,String language){
        Configuration config = new android.content.res.Configuration();
        config.locale  = new Locale(language);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }
}
