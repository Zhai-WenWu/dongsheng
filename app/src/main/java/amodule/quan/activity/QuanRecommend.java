package amodule.quan.activity;

import java.util.ArrayList;

import xh.basic.internet.UtilInternet;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.quan.db.CircleData;
import amodule.quan.db.CircleSqlite;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;

import com.xiangha.R;

/**
 * 管理员推荐
 * @author yu
 */
@SuppressLint("InflateParams")
public class QuanRecommend extends BaseActivity implements OnClickListener {

	private int userSelect = 0, userSelectItem = 100;
	private boolean isUserSelect = false;
	private String code = "";
	private ArrayList<CircleData> circleList;// 模块数据集合
	private boolean isJingHua=false;
	private String classId;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		initActivity("推荐", 2, 0, R.layout.c_view_bar_title, R.layout.a_quan_recommend);
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			code = bundle.getString("code");
			classId = bundle.getString("classId");
			isJingHua = bundle.getBoolean("isJingHua");
		}
		loadManager.hideProgressBar();
		initView();
	}

	/**
	 * 移动模块的布局
	 */
	private void initView() {
		findViewById(R.id.btn_quan_recommend_commit).setOnClickListener(this);
		findViewById(R.id.user_recommend_home).setOnClickListener(this);
		findViewById(R.id.user_recommend_cream).setOnClickListener(this);
		LinearLayout layout_recommend = (LinearLayout) findViewById(R.id.admin_recommend_conlection);
		circleList= new ArrayList<CircleData>();
		CircleSqlite sqlite = new CircleSqlite(this);
		ArrayList<CircleData> userChannelLists = sqlite.getAllCircleData();
		if (userChannelLists.size() < 1)
			return;
		for (int i = 0; i < userChannelLists.size(); i++) {
			circleList.add(userChannelLists.get(i));
		}
		for (int index = 0 ; index < circleList.size();index++) {
			CircleData list = circleList.get(index);
			View view = LayoutInflater.from(this).inflate(R.layout.a_quan_report_item, null);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ToolsDevice.dp2px(this, 47f));
			TextView tv_1 = (TextView) view.findViewById(R.id.tv_1);
			tv_1.setText(list.getName());
//			ImageView iv_1 =  (ImageView) view.findViewById(R.id.img_1);
//			if(classId != null && classId.equals(list.getCid())){
//				if (userSelectItem == index && isUserSelect) {
//					iv_1.setImageResource(R.drawable.j_select);
//					userSelect = 0;
//					isUserSelect = false;
//				} else {
//					userSelectItem = index;
//					iv_1.setImageResource(R.drawable.j_select_active);
//					userSelect = index + 1;
//					isUserSelect = true;
//				}
//			}
			layout_recommend.addView(view, params);
		}
		for (int i = 0; i < layout_recommend.getChildCount(); i++) {
			layout_recommend.getChildAt(i).setOnClickListener(onReportclick(layout_recommend, i));
		}
		if(isJingHua)
			findViewById(R.id.user_recommend_cream).setVisibility(View.GONE);
	}

	/**
	 * 点击切换
	 * 
	 * @param layout_recommend
	 * @param item
	 * @return
	 */
	private OnClickListener onReportclick(final LinearLayout layout_recommend, final int item) {
		return new OnClickListener() {

			@Override
			public void onClick(View v) {
				for (int i = 0; i < layout_recommend.getChildCount(); i++) {
					RelativeLayout child = (RelativeLayout) layout_recommend.getChildAt(i);
					ImageView imageView = (ImageView) child.getChildAt(0);
					if (item == i) {
						if (userSelectItem == item && isUserSelect) {
							imageView.setImageResource(R.drawable.j_select);
							userSelect = 0;
							isUserSelect = false;
						} else {
							userSelectItem = item;
							imageView.setImageResource(R.drawable.j_select_active);
							userSelect = i + 1;
							isUserSelect = true;
						}
					} else {
						imageView.setImageResource(R.drawable.j_select);
					}
				}
			}
		};
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_quan_recommend_commit:// 提交
			moveModel();
			break;
		case R.id.user_recommend_home:// 推荐
			AlertDialog dialog = new AlertDialog.Builder(QuanRecommend.this).setTitle("确认推荐？").setMessage("您确认要推荐本贴吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					String url = StringManager.api_setSubjectRecommend;
					String params = "code=" + code;
					ReqInternet.in().doPost(url, params, new InternetCallback(QuanRecommend.this) {

						@Override
						public void loaded(int flag, String url, Object returnObj) {
							QuanRecommend.this.finish();
						}
					});
				}
			}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			}).create();
			dialog.show();
			break;
		case R.id.user_recommend_cream:// 加精
			addCream();
			break;
		}
	}

	/**
	 * 移动模块
	 */
	private void moveModel() {
		if(userSelect==0){
			Tools.showToast(this, "你还没有选择，请先选择再提交！");
			return;
		}
		if (circleList.size() > 0 && circleList.size() >= userSelect) {
			CircleData item = circleList.get(userSelect - 1);
			if(item.getCid().equals(classId)){
				Tools.showToast(this, "已在"+item.getName());
				return;
			}
			String url = StringManager.api_changeSubjectClassify+"?subCode="+code+"&cid="+item.getCid();
			ReqInternet.in().doGet(url, new InternetCallback(QuanRecommend.this) {
				
				@Override
				public void loaded(int flag, String url, Object returnObj) {
					QuanRecommend.this.finish();
				}
			});
		}
	}
	/**
	 * 加精
	 */
	private void addCream(){
		AlertDialog dialog = new AlertDialog.Builder(QuanRecommend.this).setTitle("确认加精？").setMessage("您确认要对该贴加精吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String url =StringManager.api_addJingHua+"?code="+code;
				QuanRecommend.this.finish();
				ReqInternet.in().doGet(url, new InternetCallback(QuanRecommend.this) {
					
					@Override
					public void loaded(int flag, String url, Object returnObj) {
						if (flag < UtilInternet.REQ_OK_STRING){
							toastFaildRes(flag,true,returnObj);
						}
					}
				});
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		}).create();
		dialog.show();
	}
}
