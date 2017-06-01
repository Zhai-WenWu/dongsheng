package amodule.article.adapter;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.helper.XHActivityManager;
import acore.tools.Tools;
import amodule.article.view.ArticleCommentView;
import amodule.article.view.CommodityItemView;
import amodule.article.view.DishItemView;
import amodule.article.view.ImageShowView;
import amodule.article.view.RecommendItemView;
import amodule.article.view.VideoShowView;
import amodule.article.view.richtext.RichParser;
import amodule.article.view.richtext.RichURLSpan;

/**
 * 文章详情页adapter
 */
public class ArticleDetailAdapter extends BaseAdapter {
    public final static int Type_recommed = 1;//推荐类型
    public final static int Type_text = 2;//文本
    public final static int Type_image = 3;//图片
    public final static int Type_gif = 4;//gif
    public final static int Type_video = 5;//视频
    public final static int Type_caipu = 6;//菜谱
    public final static int Type_ds = 7;//电商
    public final static int Type_comment = 8;//电商
    private ArrayList<Map<String, String>> listMap;

    int dp_20;
    String type;
    String code;

    public ArticleDetailAdapter(ArrayList<Map<String, String>> list,String type,String code) {
        this.type = type;
        this.code = code;
        this.listMap = list;
        dp_20 = Tools.getDimen(XHActivityManager.getInstance().getCurrentActivity(), R.dimen.dp_20);
    }

    @Override
    public int getCount() {
        return listMap.size();
    }

    @Override
    public Map<String, String> getItem(int position) {
        return listMap.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Map<String, String> map = listMap.get(position);
        int dataType = getItemViewType(position);
        switch (dataType) {
            case Type_text:
                convertView = getTextView(map);
                break;
            case Type_image:
                convertView = getImageView(map,"image");
                break;
            case Type_gif:
                convertView = getImageView(map,"gif");
                break;
            case Type_video:
                convertView = getVideoView(map,position);
                break;
            case Type_caipu:
                convertView = getCaipuView(map);
                break;
            case Type_ds:
                convertView = getCommodityView(map);
                break;
            case Type_comment:
                convertView = getCommentView(map);
                break;
            case Type_recommed:
                RecommedViewHolder viewHolder = null;
                if (convertView == null
                        || !(convertView.getTag() instanceof RecommedViewHolder)) {
                    viewHolder = new RecommedViewHolder(new RecommendItemView(XHActivityManager.getInstance().getCurrentActivity()));
                    convertView = viewHolder.view;
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (RecommedViewHolder) convertView.getTag();
                }
                viewHolder.setData(map, position);
                break;
        }
        return convertView;
    }

    private View getCommentView(Map<String, String> map) {
        ArticleCommentView view = new ArticleCommentView(XHActivityManager.getInstance().getCurrentActivity());
        view.setType(type);
        view.setCode(code);
        view.setData(map);
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 11;
    }

    @Override
    public int getItemViewType(int position) {
        return Integer.parseInt(getItem(position).get("datatype"));
    }

    private View getTextView(Map<String, String> map) {
        TextView textView = new TextView(XHActivityManager.getInstance().getCurrentActivity());
        textView.setClickable(true);
        textView.setPadding(dp_20, 0, dp_20, 0);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        //处理html文本
        builder.append(RichParser.fromHtml(map.get("html")));
        //处理特效链接
        URLSpan[] urlSpans = builder.getSpans(0, builder.length(), URLSpan.class);
        for (final URLSpan span : urlSpans) {
            int spanStart = builder.getSpanStart(span);
            int spanEnd = builder.getSpanEnd(span);
            builder.removeSpan(span);
            Log.i("tzy", "url = " + span.getURL());
            builder.setSpan(new RichURLSpan(span.getURL(), Color.parseColor("#0872dd"), false) {
                @Override
                public void onClick(View widget) {
                    AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), span.getURL(), true);
                }
            }, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(builder);
        //通过setMovementMethod设置LinkMovementMethod类型来使LinkText有效
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        return textView;
    }

    private View getImageView(Map<String, String> map, String type) {
        ImageShowView imageShowView = new ImageShowView(XHActivityManager.getInstance().getCurrentActivity());
        imageShowView.setEnableEdit(false);
        imageShowView.showImage(map.get("imageUrl"), type);
        return imageShowView;
    }

    private View getVideoView(Map<String, String> map,int position) {
        VideoShowView videoShowView = new VideoShowView(XHActivityManager.getInstance().getCurrentActivity());
        videoShowView.setEnableEdit(false);
        videoShowView.setVideoDataFromService(map.get("videoUrl"),map.get("videoImageUrl"),position);
        if(mVideoClickCallBack != null)
            videoShowView.setVideoClickCallBack(mVideoClickCallBack);
        return videoShowView;
    }

    private View getCommodityView(final Map<String, String> map) {
        CommodityItemView commodityItemView = new CommodityItemView(XHActivityManager.getInstance().getCurrentActivity());
        commodityItemView.setData(map);
        commodityItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(map.get("url"))) {
                    AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), map.get("url"), true);
                }
            }
        });
        return commodityItemView;
    }

    private View getCaipuView(final Map<String, String> map) {
        DishItemView dishItemView = new DishItemView(XHActivityManager.getInstance().getCurrentActivity());
        dishItemView.setData(map);
        dishItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(map.get("url"))) {
                    AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(), map.get("url"), true);
                }
            }
        });
        return dishItemView;
    }

    private class RecommedViewHolder {
        private RecommendItemView view;

        public RecommedViewHolder(RecommendItemView itemView) {
            this.view = itemView;
        }

        public void setData(Map<String, String> map, int position) {
            if (view != null) {
                view.setData(map);
            }
        }
    }

    private VideoShowView.VideoClickCallBack mVideoClickCallBack;

    public void setVideoClickCallBack(VideoShowView.VideoClickCallBack clickCallBack) {
        mVideoClickCallBack = clickCallBack;
    }
}
