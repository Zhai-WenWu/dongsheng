package amodule.home.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.DividerItemDecoration;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.logic.stat.StatisticsManager;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.Tools;
import acore.widget.rvlistview.RvListView;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.adapter.RvBaseSimpleAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;
import amodule.article.activity.edit.ArticleEidtActivity;
import amodule.article.activity.edit.VideoEditActivity;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.user.activity.login.LoginByAccout;
import third.aliyun.work.AliyunCommon;

/**
 * Description :
 * PackageName : amodule.home.view
 * Created by MrTrying on 2017/11/13 18:39.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class HomePushIconView extends AppCompatImageView {

    private String statictusID = "";
    PopupWindow mPopupWindow;
    private List<Map<String, String>> mDatas = new ArrayList<>();

    public HomePushIconView(Context context) {
        this(context, null);
    }

    public HomePushIconView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomePushIconView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    int paddingTop = 6;
    private void initialize() {
        paddingTop = Tools.getDimen(getContext(),R.dimen.dp_3);
    }

    private void initPopuWindow() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.a_home_popup_layout, null, true);
        mPopupWindow = new PopupWindow(view, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.setFocusable(true);
        // 设置PopupWindow是否能响应外部点击事件
        mPopupWindow.setOutsideTouchable(true);

        RvListView rvListView = (RvListView) view.findViewById(R.id.rvListview);
        int[] images = {R.drawable.pulish_subject_popup, R.drawable.pulish_video_popup, R.drawable.pulish_dish_popup, R.drawable.pulish_article_popup};
        String[] texts = {"晒美食", "拍视频", "写菜谱", "发文章"};
        for (int index = 0; index < images.length; index++) {
            Map<String, String> map = new HashMap<>();
            map.put("image", String.valueOf(images[index]));
            map.put("text", texts[index]);
            mDatas.add(map);
        }
        HomePushIconAdapter adapter = new HomePushIconAdapter(getContext(), mDatas);
        rvListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        rvListView.setOnItemClickListener((view1, holder, position) -> {
            //统计
            if (!TextUtils.isEmpty(statictusID) && position < texts.length) {
                XHClick.mapStat(getContext(), statictusID, texts[position], "");
            }
            StatisticsManager.btnClick(getContext().getClass().getSimpleName(),"发布选项",texts[position]);
            mPopupWindow.dismiss();
            switch (position) {
                case 0:
                    Intent subIntent = new Intent(getContext(), UploadSubjectNew.class);
                    subIntent.putExtra("skip", true);
                    getContext().startActivity(subIntent);
                    break;
                case 1:
                    if (!LoginManager.isLogin()) {
                        gotoLogin();
                    } else if (LoginManager.isBindMobilePhone()) {
                        AliyunCommon.getInstance().startRecord(getContext());
                    } else
                        BaseLoginActivity.gotoBindPhoneNum(getContext());
                    break;
                case 2:
                    if (LoginManager.isLogin()) {
                        getContext().startActivity(new Intent(getContext(), UploadDishActivity.class));
                    } else {
                        gotoLogin();
                    }
                    break;
                case 3:
                    if (!LoginManager.isLogin()) {
                        gotoLogin();
                    } else if (LoginManager.isBindMobilePhone()) {
                        getContext().startActivity(new Intent(getContext(), ArticleEidtActivity.class));
                    } else {
                        BaseLoginActivity.gotoBindPhoneNum(getContext());
                    }
                    break;
            }
        });
        adapter.notifyDataSetChanged();
    }

    public void showPulishMenu() {
        if (mPopupWindow == null) {
            initPopuWindow();
        }
        mPopupWindow.showAsDropDown(this, 0, -paddingTop);
    }

    /** 去登录 */
    private void gotoLogin() {
        getContext().startActivity(new Intent(getContext(), LoginByAccout.class));
    }

    public void setStatictusID(String statictusID) {
        this.statictusID = statictusID;
    }

    private class HomePushIconAdapter extends RvBaseAdapter<Map<String, String>> {

        public HomePushIconAdapter(Context context, @Nullable List<Map<String, String>> data) {
            super(context, data);
        }

        @Override
        public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
            return new HomePushIconHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.a_home_popup_item, null));
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }
    }

    private class HomePushIconHolder extends RvBaseViewHolder<Map<String, String>> {

        ImageView mIcon;
        TextView mTitle;
        View mLine;
        public HomePushIconHolder(@NonNull View itemView) {
            super(itemView);
            mIcon = findViewById(R.id.image);
            mTitle = findViewById(R.id.text);
            mLine = findViewById(R.id.line);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(getResources().getDimensionPixelSize(R.dimen.dp_150), getResources().getDimensionPixelSize(R.dimen.dp_50));
            itemView.setLayoutParams(lp);
        }

        @Override
        public void bindData(int position, @Nullable Map<String, String> data) {
            if (position == mDatas.size() - 1) {
                Log.i("TAG", "bindData  GONE  : pos = " + position + "   title = " + data.get("text"));
                mLine.setVisibility(View.GONE);
            } else {
                mLine.setVisibility(View.VISIBLE);
                Log.i("TAG", "bindData  VISIBLE  : pos = " + position + "   title = " + data.get("text"));

            }
            mIcon.setImageResource(Integer.parseInt(data.get("image")));
            mTitle.setText(data.get("text"));
        }
    }
}
