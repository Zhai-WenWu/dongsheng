package amodule.quan.view;

import amodule.quan.activity.upload.UploadSubjectNew;
import amodule.quan.db.SubjectData;
import aplug.shortvideo.activity.PulishVideo;
import aplug.shortvideo.activity.SelectVideoActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xianghatest.R;

/**
 * 发送失败假界面
 * @author Administrator
 *
 */
public class CircleHeaderTopFakeView extends LinearLayout{

	private Context mContext;
	private SubjectData mSubjectData;
	private interfaceTopFakeView mInterfaceTopFakeView;
	public CircleHeaderTopFakeView(Context context,SubjectData subjectData) {
		super(context);
		this.mContext= context;
		this.mSubjectData=subjectData;
		LayoutInflater.from(context).inflate(R.layout.circle_header_item_other, this,true);
		initView();
	}
	public void setInterfaceTopFakeView(interfaceTopFakeView mInterfaceTopFakeView){
		this.mInterfaceTopFakeView= mInterfaceTopFakeView;
	}
	
	private void initView() {
		RelativeLayout quan_other = (RelativeLayout) findViewById(R.id.quan_other);
		String backgroupColor = "#ddfbf4cb";
		String textColor = "#fb9178";
		quan_other.setBackgroundColor(Color.parseColor(backgroupColor));
		TextView tv_content = (TextView)findViewById(R.id.tv_content);
		ImageView img_content = (ImageView)findViewById(R.id.img_content);
		ImageView img_delete = (ImageView)findViewById(R.id.img_delete);
		tv_content.setText(mSubjectData.getTitle());
		tv_content.setTextColor(Color.parseColor(textColor));
		setTag(String.valueOf(mSubjectData.getId()));
		setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//跳转发贴界面
				Intent intent = null;
				if(TextUtils.isEmpty(mSubjectData.getVideo())){
					intent = new Intent(mContext, UploadSubjectNew.class);
				}else{
					intent = new Intent(mContext, PulishVideo.class);
					intent.putExtra(SelectVideoActivity.EXTRAS_VIDEO_PATH,mSubjectData.getVideoLocalPath());
					intent.putExtra(SelectVideoActivity.EXTRAS_IMAGE_PATH,mSubjectData.getVideoSImgLocal());
				}
				intent.putExtra("id", mSubjectData.getId());
				mContext.startActivity(intent);
				if(mInterfaceTopFakeView!=null)
					mInterfaceTopFakeView.clickDel(CircleHeaderTopFakeView.this);
			}
		});
		// 是否显示图片
		img_content.setVisibility(View.VISIBLE);
		img_content.setImageResource(R.drawable.z_quan_item_failure);
		tv_content.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		// 是否显示删除控件
		img_delete.setVisibility(View.VISIBLE);
		img_delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mInterfaceTopFakeView!=null)
					mInterfaceTopFakeView.clickDel(CircleHeaderTopFakeView.this);
			}
		});
	}

	public interface interfaceTopFakeView{
		public void clickDel(View view);
	}
}
