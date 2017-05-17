package aplug.datepicker.adapter;

/**滚轮适配器接�?*/
public interface WheelAdapter {
	/**
	 * 得到items的数�?
	 * @return item的数�?
	 */
	public int getItemsCount();
	
	/**
	 * 通过index得到对应的item
	 * @param index item的index
	 * @return item或�?�null
	 */
	public String getItem(int index);
	
	
	/**
	 * 得到item的最大长度，用来决定滚轮的宽�?
	 * 若返�?-1，则将使用默认滚轮宽�?
	 * @return
	 */
	public int getMaximumLength();

}
