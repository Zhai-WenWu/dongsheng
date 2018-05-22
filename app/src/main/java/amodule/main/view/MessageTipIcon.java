package amodule.main.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.HashMap;
import java.util.Map;

import acore.logic.MessageTipController;
import acore.logic.XHClick;
import amodule._common.utility.WidgetUtility;
import amodule.main.StatisticData;
import amodule.main.delegate.ISetMessageTip;
import amodule.main.delegate.IStatistics;
import amodule.user.activity.MyMessage;

/**
 * Description :
 * PackageName : amodule.main.view
 * Created by mrtrying on 2018/1/5 11:18:36.
 * e_mail : ztanzeyu@gmail.com
 */
public class MessageTipIcon extends RelativeLayout implements ISetMessageTip, View.OnClickListener, IStatistics{

    private TextView tipLessTen, tipMore;

    private Map<String, StatisticData> mStatisticMap;

    public MessageTipIcon(Context context) {
        super(context);
        initialize();
    }

    public MessageTipIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public MessageTipIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        LayoutInflater.from(getContext()).inflate(R.layout.message_tip_icon_layout, this);
        tipLessTen = (TextView) findViewById(R.id.tv_tab_msg_num);
        tipMore = (TextView) findViewById(R.id.tv_tab_msg_tow_num);

        setOnClickListener(this);
    }

    public void setMessage(int messgeTip) {
        String lessTenValue = messgeTip > 0 && messgeTip < 10 ? String.valueOf(messgeTip) : "";
        String moreValue = "";
        if (messgeTip >= 10 && messgeTip < 100) {
            moreValue = String.valueOf(messgeTip);
        } else if (messgeTip >= 100) {
            moreValue = "99+";
        }
        WidgetUtility.setTextToView(tipLessTen, lessTenValue);
        WidgetUtility.setTextToView(tipMore, moreValue);
    }

    @Override
    public void setMessageTip(int tipCourn) {
        setMessage(tipCourn);
    }

    @Override
    public void onClick(View v) {
        getContext().startActivity(new Intent(getContext(), MyMessage.class));
        MessageTipController.newInstance().setQuanMessage(0);
        setMessage(MessageTipController.newInstance().getMessageNum());
        if (mStatisticMap != null && mStatisticMap.containsKey(type_click)){
            StatisticData data = mStatisticMap.get(type_click);
            if (data != null && !data.isEmpty())
                XHClick.mapStat(getContext(), data.getId(), data.getContentTwo(), data.getContentThree());
        }
    }

    @Override
    public void setStatisticsData(String type, StatisticData statisticData) {
        if (!TextUtils.isEmpty(type)) {
            if (mStatisticMap == null)
                mStatisticMap = new HashMap<>();
            mStatisticMap.put(type, statisticData);
        }
    }
}
