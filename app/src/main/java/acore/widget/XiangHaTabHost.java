package acore.widget;

import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TabHost.TabContentFactory;

import java.util.ArrayList;
import java.util.List;

import amodule.main.view.CommonBottomView;

@SuppressWarnings("deprecation")
public class XiangHaTabHost extends FrameLayout {

	private LocalActivityManager mLocalActivityManager;

	private List<ContentStrategy> mContentStrategys = new ArrayList<ContentStrategy>(2);

	private int mCurrentTab = -1;

	private View mCurrentView = null;
	private Context context;

	public XiangHaTabHost(Context context) {
		super(context);
		this.context=context;
	}

	public XiangHaTabHost(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context=context;
	}

	public XiangHaTabHost(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context=context;
	}

	public void setup(LocalActivityManager localActivityManager) {
		mLocalActivityManager = localActivityManager;
	}

	public void addContent(String tag, Intent intent) {
		mContentStrategys.add(new IntentContentStrategy(tag, intent));
	}

	public void addContent(CharSequence tag, TabContentFactory factory) {
		mContentStrategys.add(new FactoryContentStrategy(tag, factory));
	}

	public void addContent(int viewId) {
		mContentStrategys.add(new ViewIdContentStrategy(viewId));
	}

	public void setCurrentTab(int index) {
		if (index < 0 || index >= mContentStrategys.size())
			return;

		if (index == mCurrentTab)
			return;

		// notify old tab content
		if (mCurrentTab != -1)
			mContentStrategys.get(mCurrentTab).tabClosed();

		mCurrentTab = index;
		ContentStrategy contentStrategy = mContentStrategys.get(index);

		// tab content
		try {
			mCurrentView = contentStrategy.getContentView();
		}catch (Exception e){e.printStackTrace();}

		if (mCurrentView == null)
			return;
		if (mCurrentView.getParent() == null){
			addView(mCurrentView, new LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.FILL_PARENT));
		}
		mCurrentView.requestFocus();
		CommonBottomView.BottomViewBuilder.getInstance().setMainIndex(mCurrentTab);
	}

	/**
	 * How tab content is managed via an {@link Intent}: the content view is the
	 * decorview of the launched activity.
	 */
	private class IntentContentStrategy implements ContentStrategy {

		private final String mTag;
		private final Intent mIntent;

		private View mLaunchedView;

		private IntentContentStrategy(String tag, Intent intent) {
			mTag = tag;
			mIntent = intent;
		}

		@Override
		public View getContentView() {
			if (mLocalActivityManager == null) {
				throw new IllegalStateException("Did you forget to call 'public void setup(LocalActivityManager activityGroup)'?");
			}
			try {
				final Window w = mLocalActivityManager.startActivity(mTag, mIntent);
				final View wd = w != null ? w.getDecorView() : null;
				if (mLaunchedView != wd && mLaunchedView != null) {
					if (mLaunchedView.getParent() != null) {
						XiangHaTabHost.this.removeView(mLaunchedView);
					}
				}
				mLaunchedView = wd;
			}catch (Exception e){
				e.printStackTrace();
			}

			if (mLaunchedView != null) {
				mLaunchedView.setVisibility(View.VISIBLE);
				mLaunchedView.setFocusableInTouchMode(true);
				((ViewGroup) mLaunchedView).setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
			}
			return mLaunchedView;
		}

		@Override
		public void tabClosed() {
			if (mLaunchedView != null) {
				mLaunchedView.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * How tab content is managed using {@link TabContentFactory}.
	 */
	private class FactoryContentStrategy implements ContentStrategy {
		private View mTabContent;
		private final CharSequence mTag;
		private TabContentFactory mFactory;

		public FactoryContentStrategy(CharSequence tag, TabContentFactory factory) {
			mTag = tag;
			mFactory = factory;
		}

		@Override
		public View getContentView() {
			if (mTabContent == null) {
				mTabContent = mFactory.createTabContent(mTag.toString());
			}
			mTabContent.setVisibility(View.VISIBLE);
			return mTabContent;
		}

		@Override
		public void tabClosed() {
			mTabContent.setVisibility(View.GONE);
		}
	}

	/**
	 * How to create the tab content via a view id.
	 */
	private class ViewIdContentStrategy implements ContentStrategy {

		private final View mView;

		private ViewIdContentStrategy(int viewId) {
			mView = XiangHaTabHost.this.findViewById(viewId);
			if (mView != null) {
				mView.setVisibility(View.GONE);
			} else {
				throw new RuntimeException("Could not create tab content because " + "could not find view with id " + viewId);
			}
		}

		@Override
		public View getContentView() {
			mView.setVisibility(View.VISIBLE);
			return mView;
		}

		@Override
		public void tabClosed() {
			mView.setVisibility(View.GONE);
		}
	}

	private static interface ContentStrategy {

		/**
		 * Return the content view. The view should may be cached locally.
		 */
		View getContentView();

		/**
		 * Perhaps do something when the tab associated with this content has
		 * been closed (i.e make it invisible, or remove it).
		 */
		void tabClosed();
	}

	public int getCurrentTab() {
		return mCurrentTab;
	}

	public View getCurrentView() {
		return mCurrentView;
	}

	public void setCurrentView(View currentView) {
		this.mCurrentView = currentView;
	}

}