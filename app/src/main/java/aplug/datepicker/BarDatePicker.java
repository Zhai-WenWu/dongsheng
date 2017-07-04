package aplug.datepicker;

import acore.tools.Tools;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aplug.datepicker.adapter.WheelAdapterArray;
import aplug.datepicker.adapter.WheelAdapterNumberic;
import aplug.datepicker.listener.OnWheelChangedListener;
import aplug.datepicker.listener.OnWheelScrollListener;
import aplug.datepicker.view.WheelView;

import com.xiangha.R;

@SuppressLint("ClickableViewAccessibility")
public class BarDatePicker extends LinearLayout{
	public final static int TYPE_DEFAULT=1; 
	public final static int TYPE_STRING=2;
	
	private Context mContext;
	private RelativeLayout date_picker_root;
	private View view;
	private WheelView year,month,day;
	private TextView ok,cannel;
	private boolean timeChanged = false , timeScrolled = false;
	
	public int starData = 1950,endData = Tools.getDate("year");
	
	private int[] days = {0,31,30,31,30,31,30,31,31,30,31,30,31};

	public BarDatePicker(Context context) {
		super(context);
		this.mContext=context;
		
	}
	
	public BarDatePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext=context;
		view=LayoutInflater.from(context).inflate(R.layout.bar_date_picker,this,true);
		init();
	}

	//初始化
	private void init() {
		date_picker_root = (RelativeLayout) view.findViewById(R.id.date_picker_root);
		
		year=(WheelView) view.findViewById(R.id.year);
		year.setAdapter(new WheelAdapterNumberic(starData,endData));
		year.addChangingListener(wheelChangedListener);
		year.addScrollingListener(scrollListener);
		
		month=(WheelView) view.findViewById(R.id.month);
		month.setAdapter(new WheelAdapterArray<String>(new String[]{"1月","2月","3月","4月","5月","6月","7月","8月","9月","10月","11月","12月"},12));
		month.addChangingListener(wheelChangedListener);
		month.addScrollingListener(scrollListener);
		
		day=(WheelView) view.findViewById(R.id.day);
		day.setAdapter(new WheelAdapterNumberic(1, 31));
		day.addChangingListener(wheelChangedListener);
		day.addScrollingListener(scrollListener);
		
		ok=(TextView) view.findViewById(R.id.date_ok);
		cannel=(TextView) view.findViewById(R.id.date_cannel);
		
		setListener();
	}

	//设置监听
	private void setListener() {
		date_picker_root.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					hide();
					break;
				}
				return true;
			}
		});
		cannel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hide();
			}
		});
	}
	
	//设置ok的监听事件
	public void setOkClickListener(OnClickListener click){
		ok.setOnClickListener(click);
	}
	
	//设置初始化数据
	public void setDate(String bothDay){
		if (bothDay == null || bothDay.equals("0000-00-00")) {
			year.setCurrentItem(0);
			month.setCurrentItem(0);
			day.setCurrentItem(0);
		} else {
			String time = bothDay.replace("年", "-");
			time = time.replace("月", "-");
			time = time.replace("日", "");
			String[] time_array = time.split("-");
			year.setCurrentItem(Integer.valueOf(time_array[0]) - starData);
			month.setCurrentItem(Integer.valueOf(time_array[1]) - 1);
			day.setCurrentItem(Integer.valueOf(time_array[2])-1);
		}
	}
	
	public String getDate(int type){
		String y=Integer.toString(year.getCurrentItem()+starData);
		String m=Integer.toString(month.getCurrentItem()+1);
		String d=Integer.toString(day.getCurrentItem()+1);
		
		if(m.length()==1)
			m="0"+m;
		if(d.length()==1)
			d="0"+d;
		String date=y+"-"+m+"-"+d;
		String date_str=y+"年"+m+"月"+d+"日";
		if(date !=null && date_str !=null)
			switch(type){
			case TYPE_DEFAULT:
				return date;
			case TYPE_STRING:
				return date_str;
			default:
				return "0000-00-00";
			}
		else
			return "0000-00-00";
	}
	
	OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
		
		@Override
		public void onScrollingStarted(WheelView wheel) {
			timeScrolled = true;
		}
		
		@Override
		public void onScrollingFinished(WheelView wheel) {
			timeScrolled = false;
			timeChanged = true;
			int time_year=year.getCurrentItem()+starData;
			int time_month=month.getCurrentItem()+1;
			if(time_month != 2)
				day.setAdapter(new WheelAdapterNumberic(1, days[time_month]));
			else{
				if(time_year % 400 ==0 || (time_year % 4 == 0 && time_year % 100 != 0))
					day.setAdapter(new WheelAdapterNumberic(1, 29));
				else
					day.setAdapter(new WheelAdapterNumberic(1, 28));
			}
			timeChanged = false;
		}
	};
	
	OnWheelChangedListener wheelChangedListener = new OnWheelChangedListener() {
		
		@Override
		public void onChanged(WheelView wheel, int oldValue, int newValue) {
			if(!timeScrolled){//滚轮滚动了
				timeChanged = true;//时间数据改变了
				
				//将时间改变标志置为false
				timeChanged = false;
			}
			
		}
	};

	
	//显示
	public void show(){
		view.setVisibility(View.VISIBLE);
	}
	
	//消失
	public void hide(){
		view.setVisibility(View.GONE);
	}
}
