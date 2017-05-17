package aplug.datepicker.adapter;


/**
 * 滚轮数字适配器
 * @author Administrator
 *
 */
public class WheelAdapterNumberic implements WheelAdapter {
	
	/**默认最大值*/
	public static final int DEFAULT_MAX_VALUE = 9;

	/**默认最小值*/
	public static final int Default_MIN_VALUE = 0;

	private int minValue;
	private int maxValue;

	private String format;

	/**
	 * 构造函数
	 */
	public WheelAdapterNumberic() {
		this(Default_MIN_VALUE, DEFAULT_MAX_VALUE);
	}

	/**
	 * 构造函数
	 */
	public WheelAdapterNumberic(int minValue, int maxValue) {
		this(minValue, maxValue, null);
	}

	/**
	 * 构造函数
	 */
	public WheelAdapterNumberic(int minValue, int maxValue, String format) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.format = format;
	}

	@Override
	public int getItemsCount() {
		// TODO Auto-generated method stub
		return maxValue - minValue + 1;
	}

	@Override
	public String getItem(int index) {
		// TODO Auto-generated method stub
		if (index >= 0 && index < getItemsCount()) {
			int value = minValue + index;
			return format != null ? String.format(format, value) : Integer
					.toString(value);
		}
		return null;
	}

	@Override
	public int getMaximumLength() {
		// TODO Auto-generated method stub
		int max = Math.max(Math.abs(maxValue), Math.abs(minValue));
		int maxLen = Integer.toString(max).length();
		if (minValue < 0) {
			maxLen++;
		}
		return maxLen;
	}

}
