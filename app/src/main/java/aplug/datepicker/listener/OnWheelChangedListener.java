package aplug.datepicker.listener;

import aplug.datepicker.view.WheelView;

/**
 * 滚轮改变监听器的接口
 * @author vincent E
 *
 */
public interface OnWheelChangedListener {
	/**
	 * 当前item改变时唤起回调的方法
	 * @param wheel 状�?�改变的那个滚轮 
	 * @param oldValue 当前item的之前的�?
	 * @param newValue 当前item的新的�??
	 */
	void onChanged(WheelView wheel,int oldValue,int newValue);
}
