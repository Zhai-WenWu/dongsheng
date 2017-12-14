package amodule.search.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xiangha.R;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.FileManager;
import acore.tools.Tools;
import acore.widget.ImageViewVideo;
import acore.widget.TagTextView;
import amodule.dish.activity.DetailDish;
import amodule.dish.activity.ListDish;
import amodule.health.activity.DetailIngre;
import amodule.search.view.SearchResultAdDataProvider;
import amodule.search.view.SearchResultAdViewGenerater;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import third.ad.scrollerAd.XHAllAdControl;
import xh.basic.tool.UtilImage;

/**
 * Created by ：airfly on 2016/10/21 19:06.
 */

public class AdapterCaipuSearch extends BaseAdapter {

    private final BaseActivity mActivity;
    private CopyOnWriteArrayList<Map<String, String>> mListCaipuData = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Map<String, String>> mListShicaiData = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Map<String, String>> mListCaidanData = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Map<String, String>> mListZhishiData = new CopyOnWriteArrayList<>();
    private int shicaiInsertPos;
    private int caidanInsertPos;
    private int zhishiInsertPos;
    private CopyOnWriteArrayList<Map<String, String>> adDdata = new CopyOnWriteArrayList<>();
    private CaipuSearchResultCallback callback;

    private int imgResource = R.drawable.i_nopic;
    private int roundImgPixels = 0, imgWidth = 0, imgHeight = 0,// 以像素为单位
            roundType = 1; // 1为全圆角，2上半部分圆角
    private boolean imgZoom = false; // 是否允许图片拉伸来适应设置的宽或高
    private String imgLevel = FileManager.save_cache; // 图片保存等级
    private ImageView.ScaleType scaleType = ImageView.ScaleType.CENTER_CROP;
    private View mParent;
    private static final int TAG_ID = R.string.tag;
    private List<Integer> listPosUsed;
    private List<Integer> adPosList = new ArrayList<Integer>() {
    };
    private int adNum;
    private XHAllAdControl xhAllAdControl;
    private AtomicBoolean topAdHasData = new AtomicBoolean(false);


    public AdapterCaipuSearch(BaseActivity mActivty, ViewGroup mParent, CaipuSearchResultCallback callback) {
        this.mActivity = mActivty;
        this.mParent = mParent;
        this.callback = callback;
        listPosUsed = new InsertPosList();
    }

    /**
     * 绝对保证此方法在主线程中执行
     * @param isRefresh
     * @param listCaipuData
     * @param listShicaiData
     * @param listCaidanData
     * @param listZhishiData
     * @return
     */
    public synchronized int refresh(boolean isRefresh, CopyOnWriteArrayList<Map<String, String>> listCaipuData,
                                    CopyOnWriteArrayList<Map<String, String>> listShicaiData,
                                    CopyOnWriteArrayList<Map<String, String>> listCaidanData,
                                    CopyOnWriteArrayList<Map<String, String>> listZhishiData) {
        int adCanInsert = 0;
        this.mListCaipuData.clear();
        this.mListShicaiData.clear();
        this.mListCaidanData.clear();
        this.mListZhishiData.clear();

        mListCaipuData.addAll(listCaipuData);
        mListShicaiData.addAll(listShicaiData);
        mListCaidanData.addAll(listCaidanData);
        mListZhishiData.addAll(listZhishiData);

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
        caidanInsertPos = -1;
        zhishiInsertPos = -1;
        listPosUsed.clear();

        for (int pos : adPosList) {
            addToInsertList(pos);
        }

        if (mListShicaiData != null && mListShicaiData.size() > 0) {
            shicaiInsertPos = addToInsertList(0);
        }

        if (mListCaidanData != null && mListCaidanData.size() > 0) {
            caidanInsertPos = Integer.valueOf(mListCaidanData.get(0).get("showPosition")) - 1;
        }
        if (mListZhishiData != null && mListZhishiData.size() > 0) {
            zhishiInsertPos = Integer.valueOf(mListZhishiData.get(0).get("showPosition")) - 1;
        }

        if (caidanInsertPos > 0 && zhishiInsertPos > 0) {
            if (caidanInsertPos == zhishiInsertPos) {
                zhishiInsertPos++;
            }
        }

        if (caidanInsertPos < zhishiInsertPos) {
            caidanInsertPos = addToInsertList(caidanInsertPos);
            zhishiInsertPos = addToInsertList(zhishiInsertPos);
        } else {
            zhishiInsertPos = addToInsertList(zhishiInsertPos);
            caidanInsertPos = addToInsertList(caidanInsertPos);
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
        return mListCaidanData.size() + mListCaipuData.size() + mListShicaiData.size() + mListZhishiData.size() + adNum;
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
            view = createShicaiView();
        } else if (caidanInsertPos > -1 && pos == caidanInsertPos) {
            view = createCaidanView();
        } else if (zhishiInsertPos > -1 && pos == zhishiInsertPos) {
            view = createZhishiView();
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
            viewHolder.iv_caipuCover = (ImageViewVideo) convertView.findViewById(R.id.iv_caipuCover);
            viewHolder.tv_caipu_name = (TextView) convertView.findViewById(R.id.tv_caipu_name);
            viewHolder.tv_caipu_decrip = (TextView) convertView.findViewById(R.id.tv_caipu_decrip);
            viewHolder.tv_caipu_firsttime = (TextView) convertView.findViewById(R.id.tv_caipu_firsttime);
            viewHolder.tv_caipu_observed = (TextView) convertView.findViewById(R.id.tv_caipu_observed);
            viewHolder.tv_caipu_collected = (TextView) convertView.findViewById(R.id.tv_caipu_collected);
            viewHolder.tv_caipu_origin = (TextView) convertView.findViewById(R.id.tv_caipu_origin);
            viewHolder.iv_itemIsSolo = (TagTextView) convertView.findViewById(R.id.iv_itemIsSolo);
            viewHolder.iv_itemIsFine = (TextView) convertView.findViewById(R.id.iv_itemIsFine);
            viewHolder.v_caipu_item_tail = convertView.findViewById(R.id.v_caipu_item_tail);
            viewHolder.v_bottom_line = convertView.findViewById(R.id.v_bottom_line);
            viewHolder.vip = (ImageView) convertView.findViewById(R.id.vip);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CaipuViewHolder) convertView.getTag();
        }

        viewHolder.iv_caipuCover.playImgWH = Tools.getDimen(mParent.getContext(), R.dimen.dp_34);
        viewHolder.iv_caipuCover.parseItemImg(scaleType, caipuMap.get("img"), caipuMap.get("hasVideo"), true, imgResource, imgLevel);
        setViewText(viewHolder.tv_caipu_name, caipuMap.get("name"));
        setViewText(viewHolder.tv_caipu_decrip, caipuMap.get("burdens"));
        setViewText(viewHolder.tv_caipu_firsttime, caipuMap.get("dishAddTime"));
        setViewText(viewHolder.tv_caipu_observed, caipuMap.get("allClick"));
        setViewText(viewHolder.tv_caipu_collected, caipuMap.get("favorites"));
        setViewText(viewHolder.tv_caipu_origin, caipuMap.get("cusNickName"));

        boolean vipShow = "2".equals(caipuMap.get("isVip"));
        viewHolder.vip.setVisibility(vipShow?View.VISIBLE:View.GONE);
        viewHolder.iv_itemIsSolo.setVisibility(("2").equals(caipuMap.get("exclusive")) && !vipShow?View.VISIBLE:View.GONE);
        viewHolder.iv_itemIsFine.setVisibility(("2").equals(caipuMap.get("isFine"))?View.VISIBLE:View.GONE);
        viewHolder.v_bottom_line.setVisibility(viewHolder.v_caipu_item_tail.getVisibility() == View.VISIBLE?View.GONE:View.VISIBLE);

        viewHolder.v_caipu_item_tail.setVisibility(View.GONE);
        for (int index : listPosUsed) {
            if (index == pos + 1
                    && !adPosList.contains(pos + 1)) {
                viewHolder.v_caipu_item_tail.setVisibility(View.VISIBLE);
                break;
            }
        }

        convertView.setOnClickListener(v -> {
            XHClick.mapStat(mActivity, "a_search_result", "菜谱结果页", "点击菜谱");
            Intent intent = new Intent(mActivity, DetailDish.class);
            intent.putExtra("code", caipuMap.get("code"))
                    .putExtra("name", caipuMap.get("name"))
                    .putExtra("dishInfo",getDishInfo(caipuMap))
                    .putExtra("img", caipuMap.get("img"));
            mActivity.startActivity(intent);
        });
        return convertView;
    }

    private String getDishInfo(Map<String,String> data) {
        try{
            JSONObject dishInfoJson = new JSONObject();
            dishInfoJson.put("code",data.get("code"));
            dishInfoJson.put("name",data.get("name"));
            dishInfoJson.put("img",data.get("img"));
            dishInfoJson.put("type",TextUtils.equals(data.get("hasVideo"), "2") ? "2" : "1");
            dishInfoJson.put("allClick",data.get("allClick").replace("浏览",""));
            dishInfoJson.put("favorites",data.get("favorites").replace("收藏",""));
            dishInfoJson.put("info","");
            JSONObject customerJson = new JSONObject();
            customerJson.put("customerCode",data.get("cusCode"));
            customerJson.put("nickName",data.get("cusNickName"));
            customerJson.put("info","");
            customerJson.put("img",data.get("cusImg"));
            dishInfoJson.put("customer",customerJson);
            return Uri.encode(dishInfoJson.toString());
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    private View createZhishiView() {
        if (mListZhishiData == null || mListZhishiData.size() < 1)
            return null;
        View view = LayoutInflater.from(mActivity).inflate(R.layout.c_search_result_zhishi_item, null);
        final Map<String, String> zhishiMap = mListZhishiData.get(0);
        ImageView iv_img_zhishi = (ImageView) view.findViewById(R.id.iv_img_zhishi);
        TextView tv_des_zhishi = (TextView) view.findViewById(R.id.tv_des_zhishi);
        TextView tv_cate_zhishi = (TextView) view.findViewById(R.id.tv_cate_zhishi);
        TextView tv_observed_candan = (TextView) view.findViewById(R.id.tv_observed_zhishi);
        RelativeLayout rl_zhishi_info = (RelativeLayout) view.findViewById(R.id.rl_zhishi_info);
        RelativeLayout rl_zhishi_more_item = (RelativeLayout) view.findViewById(R.id.rl_zhishi_more_item);

        setViewImage(iv_img_zhishi, zhishiMap.get("img"));
        setViewText(tv_des_zhishi, zhishiMap.get("title"));
        setViewText(tv_cate_zhishi, zhishiMap.get("classifyName"));
        setViewText(tv_observed_candan, zhishiMap.get("allClick"));
        if ("2".equals(zhishiMap.get("hasMore"))) {
            rl_zhishi_more_item.setVisibility(View.VISIBLE);
            rl_zhishi_more_item.setOnClickListener(v -> {
                if (callback != null) {
                    XHClick.mapStat(mActivity, "a_search_result", "菜谱结果页", "点击更多知识");
                    callback.searchMoreZhishi();
                }
            });
        } else {
            rl_zhishi_more_item.setVisibility(View.GONE);
        }
        rl_zhishi_info.setOnClickListener(v -> {
            XHClick.mapStat(mActivity, "a_search_result", "菜谱结果页", "点击知识");
            AppCommon.openUrl(mActivity, "nousInfo.app?code=" + zhishiMap.get("code"), true);
        });
        return view;
    }


    private View createCaidanView() {
        View view = null;
        if (mListCaidanData == null || mListCaidanData.size() < 1)
            return view;
        final Map<String, String> caidanMap = mListCaidanData.get(0);
        view = LayoutInflater.from(mActivity).inflate(R.layout.c_search_result_caidan_item, null);
        ImageView iv_img_left_caidan = (ImageView) view.findViewById(R.id.iv_img_left_caidan);
        ImageView iv_img_right_caidan = (ImageView) view.findViewById(R.id.iv_img_right_caidan);
        TextView tv_tag_caidan = (TextView) view.findViewById(R.id.tv_tag_caidan);
        TextView tv_num_caidan = (TextView) view.findViewById(R.id.tv_num_caidan);
        TextView tv_observed_caidan = (TextView) view.findViewById(R.id.tv_observed_caidan);
        RelativeLayout rl_caidan_more_item = (RelativeLayout) view.findViewById(R.id.rl_caidan_more_item);
        RelativeLayout rl_caidan_info = (RelativeLayout) view.findViewById(R.id.rl_caidan_info);


        setViewImage(iv_img_left_caidan, caidanMap.get("img1"));
        setViewImage(iv_img_right_caidan, caidanMap.get("img2"));
        setViewText(tv_tag_caidan, caidanMap.get("name"));
        setViewText(tv_num_caidan, caidanMap.get("dishNum"));
        setViewText(tv_observed_caidan, caidanMap.get("allClick"));

        if ("2".equals(caidanMap.get("hasMore"))) {
            rl_caidan_more_item.setVisibility(View.VISIBLE);
            rl_caidan_more_item.setOnClickListener(v -> {
                if (callback != null) {
                    callback.searchMoreCaidan();
                    XHClick.mapStat(mActivity, "a_search_result", "菜谱结果页", "点击更多菜单");
                }

            });
        } else {
            rl_caidan_more_item.setVisibility(View.GONE);
        }
        rl_caidan_info.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, ListDish.class);
            intent.putExtra("name", caidanMap.get("name"));
            intent.putExtra("type", "caidan");
            intent.putExtra("g1", caidanMap.get("code"));
            mActivity.startActivity(intent);
            XHClick.mapStat(mActivity, "a_search_result", "菜谱结果页", "点击菜单");
        });

        return view;
    }

    private View createShicaiView() {
        View view = null;
        if (mListShicaiData == null || mListShicaiData.size() < 1)
            return view;
        final Map<String, String> shicaiMap = mListShicaiData.get(0);
        view = LayoutInflater.from(mActivity).inflate(R.layout.c_search_result_shicai_item, null);

        ImageView cover_img = (ImageView) view.findViewById(R.id.iv_shicaiCover);
        TextView tv_shicai_name = (TextView) view.findViewById(R.id.tv_shicai_name);
        TextView tv_shicai_decrip = (TextView) view.findViewById(R.id.tv_shicai_decrip);

        setViewImage(cover_img, shicaiMap.get("imgShow"));
        setViewText(tv_shicai_name, shicaiMap.get("name"));
        setViewText(tv_shicai_decrip, shicaiMap.get("info"));

        View tagView3 = view.findViewById(R.id.iv_shicai_tag3);
        if(null != tagView3)
            tagView3.setVisibility("2".equals(shicaiMap.get("hasTaboo"))?View.VISIBLE:View.GONE);

        view.findViewById(R.id.rl_shicai).setOnClickListener(v -> {
            if (TextUtils.isEmpty(shicaiMap.get("name")))
                return;
            Intent intent = new Intent(mActivity, DetailIngre.class);
            intent.putExtra("name", shicaiMap.get("name").replace("百科", ""));
            intent.putExtra("code", shicaiMap.get("code"));
            intent.putExtra("page", "0");
            mActivity.startActivity(intent);
            XHClick.mapStat(mActivity, "a_search_result", "菜谱结果页", "点击食材");
        });

        return view;
    }

    private void setViewText(TextView v, String text) {
        if (text == null || text.length() == 0 || text.equals("hide"))
            v.setVisibility(View.GONE);
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
            v.setImageResource(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon);
            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
            v.setTag(TAG_ID, value);
            BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(mParent.getContext())
                    .load(value)
                    .setImageRound(roundImgPixels)
                    .setPlaceholderId(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
                    .setErrorId(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
                    .setSaveType(imgLevel)
                    .build();
            if (bitmapRequest != null) {
                bitmapRequest.into(getTarget(v, value));
            }
        }
        // 直接设置为内部图片
        else if (value.indexOf("ico") == 0) {
            InputStream is = v.getResources().openRawResource(Integer.parseInt(value.replace("ico", "")));
            Bitmap bitmap = UtilImage.inputStreamTobitmap(is);
            bitmap = UtilImage.toRoundCorner(v.getResources(), bitmap, roundType, roundImgPixels);
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
                    .setImageRound(roundImgPixels)
                    .setSaveType(imgLevel)
                    .build();
            if (bitmapRequest != null) {
                bitmapRequest.placeholder(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
                        .error(roundImgPixels == 0 ? imgResource : R.drawable.bg_round_user_icon)
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

                    UtilImage.setImgViewByWH(v, bitmap, imgWidth, imgHeight, imgZoom);
                }
            }
        };
    }


    class CaipuViewHolder {
        ImageViewVideo iv_caipuCover;
        TextView tv_caipu_name;
        TextView tv_caipu_decrip;
        TextView tv_caipu_firsttime;
        TextView tv_caipu_observed;
        TextView tv_caipu_collected;
        TextView tv_caipu_origin;
        TagTextView iv_itemIsSolo;
        TextView iv_itemIsFine;
        View v_caipu_item_tail;
        View v_bottom_line;
        ImageView vip;
    }

    private View createAdView(int pos) {
        View view = null;
        if (pos == 0) {
            if (adDdata.get(0) != null)
                view = SearchResultAdViewGenerater.generateTopAdView(mActivity, xhAllAdControl, adDdata.get(0));
        } else {
            int adIndex = -1;
            for (int i = 0; i < adPosList.size(); i++) {
                if (pos == adPosList.get(i)) {
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

    public interface CaipuSearchResultCallback {
        void searchMoreZhishi();

        void searchMoreCaidan();
    }


    /**
     * 计算能插入几个广告&初始化
     *
     * @return
     */
    private int generateAdPos(boolean isRefresh) {
        adPosList.clear();
        int adPos[];
        if ((mListShicaiData == null || mListShicaiData.size() == 0) && topAdHasData.get()) {
            if (isRefresh && adDdata.size()>1)
                adDdata.remove(1);
            adPos = new int[]{0, 8, 15, 23, 32, 42};
        } else {
            if (topAdHasData.get()) {
                topAdHasData.set(false);
            }
            if (isRefresh && adDdata.size()>0)
                adDdata.remove(0);
            adPos = new int[]{2, 8, 15, 23, 32, 42};
        }
        for (int i = 0; i < adDdata.size() && i < adPos.length; i++) {
            adPosList.add(adPos[i]);
        }
        int adNumCanInsert = computeAdNumCanInsert(adPosList);
        adPosList = adPosList.subList(0, adNumCanInsert);
        this.adNum = adNumCanInsert;
        return adNumCanInsert;
    }

    private int computeAdNumCanInsert(List<Integer> origin) {
        int adNum = 0;
        int num = mListCaipuData.size() + mListShicaiData.size() + mListCaidanData.size() + mListZhishiData.size();
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

    private void getAdDataInfo(boolean isRefresh) {
        if (adDdata.isEmpty() || isRefresh) {
            adDdata.clear();
            adDdata.addAll(SearchResultAdDataProvider.getInstance().getAdDataList());
        }
        xhAllAdControl = SearchResultAdDataProvider.getInstance().getXhAllAdControl();
        topAdHasData = SearchResultAdDataProvider.getInstance().HasTopAdData();
    }

}
