package acore.widget;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import acore.tools.Tools;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

import com.xianghatest.R;

/**
 *
 * @describe
 */
public class TextSwitchView extends TextSwitcher implements ViewFactory {
	private int index = -1;
	private Context context;
	private ArrayList<Map<String,String>> list;
	private TextswithCallBack callback;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				index = next(); // 取得下标值
				updateText(); // 更新TextSwitcherd显示内容;
				break;
			}
		};
	};
	
	private Timer timer; //
	private ArrayList<String> resources= new ArrayList<String>();
	public TextSwitchView(Context context) {
		super(context);
		this.context = context;
		init();
	}
	public TextSwitchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}

	private void init() {
		if (timer == null)
			timer = new Timer();
		this.setFactory(this);
		this.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.in_animation));
		this.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.out_animation));
	}

	public void setResources(ArrayList<Map<String,String>> list) {
		this.list = list;
		for (int i = 0,size=list.size(); i < size; i++) {
			String temp="<font color='#333333'>"+list.get(i).get("name")+"</font>";
			if(!TextUtils.isEmpty(list.get(i).get("commentNum"))&&Integer.parseInt(list.get(i).get("commentNum"))>=0){
				temp+="<font color='#999999'> - "+list.get(i).get("commentNum")+"评论</font>";
			}
			resources.add(temp);
		}
	}

	public void setTextStillTime(long time) {
		if (timer == null) {
			timer = new Timer();
		} else {
			timer.scheduleAtFixedRate(new MyTask(), 0, time);// 每3秒更新
		}
	}
	public void setInterface(TextswithCallBack callback){
		this.callback=callback;
	}
	public void setTextStart(){
		index=0;
		updateText();
	}

	private class MyTask extends TimerTask {
		@Override
		public void run() {
			mHandler.sendEmptyMessage(1);
		}
	}

	private int next() {
		int flag = index + 1;
		if (flag > resources.size() - 1) {
			flag = flag - resources.size();
		}
		return flag;
	}

	private void updateText() {
		this.setText(Html.fromHtml(resources.get(index)));
	}

	@Override
	public View makeView() {
		final TextView tv = new TextView(context);
		tv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				callback.onClickIndex(index);
			}
		});
		tv.setTextSize(Tools.getDimenSp(context, R.dimen.sp_14));
		return tv;
	}
	public interface TextswithCallBack{
		public void onClickIndex(int index);
	}
	public void clearData(){
		resources.clear();
		if(list!=null){ 
			list.clear();
			list=null;
			callback=null;
			index=-1;
			timer=null;
		}
	}
}
