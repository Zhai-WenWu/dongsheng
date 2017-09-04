#-injars bin\classes.jar
#-outjars bin\classes_out.jar

#----auto start

#----auto end

#----addOther start

#----addOther end

-printusage bin\proguard_usage.txt
-printmapping bin\proguard_mapping.txt
-dump bin\proguard_dump.txt
-printseeds bin\proguard_seeds.txt

-dontshrink
-dontoptimize
-keepattributes Exceptions,InnerClasses,Signature,*Annotation*,SourceFile,LineNumberTable
-dontwarn com.google.android.maps.**,android.webkit.WebView,com.umeng.**,com.tencent.weibo.sdk.**,com.facebook.**


-keep enum  com.facebook.**

-keep public interface  com.facebook.**

-keep public interface  com.tencent.**

-keep public interface  com.umeng.socialize.**

-keep public interface  com.umeng.socialize.sensor.**

-keep public interface  com.umeng.scrshot.**

-keep public class com.umeng.socialize.* {
    <fields>;
    <methods>;
}

-keep public class javax.**

-keep public class android.webkit.**

-keep class com.facebook.**

-keep class com.umeng.scrshot.**

-keep public class com.tencent.** {
    <fields>;
    <methods>;
}

-keep class com.umeng.socialize.sensor.**

-keep class com.tencent.mm.sdk.openapi.WXMediaMessage {
    <fields>;
    <methods>;
}

-keep class com.tencent.mm.sdk.openapi.** extends com.tencent.mm.sdk.openapi.WXMediaMessage$IMediaObject {
    <fields>;
    <methods>;
}

-keep class com.qq.e.** {
   public protected *; 
} 
-keep class com.tencent.gdt.**{ 
   public protected *; 
}

-keep public class xiangha.R$* {
    public static final int *;
}

-keep public class * {
    public <methods>;
}

#Umen社会化 start
-dontshrink
-dontoptimize
-dontwarn com.google.android.maps.**
-dontwarn android.webkit.WebView
-dontwarn com.umeng.**
-dontwarn com.tencent.weibo.sdk.**
-dontwarn com.facebook.**

-keep enum com.facebook.**
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

-keep public interface com.facebook.**
-keep public interface com.tencent.**
-keep public interface com.umeng.socialize.**
-keep public interface com.umeng.socialize.sensor.**
-keep public interface com.umeng.scrshot.**

-keep public class com.umeng.socialize.* {*;}
-keep public class javax.**
-keep public class android.webkit.**

-keep class com.facebook.**
-keep class com.umeng.scrshot.**
-keep public class com.tencent.** {*;}
-keep class com.umeng.socialize.sensor.**

-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}

-keep class com.tencent.mm.sdk.modelmsg.** implements com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}

-keep class im.yixin.sdk.api.YXMessage {*;}
-keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{*;}

-keep public class [xiangha].R$*{
    public static final int *;
}
#Umen 社会化 end

#Umeng在线参数 start
-keep class org.json.JSONObject {
        *;
}

-keep class com.umeng.onlineconfig.OnlineConfigAgent {
        public <fields>;
        public <methods>;

}

-keep class com.umeng.onlineconfig.OnlineConfigLog {
        public <fields>;
        public <methods>;
}

-keep interface com.umeng.onlineconfig.UmengOnlineConfigureListener {
        public <methods>;
}
#Umeng在线参数 end

#Umeng Push
-dontwarn com.taobao.**
-dontwarn anet.channel.**
-dontwarn anetwork.channel.**
-dontwarn org.android.**
-dontwarn org.apache.thrift.**
-dontwarn com.xiaomi.**
-dontwarn com.huawei.**

-keepattributes *Annotation*

-keep class com.taobao.** {*;}
-keep class org.android.** {*;}
-keep class anet.channel.** {*;}
-keep class com.umeng.** {*;}
-keep class com.xiaomi.** {*;}
-keep class com.huawei.** {*;}
-keep class org.apache.thrift.** {*;}

-keep public class **.R$*{
   public static final int *;
}
#Umeng Push end

-keep class com.yixia.** {*;}

#XG 
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep class com.tencent.android.tpush.**  {* ;}
-keep class com.tencent.mid.**  {* ;}
#XG end

#避免警告 start
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-ignorewarnings
#避免警告 end
# mob短信 start
-keep class cn.smssdk.**{*;}
-keep class com.mob.**{*;}

-dontwarn com.mob.**
-dontwarn cn.smssdk.**
# mob短信 end
#支付宝混淆 start
-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}
#支付宝混淆end

#广点通光 start
-keep class com.qq.e.** { 
    public protected *; 
}
-keep class android.support.v4.app.NotificationCompat**{
    public *;
}
-keep class MTT.ThirdAppInfoNew { 
    *; 
}
-keep class com.tencent.** { 
    *;
}
#广点通光 end

# 腾讯bugly start
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
# 腾讯bugly end

# 讯飞 start
-keep class com.iflytek.**{*;}
-keepattributes Signature
# 讯飞 end

#返利混淆规则
-keepattributes Signature
-keep class sun.misc.Unsafe { *; }
-keep class com.taobao.** {*;}
-keep class com.alibaba.** {*;}
-keep class com.alipay.** {*;}
-dontwarn com.taobao.**
-dontwarn com.alibaba.**
-dontwarn com.alipay.**
-keep class com.ut.** {*;}
-dontwarn com.ut.**
-keep class com.ta.** {*;}
-dontwarn com.ta.**
-keep class com.yidonglianjie.sdk.connector.** {*;}
-dontwarn com.yidonglianjie.sdk.connector.**
-keep class hhyj.imageloader.**{*;}
-dontwarn hhyj.imageloader.**
#返利混淆规则
#视频合成
-keep public class com.lansosdk.videoeditor.** {
<fields>;
<methods>;
}
-keep public class com.lansosdk.videoplayer.** {
<fields>;
<methods>;
}
#inmobi广告 start
-keepattributes SourceFile,LineNumberTable
-keep class com.inmobi.** { *; }
-dontwarn com.inmobi.**
-keep public class com.google.android.gms.**
-dontwarn com.google.android.gms.**
-dontwarn com.squareup.picasso.**
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient{
     public *;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info{
     public *;
}
# skip the Picasso library classes
-keep class com.squareup.picasso.** {*;}
-dontwarn com.squareup.picasso.**
-dontwarn com.squareup.okhttp.**
# skip Moat classes
-keep class com.moat.** {*;}
-dontwarn com.moat.**
# skip AVID classes
-keep class com.integralads.avid.library.* {*;}
#inmobi广告 end

#GrowingIO start
-keep class com.growingio.android.sdk.** {*;}
-dontwarn com.growingio.android.sdk.**
-keepnames class * extends android.view.View

-keep class * extends android.app.Fragment {
 public void setUserVisibleHint(boolean);
 public void onHiddenChanged(boolean);
 public void onResume();
 public void onPause();
}
-keep class android.support.v4.app.Fragment {
 public void setUserVisibleHint(boolean);
 public void onHiddenChanged(boolean);
 public void onResume();
 public void onPause();
}
-keep class * extends android.support.v4.app.Fragment {
 public void setUserVisibleHint(boolean);
 public void onHiddenChanged(boolean);
 public void onResume();
 public void onPause();
}
#GrowingIO end

#mta start
-keep class com.tencent.stat.**  {* ;}
-keep class com.tencent.mid.**  {* ;}
#mta end

#hotfix
#基线包使用，生成mapping.txt
-printmapping mapping.txt
#生成的mapping.txt在app/buidl/outputs/mapping/release路径下，移动到/app路径下
#修复后的项目使用，保证混淆结果一致
#-applymapping mapping.txt
#hotfix
-keep class com.taobao.sophix.**{*;}
-keep class com.ta.utdid2.device.**{*;}
#防止inline
-dontoptimize
#hotfix end

#baidu ad
-keepclassmembers class * extends android.app.Activity {
public void *(android.view.View);
}
-keepclassmembers enum * {
public static **[] values();
public static ** valueOf(java.lang.String);
}
-keep class com.baidu.mobads.*.** { *; }
#baidu ad

#视频播放器
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
-keep class com.shuyu.gsyvideoplayer.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.**
-keep class com.example.gsyvideoplayer.** { *; }
-dontwarn com.example.gsyvideoplayer.**
-keep class moe.codeest.enviews.** { *; }
-dontwarn moe.codeest.enviews.**