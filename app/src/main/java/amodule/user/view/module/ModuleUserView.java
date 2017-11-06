package amodule.user.view.module;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xiangha.R;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.helper.XHActivityManager;
import acore.tools.StringManager;


/**
 * 用户信息
 */
public class ModuleUserView extends ModuleBaseView{
    private ImageView auther_userImg;
    private TextView auther_name,right_title;
    private String userUrl="";//用户点击跳转url
    public ModuleUserView(Context context) {
        super(context,R.layout.module_user_view);
    }

    public ModuleUserView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.module_user_view);
    }

    public ModuleUserView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr,R.layout.module_user_view);
    }
    @Override
    public void initUI() {
        auther_userImg= (ImageView) findViewById(R.id.auther_userImg);
        auther_name= (TextView) findViewById(R.id.auther_name);
        right_title= (TextView) findViewById(R.id.right_title);
    }
    @Override
    public void initData(Map<String, String> map) {
        //用户信息
        if(map!=null&&!map.isEmpty()){
            auther_name.setText(map.get("text2"));
            findViewById(R.id.cusType).setVisibility(map.containsKey("iconGourmet")&&"2".equals(map.get("iconGourmet"))?VISIBLE:GONE);
            if(map.containsKey("img")&&!TextUtils.isEmpty(map.get("img")))setViewImage(auther_userImg,map.get("img"));
        }
        //url点击跳转
        if(map.containsKey("url")&& !TextUtils.isEmpty(map.get("url")))userUrl= map.get("url");
        //右标题
        if(map.containsKey("text3")&& !TextUtils.isEmpty(map.get("text3"))){
            right_title.setText(map.get("text3"));
            right_title.setVisibility(View.VISIBLE);
        }else{
            right_title.setVisibility(View.GONE);
        }
        setListener();
    }

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        super.setOnLongClickListener(l);
        findViewById(R.id.auther_userImg).setOnLongClickListener(l);
        findViewById(R.id.auther_name).setOnLongClickListener(l);
    }

    @Override
    public void setListener() {
        auther_userImg.setOnClickListener(UserOnClickListener);
        auther_name.setOnClickListener(UserOnClickListener);
    }

    /**
     * 用户位置点击监听
     */
    private OnClickListener UserOnClickListener= new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!TextUtils.isEmpty(userUrl)){
                AppCommon.openUrl(XHActivityManager.getInstance().getCurrentActivity(),userUrl,false);
                if(TextUtils.isEmpty(getStatisticId())&&mContext!=null) XHClick.mapStat(mContext,getStatisticId(),"点击头像","");
            }
        }
    };
}
