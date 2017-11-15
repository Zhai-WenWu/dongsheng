package amodule._common.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.banner.Banner;
import acore.widget.banner.BannerAdapter;
import amodule._common.delegate.IBindMap;

/**
 * Description : //TODO
 * PackageName : amodule._common.widget
 * Created by MrTrying on 2017/11/13 15:32.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class BannerView extends Banner implements IBindMap {

    private OnBannerItemClickCallback mOnBannerItemClickCallback;

    public BannerView(Context context) {
        this(context,null);
    }

    public BannerView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int height = (int) (ToolsDevice.getWindowPx(context).widthPixels * 32 / 75f);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,height));
        setVisibility(GONE);
    }

    @Override
    public void setData(Map<String, String> data) {
//        Log.i("tzy","banner data = " + data.toString());
        if (null == data || data.isEmpty()){
            setVisibility(GONE);
            return;
        }
        Map<String,String> dataMap = StringManager.getFirstMap(data.get("data"));
        ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(dataMap.get("list"));
        if (arrayList.isEmpty()){
            setVisibility(GONE);
            return;
        }

        BannerAdapter<Map<String, String>> bannerAdapter = new BannerAdapter<Map<String, String>>(arrayList) {
            @Override
            protected void bindTips(TextView tv, Map<String, String> stringStringMap) {
            }

            @Override
            public void bindImage(ImageView imageView, Map<String, String> stringStringMap) {
                Glide.with(getContext())
                        .load(stringStringMap.get("img"))
                        .placeholder(R.drawable.i_nopic)
                        .error(R.drawable.i_nopic)
                        .into(imageView);
            }
        };
        setBannerAdapter(bannerAdapter);
        notifyDataHasChanged();
        setOnBannerItemClickListener(position -> {
            if (mOnBannerItemClickCallback != null) {
                mOnBannerItemClickCallback.onBannerItemClick(position, arrayList.get(position));
            } else {
                String url = arrayList.get(position).get("url");
                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), url, true);
            }
        });
        setPageChangeDuration(5 * 1000);
        setRandomItem(arrayList);
        setVisibility(VISIBLE);
    }

    int weightSum = 0;
    int[] weightArray;

    private void setRandomItem(ArrayList<Map<String, String>> arrayList) {
        if (null == arrayList || arrayList.isEmpty()) {
            return;
        }
        weightArray = new int[arrayList.size()];
        for (int index = 0; index < weightArray.length; index++) {
            Map<String, String> map = arrayList.get(index);
            String weightStr = map.get("weight");
            int currentWeight = TextUtils.isEmpty(weightStr) || "null".equals(weightStr) ? 0 : Integer.parseInt(weightStr);
            weightSum += currentWeight;
            weightArray[index] = weightSum;
        }
        //随机权重
        final int randomWeight = Tools.getRandom(0,weightSum);
        for (int index = 0; index < weightArray.length; index++) {
            if(randomWeight < weightArray[index]){
                setCurrentItem(index);
                break;
            }
        }

    }

    public interface OnBannerItemClickCallback {
        void onBannerItemClick(int position, Map<String, String> map);
    }

    public void setOnBannerItemClickCallback(OnBannerItemClickCallback onBannerItemClickCallback) {
        mOnBannerItemClickCallback = onBannerItemClickCallback;
    }
}
