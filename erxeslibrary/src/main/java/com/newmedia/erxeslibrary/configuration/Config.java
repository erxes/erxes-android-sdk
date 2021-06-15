package com.newmedia.erxeslibrary.configuration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.erxes.io.opens.type.FieldValueInput;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
import com.mikepenz.iconics.Iconics;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.GenericFont;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.connection.service.ListenerService;
import com.newmedia.erxeslibrary.model.FormConnect;
import com.newmedia.erxeslibrary.model.KnowledgeBaseTopic;
import com.newmedia.erxeslibrary.model.Messengerdata;
import com.newmedia.erxeslibrary.model.User;
import com.newmedia.erxeslibrary.ui.ErxesActivity;
import com.newmedia.erxeslibrary.ui.faq.FaqActivity;
import com.newmedia.erxeslibrary.ui.faq.FaqDetailActivity;
import com.newmedia.erxeslibrary.ui.message.MessageActivity;
import com.newmedia.erxeslibrary.utils.DataManager;
import com.newmedia.erxeslibrary.utils.ListTagHandler;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Config {

    String host3100 = null;
    String host3300 = null;
    public String hostUpload = null;
    public String customerId;
    public String integrationId;
    public String language, wallpaper;
    public Messengerdata messengerdata;
    public int colorCode, textColorCode;
    public String conversationId = null;
    public String brandCode;
    private final boolean isMessengerOnline = false;
    public boolean showVideoCallRequest = false;
    private static DataManager dataManager;
    private Activity activityConfig;
    public Context context;
    private ErxesRequest erxesRequest;
    private static Config config;
    public FormConnect formConnect = null;
    public List<FieldValueInput> fieldValueInputs = new ArrayList<>();
    public KnowledgeBaseTopic knowledgeBaseTopic = null;
    public List<User> supporters = new ArrayList<>();
    public List<String> conversationIds = new ArrayList<>();

    public boolean requireAuth = false;
    public Intent intent;
    public boolean isOnline;
    public String brandDescription, brandName, serverTime = "";

    public String email, phone, companyData, data, code;

    private Config(Context context) {
        this.context = context;
        dataManager = DataManager.getInstance(context);
    }

    public static Config getInstance(Context context) {
        if (config == null) {
            config = new Config(context);
            config.erxesRequest = ErxesRequest.getInstance(config);
            if (config.host3100 != null)
                config.erxesRequest.set_client();
        }
        config.email = dataManager.getDataS(DataManager.EMAIL);
        config.phone = dataManager.getDataS(DataManager.PHONE);
        config.companyData = dataManager.getDataS(DataManager.COMPANYDATA);
        config.data = dataManager.getDataS(DataManager.DATA);
        config.code = dataManager.getDataS(DataManager.CODE);
        return config;
    }

    public boolean isUser() {
        return email != null || phone != null;
    }

    public String convertDatetime(long createDate) {
        long diffTime = Calendar.getInstance().getTimeInMillis() - createDate;

        diffTime = diffTime / 1000;
        long weeks = diffTime / 604800;
        long days = (diffTime % 604800) / 86400;
        long hours = ((diffTime % 604800) % 86400) / 3600;
        long minutes = (((diffTime % 604800) % 86400) % 3600) / 60;
        long seconds = (((diffTime % 604800) % 86400) % 3600) % 60;
        if (language == null || language.equalsIgnoreCase("en")) {
            if (weeks > 0) {
                return ("" + weeks + " weeks ago");
            } else if (days > 0) {
                return ("" + days + " d");
            } else if (hours > 0) {
                return ("" + hours + " h");
            } else if (minutes > 0) {
                return ("" + minutes + " m");
            } else {
                return ("" + seconds + " s");
            }
        } else {
            if (weeks > 0) {
                return ("" + weeks + " 7-хоногийн өмнө");
            } else if (days > 0) {
                return ("" + days + " өдрийн өмнө");
            } else if (hours > 0) {
                return ("" + hours + " цагийн өмнө");
            } else if (minutes > 0) {
                return ("" + minutes + " минутын өмнө");
            } else {
                return ("" + seconds + " секундын өмнө");
            }
        }
    }

    public String MessageDatetime(String createDateS) {
        long createDate;
        try {
            createDate = Long.valueOf(createDateS);
        } catch (NumberFormatException e) {
            return "";
        }


        Date date = new Date();
        date.setTime(createDate);

        long diffTime = Calendar.getInstance().getTimeInMillis() - createDate;

        diffTime = diffTime / 1000;
        long weeks = diffTime / 604800;
        long days = (diffTime % 604800) / 86400;
        long hours = ((diffTime % 604800) % 86400) / 3600;
        long minutes = (((diffTime % 604800) % 86400) % 3600) / 60;
        long seconds = (((diffTime % 604800) % 86400) % 3600) % 60;
        SimpleDateFormat format =
                new SimpleDateFormat("h:mm a");
        SimpleDateFormat format2 =
                new SimpleDateFormat("EEE h:mm a");
        SimpleDateFormat format3 =
                new SimpleDateFormat("MMM d,h:mm a");
        if (language.equalsIgnoreCase("mn")) {
            format3 = new SimpleDateFormat("MMM сарын d,HH:mm");
            format2 = format3;
        }
        if (weeks > 0) {
            return format3.format(date);
        } else if (days > 0) {
            return format2.format(date);
        } else {
            return format.format(date);
        }
    }

    public String now() {
        Date date = new Date();
        SimpleDateFormat format =
                new SimpleDateFormat("yyyy оны MM сарын d, HH:mm");
        SimpleDateFormat format2 =
                new SimpleDateFormat("MMM dd, yyyy h:mm a");

        if (this.language.equalsIgnoreCase("mn")) {
            return format.format(date);
        } else {
            return format2.format(date);
        }
    }

    public String now(long serverTime) {
        Date date = new Date(serverTime);
        SimpleDateFormat format =
                new SimpleDateFormat("yyyy оны MM сарын d, HH:mm");
        SimpleDateFormat format2 =
                new SimpleDateFormat("MMM dd, yyyy h:mm a");

        if (this.language.equalsIgnoreCase("mn")) {
            return format.format(date);
        } else {
            return format2.format(date);
        }
    }

    public String FullDate(String createDateS) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        SimpleDateFormat format =
                new SimpleDateFormat("yyyy оны MM сарын d, HH:mm");
        SimpleDateFormat format2 =
                new SimpleDateFormat("MMM dd / yyyy h:mm a");
        try {
            if (this.language.equalsIgnoreCase("mn")) {
                return format.format(sdf.parse(createDateS));
            } else {
                return format2.format(sdf.parse(createDateS));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return createDateS;
        }
    }

    public String conversationDate(long createdDate) {
        Date date = new Date();
        date.setTime(createdDate);

        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("h:mm a");
        return simpleDateFormat.format(date);
    }

    public void setActivityConfig(Activity activity) {
        activityConfig = activity;
    }

    public boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    private void Init(String brandCode, String host3100, String host3300, String hostUpload, String email, String phone, String companyData, String data, String code) {
        this.host3100 = host3100;
        this.host3300 = host3300;
        this.hostUpload = hostUpload;
        this.brandCode = brandCode;
        this.email = email;
        this.phone = phone;
        this.companyData = companyData;
        this.data = data;
        this.code = code;
        dataManager.setData("host3100", this.host3100);
        dataManager.setData("host3300", this.host3300);
        dataManager.setData("hostUpload", this.hostUpload);
        dataManager.setData("BRANDCODE", this.brandCode);
        dataManager.setData(DataManager.EMAIL, this.email);
        dataManager.setData(DataManager.PHONE, this.phone);
        dataManager.setData(DataManager.COMPANYDATA, this.companyData);
        dataManager.setData(DataManager.DATA, this.data);
        dataManager.setData(DataManager.CODE, this.code);
        if (dataManager.getDataS("host3300") != null)
            intent = new Intent(context, ListenerService.class);
        LoadDefaultValues();
        erxesRequest.set_client();
    }

    public void Start() {
        checkRequired(isUser());
    }

    public void initActivity() {
        initializeIcon();
        initializeFresco();

        Intent a = new Intent(context, ErxesActivity.class);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(a);
    }

    private void checkRequired(boolean isUser) {
        if (isUser) erxesRequest.setConnect(true, isUser());
        else erxesRequest.getIntegration();
    }

    public void LoadDefaultValues() {
        dataManager = DataManager.getInstance(context);
        host3100 = dataManager.getDataS("host3100");
        host3300 = dataManager.getDataS("host3300");
        hostUpload = dataManager.getDataS("hostUpload");
        brandCode = dataManager.getDataS("BRANDCODE");
        email = dataManager.getDataS("EMAIL");
        phone = dataManager.getDataS("PHONE");
        companyData = dataManager.getDataS("COMPANYDATA");
        data = dataManager.getDataS("DATA");

        customerId = dataManager.getDataS(DataManager.CUSTOMERID);
        integrationId = dataManager.getDataS(DataManager.INTEGRATIONID);
        String color = dataManager.getDataS(DataManager.COLOR);
        String textColor = dataManager.getDataS(DataManager.TEXTCOLOR);
        if (color != null)
            colorCode = Color.parseColor(color);
        else
            colorCode = Color.parseColor("#5629B6");
        if (textColor != null) {
            try {
                textColorCode = Color.parseColor(textColor);
            } catch (Exception e) {
                e.printStackTrace();
                textColorCode = getInColor(colorCode);
            }
        } else
            textColorCode = getInColor(colorCode);
        wallpaper = dataManager.getDataS("wallpaper");
        language = dataManager.getDataS(DataManager.LANGUAGE);
        changeLanguage(language);

    }

    public CharSequence getHtml(String content) {

        if (content != null) {
            Spanned htmlDescription;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                htmlDescription = Html.fromHtml(
                        customizeListTags(content),
                        Html.FROM_HTML_MODE_LEGACY,
                        null,
                        new ListTagHandler());
            } else {
                htmlDescription = Html.fromHtml(
                        customizeListTags(content),
                        null,
                        new ListTagHandler());
            }
            String descriptionWithOutExtraSpace = htmlDescription.toString().trim();
            return htmlDescription.subSequence(0, descriptionWithOutExtraSpace.length());
        }
        return null;
    }

    private String customizeListTags(@Nullable String content) {
        String mContent = content;
        if (mContent == null) {
            return null;
        }
        mContent = mContent.replaceAll("<ul", "<rxxUL");
        mContent = mContent.replaceAll("</ul>", "</rxxUL>");
        mContent = mContent.replaceAll("<ol", "<rxxOL");
        mContent = mContent.replaceAll("</ol>", "</rxxOL>");
        mContent = mContent.replaceAll("<dd", "<rxxDD");
        mContent = mContent.replaceAll("</dd>", "</rxxDD>");
        mContent = mContent.replaceAll("<li", "<rxxLI");
        mContent = mContent.replaceAll("</li>", "</rxxLI>");
        return mContent;
    }

    public boolean isLoggedIn() {
        return dataManager.getDataS(DataManager.CUSTOMERID) != null;
    }

    public void Logout(Activity activity) {
        customerId = null;
        dataManager.setData(DataManager.CUSTOMERID, null);
        dataManager.setData(DataManager.INTEGRATIONID, null);
        dataManager.setData(DataManager.EMAIL, null);
        dataManager.setData(DataManager.PHONE, null);
        if (activity.getClass().getName().contains("ConversationListActivity")) {
            if (activityConfig.getClass().getName().contains("ConversationListActivity")) {
                activity.startActivity(new Intent(activity, ErxesActivity.class));
                activity.finish();
            } else if (activityConfig.getClass().getName().contains("FaqActivity")) {
                ((FaqActivity) activityConfig).logout();
            } else if (activityConfig.getClass().getName().contains("FaqDetailActivity")) {
                ((FaqDetailActivity) activityConfig).logout();
            } else {
                ((MessageActivity) activityConfig).logout();
            }
        } else {
            activity.finish();
        }
    }

    public void changeLanguage(String language) {
        if (language == null || language.equalsIgnoreCase(""))
            return;

        this.language = language.substring(0, 2);
        dataManager.setData(DataManager.LANGUAGE, this.language);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(new Locale(language));

        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    public static class Builder {
        private final String brand;
        private String apiHost;
        private String graphqlApiHost;
        private String subscriptionHost;
        private String uploadHost;
        private String protocal = "https";

        private String email;
        private String phone;
        private String companyData;
        private String data;
        private String code;

        public Builder(@NonNull String brand) {
            this.brand = brand;

        }

        public Builder setApiHost(String apiHost) {
            if(apiHost != null) {
                if (apiHost.contains("://")) {
                    this.protocal = apiHost.substring(0, apiHost.indexOf("://"));
                    this.apiHost = apiHost.substring(apiHost.indexOf("://") + 3);
                } else {
                    this.apiHost = apiHost;
                }

                if (this.apiHost.contains("/") && String.valueOf(this.apiHost.charAt(this.apiHost.length() - 1)).equals("/")) {
                    this.apiHost = this.apiHost.substring(0, this.apiHost.length() - 1);
                }

                setGqlApiHost();
                setSubscriptionHost();
                setUploadHost();
            }
            return this;
        }

        public Builder setOrganizationName(String organizationName) {
            if(organizationName != null) {
                this.apiHost = organizationName + ".app.erxes.io/api";
                setGqlApiHost();
                setSubscriptionHost();
                setUploadHost();
            }
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder setCompanyData(String companyData) {
            this.companyData = companyData;
            return this;
        }

        public Builder setData(String data) {
            this.data = data;
            return this;
        }

        public Builder setCode(String code) {
            this.code = code;
            return this;
        }

        private void setGqlApiHost() {
            this.graphqlApiHost = protocal + "://" + this.apiHost + "/graphql";
        }

        private void setSubscriptionHost() {
            if (protocal.contains("https"))
                this.subscriptionHost = "wss://" + this.apiHost + "/subscriptions";
            else this.subscriptionHost = "ws://" + this.apiHost + "/subscriptions";
        }

        private void setUploadHost() {
            this.uploadHost = protocal + "://" + this.apiHost + "/upload-file";
        }

        public Config build(Context context) {
            Config config = Config.getInstance(context);
            config.Init(
                    this.brand,
                    this.graphqlApiHost,
                    this.subscriptionHost,
                    this.uploadHost,
                    this.email,
                    this.phone,
                    this.companyData,
                    this.data,
                    this.code
            );
            return config;
        }
    }

    private void initializeFresco() {
        ImagePipelineConfig imagePipelineConfig = ImagePipelineConfig.newBuilder(context)
                .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
                .setResizeAndRotateEnabledForNetwork(true)
                .setDiskCacheEnabled(true)
                .setDownsampleEnabled(true)
                .build();
        if (!Fresco.hasBeenInitialized())
            Fresco.initialize(context, imagePipelineConfig);
    }

    private void initializeIcon() {
        Iconics.init(context);

        GenericFont erxesSDKGF = new GenericFont("rxx", "fonts/erxes.ttf");
        erxesSDKGF.registerIcon("send", '\ueb09');
        erxesSDKGF.registerIcon("cancel", '\ue80d');
        erxesSDKGF.registerIcon("envelope_alt", '\ueae2');
        erxesSDKGF.registerIcon("phone_alt", '\uec1d');
        erxesSDKGF.registerIcon("plus_1", '\uec2d');
        erxesSDKGF.registerIcon("logout", '\ue84f');
        erxesSDKGF.registerIcon("leftarrow", '\ue84a');
        erxesSDKGF.registerIcon("attach", '\ueb3e');

        erxesSDKGF.registerIcon("alarm", '\ue802');
        erxesSDKGF.registerIcon("briefcase", '\ue809');
        erxesSDKGF.registerIcon("cloudcomputing", '\ue816');
        erxesSDKGF.registerIcon("earthgrid", '\ue829');
        erxesSDKGF.registerIcon("diagram", '\ue820');
        erxesSDKGF.registerIcon("compass", '\ue817');
        erxesSDKGF.registerIcon("idea", '\ue840');
        erxesSDKGF.registerIcon("diamond", '\ue821');
        erxesSDKGF.registerIcon("piggybank", '\ue86a');
        erxesSDKGF.registerIcon("piechart", '\ue869');
        erxesSDKGF.registerIcon("scale", '\ue87e');
        erxesSDKGF.registerIcon("megaphone", '\ue851');
        erxesSDKGF.registerIcon("tools", '\ue895');
        erxesSDKGF.registerIcon("umbrella", '\ue899');
        erxesSDKGF.registerIcon("bar_chart", '\ue8aa');
        erxesSDKGF.registerIcon("star", '\ue88a');
        erxesSDKGF.registerIcon("head_1", '\ue8cd');
        erxesSDKGF.registerIcon("settings", '\ue880');
        erxesSDKGF.registerIcon("users", '\ue927');
        erxesSDKGF.registerIcon("paintpalette", '\ue940');
        erxesSDKGF.registerIcon("stamp", '\ue94f');
        erxesSDKGF.registerIcon("flag", '\ue837');
        erxesSDKGF.registerIcon("phone_call", '\ue8ed');
        erxesSDKGF.registerIcon("laptop", '\ue8d3');
        erxesSDKGF.registerIcon("dashboard", '\ue81f');
        erxesSDKGF.registerIcon("calculator", '\ue80b');
        erxesSDKGF.registerIcon("home", '\ue83e');
        erxesSDKGF.registerIcon("puzzle", '\ue872');
        erxesSDKGF.registerIcon("medal", '\ue850');
        erxesSDKGF.registerIcon("calendar", '\ue80c');

        erxesSDKGF.registerIcon("like", '\ue84b');
        erxesSDKGF.registerIcon("book", '\ue8ac');
        erxesSDKGF.registerIcon("clipboard", '\ue8b3');
        erxesSDKGF.registerIcon("computer", '\ue8bb');
        erxesSDKGF.registerIcon("paste", '\ue861');
        erxesSDKGF.registerIcon("folder", '\ue838');
        erxesSDKGF.registerIcon("image_v", '\ueb90');
        erxesSDKGF.registerIcon("videocamera", '\ue8a2');

        Iconics.registerFont(erxesSDKGF);
    }

    public Drawable getKnowledgeIcon(Activity activity, String value) {
        switch (value) {
            case "like":
                return new IconicsDrawable(activity).icon("rxx-like").sizeDp(24);
            case "book":
                return new IconicsDrawable(activity).icon("rxx-book").sizeDp(24);
            case "computer":
                return new IconicsDrawable(activity).icon("rxx-computer").sizeDp(24);
            case "paste":
                return new IconicsDrawable(activity).icon("rxx-paste").sizeDp(24);
            case "folder":
                return new IconicsDrawable(activity).icon("rxx-folder").sizeDp(24);
            case "calculator":
                return new IconicsDrawable(activity).icon("rxx-calculator").sizeDp(24);
            case "home":
                return new IconicsDrawable(activity).icon("rxx-home").sizeDp(24);
            case "puzzle":
                return new IconicsDrawable(activity).icon("rxx-puzzle").sizeDp(24);
            case "medal":
                return new IconicsDrawable(activity).icon("rxx-medal").sizeDp(24);
            case "calendar":
                return new IconicsDrawable(activity).icon("rxx-calendar").sizeDp(24);
            case "stamp":
                return new IconicsDrawable(activity).icon("rxx-stamp").sizeDp(24);
            case "flag":
                return new IconicsDrawable(activity).icon("rxx-flag").sizeDp(24);
            case "phone-call":
                return new IconicsDrawable(activity).icon("rxx-phone_call").sizeDp(24);
            case "laptop":
                return new IconicsDrawable(activity).icon("rxx-laptop").sizeDp(24);
            case "dashboard":
                return new IconicsDrawable(activity).icon("rxx-dashboard").sizeDp(24);
            case "star":
                return new IconicsDrawable(activity).icon("rxx-star").sizeDp(24);
            case "head-1":
                return new IconicsDrawable(activity).icon("rxx-head_1").sizeDp(24);
            case "settings":
                return new IconicsDrawable(activity).icon("rxx-settings").sizeDp(24);
            case "users":
                return new IconicsDrawable(activity).icon("rxx-users").sizeDp(24);
            case "paintpalette":
                return new IconicsDrawable(activity).icon("rxx-paintpalette").sizeDp(24);
            case "alarm":
                return new IconicsDrawable(activity).icon("rxx-alarm").sizeDp(24);
            case "briefcase":
                return new IconicsDrawable(activity).icon("rxx-briefcase").sizeDp(24);
            case "cloudcomputing":
                return new IconicsDrawable(activity).icon("rxx-cloudcomputing").sizeDp(24);
            case "earthgrid":
                return new IconicsDrawable(activity).icon("rxx-earthgrid").sizeDp(24);
            case "diagram":
                return new IconicsDrawable(activity).icon("rxx-diagram").sizeDp(24);
            case "compass":
                return new IconicsDrawable(activity).icon("rxx-compass").sizeDp(24);
            case "idea":
                return new IconicsDrawable(activity).icon("rxx-idea").sizeDp(24);
            case "diamond":
                return new IconicsDrawable(activity).icon("rxx-diamond").sizeDp(24);
            case "piggybank":
                return new IconicsDrawable(activity).icon("rxx-piggybank").sizeDp(24);
            case "piechart":
                return new IconicsDrawable(activity).icon("rxx-piechart").sizeDp(24);
            case "scale":
                return new IconicsDrawable(activity).icon("rxx-scale").sizeDp(24);
            case "megaphone":
                return new IconicsDrawable(activity).icon("rxx-megaphone").sizeDp(24);
            case "tools":
                return new IconicsDrawable(activity).icon("rxx-tools").sizeDp(24);
            case "umbrella":
                return new IconicsDrawable(activity).icon("rxx-umbrella").sizeDp(24);
            case "bar-chart":
                return new IconicsDrawable(activity).icon("rxx-bar_chart").sizeDp(24);
            default:
                return new IconicsDrawable(activity).icon("rxx-clipboard").sizeDp(24);
        }
    }

    public Drawable getCancelIcon(int colorCode) {
        if (colorCode != 0)
            return new IconicsDrawable(context).icon("rxx-cancel").sizeDp(24).color(colorCode);
        else return new IconicsDrawable(context).icon("rxx-cancel").sizeDp(24);
    }

    public Drawable getsendIcon(Activity activity, int colorCode) {
        if (colorCode != 0)
            return new IconicsDrawable(activity).icon("rxx-send").sizeDp(24).color(colorCode);
        else return new IconicsDrawable(activity).icon("rxx-send").sizeDp(24);
    }

    public Drawable getEmailIcon(Activity activity, int colorCode) {
        if (colorCode != 0)
            return new IconicsDrawable(activity).icon("rxx-envelope_alt").sizeDp(24).color(colorCode);
        else return new IconicsDrawable(activity).icon("rxx-envelope_alt").sizeDp(24);
    }

    public Drawable getPhoneIcon(Activity activity, int colorCode) {
        if (colorCode != 0)
            return new IconicsDrawable(activity).icon("rxx-phone_alt").sizeDp(24).color(colorCode);
        else return new IconicsDrawable(activity).icon("rxx-phone_alt").sizeDp(24);
    }

    public Drawable getPlusIcon(Activity activity, int colorCode) {
        if (colorCode != 0)
            return new IconicsDrawable(activity).icon("rxx-plus_1").sizeDp(24).color(colorCode);
        else return new IconicsDrawable(activity).icon("rxx-plus_1").sizeDp(24);
    }

    public Drawable getBackIcon(Activity activity, int colorCode) {
        if (colorCode != 0)
            return new IconicsDrawable(activity).icon("rxx-leftarrow").sizeDp(24).color(colorCode);
        else return new IconicsDrawable(activity).icon("rxx-leftarrow").sizeDp(24);
    }

    public Drawable getLogoutIcon(Activity activity, int colorCode) {
        if (colorCode != 0)
            return new IconicsDrawable(activity).icon("rxx-logout").sizeDp(24).color(colorCode);
        else return new IconicsDrawable(activity).icon("rxx-logout").sizeDp(24);
    }

    public Drawable getAttachmentIcon(Activity activity, int colorCode) {
        if (colorCode != 0)
            return new IconicsDrawable(activity).icon("rxx-attach").sizeDp(24).color(colorCode);
        else return new IconicsDrawable(activity).icon("rxx-attach").sizeDp(24);
    }

    public Drawable getVCallIcon(Activity activity, int colorCode) {
        if (colorCode != 0)
            return new IconicsDrawable(activity).icon("rxx-videocamera").sizeDp(24).color(colorCode);
        else return new IconicsDrawable(activity).icon("rxx-videocamera").sizeDp(24);
    }

    public Drawable getImageVIcon(Activity activity, int colorCode) {
        if (colorCode != 0)
            return new IconicsDrawable(activity).icon("rxx-image_v").sizeDp(96).color(colorCode);
        else return new IconicsDrawable(activity).icon("rxx-image_v").sizeDp(96);
    }

    public int getInColor(int backgroundColor) {
        if (ColorUtils.calculateLuminance(backgroundColor) < 0.5)
            return context.getResources().getColor(R.color.md_white_1000);
        else return context.getResources().getColor(R.color.md_black_1000);
    }

    public int getInColorGray(int backgroundColor) {
        if (ColorUtils.calculateLuminance(backgroundColor) < 0.5)
            return context.getResources().getColor(R.color.md_grey_300);
        else return context.getResources().getColor(R.color.md_grey_600);
    }

    public void setCursorColor(EditText view, @ColorInt int color) {
        try {
            // Get the cursor resource id
            Field field = TextView.class.getDeclaredField("mCursorDrawableRes");
            field.setAccessible(true);
            int drawableResId = field.getInt(view);

            // Get the editor
            field = TextView.class.getDeclaredField("mEditor");
            field.setAccessible(true);
            Object editor = field.get(view);

            // Get the drawable and set a color filter
            Drawable drawable = ContextCompat.getDrawable(view.getContext(), drawableResId);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            Drawable[] drawables = {drawable, drawable};

            // Set the drawables
            field = editor.getClass().getDeclaredField("mCursorDrawable");
            field.setAccessible(true);
            field.set(editor, drawables);
        } catch (Exception ignored) {
        }
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
