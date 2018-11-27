package acore.tools;

import android.content.Context;
import android.text.TextUtils;

import com.meituan.android.walle.WalleChannelReader;

/**
 * Created by XiangHa on 2016/8/25.
 */

public class ChannelManager {
    private static volatile ChannelManager mInstance;
    private static final Object mObj = new Object();
    private final String mChannelDef = "xiangha";
    private String mChannel;

    private ChannelManager() {

    }

    public static synchronized ChannelManager getInstance() {
        if (mInstance == null) {
            synchronized (mObj) {
                if (mInstance == null) {
                    mInstance = new ChannelManager();
                    return mInstance;
                }
            }
        }
        return mInstance;
    }

    /**
     * 返回市场。  如果获取失败返回"xiangha".
     * @param context
     * @return
     */
    public String getChannel(Context context){
        if (context == null) {
            return mChannelDef;
        }
        if (TextUtils.isEmpty(mChannel)) {
            try {
                final String channel = WalleChannelReader.getChannel(context.getApplicationContext());
                mChannel = TextUtils.isEmpty(channel) ? mChannelDef : channel;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mChannel;
    }
}
