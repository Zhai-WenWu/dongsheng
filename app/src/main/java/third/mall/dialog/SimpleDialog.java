package third.mall.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.xianghatest.R;

/**
 * dialog
 * @author yujian
 *
 */
public class SimpleDialog extends Dialog implements android.view.View.OnClickListener{

	protected View view;
	protected Context mContext;
	protected int height;
	
	private CloseDialogListener closeDialogListener;
	public SimpleDialog(Activity activity, int layoutID) {
		super(activity, layoutID);
		this.mContext = activity;
		this.getWindow().setBackgroundDrawableResource(R.color.c_back_transparent_80);
		/* 无标题栏 */
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		Window dialogWindow = activity.getWindow();

		/* 设置为全屏 */
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.width = WindowManager.LayoutParams.FLAG_FULLSCREEN;
		lp.height = WindowManager.LayoutParams.FLAG_FULLSCREEN;

		this.getWindow().setGravity(Gravity.BOTTOM);

		// view
		this.view = this.getLayoutInflater().inflate(layoutID, null);
		Display display = this.getWindow().getWindowManager().getDefaultDisplay();

		this.height = display.getHeight();
		this.addContentView(view, new LayoutParams(display.getWidth(), LayoutParams.WRAP_CONTENT));

		// 对话框设置监听
		this.setOnDismissListener(onDismissListener);
//		this.setCanceledOnTouchOutside(true);
		findViewById(R.id.dialog_rela).setOnClickListener(this);
		findViewById(R.id.ll_view).setOnClickListener(this);
		
	}
	public void setLatyoutHeight(){
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		int height = wm.getDefaultDisplay().getHeight();
		RelativeLayout ll_view=(RelativeLayout) findViewById(R.id.ll_view);
		RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height*2/3);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		ll_view.setLayoutParams(params);
		findViewById(R.id.dialog_close).setOnClickListener(this);
	}

	@Override
	public void show() {
		super.show();

		TranslateAnimation animation = new TranslateAnimation(0, 0, height, 0);
		animation.setDuration(500);

		// 开始动画
		view.startAnimation(animation);
	}

	/**
	 * 关闭dialog
	 */
	public void closeDialog() {

		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, height);
		animation.setDuration(500);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				myDismiss();
				if (closeDialogListener != null)
					closeDialogListener.closeDialog();
			}
		});
		view.startAnimation(animation);
	}
	private OnDismissListener onDismissListener = new OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface dialog) {
			closeDialog();
		}
	};
	public interface CloseDialogListener {
		public void closeDialog();
	}

	@Override
	public void dismiss() {
		closeDialog();
	}

	public void myDismiss() {
		super.dismiss();
	}

	public CloseDialogListener getCloseDialogListener() {
		return closeDialogListener;
	}

	public void setCloseDialogListener(CloseDialogListener closeDialogListener) {
		this.closeDialogListener = closeDialogListener;
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dialog_rela:
		case R.id.dialog_close:
			this.dismiss();
			break;

		case R.id.ll_view:
			break;
		default:
			break;
		}
	}
}
