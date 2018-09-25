[1mdiff --git a/app/src/main/java/com/example/lol/uusususuasd/MainActivity.java b/app/src/main/java/com/example/lol/uusususuasd/MainActivity.java[m
[1mindex c5ad56a..50e5502 100644[m
[1m--- a/app/src/main/java/com/example/lol/uusususuasd/MainActivity.java[m
[1m+++ b/app/src/main/java/com/example/lol/uusususuasd/MainActivity.java[m
[36m@@ -24,10 +24,10 @@[m [mpublic class MainActivity extends AppCompatActivity {[m
 //        Config.Init(this,"yPv5aN","172.20.10.3");[m
 //        Config.Init(this,"yPv5aN","192.168.100.185");[m
         config  = Config.getInstance(this);[m
[31m-        config.Init("yPv5aN",[m
[31m-                "http://192.168.50.57:3100/graphql",[m
[31m-                "ws://192.168.50.57:3300/subscriptions",[m
[31m-                "http://192.168.50.57:3300/upload-file" );[m
[32m+[m[32m        config.Init("YDEdKj",[m
[32m+[m[32m                "http://192.168.1.6:3100/graphql",[m
[32m+[m[32m                "ws://192.168.1.6:3300/subscriptions",[m
[32m+[m[32m                "http://192.168.1.6:3300/upload-file" );[m
 //        config.Init("yPv5aN",[m
 //                "http://192.168.100.185:3100/graphql",[m
 //                "ws://192.168.100.185:3300/subscriptions",[m
[36m@@ -48,8 +48,8 @@[m [mpublic class MainActivity extends AppCompatActivity {[m
         this.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {[m
             @Override[m
             public void onClick(View view) {[m
[31m-//                config.Start();[m
[31m-                config.Start_login_email("orshih_bat@yahoo.com");[m
[32m+[m[32m                config.Start();[m
[32m+[m[32m//                config.Start_login_email("orshih_bat@yahoo.com");[m
 //                config.Start_login_phone("99001100");[m
             }[m
         });[m
[1mdiff --git a/erxeslibrary/src/main/java/com/newmedia/erxeslibrary/MessageListAdapter.java b/erxeslibrary/src/main/java/com/newmedia/erxeslibrary/MessageListAdapter.java[m
[1mindex 1dab82e..4d52fb7 100644[m
[1m--- a/erxeslibrary/src/main/java/com/newmedia/erxeslibrary/MessageListAdapter.java[m
[1m+++ b/erxeslibrary/src/main/java/com/newmedia/erxeslibrary/MessageListAdapter.java[m
[36m@@ -1,332 +1,355 @@[m
[31m-package com.newmedia.erxeslibrary[m
[31m-[m
[31m-import android.content.Context[m
[31m-import android.content.Intent[m
[31m-import android.graphics.Bitmap[m
[31m-import android.graphics.Color[m
[31m-import android.graphics.Paint[m
[31m-import android.graphics.PorterDuff[m
[31m-import android.graphics.drawable.ColorDrawable[m
[31m-import android.graphics.drawable.GradientDrawable[m
[31m-import android.media.Image[m
[31m-import android.net.Uri[m
[31m-import android.support.v4.widget.CircularProgressDrawable[m
[31m-import android.support.v7.widget.RecyclerView[m
[31m-import android.text.Html[m
[31m-import android.text.format.DateUtils[m
[31m-import android.util.Log[m
[31m-import android.view.LayoutInflater[m
[31m-import android.view.View[m
[31m-import android.view.ViewGroup[m
[31m-import android.webkit.WebView[m
[31m-import android.widget.ImageView[m
[31m-import android.widget.TextView[m
[31m-[m
[31m-[m
[31m-import com.bumptech.glide.Glide[m
[31m-import com.bumptech.glide.load.engine.DiskCacheStrategy[m
[31m-import com.bumptech.glide.request.RequestOptions[m
[31m-import com.bumptech.glide.request.target.BitmapImageViewTarget[m
[31m-import com.bumptech.glide.request.target.SimpleTarget[m
[31m-import com.bumptech.glide.request.target.SizeReadyCallback[m
[31m-import com.bumptech.glide.request.target.Target[m
[31m-import com.bumptech.glide.request.transition.Transition[m
[31m-import com.newmedia.erxeslibrary.Configuration.Config[m
[31m-import com.newmedia.erxeslibrary.Configuration.GlideApp[m
[31m-import com.newmedia.erxeslibrary.Model.*[m
[31m-[m
[31m-import org.json.JSONArray[m
[31m-import org.json.JSONException[m
[31m-import org.json.JSONObject[m
[31m-[m
[31m-[m
[31m-import java.util.ArrayList[m
[31m-import java.util.Date[m
[31m-[m
[31m-class MessageListAdapter(private val context: Context, private var mMessageList: List<ConversationMessage>?) : RecyclerView.Adapter<*>() {[m
[31m-    private var previous_size = 0[m
[31m-    private val config: Config[m
[31m-    private val fileDownload = View.OnClickListener { view ->[m
[31m-        val url = view.tag as String[m
[31m-        if (url.startsWith("http")) {[m
[31m-            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(view.tag as String))[m
[31m-            context.startActivity(browserIntent)[m
[31m-        }[m
[31m-    }[m
[31m-[m
[31m-    init {[m
[31m-        this.config = Config.getInstance(context)[m
[31m-        this.previous_size = this.mMessageList!!.size[m
[32m+[m[32mpackage com.newmedia.erxeslibrary;[m
[32m+[m
[32m+[m[32mimport android.content.Context;[m
[32m+[m[32mimport android.content.Intent;[m
[32m+[m[32mimport android.graphics.Bitmap;[m
[32m+[m[32mimport android.graphics.Color;[m
[32m+[m[32mimport android.graphics.Paint;[m
[32m+[m[32mimport android.graphics.PorterDuff;[m
[32m+[m[32mimport android.graphics.drawable.ColorDrawable;[m
[32m+[m[32mimport android.graphics.drawable.GradientDrawable;[m
[32m+[m[32mimport android.media.Image;[m
[32m+[m[32mimport android.net.Uri;[m
[32m+[m[32mimport android.support.annotation.NonNull;[m
[32m+[m[32mimport android.support.annotation.Nullable;[m
[32m+[m[32mimport android.support.v4.widget.CircularProgressDrawable;[m
[32m+[m[32mimport android.support.v7.widget.RecyclerView;[m
[32m+[m[32mimport android.text.Html;[m
[32m+[m[32mimport android.text.format.DateUtils;[m
[32m+[m[32mimport android.util.Log;[m
[32m+[m[32mimport android.view.LayoutInflater;[m
[32m+[m[32mimport android.view.View;[m
[32m+[m[32mimport android.view.ViewGroup;[m
[32m+[m[32mimport android.webkit.WebView;[m
[32m+[m[32mimport android.widget.ImageView;[m
[32m+[m[32mimport android.widget.TextView;[m
[32m+[m
[32m+[m
[32m+[m[32mimport com.bumptech.glide.Glide;[m
[32m+[m[32mimport com.bumptech.glide.load.engine.DiskCacheStrategy;[m
[32m+[m[32mimport com.bumptech.glide.request.RequestOptions;[m
[32m+[m[32mimport com.bumptech.glide.request.target.BitmapImageViewTarget;[m
[32m+[m[32mimport com.bumptech.glide.request.target.SimpleTarget;[m
[32m+[m[32mimport com.bumptech.glide.request.target.SizeReadyCallback;[m
[32m+[m[32mimport com.bumptech.glide.request.target.Target;[m
[32m+[m[32mimport com.bumptech.glide.request.transition.Transition;[m
[32m+[m[32mimport com.newmedia.erxeslibrary.Configuration.Config;[m
[32m+[m[32mimport com.newmedia.erxeslibrary.Configuration.GlideApp;[m
[32m+[m[32mimport com.newmedia.erxeslibrary.Model.*;[m
[32m+[m
[32m+[m[32mimport org.json.JSONArray;[m
[32m+[m[32mimport org.json.JSONException;[m
[32m+[m[32mimport org.json.JSONObject;[m
[32m+[m
[32m+[m
[32m+[m[32mimport java.util.ArrayList;[m
[32m+[m[32mimport java.util.Date;[m
[32m+[m[32mimport java.util.List;[m
[32m+[m
[32m+[m[32mpublic class MessageListAdapter extends RecyclerView.Adapter {[m
[32m+[m
[32m+[m
[32m+[m[32m    private List<ConversationMessage> mMessageList;[m
[32m+[m[32m    private Context context;[m
[32m+[m[32m    private int previous_size = 0;[m
[32m+[m[32m    private Config config;[m
[32m+[m[32m    public MessageListAdapter( Context context,List<ConversationMessage> mMessageList) {[m
[32m+[m[32m        this.context = context;[m
[32m+[m[32m        this.config = Config.getInstance(context);[m
[32m+[m[32m        this.mMessageList =  mMessageList;[m
[32m+[m[32m        this.previous_size = this.mMessageList.size();[m
     }[m
 [m
[31m-    fun setmMessageList(mMessageList: List<ConversationMessage>) {[m
[31m-        this.mMessageList = mMessageList[m
[32m+[m[32m    public void setmMessageList(List<ConversationMessage> mMessageList) {[m
[32m+[m[32m        this.mMessageList = mMessageList;[m
     }[m
[31m-[m
[31m-    fun IsBeginningChat(): Boolean {[m
[31m-        return if (mMessageList!!.size == 0)[m
[31m-            true[m
[32m+[m[32m    public boolean IsBeginningChat(){[m
[32m+[m[32m        if(mMessageList.size() == 0)[m
[32m+[m[32m            return true;[m
         else[m
[31m-            false[m
[32m+[m[32m            return false;[m
     }[m
 [m
[31m-    fun refresh_data(): Boolean {[m
[32m+[m[32m    public boolean refresh_data(){[m
 [m
[31m-        if (mMessageList!!.size > previous_size) {[m
[31m-            val counter_before = mMessageList!!.size[m
[31m-            val zoruu = mMessageList!!.size - previous_size[m
[32m+[m[32m        if(mMessageList.size() > previous_size) {[m
[32m+[m[32m            int counter_before = mMessageList.size();[m
[32m+[m[32m            int zoruu = mMessageList.size() - previous_size;[m
 [m
[31m-            previous_size = mMessageList!!.size[m
[31m-            if (config.welcomeMessage != null) {[m
[32m+[m[32m            previous_size = mMessageList.size();[m
[32m+[m[32m            if(config.welcomeMessage!=null) {[m
                 if (zoruu == 1)[m
[31m-                    notifyItemInserted(mMessageList!!.size)[m
[32m+[m[32m                    notifyItemInserted(mMessageList.size());[m
                 else[m
[31m-                    notifyItemRangeInserted(counter_before + 1, zoruu)[m
[31m-            } else {[m
[32m+[m[32m                    notifyItemRangeInserted(counter_before+1, zoruu);[m
[32m+[m[32m            }else{[m
                 if (zoruu == 1)[m
[31m-                    notifyItemInserted(mMessageList!!.size - 1)[m
[32m+[m[32m                    notifyItemInserted(mMessageList.size() - 1);[m
                 else[m
[31m-                    notifyItemRangeInserted(counter_before, zoruu)[m
[32m+[m[32m                    notifyItemRangeInserted(counter_before, zoruu);[m
 [m
             }[m
[31m-            return true[m
[31m-        } else[m
[31m-            return false[m
[32m+[m[32m            return true;[m
[32m+[m[32m        }[m
[32m+[m[32m        else[m
[32m+[m[32m            return false;[m
     }[m
 [m
[31m-    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {[m
[31m-        val layoutInflater = LayoutInflater.from(parent.context)[m
[31m-[m
[31m-        if (viewType == 0) {[m
[31m-            val view = layoutInflater.inflate(R.layout.item_message_sent, parent, false)[m
[31m-            //        view.setOnClickListener(onClickListener);[m
[31m-            return SentMessageHolder(view)[m
[31m-        } else if (viewType == 1) {[m
[31m-            val view = layoutInflater.inflate(R.layout.item_message_received, parent, false)[m
[31m-            //        view.setOnClickListener(onClickListener);[m
[31m-            return ReceivedMessageHolder(view)[m
[31m-        } else {[m
[31m-            val view = layoutInflater.inflate(R.layout.item_message_welcome, parent, false)[m
[31m-            //        view.setOnClickListener(onClickListener);[m
[31m-            return WelcomeMessageHolder(view)[m
[32m+[m[32m    @NonNull[m
[32m+[m[32m    @Override[m
[32m+[m[32m    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {[m
[32m+[m[32m        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());[m
[32m+[m
[32m+[m[32m        if(viewType == 0) {[m
[32m+[m[32m            View view = layoutInflater.inflate(R.layout.item_message_sent, parent, false);[m
[32m+[m[32m//        view.setOnClickListener(onClickListener);[m
[32m+[m[32m            return new SentMessageHolder(view);[m
[32m+[m[32m        }[m
[32m+[m[32m        else if(viewType == 1){[m
[32m+[m[32m            View view = layoutInflater.inflate(R.layout.item_message_received, parent, false);[m
[32m+[m[32m//        view.setOnClickListener(onClickListener);[m
[32m+[m[32m            return new ReceivedMessageHolder(view);[m
[32m+[m[32m        }[m
[32m+[m[32m        else {[m
[32m+[m[32m            View view = layoutInflater.inflate(R.layout.item_message_welcome, parent, false);[m
[32m+[m[32m//        view.setOnClickListener(onClickListener);[m
[32m+[m[32m            return new WelcomeMessageHolder(view);[m
         }[m
     }[m
 [m
[31m-    override fun getItemViewType(position: Int): Int {[m
[31m-        var position = position[m
[31m-        if (position == 0 && config.welcomeMessage != null)[m
[31m-            return 2 //welcomeMessage[m
[32m+[m[32m    @Override[m
[32m+[m[32m    public int getItemViewType(int position) {[m
[32m+[m[32m        if(position == 0 && config.welcomeMessage!=null)[m
[32m+[m[32m            return 2; //welcomeMessage[m
 [m
[31m-        if (config.welcomeMessage != null)[m
[31m-            position = position - 1[m
[32m+[m[32m        if(config.welcomeMessage!=null)[m
[32m+[m[32m            position = position - 1;[m
 [m
[31m-        return if (config.customerId.equals(mMessageList!![position].customerId, ignoreCase = true))[m
[31m-            0[m
[32m+[m[32m        if( config.customerId.equalsIgnoreCase(mMessageList.get(position).customerId ))[m
[32m+[m[32m            return 0;[m
         else[m
[31m-            1[m
[32m+[m[32m            return 1;[m
     }[m
 [m
[31m-    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {[m
[32m+[m[32m    @Override[m
[32m+[m[32m    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {[m
 [m
 [m
[31m-        val message: ConversationMessage[m
[32m+[m[32m        ConversationMessage message ;[m
 [m
[31m-        if (config.welcomeMessage != null && position == 0) {[m
[31m-            message = ConversationMessage()[m
[31m-            message.content = config.welcomeMessage[m
[31m-            message.createdAt = ""[m
[31m-        } else if (config.welcomeMessage != null)[m
[31m-            message = mMessageList!![position - 1][m
[32m+[m[32m        if(config.welcomeMessage!=null && (position == 0)){[m
[32m+[m[32m            message = new ConversationMessage();[m
[32m+[m[32m            message.content = (config.welcomeMessage);[m
[32m+[m[32m            message.createdAt = ("");[m
[32m+[m[32m        }[m
[32m+[m[32m        else if(config.welcomeMessage != null)[m
[32m+[m[32m            message = mMessageList.get(position - 1);[m
         else[m
[31m-            message = mMessageList!![position][m
[31m-[m
[31m-        when (holder.itemViewType) {[m
[31m-            0 -> (holder as SentMessageHolder).bind(message)[m
[31m-            1 -> (holder as ReceivedMessageHolder).bind(message)[m
[31m-            2 -> (holder as WelcomeMessageHolder).bind(message)[m
[32m+[m[32m            message = mMessageList.get(position);[m
[32m+[m
[32m+[m[32m        switch (holder.getItemViewType()) {[m
[32m+[m[32m            case 0:[m
[32m+[m[32m                ((SentMessageHolder) holder).bind(message);[m
[32m+[m[32m                break;[m
[32m+[m[32m            case 1:[m
[32m+[m[32m                ((ReceivedMessageHolder) holder).bind(message);[m
[32m+[m[32m                break;[m
[32m+[m[32m            case 2:[m
[32m+[m[32m                ((WelcomeMessageHolder) holder).bind(message);[m
[32m+[m[32m                break;[m
         }[m
     }[m
 [m
[31m-    override fun getItemCount(): Int {[m
[31m-        return if (config.welcomeMessage != null)[m
[31m-            mMessageList!!.size + 1[m
[32m+[m[32m    @Override[m
[32m+[m[32m    public int getItemCount() {[m
[32m+[m[32m        if(config.welcomeMessage != null)[m
[32m+[m[32m            return mMessageList.size() + 1;[m
         else[m
[31m-            mMessageList!!.size[m
[32m+[m[32m            return mMessageList.size() ;[m
 [m
     }[m
[31m-[m
[31m-    private inner class SentMessageHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {[m
[31m-        internal var messageText: TextView[m
[31m-        internal var timeText: TextView[m
[31m-        internal var filelist: ViewGroup[m
[31m-[m
[31m-        init {[m
[31m-[m
[31m-            messageText = itemView.findViewById(R.id.text_message_body)[m
[31m-            timeText = itemView.findViewById(R.id.text_message_time)[m
[31m-            filelist = itemView.findViewById(R.id.filelist)[m
[32m+[m[32m    private View.OnClickListener fileDownload = new View.OnClickListener() {[m
[32m+[m[32m        @Override[m
[32m+[m[32m        public void onClick(View view) {[m
[32m+[m[32m            String url = (String)view.getTag();[m
[32m+[m[32m            if(url.startsWith("http")) {[m
[32m+[m[32m                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) view.getTag()));[m
[32m+[m[32m                context.startActivity(browserIntent);[m
[32m+[m[32m            }[m
[32m+[m[32m        }[m
[32m+[m[32m    };[m
[32m+[m[32m    private class SentMessageHolder extends RecyclerView.ViewHolder {[m
[32m+[m[32m        TextView messageText, timeText;[m
[32m+[m[32m        ViewGroup filelist;[m
[32m+[m[32m        SentMessageHolder(View itemView) {[m
[32m+[m[32m            super(itemView);[m
[32m+[m
[32m+[m[32m            messageText =  itemView.findViewById(R.id.text_message_body);[m
[32m+[m[32m            timeText =  itemView.findViewById(R.id.text_message_time);[m
[32m+[m[32m            filelist = itemView.findViewById(R.id.filelist);[m
         }[m
 [m
[31m-        internal fun bind(message: ConversationMessage) {[m
[31m-            messageText.text = Html.fromHtml(message.content)[m
[31m-            timeText.text = config.Message_datetime(message.createdAt)[m
[31m-            val a1 = messageText.background as GradientDrawable[m
[31m-            a1.setColor(config.colorCode)[m
[31m-            //                messageText.setBackgroundColor(Color.parseColor(Config.color));[m
[31m-            filelist.removeAllViews()[m
[31m-            timeText.text = config.Message_datetime(message.createdAt)[m
[31m-            if (message.attachments != null) {[m
[31m-                val layoutInflater = LayoutInflater.from(context)[m
[32m+[m[32m        void bind(ConversationMessage message) {[m
[32m+[m[32m            messageText.setText(Html.fromHtml(message.content));;[m
[32m+[m[32m            timeText.setText(config.Message_datetime(message.createdAt));[m
[32m+[m[32m            GradientDrawable a1 = (GradientDrawable) messageText.getBackground();[m
[32m+[m[32m            a1.setColor(config.colorCode);[m
[32m+[m[32m//                messageText.setBackgroundColor(Color.parseColor(Config.color));[m
[32m+[m[32m            filelist.removeAllViews();[m
[32m+[m[32m            timeText.setText(config.Message_datetime(message.createdAt));[m
[32m+[m[32m            if(message.attachments !=null) {[m
[32m+[m[32m                LayoutInflater layoutInflater = LayoutInflater.from(context);[m
 [m
                 try {[m
 [m
[31m-                    val a = JSONArray(message.attachments)[m
[31m-                    for (i in 0 until a.length()) {[m
[31m-                        val view = layoutInflater.inflate(R.layout.file_item, filelist, false)[m
[32m+[m[32m                    JSONArray a = new JSONArray(message.attachments);[m
[32m+[m[32m                    for (int i = 0; i < a.length(); i++) {[m
[32m+[m[32m                        View view = layoutInflater.inflate(R.layout.file_item, filelist, false);[m
                         draw_file(a.getJSONObject(i),[m
[31m-                                view.findViewById<View>(R.id.image_input) as ImageView,[m
[32m+[m[32m                                (ImageView) view.findViewById(R.id.image_input),[m
                                 view,[m
[31m-                                view.findViewById<View>(R.id.filename) as TextView)[m
[31m-                        filelist.addView(view)[m
[32m+[m[32m                                (TextView) view.findViewById(R.id.filename));[m
[32m+[m[32m                        filelist.addView(view);[m
                     }[m
[31m-                } catch (e: JSONException) {[m
[31m-                    e.printStackTrace()[m
[32m+[m[32m                } catch (JSONException e) {[m
[32m+[m[32m                    e.printStackTrace();[m
                 }[m
[31m-[m
             }[m
 [m
         }[m
     }[m
 [m
 [m
[31m-    private inner class ReceivedMessageHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {[m
[31m-        internal var timeText: TextView[m
[31m-        internal var messageText: TextView[m
[31m-        internal var profileImage: ImageView[m
[31m-        internal var filelist: ViewGroup[m
[32m+[m[32m    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {[m
[32m+[m[32m        TextView  timeText;[m
[32m+[m[32m        TextView messageText;[m
[32m+[m[32m        ImageView profileImage;[m
[32m+[m[32m        ViewGroup filelist;[m
 [m
 [m
[31m-        init {[m
[32m+[m[32m        ReceivedMessageHolder(View itemView) {[m
[32m+[m[32m            super(itemView);[m
 [m
[31m-            messageText = itemView.findViewById(R.id.text_message_body)[m
[31m-            timeText = itemView.findViewById(R.id.text_message_time)[m
[31m-            profileImage = itemView.findViewById(R.id.image_message_profile)[m
[31m-            filelist = itemView.findViewById(R.id.filelist)[m
[32m+[m[32m            messageText =  itemView.findViewById(R.id.text_message_body);[m
[32m+[m[32m            timeText =  itemView.findViewById(R.id.text_message_time);[m
[32m+[m[32m            profileImage = itemView.findViewById(R.id.image_message_profile);[m
[32m+[m[32m            filelist = itemView.findViewById(R.id.filelist);[m
 [m
         }[m
 [m
[31m-        internal fun bind(message: ConversationMessage) {[m
[31m-            //            messageText.loadData(message.content,"text/html","utf-8");[m
[31m-            messageText.text = Html.fromHtml(message.content.replace("\n", ""))[m
[31m-            //            messageText.setText(message.content);;[m
[31m-            timeText.text = config.Message_datetime(message.createdAt)[m
[32m+[m[32m        void bind(ConversationMessage message) {[m
[32m+[m[32m//            messageText.loadData(message.content,"text/html","utf-8");[m
[32m+[m[32m            messageText.setText(Html.fromHtml(message.content.replace("\n","")));;[m
[32m+[m[32m//            messageText.setText(message.content);;[m
[32m+[m[32m            timeText.setText(config.Message_datetime(message.createdAt));[m
 [m
[31m-            /**/[m
[31m-            if (message.user != null) {[m
[32m+[m[32m/**/[m
[32m+[m[32m            if(message.user!=null){[m
 [m
                 GlideApp.with(context).load(message.user.avatar).placeholder(R.drawable.avatar)[m
                         .diskCacheStrategy(DiskCacheStrategy.ALL)[m
[31m-                        .into(profileImage)[m
[31m-            } else[m
[31m-                profileImage.setImageResource(R.drawable.avatar)[m
[32m+[m[32m                        .into(profileImage);[m
[32m+[m[32m            }[m
[32m+[m[32m            else[m
[32m+[m[32m                profileImage.setImageResource(R.drawable.avatar);[m
 [m
[31m-            filelist.removeAllViews()[m
[31m-            timeText.text = config.Message_datetime(message.createdAt)[m
[31m-            if (message.attachments != null) {[m
[31m-                val layoutInflater = LayoutInflater.from(context)[m
[32m+[m[32m            filelist.removeAllViews();[m
[32m+[m[32m            timeText.setText(config.Message_datetime(message.createdAt));[m
[32m+[m[32m            if(message.attachments !=null) {[m
[32m+[m[32m                LayoutInflater layoutInflater = LayoutInflater.from(context);[m
 [m
                 try {[m
 [m
[31m-                    val a = JSONArray(message.attachments)[m
[31m-                    for (i in 0 until a.length()) {[m
[31m-                        val view = layoutInflater.inflate(R.layout.file_item, filelist, false)[m
[32m+[m[32m                    JSONArray a = new JSONArray(message.attachments);[m
[32m+[m[32m                    for (int i = 0; i < a.length(); i++) {[m
[32m+[m[32m                        View view = layoutInflater.inflate(R.layout.file_item, filelist, false);[m
                         draw_file(a.getJSONObject(i),[m
[31m-                                view.findViewById<View>(R.id.image_input) as ImageView,[m
[32m+[m[32m                                (ImageView) view.findViewById(R.id.image_input),[m
                                 view,[m
[31m-                                view.findViewById<View>(R.id.filename) as TextView)[m
[31m-                        filelist.addView(view)[m
[32m+[m[32m                                (TextView) view.findViewById(R.id.filename));[m
[32m+[m[32m                        filelist.addView(view);[m
                     }[m
[31m-                } catch (e: JSONException) {[m
[31m-                    e.printStackTrace()[m
[32m+[m[32m                } catch (JSONException e) {[m
[32m+[m[32m                    e.printStackTrace();[m
                 }[m
[31m-[m
             }[m
 [m
         }[m
     }[m
[32m+[m[32m    private class WelcomeMessageHolder extends RecyclerView.ViewHolder {[m
[32m+[m[32m        TextView messageText;[m
 [m
[31m-    private inner class WelcomeMessageHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {[m
[31m-        internal var messageText: TextView[m
[32m+[m[32m        WelcomeMessageHolder(View itemView) {[m
[32m+[m[32m            super(itemView);[m
 [m
[31m-        init {[m
[31m-[m
[31m-            messageText = itemView.findViewById(R.id.text_message_body)[m
[32m+[m[32m            messageText =  itemView.findViewById(R.id.text_message_body);[m
         }[m
 [m
[31m-        internal fun bind(message: ConversationMessage) {[m
[31m-            messageText.text = Html.fromHtml(message.content)[m
[32m+[m[32m        void bind(ConversationMessage message) {[m
[32m+[m[32m            messageText.setText(Html.fromHtml(message.content));;[m
         }[m
     }[m
[31m-[m
[31m-    private fun draw_file(o: JSONObject, inputImage: ImageView, fileview: View, filename: TextView) {[m
[32m+[m[32m    private void draw_file(JSONObject o,ImageView inputImage,View fileview,TextView filename){[m
 [m
 [m
[31m-        try {[m
[31m-            val type = o.getString("type")[m
[31m-            val size = o.getString("size")[m
[31m-            val name = o.getString("name")[m
[31m-            val url = o.getString("url")[m
[31m-            val circularProgressDrawable = CircularProgressDrawable(context)[m
[31m-            circularProgressDrawable.strokeWidth = 5f[m
[31m-            circularProgressDrawable.centerRadius = 30f[m
[31m-            circularProgressDrawable.start()[m
[32m+[m[32m        try{[m
[32m+[m[32m            String type = o.getString("type");[m
[32m+[m[32m            String size = o.getString("size");[m
[32m+[m[32m            String name = o.getString("name");[m
[32m+[m[32m            String url = o.getString("url");[m
[32m+[m[32m            CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);[m
[32m+[m[32m            circularProgressDrawable.setStrokeWidth(  5f);[m
[32m+[m[32m            circularProgressDrawable.setCenterRadius(  30f);[m
[32m+[m[32m            circularProgressDrawable.start();[m
 [m
[31m-            val scale = context.resources.displayMetrics.density[m
[31m-            var pixels = (20 * scale + 0.5f).toInt()[m
[31m-            inputImage.layoutParams.width = pixels[m
[31m-            inputImage.requestLayout()[m
[32m+[m[32m            float scale = context.getResources().getDisplayMetrics().density;[m
[32m+[m[32m            int pixels = (int) (20 * scale + 0.5f);[m
[32m+[m[32m            inputImage.getLayoutParams().width = pixels;[m
[32m+[m[32m            inputImage.requestLayout();[m
 [m
[31m-            inputImage.setImageDrawable(circularProgressDrawable)[m
[32m+[m[32m            inputImage.setImageDrawable(circularProgressDrawable);[m
 [m
[31m-            inputImage.drawable.setColorFilter(config.colorCode, PorterDuff.Mode.SRC_ATOP)[m
[32m+[m[32m            inputImage.getDrawable().setColorFilter(config.colorCode, PorterDuff.Mode.SRC_ATOP);[m
 [m
[31m-            fileview.tag = url[m
[31m-            fileview.setOnClickListener(fileDownload)[m
[32m+[m[32m            fileview.setTag(url);[m
[32m+[m[32m            fileview.setOnClickListener(fileDownload);[m
 [m
[31m-            filename.text = name[m
[31m-            filename.visibility = View.VISIBLE[m
[32m+[m[32m            filename.setText(name);[m
[32m+[m[32m            filename.setVisibility(View.VISIBLE);[m
 [m
 [m
 [m
[31m-            if (type.contains("image")) {[m
[31m-                pixels = (200 * scale + 0.5f).toInt()[m
[31m-                inputImage.layoutParams.width = pixels[m
[31m-                //                inputImage.getLayoutParams().height = pixels;[m
[31m-                inputImage.requestLayout()[m
[32m+[m[32m            if(type.contains("image")) {[m
[32m+[m[32m                pixels = (int) (200 * scale + 0.5f);[m
[32m+[m[32m                inputImage.getLayoutParams().width = pixels;[m
[32m+[m[32m//                inputImage.getLayoutParams().height = pixels;[m
[32m+[m[32m                inputImage.requestLayout();[m
 [m
                 GlideApp.with(context).load(url).placeholder(circularProgressDrawable)[m
[31m-                        .diskCacheStrategy(DiskCacheStrategy.ALL).override(pixels, Target.SIZE_ORIGINAL)[m
[31m-                        .into(inputImage)[m
[31m-                fileview.setOnClickListener(null)[m
[31m-                filename.visibility = View.GONE[m
[31m-            } else if (type.contains("application/pdf")) {[m
[31m-                inputImage.setImageResource(R.drawable.filepdf)[m
[31m-            } else if (type.contains("application") && type.contains("word")) {[m
[31m-                inputImage.setImageResource(R.drawable.fileword)[m
[31m-            } else {[m
[31m-                inputImage.setImageResource(R.drawable.file)[m
[32m+[m[32m                        .diskCacheStrategy(DiskCacheStrategy.ALL).override(pixels,Target.SIZE_ORIGINAL)[m
[32m+[m[32m                        .into(inputImage);[m
[32m+[m[32m                fileview.setOnClickListener(null);[m
[32m+[m[32m                filename.setVisibility(View.GONE);[m
[32m+[m[32m            }[m
[32m+[m[32m            else if(type.contains("application/pdf")){[m
[32m+[m[32m                inputImage.setImageResource(R.drawable.filepdf);[m
             }[m
[31m-        } catch (e: JSONException) {[m
[31m-            e.printStackTrace()[m
[32m+[m[32m            else if(type.contains("application")&&type.contains("word")){[m
[32m+[m[32m                inputImage.setImageResource(R.drawable.fileword);[m
[32m+[m[32m            }[m
[32m+[m[32m            else{[m
[32m+[m[32m                inputImage.setImageResource(R.drawable.file);[m
[32m+[m[32m            }[m
[32m+[m[32m        } catch (JSONException e) {[m
[32m+[m[32m            e.printStackTrace();[m
         }[m
[31m-[m
     }[m
[31m-    //    private SimpleTarget target = new SimpleTarget<Bitmap>() {[m
[31m-    //        @Override[m
[31m-    //        public void onResourceReady(Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {[m
[31m-    //            // do something with the bitmap[m
[31m-    //            // set it to an ImageView[m
[31m-    //            inputImage.setImageBitmap(bitmap);[m
[31m-    //        }[m
[31m-    //    };[m
[32m+[m[32m//    private SimpleTarget target = new SimpleTarget<Bitmap>() {[m
[32m+[m[32m//        @Override[m
[32m+[m[32m//        public void onResourceReady(Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {[m
[32m+[m[32m//            // do something with the bitmap[m
[32m+[m[32m//            // set it to an ImageView[m
[32m+[m[32m//            inputImage.setImageBitmap(bitmap);[m
[32m+[m[32m//        }[m
[32m+[m[32m//    };[m
 }[m
