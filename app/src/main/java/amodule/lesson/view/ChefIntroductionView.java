package amodule.lesson.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.stat.StatModel;
import acore.logic.stat.StatisticsManager;
import acore.tools.ColorUtil;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.TagTextView;
import acore.widget.banner.CardPageTransformer;
import acore.widget.banner.SLooperViewPager;
import acore.widget.rcwidget.RCRelativeLayout;
import amodule.topic.adapter.OverlayBaseAdapter;

/**
 * Description :
 * PackageName : amodule.lesson.view
 * Created by mrtrying on 2018/12/4 19:03.
 * e_mail : ztanzeyu@gmail.com
 */
public class ChefIntroductionView extends FrameLayout {
    final int LAYOUT_ID = R.layout.view_chef_introduction;
    private SLooperViewPager mSLooperViewPager;
    private TextView mTitle, mSubTitle;
    private List<Map<String, String>> authorList;
    private int mViewPageHeight;
    private Context mContext;

    public ChefIntroductionView(@NonNull Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public ChefIntroductionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public ChefIntroductionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        this.mContext = context;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        LayoutInflater.from(context).inflate(LAYOUT_ID, this);
        mSLooperViewPager = findViewById(R.id.overlay_view);
        mTitle = findViewById(R.id.title);
        mSubTitle = findViewById(R.id.sub_title);
    }

    public void setData(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            setVisibility(GONE);
            return;
        }
        //title
        setTitleData(data);
        //设置名厨介绍
        authorList = StringManager.getListMapByJson(data.get("info"));
        if (authorList.isEmpty()) {
            setVisibility(GONE);
            return;
        }
        authorList.get(0).put("showTip", "2");
        mViewMap.clear();
        OverlayAdapter adapter = new OverlayAdapter();
        adapter.setData(authorList);
        mSLooperViewPager.setOffscreenPageLimit(5);
        mSLooperViewPager.setAdapter(adapter);
        mSLooperViewPager.setPageTransformer(true,
                new CardPageTransformer(Tools.getDimen(getContext(), R.dimen.dp_26), Tools.getDimen(getContext(), R.dimen.dp_7)));

        mSLooperViewPager.setCurrentItem(0);
        setVisibility(VISIBLE);
    }

    @NonNull
    private View createPagerView() {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_chef_introduction, mSLooperViewPager, false);
        RelativeLayout root = view.findViewById(R.id.root);
        RCRelativeLayout shadowLayout = view.findViewById(R.id.shadow_layout);
        root.setPadding(root.getPaddingLeft() - shadowLayout.getPaddingLeft(), 0,
                authorList.size() == 1 ? Tools.getDimen(getContext(), R.dimen.dp_20) - shadowLayout.getPaddingRight() : root.getPaddingRight() - shadowLayout.getPaddingRight(), 0);
        setPadding(0, 0, 0, Tools.getDimen(getContext(), R.dimen.dp_15) - shadowLayout.getPaddingBottom());
        if (mViewPageHeight == 0) {
            mViewPageHeight = Tools.getMeasureHeight(view);
            mSLooperViewPager.getLayoutParams().height = mViewPageHeight;
        }
        return view;
    }

    private void setDataToView(Map<String, String> chefData, View view, int position) {
        //埋点
        String isShow = chefData.get("isShow");
        if (!TextUtils.equals("show", isShow)) {
            StatisticsManager.saveData(StatModel.createListShowModel(mContext.getClass().getSimpleName(), "", String.valueOf(position + 1), "", chefData.get("statJson")));
            chefData.put("isShow", "show");
        }

        //设置头像
        ImageView chefImageView = view.findViewById(R.id.chef_image);
        String chefImageUrl = chefData.get("img");
        if (!TextUtils.isEmpty(chefImageUrl)) {
            Glide.with(getContext()).load(chefImageUrl).into(chefImageView);
        }
        //设置用户名
        TextView chefNameView = view.findViewById(R.id.chef_name);
        chefNameView.setText(checkStrNull(chefData.get("nickName")));
        //设置标签
        TagTextView chefTagView = view.findViewById(R.id.chef_tag);
        Map<String, String> tagMap = StringManager.getFirstMap(chefData.get("tag"));
        if (!TextUtils.isEmpty(tagMap.get("title"))) {
            chefTagView.setBackgroundColor(ColorUtil.parseColor(tagMap.get("bgColor"), ColorUtil.parseColor("#EBB54E")));
            chefTagView.setTextColor(ColorUtil.parseColor(tagMap.get("color"), Color.WHITE));
            chefTagView.setText(checkStrNull(tagMap.get("title")));
            chefTagView.setVisibility(VISIBLE);
        } else {
            chefTagView.setVisibility(GONE);
        }
        //设置简介
        TextView chef_desc = view.findViewById(R.id.chef_desc);
        chef_desc.setText(checkStrNull(chefData.get("info")));
        ImageView tipIcon = view.findViewById(R.id.tip);
        tipIcon.setVisibility(TextUtils.equals("2", chefData.get("showTip")) ? VISIBLE : GONE);
    }

    private void setTitleData(Map<String, String> data) {
        mTitle.setText(checkStrNull(data.get("title")));
        Map<String, String> comProblemMap = StringManager.getFirstMap(data.get("comProblem"));
        String subTitleStr = comProblemMap.get("text");
        if (TextUtils.isEmpty(subTitleStr)) {
            findViewById(R.id.sub_title_layout).setVisibility(GONE);
        } else {
            mSubTitle.setText(subTitleStr);
            mSubTitle.setOnClickListener(v -> AppCommon.openUrl(comProblemMap.get("url"), true));
            findViewById(R.id.sub_title_layout).setVisibility(VISIBLE);
        }
    }

    private String checkStrNull(String text) {
        return TextUtils.isEmpty(text) ? "" : text;
    }

    Map<Integer, View> mViewMap = new HashMap<>();

    class OverlayAdapter extends OverlayBaseAdapter<Map<String, String>> {

        @Override
        public Object overWriteInstantiateItem(ViewGroup container, int position) {
            View view;
            if (position != 0 && position != getCount() - 1 && mViewMap.containsKey(position)) {
                view = mViewMap.get(position);
            } else {
                view = createPagerView();
                setDataToView(getmData().get(position), view, position);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }
    }
}
