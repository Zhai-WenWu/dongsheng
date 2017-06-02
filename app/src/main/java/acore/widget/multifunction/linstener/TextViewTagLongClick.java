package acore.widget.multifunction.linstener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.text.ClipboardManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.xiangha.R;

import acore.tools.Tools;

@SuppressWarnings("deprecation")
public class TextViewTagLongClick implements OnLongClickListener{
	private Context mCon;
	private TextView mTv;
	private CustomPopupWindow mPopupWindow;
	/**
	 * 长按弹框类型，
	 */
	private int typeOwer =0;
	private OnClickListener mRightClicker;
	private OnClickListener mUserClicker;//中间数据
	private String mRightBtnName = "投诉";
	private String mCopyText="";

	private int normBackColor,choseBackColor = Color.parseColor("#E3E3E3");

	private OnLongClickListener longClickListener;

	//是否删除掉star和end时前面添加@后面添加 <空格>
	public boolean isHaveAt = true,nIsHaveCopy=true;
	
	public TextViewTagLongClick(Context con,TextView tv){
		mCon = con;
		mTv = tv;
		mTv.setOnLongClickListener(this);
		normBackColor = Color.parseColor("#ffffff");
	}

	public void setOnLongClickListener(OnLongClickListener listener){
		longClickListener = listener;
	}
	
	@Override
	public boolean onLongClick(View v) {
		if(nIsHaveCopy){
			if(longClickListener != null) longClickListener.onLongClick();
			mTv.setBackgroundColor(choseBackColor);
			getPopupWindowsInstance();
			mPopupWindow.showAsPullUp(mTv, 0, -20);
			return true;
		}else
			return false;
	}
	
	public void setCopyText(String copyCon){
		mCopyText = copyCon;
	}
	
	public void setmRightBtnName(String rightBtnName) {
		mRightBtnName = rightBtnName;
	}
	
	public void setRightClicker(OnClickListener rightClicker){
		mRightClicker = rightClicker;
	}
	public void setUserClicker(OnClickListener userClicker){
		mUserClicker = userClicker;
	}
	
	public void setTypeOwer(int typeOwer){
		this.typeOwer= typeOwer;
	}
	
	public void setHaveCopyFunction(boolean isHaveCopy){
		nIsHaveCopy = isHaveCopy;
	}
	
	/**
	 * 获得PopupWindow 实例
	 */
	private void getPopupWindowsInstance() {
		if (mPopupWindow != null) {
			mPopupWindow.dismiss();
			//添加页面生成.非常重要
			if(typeOwer==0)
				initPopuptWindow(null,mRightClicker, mRightBtnName);
			else if(typeOwer==1)
				initPopuptWindow(mUserClicker,mRightClicker, mRightBtnName);
		} else {
			if(typeOwer==0)
				initPopuptWindow(null,mRightClicker, mRightBtnName);
			else if(typeOwer==1)
				initPopuptWindow(mUserClicker,mRightClicker, mRightBtnName);
		}
	}
	
	/**
	 * 创建PopupWindow
	 * @param userClicker
	 * @param rightclicker
	 * @param rightBtnName
	 */
	private void initPopuptWindow(final OnClickListener userClicker,final OnClickListener rightclicker, String rightBtnName) {
		LayoutInflater layoutInflater = LayoutInflater.from(mCon);
		
		View popupWindow = layoutInflater.inflate(R.layout.c_widget_textview_showpop, null);
		Button btnCopy = (Button) popupWindow.findViewById(R.id.copytext);
		btnCopy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissPopupWindowInstance();
				// 复制文字
				Tools.showToast(mCon, "复制成功!");
				ClipboardManager clipboard = (ClipboardManager) mCon.getSystemService(Context.CLIPBOARD_SERVICE);
				if(mCopyText != "")
					clipboard.setText(mCopyText);
				else
					clipboard.setText(mTv.getText());
			}
		});
		Button rightBtn = (Button) popupWindow.findViewById(R.id.pop_rightBtn);
		rightBtn.setText(rightBtnName);
		rightBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissPopupWindowInstance();
				if(rightclicker != null){
					rightclicker.onClick(v);
				}
			}
		});
		if(userClicker!=null){//有三个位置时，显示数据
			popupWindow.findViewById(R.id.rela_right).setVisibility(View.VISIBLE);
			popupWindow.findViewById(R.id.ll_left).setBackgroundResource(R.drawable.bg_round_black_left);
			Button user_ower=(Button) popupWindow.findViewById(R.id.user_ower);
			user_ower.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dismissPopupWindowInstance();
					userClicker.onClick(v);
				}
			});
		}else{
			popupWindow.findViewById(R.id.rela_right).setVisibility(View.GONE);
		}
		// 此处 之所以 给了 PopupWindow 一个 固定的宽度 是因为 我要让 PopupWindow 的中心位置对齐
		mPopupWindow = new CustomPopupWindow(popupWindow, 
				Tools.getDimen(mCon, R.dimen.dp_150), 
				Tools.getDimen(mCon, R.dimen.dp_35));
		// 这行代码 很重要
		mPopupWindow.setBackgroundDrawable(getDrawable());
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				mTv.setBackgroundColor(normBackColor);
			}
		});
	}
	
	/**
	 * 生成一个 透明的背景图片
	 * @return
	 */
	private Drawable getDrawable() {
		ShapeDrawable bgdrawable = new ShapeDrawable(new OvalShape());
		bgdrawable.getPaint().setColor(mCon.getResources().getColor(android.R.color.transparent));
		return bgdrawable;
	}
	
	/**
	 * 销毁 PopupWindow
	 */
	private void dismissPopupWindowInstance() {
		if (null != mPopupWindow) {
			mPopupWindow.dismiss();
			mTv.setBackgroundColor(normBackColor);
		}
	}

	public void setNormBackColor(int color){
		normBackColor = color;
	}

	public void setChoseBackColor(int choseBackColor) {
		this.choseBackColor = choseBackColor;
	}

	private class CustomPopupWindow extends PopupWindow {
		public CustomPopupWindow(View contentView, int width, int height) {
			super(contentView, width, height, false);
		}
		/**
		 * 在指定控件上方显示，默认x座标与指定控件的中点x座标相同
		 * @param anchor
		 * @param xoff
		 * @param yoff
		 */
		@SuppressLint("RtlHardcoded")
		public void showAsPullUp(View anchor, int xoff, int yoff) {
			if (anchor == null)
				return;
			// 保存anchor在屏幕中的位置
			int[] location = new int[2];
			// 保存anchor上部中点
			int[] anchorCenter = new int[2];
			// 读取位置anchor座标
			anchor.getLocationOnScreen(location);
			// 计算anchor中点
			anchorCenter[0] = location[0] + anchor.getWidth() / 2;
			anchorCenter[1] = location[1];
			super.showAtLocation(anchor, Gravity.TOP | Gravity.LEFT, anchorCenter[0] + xoff, anchorCenter[1] - mPopupWindow.getHeight() + yoff);
			// anchor.getContext().getResources().getDimensionPixelSize(R.dimen.popup_upload_height)
		}
	}

	public interface OnLongClickListener{
		public void onLongClick();
	}
}
