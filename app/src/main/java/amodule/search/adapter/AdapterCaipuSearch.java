package amodule.search.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import acore.logic.XHClick;
import acore.logic.stat.StatModel;
import acore.logic.stat.StatisticsManager;
import acore.override.activity.base.BaseActivity;
import amodule.dish.activity.DetailDish;
import amodule.health.activity.DetailIngre;
import amodule.search.view.SearchResultAdDataProvider;
import amodule.search.view.SearchResultAdViewGenerater;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.scrollerAd.XHAllAdControl;
import xh.basic.tool.UtilImage;

import static acore.logic.stat.StatisticsManager.IS_STAT;
import static acore.logic.stat.StatisticsManager.STAT_DATA;
import static acore.logic.stat.StatisticsManager.TRUE_VALUE;

public class AdapterCaipuSearch extends BaseAdapter {
    public static final String MODULE_NAME = "菜谱搜索列表";
    private final BaseActivity mActivity;
    private SearchResultAdDataProvider mSearchResultAdDataProvider;
    private CopyOnWriteArrayList<Map<String, String>> mListCaipuData = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Map<String, String>> mListShicaiData = new CopyOnWriteArrayList<>();
    private int shicaiInsertPos;
    private final CopyOnWriteArrayList<Map<String, String>> adDdata = new CopyOnWriteArrayList<>();

    private int imgResource = R.drawable.i_nopic;
    private int imgWidth = 0, imgHeight = 0;// 以像素为单位
    private boolean imgZoom = false; // 是否允许图片拉伸来适应设置的宽或高
    private ImageView.ScaleType scaleType = ImageView.ScaleType.CENTER_CROP;
    private View mParent;
    private static final int TAG_ID = R.string.tag;
    private List<Integer> listPosUsed;
    private List<Integer> adPosList = new ArrayList<Integer>() {
    };
    private String searchKey;
    private int adNum;
    private XHAllAdControl xhAllAdControl;

    public AdapterCaipuSearch(BaseActivity mActivity, ViewGroup mParent) {
        this.mActivity = mActivity;
        this.mParent = mParent;
        listPosUsed = new InsertPosList();
        if (mSearchResultAdDataProvider == null) {
            mSearchResultAdDataProvider = new SearchResultAdDataProvider(mActivity);
        }
        mSearchResultAdDataProvider.getAdData();
        mSearchResultAdDataProvider.setAutoRefreshCallback(new SearchResultAdDataProvider.OnAutoRefreshCallback() {
            @Override
            public void autoRefresh() {
                Log.i("tzy", "mSearchResultAdDataProvider::autoRefresh: ");
                CopyOnWriteArrayList<Map<String, String>> listCaipuData = new CopyOnWriteArrayList<>();
                listCaipuData.addAll(mListCaipuData);
                CopyOnWriteArrayList<Map<String, String>> listShicaiData = new CopyOnWriteArrayList<>();
                listShicaiData.addAll(mListShicaiData);
                refresh(true, listCaipuData, listShicaiData);
            }
        });
    }

    public void refreshAdData() {
        if (mSearchResultAdDataProvider != null) {
            mSearchResultAdDataProvider.getAdData();
        }
    }

    /**
     * 绝对保证此方法在主线程中执行
     *
     * @param isRefresh
     * @param listCaipuData
     * @param listShicaiData
     *
     * @return
     */
    public synchronized int refresh(boolean isRefresh, CopyOnWriteArrayList<Map<String, String>> listCaipuData,
                                    CopyOnWriteArrayList<Map<String, String>> listShicaiData) {
        int adCanInsert = 0;
        this.mListCaipuData.clear();
        this.mListShicaiData.clear();

        mListCaipuData.addAll(listCaipuData);
        mListShicaiData.addAll(listShicaiData);

        getAdDataInfo(isRefresh);
        if ((adDdata != null && adDdata.size() > 0)
                || isRefresh) {
            adCanInsert = generateAdPos(isRefresh);
        }

        computeInsertPos();
        notifyDataSetChanged();
        return adCanInsert;
    }

    private void computeInsertPos() {

        shicaiInsertPos = -1;
        listPosUsed.clear();

        for (int pos : adPosList) {
            addToInsertList(pos);
        }

        if (mListShicaiData != null && mListShicaiData.size() > 0) {
            shicaiInsertPos = addToInsertList(0);
        }
    }


    private int addToInsertList(int pos) {
        int value = 0;
        if (pos < 0)
            return -1;
        if (listPosUsed.size() == 0) {
            if (pos > mListCaipuData.size())
                pos = mListCaipuData.size();
            listPosUsed.add(pos);
            value = pos;
        } else {
            int beforeInsert = 0;
            for (int index = 0; index < listPosUsed.size(); index++) {
                int curentScanPos = listPosUsed.get(index);
                if (pos < curentScanPos) {
                    listPosUsed.add(pos);
                    value = pos;
                    break;
                } else if (pos == curentScanPos) {
                    if (index == listPosUsed.size() - 1) {
                        listPosUsed.add(++pos);
                        value = pos;
                        break;
                    }
                    pos++;
                } else {
                    beforeInsert++;
                    if (index == listPosUsed.size() - 1) {
                        if (pos > mListCaipuData.size() + beforeInsert) {
                            pos = mListCaipuData.size() + beforeInsert;
                        }
                        listPosUsed.add(pos);
                        value = pos;
                        break;
                    }
                }
            }
        }

        return value;
    }

    @Override
    public int getCount() {
        return mListCaipuData.size() + mListShicaiData.size() + adNum;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {

        View view;
        if (adDdata.size() > 0 && adPosList.contains(pos)) {
            view = createAdView(pos);
        } else if (shicaiInsertPos > -1 && pos == shicaiInsertPos) {
            view = createShicaiView(pos);
        } else {
            view = createCaipuView(pos, getCaipuDataIndex(pos), convertView);
        }

        if (view == null) {
            view = convertView;
        }

        return view;
    }


    private int getCaipuDataIndex(int pos) {

        int caipuDataIndex = pos;
        for (int index : listPosUsed) {
            if (pos > index && index > -1)
                caipuDataIndex--;
        }
        return caipuDataIndex;
    }


    private View createCaipuView(int pos, int dataIndex, View convertView) {

        if (mListCaipuData == null || mListCaipuData.size() < dataIndex + 1) {
            return null;
        }

        final Map<String, String> caipuMap = mListCaipuData.get(dataIndex);
        if (caipuMap == null || caipuMap.size() < 1) {
            return null;
        }

        CaipuViewHolder viewHolder;
        if (null == convertView || convertView.getTag() == null) {
            viewHolder = new CaipuViewHolder();
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.c_search_result_caipu_item, null);
            viewHolder.iv_caipuCover = convertView.findViewById(R.id.iv_caipuCover);
            viewHolder.tv_caipu_name = (TextView) convertView.findViewById(R.id.tv_caipu_name);
            viewHolder.tv_caipu_decrip = (TextView) convertView.findViewById(R.id.tv_caipu_decrip);
            viewHolder.tv_caipu_firsttime = (TextView) convertView.findViewById(R.id.tv_caipu_firsttime);
            viewHolder.tv_caipu_observed = (TextView) convertView.findViewById(R.id.tv_caipu_observed);
            viewHolder.tv_caipu_collected = (TextView) convertView.findViewById(R.id.tv_caipu_collected);
            viewHolder.tv_caipu_origin = (TextView) convertView.findViewById(R.id.tv_caipu_origin);
            viewHolder.tv_duration = (TextView) convertView.findViewById(R.id.video_duration);
            viewHolder.vip = (ImageView) convertView.findViewById(R.id.search_vip);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CaipuViewHolder) convertView.getTag();
        }

        // 图片保存等级
        setViewImage(viewHolder.iv_caipuCover,caipuMap.get("img"));
        setViewText(viewHolder.tv_caipu_name, caipuMap.get("name"),View.INVISIBLE);
        setViewText(viewHolder.tv_caipu_decrip, caipuMap.get("burdens"),View.INVISIBLE);
        setViewText(viewHolder.tv_caipu_firsttime, caipuMap.get("dishAddTime"));
        setViewText(viewHolder.tv_caipu_observed, caipuMap.get("allClick"));
        setViewText(viewHolder.tv_caipu_collected, caipuMap.get("favorites"));
        setViewText(viewHolder.tv_caipu_origin, caipuMap.get("cusNickName"),View.INVISIBLE);
        setViewText(viewHolder.tv_duration, caipuMap.get("duration"));

        boolean vipShow = "2".equals(caipuMap.get("isVip"));
        viewHolder.vip.setVisibility(vipShow ? View.VISIBLE : View.GONE);

        convertView.setOnClickListener(v -> {
            XHClick.mapStat(mActivity, "a_search_result", "菜谱结果页", "点击菜谱");
            Intent intent = new Intent(mActivity, DetailDish.class);
            intent.putExtra("code", caipuMap.get("code"))
                    .putExtra("name", caipuMap.get("name"))
                    .putExtra("dishInfo", getDishInfo(caipuMap))
                    .putExtra("img", handleImg(caipuMap));
            mActivity.startActivity(intent);
            //点击统计
            StatisticsManager.saveData(StatModel.createListClickModel(mActivity.getClass().getSimpleName(), MODULE_NAME, String.valueOf(pos + 1), searchKey, caipuMap.get(STAT_DATA)));
        });

        //展示统计
        if (!TextUtils.equals(TRUE_VALUE, caipuMap.get(IS_STAT))) {
            caipuMap.put(IS_STAT, TRUE_VALUE);
            StatisticsManager.saveData(StatModel.createListShowModel(mActivity.getClass().getSimpleName(), MODULE_NAME, String.valueOf(pos + 1), searchKey, caipuMap.get(STAT_DATA)));
        }
        return convertView;
    }

    private String getDishInfo(Map<String, String> data) {
        try {
            JSONObject dishInfoJson = new JSONObject();
            dishInfoJson.put("code", data.get("code"));
            dishInfoJson.put("name", data.get("name"));
            dishInfoJson.put("img", handleImg(data));
            dishInfoJson.put("type", TextUtils.equals(data.get("hasVideo"), "2") ? "2" : "1");
            dishInfoJson.put("allClick", data.get("allClick").replace("浏览", ""));
            dishInfoJson.put("favorites", data.get("favorites").replace("收藏", ""));
            dishInfoJson.put("info", "");
            JSONObject customerJson = new JSONObject();
            customerJson.put("customerCode", data.get("cusCode"));
            customerJson.put("nickName", data.get("cusNickName"));
            customerJson.put("info", "");
            customerJson.put("img", data.get("cusImg"));
            dishInfoJson.put("customer", customerJson);
            return Uri.encode(dishInfoJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String handleImg(Map<String, String> data) {
        String img = data.get("sizeImg");
        if (TextUtils.isEmpty(img)) {
            img = data.get("img");
        }
        return img;
    }

    private View createShicaiView(int pos) {
        View view = null;
        if (mListShicaiData == null || mListShicaiData.size() < 1)
            return view;
        final Map<String, String> shicaiMap = mListShicaiData.get(0);
        view = LayoutInflater.from(mActivity).inflate(R.layout.c_search_result_shicai_item, null);

        ImageView cover_img = (ImageView) view.findViewById(R.id.iv_shicaiCover);
        TextView tv_shicai_name = (TextView) view.findViewById(R.id.tv_shicai_name);

        setViewImage(cover_img, shicaiMap.get("imgShow"));
        setViewText(tv_shicai_name, shicaiMap.get("name"));

        View tagView3 = view.findViewById(R.id.iv_shicai_tag3);
        if (null != tagView3)
            tagView3.setVisibility("2".equals(shicaiMap.get("hasTaboo")) ? View.VISIBLE : View.GONE);

        view.findViewById(R.id.rl_shicai).setOnClickListener(v -> {
            if (TextUtils.isEmpty(shicaiMap.get("name")))
                return;
            Intent intent = new Intent(mActivity, DetailIngre.class);
            intent.putExtra("name", shicaiMap.get("name"));
            intent.putExtra("code", shicaiMap.get("code"));
            intent.putExtra("page", "0");
            mActivity.startActivity(intent);
            XHClick.mapStat(mActivity, "a_search_result", "菜谱结果页", "点击食材");
            StatisticsManager.saveData(StatModel.createListClickModel(mActivity.getClass().getSimpleName(), MODULE_NAME, String.valueOf(pos + 1), searchKey, shicaiMap.get(STAT_DATA)));
        });
        //展示统计
        if (!TextUtils.equals(TRUE_VALUE, shicaiMap.get(IS_STAT))) {
            shicaiMap.put(IS_STAT, TRUE_VALUE);
            StatisticsManager.saveData(StatModel.createListShowModel(mActivity.getClass().getSimpleName(), MODULE_NAME, String.valueOf(pos + 1), searchKey, shicaiMap.get(STAT_DATA)));
        }
        return view;
    }

    private void setViewText(TextView v, String text) {
        setViewText(v, text,View.GONE);
    }

    private void setViewText(TextView v, String text,int visibility) {
        if (text == null || text.length() == 0 || text.equals("hide"))
            v.setVisibility(visibility);
        else {
            v.setVisibility(View.VISIBLE);
            v.setText(text.trim());
        }
    }

    private void setViewImage(final ImageView v, String value) {
        v.setVisibility(View.VISIBLE);
        // 异步请求网络图片
        if (value.indexOf("http") == 0) {
            if (value.length() < 10)
                return;
            v.setImageResource(imgResource);
            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
            v.setTag(TAG_ID, value);
            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mParent.getContext())
                    .load(value)
                    .setPlaceholderId(imgResource)
                    .setErrorId(imgResource)
                    .build();
            if (bitmapRequest != null) {
                bitmapRequest.into(getTarget(v, value));
            }
        }
        // 直接设置为内部图片
        else if (value.indexOf("ico") == 0) {
            InputStream is = v.getResources().openRawResource(Integer.parseInt(value.replace("ico", "")));
            Bitmap bitmap = UtilImage.inputStreamTobitmap(is);
            UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
        }
        // 隐藏
        else if (value.equals("hide") || value.length() == 0)
            v.setVisibility(View.GONE);
            // 直接加载本地图片
        else if (!value.equals("ignore")) {
            if (v.getTag(TAG_ID) != null && v.getTag(TAG_ID).equals(value))
                return;
            v.setTag(TAG_ID, value);
            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mParent.getContext())
                    .load(value)
                    .build();
            if (bitmapRequest != null) {
                bitmapRequest.placeholder(imgResource)
                        .error(imgResource)
                        .into(getTarget(v, value));
            }
        }
        // 如果为ignore,则忽略图片
    }

    private SubBitmapTarget getTarget(final ImageView v, final String url) {
        return new SubBitmapTarget() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                ImageView img = null;
                if (v.getTag(TAG_ID).equals(url))
                    img = v;
                if (img != null && bitmap != null) {
                    // 图片圆角和宽高适应
                    v.setScaleType(scaleType);
                    v.setImageBitmap(bitmap);
//                    UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
                }
            }
        };
    }


    class CaipuViewHolder {
        ImageView iv_caipuCover;
        TextView tv_caipu_name;
        TextView tv_caipu_decrip;
        TextView tv_caipu_firsttime;
        TextView tv_caipu_observed;
        TextView tv_caipu_collected;
        TextView tv_caipu_origin;
        TextView tv_duration;
        ImageView vip;
    }

    private View createAdView(int pos) {
        View view = null;
        int adIndex = -1;
        int[] adPos = new int[]{0,4,10,17,25,34,44};
        for (int i = 0; i < adPos.length; i++) {
            if (pos == adPos[i]) {
                adIndex = i;
                break;
            }
        }
        if (adIndex > -1 && adDdata != null && adIndex < adDdata.size()) {
            if (adDdata.get(adIndex) != null) {
                final Map<String, String> dataMap = adDdata.get(adIndex);
                view = SearchResultAdViewGenerater.generateListAdView(mActivity, xhAllAdControl, dataMap, adIndex);
                if (listPosUsed.contains(pos + 1)) {
                    view.findViewById(R.id.v_ad_item_tail).setVisibility(View.VISIBLE);
                } else {
                    view.findViewById(R.id.v_ad_item_tail).setVisibility(View.GONE);
                }
            }
        }

        return view;
    }

    private class InsertPosList extends ArrayList implements Comparator<Integer> {

        @Override
        public int compare(Integer lhs, Integer rhs) {
            if (rhs > lhs) return 1;
            if (rhs < lhs) return -1;
            return 0;
        }
    }

    /**
     * 计算能插入几个广告&初始化
     *
     * @return
     */
    private int generateAdPos(boolean isRefresh) {
        adPosList.clear();
        //0,4,10,17,25,34,44
        int adPos[] = new int[]{0,4,10,17,25,34,44};
//        Log.i("tzy", "generateAdPos: adDdata.size()=" + adDdata.size());
        for (int i = 0; i < adDdata.size() && i < adPos.length; i++) {
            if (adDdata.get(i) != null && !adDdata.get(i).isEmpty()) {
                adPosList.add(adPos[i]);
            }
        }
//        Log.i("tzy","adPosList = " + adPosList.toString());
        int adNumCanInsert = computeAdNumCanInsert(adPosList);
        adPosList = adPosList.subList(0, adNumCanInsert);
        this.adNum = adNumCanInsert;
        return adNumCanInsert;
    }

    private int computeAdNumCanInsert(List<Integer> origin) {
        int adNum = 0;
        int num = mListCaipuData.size() + mListShicaiData.size();
        if (num == 0)
            return 0;
        for (int i = 0; i < origin.size(); i++) {
            if (num >= origin.get(i)) {
                num++;
                adNum = i + 1;
            } else {
                break;
            }
        }
        return adNum;
    }

    public void clearAdList() {
        adNum = 0;
        if (adPosList != null)
            adPosList.clear();
        if (adDdata != null)
            adDdata.clear();
    }

    private Map<String, String> AdTypeData = new HashMap<>();//获取到数据集合

    private void getAdDataInfo(boolean isRefresh) {
        if (mSearchResultAdDataProvider == null) {
            mSearchResultAdDataProvider = new SearchResultAdDataProvider(mActivity);
        }
        xhAllAdControl = mSearchResultAdDataProvider.getXhAllAdControl();
        if (adDdata.isEmpty() || isRefresh) {
            adDdata.clear();
            adDdata.addAll(mSearchResultAdDataProvider.getAdDataList());
            AdTypeData.clear();
            AdTypeData.putAll(xhAllAdControl.getAdTypeData());
        }
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }
}
