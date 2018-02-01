//package amodule.main.view.circle;
//
//import android.content.Context;
//import android.text.Html;
//import android.util.AttributeSet;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.xiangha.R;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import acore.widget.AdvTextSwitcher;
//import third.mall.tool.ToolView;
//
///**
// * PackageName : amodule.main.view.circle
// * Created by MrTrying on 2016/8/24 17:53.
// * E_mail : ztanzeyu@gmail.com
// */
//public class CircleHeaderStickyText extends RelativeLayout{
//	private Context mCon;
//	private TextView stickyTextview;
//	private AdvTextSwitcher stickyTextSwitcher;
//
//	private List<Map<String,String>> data;
//
//	private OnItemClickCallback mOnItemClickCallback;
//
//	private int maxTextCount = 0;
//
//	public CircleHeaderStickyText(Context context) {
//		this(context,null,0);
//		mCon = context;
//	}
//
//	public CircleHeaderStickyText(Context context, AttributeSet attrs) {
//		this(context, attrs,0);
//		mCon = context;
//	}
//
//	public CircleHeaderStickyText(Context context, AttributeSet attrs, int defStyleAttr) {
//		super(context, attrs, defStyleAttr);
//		mCon = context;
//		LayoutInflater.from(context).inflate(R.layout.a_circle_header_sticky_text,this);
//		initView();
//	}
//
//	private void initView() {
//		maxTextCount = getTextViewNum();
//		stickyTextview = (TextView) findViewById(R.id.circle_sticky_textview);
//		stickyTextSwitcher = (AdvTextSwitcher) findViewById(R.id.circle_advTextSwitcher);
//	}
//
//	public void setData(final List<Map<String,String>> noticeDatas){
//		this.data = noticeDatas;
//
//		final int noticeLength = data.size();
//		if(noticeLength == 0){
//			setVisibility(View.GONE);
//			return;
//		}
//		if (noticeLength > 1) {
//			//公告滚动
//			ArrayList<String> texts = new ArrayList<>();
//			for (int i = 0, size = noticeDatas.size(); i < size; i++) {
//				texts.add(handlerDataToHTML(noticeDatas.get(i)));
//			}
//			stickyTextSwitcher.setTexts(texts);
//			stickyTextSwitcher.setTextStillTime(7000);
//			stickyTextSwitcher.setCallback(new AdvTextSwitcher.Callback() {
//				@Override
//				public void onItemClick(int position) {
//					if(mOnItemClickCallback != null){
//						mOnItemClickCallback.onItemClick(position,noticeDatas.get(position));
//					}
//				}
//			});
//		} else {
//			//单条数据
//			final Map<String, String> map = noticeDatas.get(0);
//			String text = handlerDataToHTML(map);
//			stickyTextview.setText(Html.fromHtml(text));
//			stickyTextview.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					if(mOnItemClickCallback != null){
//						mOnItemClickCallback.onItemClick(0,map);
//					}
//				}
//			});
//		}
//		stickyTextSwitcher.setVisibility(noticeLength > 1 ? View.VISIBLE : View.GONE);
//		stickyTextview.setVisibility(noticeLength > 1 ? View.GONE : View.VISIBLE);
//		setVisibility(View.VISIBLE);
//	}
//
//	/** 根据需求将String转换成HTML标签的形式 */
//	private String handlerDataToHTML(Map<String, String> map) {
//		String name = map.get("name");
//		String commentNum = map.get("commentNum");
//		String data = "";
//		int num = getTextViewNum();
//		if (name.length() <= num) {
//			if (name.length() <= num - 2 - commentNum.length()) {
//				data = "<font color='#333333'>" + name + "</font>" + "<font color='#999999'> - "
//						+ commentNum + "评论</font>";
//			} else {
//				data = "<font color='#333333'>" + name.substring(0, num - 2 - commentNum.length()) + "...</font>" + "<font color='#999999'>"
//						+ commentNum + "评论</font>";
//			}
//		} else {
//			data = "<font color='#333333'>" + name.substring(0, num) + "...</font>" + "<font color='#999999'>"
//					+ commentNum + "评论</font>";
//		}
//		return data;
//	}
//
//	/**
//	 * 获取值得买每行的字数
//	 *
//	 * @return
//	 */
//	private int getTextViewNum() {
//		WindowManager wm = (WindowManager) mCon.getSystemService(Context.WINDOW_SERVICE);
//		int tv_distance = (int) this.getResources().getDimension(R.dimen.dp_14);
//		int distance = (int) this.getResources().getDimension(R.dimen.dp_55);
//
//		int screenWidth = wm.getDefaultDisplay().getWidth();
//		int tv_width = screenWidth - distance;
//		int tv_pad = ToolView.dip2px(mCon, 1.0f);
//		int num = (tv_width + tv_pad) / (tv_distance + tv_pad);
//		return num;
//	}
//
//	public interface OnItemClickCallback{
//		public void onItemClick(int position,Map<String,String> map);
//	}
//
//	public OnItemClickCallback getmOnItemClickCallback() {
//		return mOnItemClickCallback;
//	}
//
//	public void setmOnItemClickCallback(OnItemClickCallback mOnItemClickCallback) {
//		this.mOnItemClickCallback = mOnItemClickCallback;
//	}
//}
