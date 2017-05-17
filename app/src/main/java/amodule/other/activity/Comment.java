package amodule.other.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.xiangha.R;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.dialogManager.GoodCommentManager;
import aplug.feedback.activity.Feedback;

public class Comment extends BaseActivity implements OnClickListener{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("给香哈评价", 2, 0, R.layout.c_view_bar_title, R.layout.a_xh_comment);
		loadManager.hideProgressBar();
		findViewById(R.id.ll_comment).setOnClickListener(this);
		findViewById(R.id.ll_feekback).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.ll_comment:
			GoodCommentManager.setGoodComment("个人设置",Comment.this);
//			Uri uri = Uri.parse("market://details?id=" + getPackageName());
//			try {
//				Intent intent_grade = new Intent(Intent.ACTION_VIEW, uri);
//				startActivity(intent_grade);
//			} catch (Exception e) {
//				Tools.showToast(this, "没有安装任何应用市场哦~");
//			}
			//统计好评从个人(计数事件)
			XHClick.onEvent(this, "appClick", "好评从个人");
			break;
		case R.id.ll_feekback:
			Intent intent_feekback = new Intent(this, Feedback.class);
			intent_feekback.putExtra("from", "3");
			startActivity(intent_feekback);
			break;
		}
	}
}