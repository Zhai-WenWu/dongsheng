package amodule.health.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
/**
 * 用于ViewPager
 * @author Jerry
 *
 */
public class AdapterPager extends PagerAdapter {

	private View[] views;
	
	public AdapterPager(View[] views) {
		this.views=views;
	}

	@Override
	public int getCount() {
		return views.length;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
	
	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView(views[position]);
	}

	@Override
	public Object instantiateItem(View container, int position) {
		((ViewPager) container).addView(views[position]);
		return views[position];
	}

}
