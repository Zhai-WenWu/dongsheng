package amodule._common.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.GalleryViewPager;
import amodule._common.delegate.IBindMap;
import amodule._common.delegate.IStatictusData;

/**
 * Description : //TODO
 * PackageName : amodule._common.widget
 * Created by MrTrying on 2017/11/17 10:23.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class GalleryView extends GalleryViewPager implements IBindMap,IStatictusData {

    public GalleryView(Context context) {
        super(context);
    }

    public GalleryView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setData(Map<String, String> data) {
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

        ArrayList<View> views = new ArrayList<>();
        Stream.of(arrayList).forEach(map -> {
            ImageView image = new ImageView(getContext());
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(getContext())
                    .load(map.get("img"))
                    .placeholder(R.drawable.i_nopic)
                    .error(R.drawable.i_nopic)
                    .into(image);
            views.add(image);
        });
        init(views, 5 * 1000, true, new Helper() {
            @Override
            public void onClick(View view, int position) {
                String url = arrayList.get(position).get("url");
                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), url, true);
                if(!TextUtils.isEmpty(id) && !TextUtils.isEmpty(twoLevel)){
                    XHClick.mapStat(getContext(),id,twoLevel,threeLevel+position);
                }
            }

            @Override
            public void onChange(View view, int position) {

            }
        });
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

    String id, twoLevel, threeLevel;
    @Override
    public void setStatictusData(String id, String twoLevel, String threeLevel) {
        this.id = id;
        this.twoLevel = twoLevel;
        this.threeLevel = threeLevel;
    }
}
