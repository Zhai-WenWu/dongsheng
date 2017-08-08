package amodule.quan.activity.upload;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.quan.activity.CircleHome;
import amodule.quan.activity.FriendQuan;
import amodule.quan.db.CircleData;
import amodule.quan.db.CircleSqlite;
import amodule.quan.db.CircleSqlite.CircleDB;
import amodule.quan.db.SubjectData;
import amodule.quan.db.SubjectSqlite;
import amodule.quan.tool.UploadSubjectBottomControl;
import amodule.quan.tool.UploadSubjectControl;
import amodule.quan.view.BarUploadSubjectNew;
import amodule.quan.view.BarUploadSubjectNew.BarUploadSubInterface;
import amodule.quan.view.ImgTextCombineLayout;
import amodule.quan.view.UploadSubjectContent;
import amodule.user.activity.login.LoginByAccout;
import aplug.imageselector.ImageSelectorActivity;
import aplug.imageselector.constant.ImageSelectorConstant;

import static amodule.quan.activity.FriendQuan.REQUEST_CODE_QUAN_FRIEND;

//import android.util.Log;

/**
 * 发美食贴
 */
public class UploadSubjectNew extends BaseActivity implements OnClickListener{

	// 用于记录失败的数据;
	private SubjectSqlite sqlHelp;

	//-----------内容控制----------
	private UploadSubjectContent upSubContent;
	private BarUploadSubjectNew bar;
	private UploadSubjectBottomControl uploadSubjectBottomControl;
	private View upSubContentView;
	public int titleMax = -1;
	public int titleMin = -1;
	public int contentMax = -1;
	public int contentMin = -1;
	public int imgMax = ImageSelectorConstant.DEFAULTCOUNT;
	public int imgMin = -1;
	//-----------end 内容控制----------
	/** 发布类型：贴子，回复  */
	private String uploadType = SubjectData.TYPE_UPLOAD;
	//默认发布的圈子为秀美食cid=1,选择的默认cid=“-1”
	private final String defaultChooseCid = "-1";
	private String mChooseCid = "-1",cName = "";
	private String mDishCode;
	private boolean isCanBackOnNoChoose = true, isFirst = true;

	public static final String mTongjiId = "a_post";
	private RelativeLayout activityLayout;
	private RelativeLayout quan_bar_subject_reply;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("发贴",2, 0, 0,R.layout.a_common_post_new);
		String cid = getIntent().getStringExtra("cid");
		mDishCode = getIntent().getStringExtra("dishCode");
		if(!TextUtils.isEmpty(cid)){
			mChooseCid = cid;
		}
		boolean isSkip = this.getIntent().getBooleanExtra("skip", false);
		initView();
		initData(isSkip);
		//处理状态栏引发的问题
		if(Tools.isShowTitle()) {
			activityLayout = (RelativeLayout) findViewById(R.id.activityLayout);
			quan_bar_subject_reply = (RelativeLayout) findViewById(R.id.quan_bar_subject_reply);
			activityLayout.getViewTreeObserver().addOnGlobalLayoutListener(

					new ViewTreeObserver.OnGlobalLayoutListener() {
						public void onGlobalLayout() {
							int heightDiff = activityLayout.getRootView().getHeight() - activityLayout.getHeight();
							Rect r = new Rect();
							activityLayout.getWindowVisibleDisplayFrame(r);
							int screenHeight = activityLayout.getRootView().getHeight();
							int heightDifference = screenHeight - (r.bottom - r.top);
							if (heightDifference > 200) {
								heightDifference = heightDifference - heightDiff;
							} else {
								heightDifference = 0;
							}
							quan_bar_subject_reply.setPadding(0, 0, 0, heightDifference);
						}
					});
		}
		String color = Tools.getColorStr(this,R.color.common_top_bg);
		Tools.setStatusBarColor(this, Color.parseColor(color));
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK && data != null){
			switch (requestCode){
				case UP_SUBJECT_CHOOSE_IMG:
					Log.i("FRJ","isFirst:" + isFirst + "   isCanBackOnNoChoose:" + isCanBackOnNoChoose);
					ArrayList<String> array = data.getStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT);//ArrayList<String>
					int size = array.size();
					XHClick.onEventValue(this, "uploadQuanImg", "uploadQuanImg", "新贴图片", size);
					if(isFirst && size == 0 && !isCanBackOnNoChoose ){
						UploadSubjectNew.this.finish();
					}
					isFirst = false;
					upSubContent.insertImgs(array);
					break;
				case UP_SUBJECT_CHOOSE_CIRCLE:
					mChooseCid = data.getStringExtra("chooseCid");
					setCircleRule();
					chooseNextStep(true);
					break;
				case REQUEST_CODE_QUAN_FRIEND:
					upSubContent.editAdd(data.getStringExtra(FriendQuan.FRIENDS_LIST_RESULT));
					break;
				case CHOOSE_DISH:
					uploadSubjectBottomControl.onActivityResult(data);
					break;
			}
		}
	}

	private void initView(){
		DisplayMetrics metrics = ToolsDevice.getWindowPx(this);
		//屏幕高-标题高-发布的的高-定位高-状态栏高-协议高度
//		int wH = metrics.heightPixels - Tools.getDimen(this, R.dimen.dp_45) - Tools.getDimen(this, R.dimen.dp_35)
//				- Tools.getDimen(this, R.dimen.dp_101) - Tools.getDimen(this, R.dimen.dp_20);
		int wH = metrics.heightPixels - Tools.getDimen(this,R.dimen.dp_45) - Tools.getDimen(this,R.dimen.dp_50) *2 - Tools.getStatusBarHeight(this);
		LinearLayout parentRl = (LinearLayout)findViewById(R.id.post_content);
		parentRl.setMinimumHeight(wH);
		//-----------内容控制----------
		upSubContent = new UploadSubjectContent(this);
		upSubContentView=findViewById(R.id.post_content);
		//-----------内容控制----------
		//新的布局效果
		ImageView leftImgBtn=(ImageView)findViewById(R.id.leftImgBtn);
		RelativeLayout.LayoutParams params = (LayoutParams) leftImgBtn.getLayoutParams();
		params.setMargins((int) getResources().getDimension(R.dimen.dp_7), 0, 0, 0);
		leftImgBtn.setImageResource(R.drawable.z_home_center_btn_close);
		int dp_2= (int) this.getResources().getDimension(R.dimen.dp_2);
		int dp_8= (int) this.getResources().getDimension(R.dimen.dp_8);
		leftImgBtn.setPadding(dp_2, dp_8, dp_2, dp_8);
		leftImgBtn.setOnClickListener(this);
		uploadSubjectBottomControl = new UploadSubjectBottomControl(this, StringManager.api_agreementOriginal);
		uploadSubjectBottomControl.setOnBottomListener(new UploadSubjectBottomControl.OnBottomListener() {
			@Override
			public void onChoseFollowDish() {
				findViewById(R.id.nextStep).setVisibility(View.GONE);
				findViewById(R.id.upload).setVisibility(View.VISIBLE);
				mChooseCid = "1";
			}

			@Override
			public void onClearFollowDish() {
				findViewById(R.id.nextStep).setVisibility(View.VISIBLE);
				findViewById(R.id.upload).setVisibility(View.GONE);
				mChooseCid = defaultChooseCid;
			}
		});
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.nextStep).setOnClickListener(this);
		findViewById(R.id.upload).setOnClickListener(this);

	}

	private void initData(boolean isSkip){
		boolean isHaveTitle = false,isNewSubject = false,isDraft = false;
		sqlHelp = SubjectSqlite.getInstance(this);
		SubjectData upData = new SubjectData();
		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			// 是回复贴还是新贴
			if (bundle.containsKey("subjectCode") && !TextUtils.isEmpty(bundle.getString("subjectCode"))) {
				uploadType = SubjectData.TYPE_REPLY;
			}else{
				uploadType = SubjectData.TYPE_UPLOAD;
			}
			int id;
			id = bundle.getInt("id",-1);
			if(id == -1){
				//只有发贴才读草稿
				if(SubjectData.TYPE_UPLOAD.equals(uploadType)){
					upData = sqlHelp.getLastDraft(mChooseCid,false);
					if(isSkip && upData != null){
						isSkip = false;
					}
					if(upData == null){
						upData = new SubjectData();
						isNewSubject = true;
					}else{
						isDraft = true;
					}
				}else{
					upData.setCid(mChooseCid);
				}
			}else{
				upData = sqlHelp.selectById(id);
				upData.setId(id);
				mChooseCid = upData.getCid();
			}
			// 如果里面包含“title”键则设置默认值
			if (bundle.containsKey("title")) {
				String title = bundle.getString("title");
				upData.setTitle(title);
				XHClick.onEventValue(this, "uploadQuanTitle", "uploadQuanTitle", "自带标题", title.length());
				isHaveTitle = true;

				if(!TextUtils.isEmpty(mDishCode)){
					uploadSubjectBottomControl.setIsClear(false);
					uploadSubjectBottomControl.setDishInfo(mDishCode,title);
					uploadSubjectBottomControl.setIsFollowDish(false);
				}
			}
			upData.setType(uploadType);
			// 如果是回复贴则
			if (SubjectData.TYPE_REPLY.equals(uploadType)) {
				upData.setCode(bundle.getString("subjectCode"));
				//设置subjectData对象title不可修改
				upData.setTitleCanModify(false);
				findViewById(R.id.ll_location).setVisibility(View.GONE);
				uploadSubjectBottomControl.stopLocation();

				TextView titleV = (TextView) findViewById(R.id.title);
				titleV.setText("跟贴");
				uploadSubjectBottomControl.setScoreLayoutVisible(false);
			}else{
				uploadSubjectBottomControl.onLocationClick();
				upData.setTitleCanModify(true);
				String[] localIsShow = FileManager.getSharedPreference(this, FileManager.xmlKey_locationIsShow);
				if(localIsShow != null && localIsShow.length > 1){
					if("1".equals(localIsShow[1])){
						uploadSubjectBottomControl.onLocationClick();
					}
				}
			}
		}
		if(defaultChooseCid.equals(mChooseCid) && SubjectData.TYPE_UPLOAD.equals(uploadType)){
			findViewById(R.id.nextStep).setVisibility(View.VISIBLE);
		}else{
			findViewById(R.id.upload).setVisibility(View.VISIBLE);
		}

		bar = new BarUploadSubjectNew(this,new BarUploadSubInterface() {

			@Override
			public void onAddImgClick() {
				imageSelector(false);
			}

			@Override
			public void onWatermarkClick(boolean state) {
//				upSubContent.setWaterMark(state);
			}
		});

		upSubContent.init(upData, upSubContentView,bar,isHaveTitle,isNewSubject,isDraft);
		setCircleRule();
		if(isSkip){
			imageSelector(TextUtils.isEmpty(mDishCode));
		}
	}

	private void imageSelector(boolean isCanBack){
		isCanBackOnNoChoose = isCanBack;
		int num = upSubContent.getBitmapNum();
		int selectNum = imgMax - num;
		if(selectNum > 0){
			Intent intentAddImg = new Intent();
			intentAddImg.putExtra(ImageSelectorConstant.EXTRA_SELECT_MODE, ImageSelectorConstant.MODE_MULTI);
			intentAddImg.putExtra(ImageSelectorConstant.EXTRA_SELECT_COUNT, selectNum);
//			intentAddImg.putExtra(ImageSelectorConstant.IS_CAN_BACK_ON_NO_CHOOSE, isCanBack);
			intentAddImg.setClass(UploadSubjectNew.this, ImageSelectorActivity.class);
			startActivityForResult(intentAddImg, UP_SUBJECT_CHOOSE_IMG);
		}else{
			Tools.showToast(this, "最多可以选择" + imgMax + "张图片");
		}
	}

	/**
	 * 获取当前发贴rule
	 */
	public void setCircleRule(){
		resetRule();
		if(defaultChooseCid.equals(mChooseCid)){
//			mChooseCid = defaultCid;
			return;
		}
		final String TITLELENGTH = "titleLength";
		final String CONTENTLENGTH = "contentLength";
		final String IMGLEGNTH = "imgNumber";
		CircleSqlite sqlite = new CircleSqlite(this);
		CircleData circleData = sqlite.select(CircleDB.db_cid , mChooseCid);
		cName = circleData.getName();
		String ruleJson = circleData.getRule();
		ArrayList<Map<String,String>> ruleDataArray = StringManager.getListMapByJson(ruleJson);

		for(Map<String,String> rule : ruleDataArray){
			String method = rule.get("method");
			String type = rule.get("type");
			String condition = rule.get("condition");
			//判断当前的rule数据是否是当前的发布类型
			if(uploadType.equals(type)){
				if(TITLELENGTH.equals(method)){
					if("<".equals(condition)){
						titleMax = getValue(rule) -1;
					}else if(">".equals(condition)){
						titleMin = getValue(rule) + 1;
					}
				}else if(CONTENTLENGTH.equals(method)){
					if("<".equals(condition)){
						contentMax = getValue(rule) - 1;
					}else if(">".equals(condition)){
						contentMin = getValue(rule) + 1;
					}
				}else if(IMGLEGNTH.equals(method)){
					if("<".equals(condition)){
						imgMax = getValue(rule) - 1;
					}else if(">".equals(condition)){
						imgMin = getValue(rule) + 1;
					}
				}
			}
		}
		if(titleMax > 0)
			upSubContent.setTitleMaxNumber(titleMax);
	}

	//重置
	protected void resetRule() {
		titleMax = -1;
		titleMin = -1;
		contentMax = -1;
		contentMin = -1;
		imgMax = ImageSelectorConstant.DEFAULTCOUNT;
		imgMin = -1;
	}

	/**
	 * 获取规则中的value
	 * */
	private int getValue(Map<String, String> rule) {
		int value = -1;
		ArrayList<Map<String,String>> valueArray = StringManager.getListMapByJson(rule.get("value"));
		if(valueArray.size() > 0){
			String valueStr = valueArray.get(0).get("");
			if(valueStr != null){
				value = Integer.parseInt(valueStr);
			}
		}
		return value;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.leftImgBtn:
				UploadSubjectNew.this.onBackPressed();
				XHClick.mapStat(this, mTongjiId, "取消点击", "");
				break;
			case R.id.upload: //发布
				XHClick.mapStat(this, mTongjiId, "发布点击", "");
				chooseNextStep(true);
				break;
			case R.id.nextStep: //下一步
				XHClick.mapStat(this, mTongjiId, "下一步点击", "");
				mChooseCid = defaultChooseCid;
				setCircleRule();
				chooseNextStep(false);
				break;
			case R.id.ll_location: //地理位置
				XHClick.mapStat(this, mTongjiId, "地址点击", "");
				uploadSubjectBottomControl.onLocationClick();
				FileManager.setSharedPreference(this, FileManager.xmlKey_locationIsShow, uploadSubjectBottomControl.getIsLocation() ? "2" : "1");
				break;
			case R.id.back:
				this.onBackPressed();
				break;
		}
	}

	/**
	 * 点击发布后和真正发布前所需要做的事情
	 */
	private void chooseNextStep(boolean isHasCid){
		//获取数据
		SubjectData subjectData = upSubContent.getData();
		if(TextUtils.isEmpty(mDishCode))
			mDishCode = uploadSubjectBottomControl.getDishCode();
		subjectData.setDishCode(mDishCode);
		subjectData.setScoreNum(uploadSubjectBottomControl.getScoreNum());
		//只有发贴才存草稿
		if(SubjectData.TYPE_UPLOAD.equals(uploadType)){
			subjectData.setType(uploadType);
		}else{ //楼层回复
			subjectData.setType(SubjectData.TYPE_REPLY);
		}
		saveInDb(subjectData);

		String data = checkUploadData(subjectData);
		//检查通过，开始发布
		if(TextUtils.isEmpty(data)){
			if(isHasCid){
				XHClick.onEventValue(this, "uploadQuanClick", "uploadQuanClick", "成功", 100);
				starUpload(subjectData);
			}else{
				XHClick.onEventValue(this, "uploadQuanClick", "uploadQuanClick", "选择圈子", 0);
				Intent it = new Intent(this,UploadChooseCircle.class);
				it.putExtra("cid",mChooseCid);
				startActivityForResult(it, UP_SUBJECT_CHOOSE_CIRCLE);
			}
		}else{ //检查没通过
			XHClick.onEventValue(this, "uploadQuanClick", "uploadQuanClick", "data", 0);
			Tools.showToast(this, data);
		}
	}

	private void starUpload(SubjectData subjectData){
		//发送中
		this.setResult(Activity.RESULT_OK);
		UploadSubjectNew.this.finish();
		if(TextUtils.isEmpty(subjectData.getCode())
				&& (UploadSubjectControl.getInstance().getUploadCallback() == null || !subjectData.getCid().equals(getIntent().getStringExtra("cid")))){
			Intent intent = new Intent(this,CircleHome.class);
			intent.putExtra("cid", subjectData.getCid());
			startActivity(intent);
		}
		UploadSubjectControl upSubCon = UploadSubjectControl.getInstance();
		if (subjectData != null) {
			upSubCon.startUpload(subjectData);
			XHClick.track(this,"发美食贴");
		}
	}

	/**
	 * 上传数据校验
	 * @return 失败提示，成功为""
	 */
	private String checkUploadData(SubjectData subjectData){
		String title = subjectData.getTitle();
		if(SubjectData.TYPE_UPLOAD.equals(subjectData.getType())){
			if (TextUtils.isEmpty(title)) {
				XHClick.onEventValue(this, "uploadQuanClick", "uploadQuanClick", "请输入标题", 0);
				return "请输入标题";
			}else if(title.length() < titleMin){
				XHClick.onEventValue(this, "uploadQuanClick", "uploadQuanClick", "标题至少" + titleMin + "个字符", title.length());
				return "标题至少" + titleMin + "个字符";
			}
			int imgNum = upSubContent.getImagSize();
			if (imgMin > 0 && imgNum <= 0) {
				XHClick.onEventValue(this, "uploadQuanClick", "uploadQuanClick", cName + "版块是带图版块哦，请带图发贴", 0);
				return cName + "版块是带图版块哦，请带图发贴";
			}else if(imgNum >= 0 && imgNum < imgMin){
				XHClick.onEventValue(this, "uploadQuanClick", "uploadQuanClick", cName + "版块至少选择 " + imgMin + " 张图", imgNum);
				return cName + "版块至少选择 " + imgMin + " 张图";
			}
		}
		int contentNum = upSubContent.getContentNowNumber();
		if(contentMin > 0 && contentNum <= 0){
			XHClick.onEventValue(this, "uploadQuanClick", "uploadQuanClick", "请输入内容", 0);
			return "请输入内容";
		}else if(contentNum < contentMin){
			XHClick.onEventValue(this, "uploadQuanClick", "uploadQuanClick", "内容至少" + contentMin + "个字符", contentNum);
			return "内容至少" + contentMin + "个字符";
		}else if(contentMax >= 0 && contentNum > contentMax){
			XHClick.onEventValue(this, "uploadQuanClick", "uploadQuanClick", "内容至多" + contentMax + "个字符", contentNum);
			return "内容至多" + contentMax + "个字符";
		}else if(!uploadSubjectBottomControl.getIsChecked()){
			XHClick.onEventValue(this, "uploadQuanClick", "uploadQuanClick", "没有确认香哈协议", 0);
			return "同意原创内容发布协议后才能提交哦";
		}
		if(!LoginManager.isLogin()){
			XHClick.onEventValue(this, "uploadQuanClick", "uploadQuanClick", "没登录", 0);
			Intent intent = new Intent(this, LoginByAccout.class);
			startActivity(intent);
			return "请登录";
		}
		return "";
	}

	@Override
	public void onBackPressed() {
		//如果bar没处理返回键，则我方处理
		if(!bar.onBackPressed()){
			super.onBackPressed();
			SubjectData subjectData = upSubContent.getData();
			saveInDb(subjectData);
			uploadSubjectBottomControl.stopLocation();
		}
	}


	/** 保存草稿到数据库 */
	private void saveInDb(SubjectData subjectData){
		if(subjectData != null){
			boolean hasContent = false;
			ArrayList<Map<String,String>> contentArray = subjectData.getContentArray();
			if(contentArray != null && contentArray.size() > 0){
				Map<String,String> content =  contentArray.get(0);
				hasContent = !(TextUtils.isEmpty(content.get(ImgTextCombineLayout.CONTENT))
						&& TextUtils.isEmpty(content.get(ImgTextCombineLayout.IMGEURL)));
			}
			if(subjectData == null || (TextUtils.isEmpty(subjectData.getTitle()) && !hasContent)){
				return;
			}
			subjectData.setUploadState(SubjectData.UPLOAD_DRAF);
			if(subjectData.getType() != SubjectData.TYPE_REPLY){
				if(mChooseCid != null && !defaultChooseCid.equals(mChooseCid)){
					subjectData.setCid(mChooseCid);
				}
				//添加位置信息
				subjectData.setLocation(uploadSubjectBottomControl.getLocationJson());
				subjectData.setIsLocation(uploadSubjectBottomControl.getIsLocation() ? "2" : "1");
			}
			sqlHelp.inser(subjectData);
		}
	}

	//开始联网发布啦
	public final static int MSG_SEND_SUBJECT_ING = 12;
	public final static int MSG_SEND_SUBJECT_FAILD = 13;
	public final static int MSG_SEND_SUBJECT_OK = 14;
	public static final int UP_SUBJECT_CHOOSE_IMG = 12200;
	public static final int UP_SUBJECT_CHOOSE_CIRCLE = 12202;
	public static final int CHOOSE_DISH = 12204;
}