package amodule.quan.adapter;

import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import acore.tools.ToolsDevice;
import acore.widget.DownRefreshList;
import amodule.quan.activity.CircleUser;
import amodule.user.view.UserIconView;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.xianghatest.R;

public class AdapterCircleUser extends AdapterSimple{
	private Context mContext;
	private DownRefreshList mParent;
	private CircleUserOptionListener mOptioListener;
	private List<? extends Map<String, String>> mData;
	private GestureDetector detector;
	private FlingListeber listener;
	
	public boolean isBlack = false;
	
	private View mCurrentRightShow = null;
	private boolean mIsShow = false;
	
	public AdapterCircleUser(Context context,DownRefreshList parentList,CircleUserOptionListener optioListener, List<? extends Map<String, String>> data,
								int resource, String[] from, int[] to) {
		super(parentList, data, resource, from, to);
		mContext = context;
		mParent = parentList;
		mOptioListener = optioListener;
		mData = data;
		listener = new FlingListeber();
		detector = new GestureDetector(listener);
		mParent.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(mIsShow)
					hiddenRight(mCurrentRightShow);
				return false;
			}
		});
		if(mOptioListener != null){
			mParent.setOnItemClickListener(new OnItemClickListener() {
			
				@Override
				public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
					if(mIsShow)
						hiddenRight(mCurrentRightShow);
					int headerCount = mParent.getHeaderViewsCount();
					if (position < headerCount) {
						return;
					}
					position -= headerCount;
					mOptioListener.onItemClick(position);
				}
						
			});
		}
	}

	float dowX = 0,dowY = 0;
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		final Map<String, String> map = (Map<String, String>) mData.get(position);
		if((map.containsKey("addBlack") && !TextUtils.isEmpty(map.get("addBlack"))) 
				|| (map.containsKey("addManager") && !TextUtils.isEmpty(map.get("addManager")))){
			view.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						dowX = event.getX();
						dowY = event.getY();
						break;
					case MotionEvent.ACTION_UP:
						float upX,upY;
						upX = event.getX();
						upY = event.getY();
						if(mOptioListener != null && upX == dowX && upY == dowY){
							mOptioListener.onItemClick(position);
						}
						break;
					case MotionEvent.ACTION_CANCEL:
						float canX,canY;
						canX = event.getX();
						canY = event.getY();
						if(mOptioListener != null && canX == dowX && canY == dowY){
							mOptioListener.onItemClick(position);
						}
						break;
					}
					listener.setItem(view);
					listener.setPosition(position);
					boolean is = detector.onTouchEvent(event);
					return is;
				}
			});
		}else{
			view.setOnTouchListener(null);
		}
		UserIconView userIcon = (UserIconView)view.findViewById(R.id.a_circle_user_item_user_icon);
		userIcon.setData(map.get("sex"), map.get("lv"), map.get("city"));
		return view;
	}
	
	private void setItemConfigure(final View parentView,final int position,boolean isShow){
		OnClickListener listener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.a_circle_user_item_add_black: //黑名单
//					Toast.makeText(mContext, mData.get(position).get("title").toString(), Toast.LENGTH_SHORT).show();
					mOptioListener.oAddBlack(parentView,position);
					hiddenRight(mCurrentRightShow);
					break;
				case R.id.a_circle_user_item_add_manager: //管理员
					Map<String, String> map = mData.get(position);
					String addManager = map.get("addManager");
					if(CircleUser.mAddManager.equals(addManager)){
						mOptioListener.oAddManager(position);
					}
					else{
						mOptioListener.oRemoManager(position);
					}
					hiddenRight(mCurrentRightShow);
					break;
				}
			}
		};
		TextView addBlack =(TextView)parentView.findViewById(R.id.a_circle_user_item_add_black);
		addBlack.setOnClickListener(listener);
		View addManager = parentView.findViewById(R.id.a_circle_user_item_add_manager);
		addManager.setOnClickListener(listener);
	}
	
	class FlingListeber implements GestureDetector.OnGestureListener{
		
		private View item;
		private int mPosition;

		public void setItem(View item) {
			this.item = item;
		}
		
		public void setPosition(int position){
			mPosition = position;
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
			if(e2.getX()-e1.getX()>20){
				//向右滑;
				if(item == mCurrentRightShow){
					mCurrentRightShow = null;
					mIsShow = false;
				}
				hiddenRight(item);
				item.findViewById(R.id.a_circle_user_item_line).setVisibility(View.GONE);
				setItemConfigure(item,mPosition, false);
			}else if(e1.getX()-e2.getX()>20){
				//向左滑
				if(mIsShow && item != mCurrentRightShow)
					hiddenRight(mCurrentRightShow);
				mCurrentRightShow = item;
				mIsShow = true;
				showRight(item);
				item.findViewById(R.id.a_circle_user_item_line).setVisibility(View.VISIBLE);
				setItemConfigure(item,mPosition, true);
			}

			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {}
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,float distanceX, float distanceY) {
			return false;
		}
		@Override
		public void onShowPress(MotionEvent e) {}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			//点击item;
			return false;
		}
	}
	
	private void showRight(View view) {
        Message msg = new MoveHandler().obtainMessage();
        msg.obj = view;
        msg.arg1 = view.getScrollX();
        msg.arg2 = ToolsDevice.dp2px(mContext, mRightLenght);
        msg.sendToTarget();
    }

    private void hiddenRight(View view) {
        Message msg = new MoveHandler().obtainMessage();//
        msg.obj = view;
        msg.arg1 = view.getScrollX();
        msg.arg2 = 0;
        msg.sendToTarget();
    }
	
    public int mRightLenght = 192;
    public final int mShowAll = 192;
    public final int mShowOne = 96;
    
	class MoveHandler extends Handler {
		private final int mDurationStep = 10;
		private final int mDuration = 100;
        public int stepX = 0;
        public int fromX;
        public int toX;
        public View view;
        private boolean mIsInAnimation = false;

        private void animatioOver() {
            mIsInAnimation = false;
            stepX = 0;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (stepX == 0) {
                if (mIsInAnimation) {
                    return;
                }
                mIsInAnimation = true;
                view = (View)msg.obj;
                fromX = msg.arg1;
                toX = msg.arg2;
                stepX = (int)((toX - fromX) * mDurationStep * 1.0 / mDuration);
                if (stepX < 0 && stepX > -1) {
                    stepX = -1;
                } else if (stepX > 0 && stepX < 1) {
                    stepX = 1;
                }
                if (Math.abs(toX - fromX) < 10) {
                    view.scrollTo(toX, 0);
                    animatioOver();
                    return;
                }
            }

            fromX += stepX;
            boolean isLastStep = (stepX > 0 && fromX > toX) || (stepX < 0 && fromX < toX);
            if (isLastStep) {
                fromX = toX;
            }

            view.scrollTo(fromX, 0);
            mParent.invalidate();

            if (!isLastStep) {
                this.sendEmptyMessageDelayed(0, mDurationStep);
            } else {
                animatioOver();
            }
        }
    }
	
	public interface CircleUserOptionListener{
		/**
		 * 加入黑名单
		 * @param position ： 第几个数据
		 */
		public void oAddBlack(final View parentView,final int position);
		/**
		 * 设为管理员
		 * @param position ： 第几个数据
		 */
		public void oAddManager(int position);
		/**
		 * 取消管理员
		 * @param position ： 第几个数据
		 */
		public void oRemoManager(int position);
		/**
		 * 列表项点击事件
		 * @param position
		 */
		public abstract void onItemClick(int position);
		
	}
}
