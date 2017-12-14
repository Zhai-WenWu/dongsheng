package amodule.home.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.DividerItemDecoration;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseLoginActivity;
import acore.tools.Tools;
import acore.widget.rvlistview.RvListView;
import acore.widget.rvlistview.adapter.RvBaseSimpleAdapter;
import amodule.article.activity.edit.ArticleEidtActivity;
import amodule.article.activity.edit.VideoEditActivity;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.user.activity.login.LoginByAccout;

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
        int[] images = {R.drawable.pulish_subject_popup, R.drawable.pulish_dish_popup, R.drawable.pulish_article_popup, R.drawable.pulish_video_popup};
        String[] texts = {"晒美食", "写菜谱", "发文章", "短视频"};
        List<Map<String, String>> data = new ArrayList<>();
        for (int index = 0; index < images.length; index++) {
            Map<String, String> map = new HashMap<>();
            map.put("image", String.valueOf(images[index]));
            map.put("text", texts[index]);
            data.add(map);
        }
        RvBaseSimpleAdapter adapter = new RvBaseSimpleAdapter(getContext(), data,
                R.layout.a_home_popup_item,
                new String[]{"image", "text"},
                new int[]{R.id.image, R.id.text});
        rvListView.setAdapter(adapter);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider_black));
        rvListView.addItemDecoration(itemDecoration);
        rvListView.setOnItemClickListener((view1, holder, position) -> {
            //统计
            if (!TextUtils.isEmpty(statictusID) && position < texts.length) {
                XHClick.mapStat(getContext(), statictusID, texts[position], "");
            }
            mPopupWindow.dismiss();
            switch (position) {
                case 0:
                    Intent subIntent = new Intent(getContext(), UploadSubjectNew.class);
                    subIntent.putExtra("skip", true);
                    getContext().startActivity(subIntent);
                    break;
                case 1:
                    if (LoginManager.isLogin()) {
                        getContext().startActivity(new Intent(getContext(), UploadDishActivity.class));
                    } else {
                        gotoLogin();
                    }
                    break;
                case 2:
                    if (!LoginManager.isLogin()) {
                        gotoLogin();
                    } else if (LoginManager.isBindMobilePhone()) {
                        getContext().startActivity(new Intent(getContext(), ArticleEidtActivity.class));
                    } else {
                        BaseLoginActivity.gotoBindPhoneNum(getContext());
                    }
                    break;
                case 3:
                    if (!LoginManager.isLogin()) {
                        gotoLogin();
                    } else if (LoginManager.isBindMobilePhone()) {
                        getContext().startActivity(new Intent(getContext(), VideoEditActivity.class));
                    } else
                        BaseLoginActivity.gotoBindPhoneNum(getContext());
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
}
