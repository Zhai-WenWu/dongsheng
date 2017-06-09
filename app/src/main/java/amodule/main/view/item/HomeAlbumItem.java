package amodule.main.view.item;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import amodule.main.activity.MainHome;

/**
 * 专辑Item
 * Created by sll on 2017/4/18.
 */

public class HomeAlbumItem extends HomeItem {

    private ImageView mImg;
    private TextView mTitle;
    private TextView mNum1;

    public HomeAlbumItem(Context context) {
        super(context, R.layout.home_albumitem);
    }

    public HomeAlbumItem(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.home_albumitem);
    }

    public HomeAlbumItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.home_albumitem);
    }

    @Override
    protected void initView() {
        super.initView();
        mImg = (ImageView) findViewById(R.id.img);
        mTitle = (TextView) findViewById(R.id.title);
        mNum1 = (TextView) findViewById(R.id.num1);
        addListener();
    }

    private void addListener() {
        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mTransferUrl)) {
                    if (mModuleBean != null && MainHome.recommedType.equals(mModuleBean.getType())) {//保证推荐模块类型
                        if(mTransferUrl.contains("?"))mTransferUrl+="&data_type="+mDataMap.get("type");
                        else mTransferUrl+="?data_type="+mDataMap.get("type");
                        mTransferUrl+="&module_type="+(isTopTypeView()?"top_info":"info");
                        XHClick.saveStatictisFile("home",getModleViewType(),mDataMap.get("type"),mDataMap.get("code"),"","click","","",String.valueOf(mPosition+1),"","");
                    }
                    AppCommon.openUrl((Activity) getContext(), mTransferUrl, false);
                }
                onItemClick();
            }
        });
    }

    @Override
    public void setData(Map<String, String> dataMap, int position) {
        super.setData(dataMap, position);
        if (mDataMap == null)
            return;
        if (mDataMap.containsKey("img") && !TextUtils.isEmpty(mDataMap.get("img"))) {
            loadImage(mDataMap.get("img"), mImg);
            findViewById(R.id.layer_view).setVisibility(View.VISIBLE);
        }else findViewById(R.id.layer_view).setVisibility(View.GONE);
        if (mDataMap.containsKey("name") && mTitle != null) {
            String albumName = mDataMap.get("name");
            if (!TextUtils.isEmpty(albumName)) {
                mTitle.setText(albumName);
                mTitle.setVisibility(View.VISIBLE);
            }
        }
        if (mDataMap.containsKey("dishNum") && mNum1 != null) {
            String favorites = handleNumber(mDataMap.get("dishNum"));
            if (!TextUtils.isEmpty(favorites)) {
                mNum1.setText(favorites + "道菜");
                mNum1.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    protected void resetView() {
        super.resetView();
        if (viewIsVisible(mTitle))
            mTitle.setVisibility(View.GONE);
        if (viewIsVisible(mNum1))
            mNum1.setVisibility(View.GONE);

    }
}
