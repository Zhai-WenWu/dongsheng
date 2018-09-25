package acore.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import acore.logic.stat.intefaces.OnClickListenerStat;

@SuppressLint("UseSparseArrays")
public class FlowLayout extends ViewGroup{
	public FlowLayoutAdapter adapter;
	private Context mContext;
	private List<List<View>> mAllViews = new ArrayList<List<View>>();//存储所有的View
	private List<Integer> mLineHeight = new ArrayList<Integer>();	//每一行的高度
	private Map<Integer,OnItemClickListenerById> mListenerMap;
	
	public FlowLayout(Context context)	{
		this(context, null);
	}
	
	public FlowLayout(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public FlowLayout(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		this.mContext = context;
		mListenerMap = new HashMap<Integer,OnItemClickListenerById>();
	}
	
	/**
	 * 必须调用
	 * @param data
	 * @param resource
	 * @param from
	 * @param to
	 */
	public void initFlowLayout(List<Map<String,String>> data , int resource , String[] from , int[] to){
		adapter = new FlowLayoutAdapter(mContext, data, resource, from, to);
		addChildView(data);
	}

	/**
	 * 根据data添加childView
	 * @param data
	 */
	private void addChildView(List<Map<String, String>> data) {
		for(int i = 0 ; i < data.size() ; i++){
			View view = adapter.getView( i ,  null , this);
			this.addView(view);
			Iterator<Entry<Integer, OnItemClickListenerById>> it = mListenerMap.entrySet().iterator();
			while(it.hasNext()){
				Entry<Integer, OnItemClickListenerById> entry = it.next();
				setClickListener(view, i ,entry.getKey(), entry.getValue());
			}
		}
	}
	
	/**
	 * 
	 * @param rootView
	 * @param position
	 * @param id
	 * @param listener
	 */
	private void setClickListener(View rootView,final int position,Integer id, final OnItemClickListenerById listener) {
		View clickView;
		if(id != 0){
			clickView = rootView.findViewById(id);
		}else
			clickView = rootView;
		clickView.setOnClickListener(new OnClickListenerStat(getContext(),getClass().getSimpleName(),"按钮"+(position + 1)) {
			@Override
			public void onClicked(View v) {
				listener.onClick(v, position);
			}
		});
	}
	
	/**
	 * 添加childView的click
	 * @param id	设置click的id(如果id为0则设置childView的click)
	 * @param listener		需要这是的listener
	 */
	public void setOnItemClickListenerById(Integer id , final OnItemClickListenerById listener){
		mListenerMap.put(id, listener);
	}
	
	//刷新FlowLayout布局
	public void refreshLayout(){
		adapter.notifyDataSetChanged();
		this.removeAllViews();
		addChildView(adapter.data);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)	{
		int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
		int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
		int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
		int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

		// wrap_content
		int width = 0;
		int height = 0;

		// 记录每一行的宽度与高度
		int lineWidth = 0;
		int lineHeight = 0;

		// 得到内部元素的个数
		int cCount = getChildCount();

		for (int i = 0; i < cCount; i++){
			View child = getChildAt(i);
			// 测量子View的宽和高
			measureChild(child, widthMeasureSpec, heightMeasureSpec);
			// 得到LayoutParams
			MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

			// 子View占据的宽度
			int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
			// 子View占据的高度
			int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

			// 换行
			if (lineWidth + childWidth > sizeWidth - getPaddingLeft() - getPaddingRight()){
				// 对比得到最大的宽度
				width = Math.max(width, lineWidth);
				// 重置lineWidth
				lineWidth = childWidth;
				// 记录行高
				height += lineHeight;
				lineHeight = childHeight;
			} 
			// 未换行
			else	{
				// 叠加行宽
				lineWidth += childWidth;
				// 得到当前行最大的高度
				lineHeight = Math.max(lineHeight, childHeight);
			}
			// 最后一个控件
			if (i == cCount - 1){
				width = Math.max(lineWidth, width);
				height += lineHeight;
			}
		}

		setMeasuredDimension(
				modeWidth == MeasureSpec.EXACTLY ? sizeWidth : width + getPaddingLeft() + getPaddingRight(),
				modeHeight == MeasureSpec.EXACTLY ? sizeHeight : height + getPaddingTop()+ getPaddingBottom()
		);
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b){
		mAllViews.clear();
		mLineHeight.clear();
		// 当前ViewGroup的宽度
		int width = getWidth();
		int lineWidth = 0;
		int lineHeight = 0;
		List<View> lineViews = new ArrayList<View>();
		int cCount = getChildCount();
		for (int i = 0; i < cCount; i++){
			View child = getChildAt(i);
			MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
			int childWidth = child.getMeasuredWidth();
			int childHeight = child.getMeasuredHeight();
			// 如果需要换行
			if (childWidth + lineWidth + lp.leftMargin + lp.rightMargin > width - getPaddingLeft() - getPaddingRight()){
				// 记录LineHeight
				mLineHeight.add(lineHeight);
				// 记录当前行的Views
				mAllViews.add(lineViews);

				// 重置我们的行宽和行高
				lineWidth = 0;
				lineHeight = childHeight + lp.topMargin + lp.bottomMargin;
				// 重置我们的View集合
				lineViews = new ArrayList<View>();
			}
			lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
			lineHeight = Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin);
			lineViews.add(child);
		}// for end
		// 处理最后一行
		mLineHeight.add(lineHeight);
		mAllViews.add(lineViews);

		// 设置子View的位置
		int left = getPaddingLeft();
		int top = getPaddingTop();
		// 行数
		int lineNum = mAllViews.size();
		for (int i = 0; i < lineNum; i++)	{
			// 当前行的所有的View
			lineViews = mAllViews.get(i);
			lineHeight = mLineHeight.get(i);

			for (int j = 0; j < lineViews.size(); j++){
				View child = lineViews.get(j);
				// 判断child的状态
				if (child.getVisibility() == View.GONE){
					continue;
				}
				MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
				int lc = left + lp.leftMargin;
				int tc = top + lp.topMargin;
				int rc = lc + child.getMeasuredWidth();
				int bc = tc + child.getMeasuredHeight();
				// 为子View进行布局
				child.layout(lc, tc, rc, bc);
				left += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
			}
			left = getPaddingLeft() ; 
			top += lineHeight ; 
		}
	}

	/**
	 * 与当前ViewGroup对应的LayoutParams
	 */
	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs)	{
		return new MarginLayoutParams(getContext(), attrs);
	}
	
	public class FlowLayoutAdapter extends SimpleAdapter{
		private List<Map<String,String>> data;

		@SuppressWarnings("unchecked")
		public FlowLayoutAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from,
				int[] to) {
			super(context, data, resource, from, to);
			setData((List<Map<String, String>>) data);
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);

			return view;
		}

		public List<Map<String, String>> getData() {
			return data;
		}

		public void setData(List<Map<String, String>> data) {
			this.data = data;
		}
	}
	
    public interface OnItemClickListenerById {
        void onClick(View v , int position );
    }

}
