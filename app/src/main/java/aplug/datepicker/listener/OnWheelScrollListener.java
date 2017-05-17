package aplug.datepicker.listener;

import aplug.datepicker.view.WheelView;

/**
 * 轮子滚动监听器的接口
 * @author Administrator
 *
 */
public interface OnWheelScrollListener {
	/**
	 * 轮子�?始滚动时唤起回调的方�?
	 * @param wheel 状�?�改变的那个滚轮
	 */
	void onScrollingStarted(WheelView wheel);
	/**
	 * 轮子结束滚动时唤起回调的方法
	 * @param wheel 状�?�改变的那个滚轮
	 */
	void onScrollingFinished(WheelView wheel);
}
