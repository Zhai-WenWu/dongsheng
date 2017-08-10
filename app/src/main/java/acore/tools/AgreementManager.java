package acore.tools;

import acore.logic.AppCommon;
import acore.override.activity.base.BaseActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.xianghatest.R;

public class AgreementManager {
	private BaseActivity mAct;
	private boolean mIsChecked = true;
	
	public AgreementManager(BaseActivity act,final String atreementUrl){
		mAct = act;
		View view = mAct.findViewById(R.id.a_agreement_check_img);
		if(view == null){
			return;
		}
		final ImageView checkBox = (ImageView)view;
		checkBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsChecked = !mIsChecked;
				if(mIsChecked)
					checkBox.setImageResource(R.drawable.i_agreement_check);
				else
					checkBox.setImageResource(R.drawable.i_agreement_check_not);
				
			}
		});
		mAct.findViewById(R.id.a_agreement_text).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AppCommon.openUrl(mAct, atreementUrl, true);
			}
		});
		mAct.findViewById(R.id.a_agreement_text1).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AppCommon.openUrl(mAct, atreementUrl, true);
			}
		});
	}
	
	public boolean getIsChecked(){
		return mIsChecked;
	}
	
}
