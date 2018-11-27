package amodule.dish.tools;

import acore.logic.AppCommon;
import acore.override.activity.base.BaseActivity;
import android.view.View;
import android.widget.ImageView;

import com.xiangha.R;

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
		checkBox.setOnClickListener(v -> {
			mIsChecked = !mIsChecked;
			checkBox.setImageResource(mIsChecked ? R.drawable.i_agreement_check : R.drawable.i_agreement_check_not);
		});
		mAct.findViewById(R.id.a_agreement_text).setOnClickListener(v -> AppCommon.openUrl(mAct, atreementUrl, true));
		mAct.findViewById(R.id.a_agreement_text1).setOnClickListener(v -> AppCommon.openUrl(mAct, atreementUrl, true));
	}
	
	public boolean getIsChecked(){
		return mIsChecked;
	}
	
}
