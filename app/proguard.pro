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
-dontwarn com.google.android.maps.**,com.tencent.smtt.sdk.WebView,com.umeng.**,com.tencent.weibo.sdk.**,com.facebook.**


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
-dontwarn com.tencent.smtt.sdk.WebView
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

#mob share
-keep class cn.sharesdk.**{*;}
-keep class com.sina.**{*;}
-keep class **.R$* {*;}
-keep class **.R{*;}
-keep class com.mob.**{*;}
-dontwarn com.mob.**
-dontwarn cn.sharesdk.**
-dontwarn **.R$*
#mob share end

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
#mta start
-keep class com.tencent.stat.**  {* ;}
-keep class com.tencent.mid.**  {* ;}
#mta end

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
#七鱼客服
-dontwarn com.qiyukf.**
-keep class com.qiyukf.** {*;}
#友盟升级sdk-4.0.0版本-start
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

-keep class com.alibaba.sdk.android.**{*;}
-keep class com.ut.**{*;}
-keep class com.ta.**{*;}

-keep public class **.R$*{
   public static final int *;
}

#（可选）避免Log打印输出
-assumenosideeffects class android.util.Log {
   public static *** v(...);
   public static *** d(...);
   public static *** i(...);
   public static *** w(...);
 }
 #友盟升级sdk-4.0.0版本-end

 #阿里视频合成 start
 -ignorewarnings
 -dontwarn okio.**
 -dontwarn com.google.common.cache.**
 -dontwarn java.nio.file.**
 -dontwarn sun.misc.**
 -keep class android.support.v4.** { *; }
 -keep class android.support.v7.** { *; }
 -keep class okhttp3.** { *; }
 -keep class com.bumptech.glide.integration.okhttp3.** { *; }
 -keep class com.liulishuo.filedownloader.** { *; }
 -keep class java.nio.file.** { *; }
 -keep class sun.misc.** { *; }

 -keep class com.qu.preview.** { *; }
 -keep class com.qu.mp4saver.** { *; }
 -keep class com.duanqu.transcode.** { *; }
 -keep class com.duanqu.qupai.render.** { *; }
 -keep class com.duanqu.qupai.player.** { *; }
 -keep class com.duanqu.qupai.audio.** { *; }
 -keep class com.aliyun.qupai.encoder.** { *; }
 -keep class com.sensetime.stmobile.** { *; }
 -keep class com.duanqu.qupai.yunos.** { *; }
 -keep class com.aliyun.common.** { *; }
 -keep class com.aliyun.jasonparse.** { *; }
 -keep class com.aliyun.struct.** { *; }
 -keep class com.aliyun.recorder.AliyunRecorderCreator { *; }
 -keep class com.aliyun.recorder.supply.** { *; }
 -keep class com.aliyun.querrorcode.** { *; }
 -keep class com.qu.preview.callback.** { *; }
 -keep class com.aliyun.qupaiokhttp.** { *; }
 -keep class com.aliyun.crop.AliyunCropCreator { *; }
 -keep class com.aliyun.crop.struct.CropParam { *; }
 -keep class com.aliyun.crop.supply.** { *; }
 -keep class com.aliyun.qupai.editor.pplayer.AnimPlayerView { *; }
 -keep class com.aliyun.qupai.editor.impl.AliyunEditorFactory { *; }
 -keep interface com.aliyun.qupai.editor.** { *; }
 -keep interface com.aliyun.qupai.import_core.AliyunIImport { *; }
 -keep class com.aliyun.qupai.import_core.AliyunImportCreator { *; }
 -keep class com.aliyun.qupai.encoder.** { *; }
 -keep class com.aliyun.leaktracer.** { *;}
 -keep class com.duanqu.qupai.adaptive.** { *; }
 -keep class com.aliyun.thumbnail.** { *;}
 -keep class com.aliyun.demo.importer.media.MediaCache { *;}
 -keep class com.aliyun.demo.importer.media.MediaDir { *;}
 -keep class com.aliyun.demo.importer.media.MediaInfo { *;}
 -keep class com.alivc.component.encoder.**{ *;}
 -keep class com.aliyun.log.core.AliyunLogCommon { *;}
 -keep class com.aliyun.log.core.AliyunLogger { *;}
 -keep class com.aliyun.log.core.AliyunLogParam { *;}
 -keep class com.aliyun.log.core.LogService { *;}
 -keep class com.aliyun.log.struct.** { *;}
 -keep class com.aliyun.demo.publish.SecurityTokenInfo { *; }

 -keep class com.aliyun.vod.common.** { *; }
 -keep class com.aliyun.vod.jasonparse.** { *; }
 -keep class com.aliyun.vod.qupaiokhttp.** { *; }
 -keep class com.aliyun.vod.log.core.AliyunLogCommon { *;}
 -keep class com.aliyun.vod.log.core.AliyunLogger { *;}
 -keep class com.aliyun.vod.log.core.AliyunLogParam { *;}
 -keep class com.aliyun.vod.log.core.LogService { *;}
 -keep class com.aliyun.vod.log.struct.** { *;}
 -keep class com.aliyun.auth.core.**{*;}
 -keep class com.aliyun.auth.common.AliyunVodHttpCommon{*;}
 -keep class com.alibaba.sdk.android.vod.upload.exception.**{*;}
 -keep class com.alibaba.sdk.android.vod.upload.auth.**{*;}
 -keep class com.aliyun.auth.model.**{*;}
 -keep class component.alivc.com.facearengine.** {*;}
 -keep class com.aliyun.svideo.sdk.external.struct.**{*;}
 -keep class com.aliyun.svideo.sdk.internal.common.project.* {*;}

 -keep class **.R$* { *; }

 ## Event Bus
 -keepattributes *Annotation*
 -keepclassmembers class ** {
     @org.greenrobot.eventbus.Subscribe <methods>;
 }
 -keep enum org.greenrobot.eventbus.ThreadMode { *; }

 # Only required if you use AsyncExecutor
 -keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
     <init>(java.lang.Throwable);
 }
 #阿里视频合成 end

 ##高德定位
 -keep class com.amap.api.location.**{*;}
 -keep class com.amap.api.fence.**{*;}
 -keep class com.autonavi.aps.amapapi.model.**{*;}