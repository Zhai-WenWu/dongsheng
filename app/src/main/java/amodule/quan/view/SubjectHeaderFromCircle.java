package amodule.quan.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

import acore.logic.XHClick;
import amodule.quan.activity.CircleHome;
import amodule.quan.db.CircleData;
import amodule.quan.db.CircleSqlite;

/**
 * PackageName : amodule.quan.view
 * Created by MrTrying on 2016/9/28 18:04.
 * E_mail : ztanzeyu@gmail.com
 */

public class SubjectHeaderFromCircle extends RelativeLayout implements View.OnClickListener{
    private TextView tv_subFrom;
    private String classId;
    public SubjectHeaderFromCircle(Context context) {
        this(context,null);
    }

    public SubjectHeaderFromCircle(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SubjectHeaderFromCircle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.subject_header_from_circle,this);
        tv_subFrom = (TextView) findViewById(R.id.tv_subFrom);
        setOnClickListener(this);
    }

    /**
     *
     * @param classId
     */
    public void setData(String classId){
        this.classId = classId;
        try {
            CircleSqlite sqlite = new CircleSqlite(getContext());
            CircleData circleData = sqlite.select(CircleSqlite.CircleDB.db_cid, classId);
            tv_subFrom.setText("来自：" + circleData.getName());
        } catch (Exception ignored) {
            //数据库异常
        }
    }

    @Override
    public void onClick(View v) {
        if (!TextUtils.isEmpty(classId)) {
            XHClick.mapStat(getContext(), BarSubjectFloorOwnerNew.tongjiId, "圈子点击（来自某圈子）", "");
            Intent it = new Intent(getContext(), CircleHome.class);
            it.putExtra("cid", classId);
            getContext().startActivity(it);
        }
    }
}
