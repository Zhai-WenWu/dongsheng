package amodule.quan.view;

import android.graphics.Rect;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import amodule.quan.db.CircleData;
import amodule.quan.db.CircleSqlite;
import amodule.quan.db.CircleSqlite.CircleDB;
import amodule.quan.db.SubjectData;
import amodule.quan.tool.UploadSubjectControl;
import amodule.quan.view.ImgTextCombineLayout.ImgTextCallBack;
import core.xiangha.emj.view.EditTextShow;
import xh.basic.tool.UtilString;

import static com.xianghatest.R.id.activityLayout;

/**
 * 发美食贴内容
 */
public class UploadSubjectContent implements OnClickListener{

	/**
	 * 当前操作的图文混排控件
	 */
	public ImgTextCombineLayout imgTextCombineLayout = null;
	private LinearLayout content_linear;
	private TextView ed_uploadSubjectTitle;
	private BaseActivity act;
	private String title;
	public ImgTextCallBack imgTextCallBack;
	private BarUploadSubjectNew bar;
	private SubjectData subjectData;
	private int titleMax = 40;//title最大数量
	private CircleData circleData;
	private TextView tv_uploadSubjectTitle_num;
	private RelativeLayout rela;
	private int FocusLocation=0;//获取当前的位置
	private ScrollView scrollview;
	
	private boolean isOneWriteTitle = true;
	
	public UploadSubjectContent(BaseActivity act) {
		this.act = act;
	}

	/**
	 * 初始化
	 */
	public void init(SubjectData Data, final View view, BarUploadSubjectNew bar,final boolean isHaveTitle,final boolean isNewSubject,boolean isDraft) {
		this.subjectData = Data;
		//处理title最大数量
		CircleSqlite circleSqlite = new CircleSqlite(act);
		if(!TextUtils.isEmpty(subjectData.getCid())){
			circleData=  circleSqlite.select(CircleDB.db_cid , subjectData.getCid());
			//正式对接是需要修改
//			if(circleData!=null)titleMax=circleData.getTitleMax();
		}
		//点击处理
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(content_linear.getChildCount()==1){
					((ImgTextCombineLayout)content_linear.getChildAt(0)).setEditTextFocus(true);
				}
			}
		});
		content_linear = (LinearLayout) view.findViewById(R.id.content_linear);
		content_linear.setOnClickListener(this);
		ed_uploadSubjectTitle = (TextView) view.findViewById(R.id.ed_uploadSubjectTitle);
		title = subjectData.getTitle();
		if(!isHaveTitle && !isNewSubject){
			//草稿
			if(isDraft){
				XHClick.onEventValue(act, "uploadQuanTitle", "uploadQuanTitle", "草稿标题", title.length());
			}else{
				XHClick.onEventValue(act, "uploadQuanTitle", "uploadQuanTitle", "发失败标题", title.length());
			}
		}
		tv_uploadSubjectTitle_num=(TextView) view.findViewById(R.id.tv_uploadSubjectTitle_num);
		
		ed_uploadSubjectTitle.setMaxEms(titleMax);
		ed_uploadSubjectTitle.setText(title);
		if(titleMax-ed_uploadSubjectTitle.length()<=5){
			tv_uploadSubjectTitle_num.setVisibility(View.VISIBLE);
			tv_uploadSubjectTitle_num.setText(String.valueOf(titleMax-ed_uploadSubjectTitle.length()));
		}else tv_uploadSubjectTitle_num.setVisibility(View.GONE);
		//文字实时变化
		ed_uploadSubjectTitle.addTextChangedListener(new TextWatcher() {
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {
				if(s.toString().length() > 0 && isOneWriteTitle && !isHaveTitle && isNewSubject){
					isOneWriteTitle = false;
					XHClick.onEventValue(act, "uploadQuanTitle", "uploadQuanTitle", "新贴写标题",s.toString().length());
				}
				int num=titleMax-ed_uploadSubjectTitle.length();
				if(num<=5){
					tv_uploadSubjectTitle_num.setVisibility(View.VISIBLE);
					tv_uploadSubjectTitle_num.setText(String.valueOf(num));
				}else tv_uploadSubjectTitle_num.setVisibility(View.GONE);
				if(num<=0)Tools.showToast(act, "标题最多输入"+titleMax+"个字");
			}
		});
		this.bar = bar;
		ArrayList<Map<String, String>> imageTextList=TextUtils.isEmpty(subjectData.getContentJson())?new ArrayList<Map<String, String>>():UtilString.getListMapByJson(subjectData.getContentJson());
		//显示布局
		boolean state = true;
		if (imageTextList.size()>0){
			// 如果图文数据中第一个数据完整，添加一个空数据在第一个位置，方便用户操作
			if (!TextUtils.isEmpty(imageTextList.get(0).get(ImgTextCombineLayout.IMGEURL)) && !TextUtils.isEmpty(imageTextList.get(0).get(ImgTextCombineLayout.CONTENT))) {
				imageTextList.add(0, getFalseData());
				state = false;
			}
		}else imageTextList.add(getFalseData());// 默认有一个数据：供用户编辑
		// 正常添加数据
		int imgNum = putContentOnView(imageTextList, state,false);//当前控件是第几个控件
		if(isDraft) XHClick.onEventValue(act, "uploadQuanImg", "uploadQuanImg", "发草稿图片", imgNum);
		else if(!isNewSubject)
			XHClick.onEventValue(act, "uploadQuanImg", "uploadQuanImg", "发失败图片", imgNum);
		
		//title是否可编辑
		if(subjectData.getTitleCanModify()){
			ed_uploadSubjectTitle.setEnabled(true);
			ed_uploadSubjectTitle.setFocusable(true);
			ed_uploadSubjectTitle.setFocusableInTouchMode(true);
			ed_uploadSubjectTitle.requestFocus();
		}else ed_uploadSubjectTitle.setEnabled(false);
		
		
		rela=(RelativeLayout) act.findViewById(activityLayout);
		scrollview = (ScrollView) act.findViewById(R.id.scrollview);
		rela.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				Rect r = new Rect();
				rela.getWindowVisibleDisplayFrame(r);
				int screenHeight = rela.getRootView().getHeight();
				int heightDiff = screenHeight - (r.bottom - r.top);
		          if (heightDiff > 200) {
		        	  DisplayMetrics dm = new DisplayMetrics();
		        	  act.getWindowManager().getDefaultDisplay().getMetrics(dm);
		        	  Log.v("键盘", "键盘弹出状态");
		        	  int distance= dm.heightPixels-FocusLocation;
					  Log.i("tzy","heightDiff = " +heightDiff);
		        	  if(distance<=heightDiff){
		        		  final int dp_50=(int) act.getResources().getDimension(R.dimen.dp_50);
		        		  final int src_distance= heightDiff-distance;
		        		  Log.v("移动", "scrollview");
		        		  scrollview.postDelayed(new Runnable() {
							
		        			  @Override
		        			  public void run() {
		        				  scrollview.scrollBy(0, dp_50);
		        			  }
		        		  }, 100);
		        	  }
		            
		          } else{
		            Log.v("键盘", "键盘收起状态");
		          }
			}
		});
	}

	/**
	 * 获取占界面的假数据
	 * @return
	 */
	private Map<String,String> getFalseData(){
		Map<String,String> map = new HashMap<String, String>();
		map.put(ImgTextCombineLayout.IMGEURL, "");
		map.put(ImgTextCombineLayout.CONTENT, "");
		return map;
	}
	/**
	 * 控件放到界面
	 * 
	 * @param imageTextList
	 * @param state true 当前编辑控件在最后一个，false 当前控件为第一个
	 * @param isInsert true是否是插入数据，false添加数据
	 */
	private int putContentOnView(ArrayList<Map<String, String>> imageTextList, boolean state,boolean isInsert) {
		int imgNum = 0;
		int index = getViewIndex();
		for (int i = 0; i < imageTextList.size(); i++) {
			int addImageTextIndex=index+i;//要添加的位置
			ImgTextCombineLayout layout = new ImgTextCombineLayout(act);
			Map<String, String> map = imageTextList.get(i);
			layout.setImgText(map.get(ImgTextCombineLayout.CONTENT),map.get(ImgTextCombineLayout.IMGEURL),true);
			if(!TextUtils.isEmpty(map.get(ImgTextCombineLayout.IMGEURL))){
				imgNum ++;
			}
			layout.setImgTextCallBack(getImgTextCallBack());
			UploadSubjectControl.getInstance().uploadImg(subjectData.uploadTimeCode, UploadSubjectControl.IMAGE_TYPE_SUBJECT, map.get(ImgTextCombineLayout.IMGEURL));
			if(isInsert) addImageTextIndex +=1;
			content_linear.addView(layout, addImageTextIndex);
			// 设置最后一个layout为当前的
			if (i == imageTextList.size() - 1 && state)
				imgTextCombineLayout = layout;
			else if(i ==0 && !state)//第一个为当前控件
				imgTextCombineLayout = layout;
			if(imgTextCombineLayout!=null)
				imgTextCombineLayout.setEditTextFocus(false);
		}
		return imgNum;
	}

	/**
	 * 获取数据
	 */
	public SubjectData getData() {
		ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
		for (int i = 0; i < content_linear.getChildCount(); i++) {
			ImgTextCombineLayout layout = (ImgTextCombineLayout) content_linear.getChildAt(i);
			Map<String, String> map = layout.getImgText();
			list.add(map);
		}
		subjectData.setTitle(ed_uploadSubjectTitle.getText().toString());
		subjectData.setContentArray(list);
		return subjectData;
	}

	/**
	 * 设置图片
	 * 
	 * @param imgs
	 *            图片路径数组
	 */
	public void insertImgs(ArrayList<String> imgs) {
		String content_after = imgTextCombineLayout.insertImg();
		ArrayList<Map<String, String>> imageTextList = new ArrayList<Map<String, String>>();
		for (int i = 0; i < imgs.size(); i++) {
			Map<String, String> map = new HashMap<String, String>();
			map.put(ImgTextCombineLayout.IMGEURL, imgs.get(i));
			if (i == imgs.size() - 1)
				map.put(ImgTextCombineLayout.CONTENT, content_after);
			else
				map.put(ImgTextCombineLayout.CONTENT, "");
			imageTextList.add(map);
		}
		putContentOnView(imageTextList, true,true);
	}

	/**
	 * 设置数据
	 * 
	 * @param data
	 */
	public void editAdd(String data) {
		if (imgTextCombineLayout != null) {
			imgTextCombineLayout.editAdd(data);
		}
	}
	/**
	 * 获取当前位置
	 */
	private int getViewIndex() {
		int index = 0;
		for (int i = 0; i < content_linear.getChildCount(); i++) {
			((ImgTextCombineLayout)content_linear.getChildAt(i)).setEditTextNoneFocus();
			if (imgTextCombineLayout != null && content_linear.getChildAt(i) == imgTextCombineLayout)
				index = i;
		}
		return index;
	}
	/**
	 * 获取当前位置
	 */
	private int getViewIndex(ImgTextCombineLayout layout) {
		int index = 0;
		for (int i = 0; i < content_linear.getChildCount(); i++) {
			((ImgTextCombineLayout)content_linear.getChildAt(i)).setEditTextNoneFocus();
			if (layout != null && content_linear.getChildAt(i) == layout)
				index = i;
		}
		return index;
	}

	/**
	 * 设置回调接口
	 */
	private ImgTextCallBack getImgTextCallBack() {
		if(imgTextCallBack==null){
			imgTextCallBack = new ImgTextCallBack() {
				@Override
				public void onDelete(ImgTextCombineLayout layout) {
					int index = getViewIndex();
					Map<String, String> map = imgTextCombineLayout.getImgText();
					if (index > 0) {
						ImgTextCombineLayout imgTextlayout = (ImgTextCombineLayout) content_linear.getChildAt(index - 1);
						Map<String, String> maplast = imgTextlayout.getImgText();
						String content = maplast.get(ImgTextCombineLayout.CONTENT);
						content += map.get(ImgTextCombineLayout.CONTENT);
						imgTextlayout.setTextView(content);
						// 删除view数据
						content_linear.removeView(imgTextCombineLayout);
					}
				}
				@Override
				public void onClick(ImgTextCombineLayout layout) {
					bar.setEmoji(false);
					bar.setSeting(false);
					imgTextCombineLayout = layout;
					int[] location = new int[2];
					layout.editText.getLocationInWindow(location);
					Log.i("location::InWindow", location[0]+":"+location[1]);
					FocusLocation=location[1];
					Log.i("VISIBLE::1::", ""+true);
				}
				@Override
				public int getWidth() {
					DisplayMetrics dm = new DisplayMetrics();
					act.getWindowManager().getDefaultDisplay().getMetrics(dm);
					return dm.widthPixels;
				}
				@Override
				public void onImageClick(ImgTextCombineLayout layout) {
				}
				@Override
				public void onFocusChange(EditTextShow editTextshow, boolean hasFocus, ImgTextCombineLayout layout) {
					Log.i("hasFocus",":"+hasFocus);
					if(hasFocus){
						imgTextCombineLayout = layout;
						bar.showBar(editTextshow);
						int[] location = new int[2];
						editTextshow.getLocationInWindow(location);
						FocusLocation=location[1];
					}else{
						bar.hindBar();
						FocusLocation=0;
					}
					
				}
				@Override
				public void initImgNull(ImgTextCombineLayout layout) {
					if(layout!=null)
						content_linear.removeView(layout);
				}
			};
		}
		return imgTextCallBack;
	}
	/**
	 * 获取当前控件图片数量
	 */
	public int getBitmapNum(){
		int num=0;
		for (int i = 0; i < content_linear.getChildCount(); i++) {
			if(!TextUtils.isEmpty(((ImgTextCombineLayout)content_linear.getChildAt(i)).getImgText().get(ImgTextCombineLayout.IMGEURL)))
				num++;
		}
		return num;
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.content_linear://点击空白位置第一控件自动获得焦点
			if(content_linear.getChildCount()==1){
				((ImgTextCombineLayout)content_linear.getChildAt(0)).setEditTextFocus(true);
			}
			break;
		}
	}
	/**
	 * 设置title最大字数
	 * @param maxNumber
	 */
	public void setTitleMaxNumber(int maxNumber){
		String title_subject=ed_uploadSubjectTitle.getText().toString();
		ed_uploadSubjectTitle.setMaxEms(maxNumber);
		titleMax= maxNumber;
		int num= maxNumber-title_subject.length();
		if(maxNumber<=title_subject.length()){//处理编辑过的
			ed_uploadSubjectTitle.setText(title_subject.substring(0, maxNumber));
			num=0;
		}
		tv_uploadSubjectTitle_num.setText(String.valueOf(num));
	}
	public int getContentNowNumber(){
		int num=0;
		for (int i = 0; i < content_linear.getChildCount(); i++) {
			ImgTextCombineLayout layout = (ImgTextCombineLayout) content_linear.getChildAt(i);
			Map<String, String> map = layout.getImgText();
			if(!TextUtils.isEmpty(map.get(ImgTextCombineLayout.CONTENT)))
				num+=map.get(ImgTextCombineLayout.CONTENT).length();
		}
		return num;
	}
	/**
	 * 获取当前图片数量
	 * @return
	 */
	public int getImagSize(){
		int index=0;
		for (int i = 0; i < content_linear.getChildCount(); i++) {
			ImgTextCombineLayout layout = (ImgTextCombineLayout) content_linear.getChildAt(i);
			Map<String, String> map = layout.getImgText();
			if(!TextUtils.isEmpty(map.get(ImgTextCombineLayout.IMGEURL)))
				index++;
		}
		return index;
	}
	/**
	 * 设置水印
	 * @param markText [null:不显]["":显示当前用户人名][有数据显示对应数据]
	 */
	public void setWaterMark(String markText){
		boolean isVisible=false;
		isVisible = null != markText;
		
		for (int i = 0; i < content_linear.getChildCount(); i++) {
			ImgTextCombineLayout layout = (ImgTextCombineLayout) content_linear.getChildAt(i);
			layout.setWaterMark(isVisible, markText);
		}
	}
	/**
	 * 设置水印
	 * @param isVisible [true 显示][false 不显示]
	 */
	public void setWaterMark(boolean isVisible){
		for (int i = 0; i < content_linear.getChildCount(); i++) {
			ImgTextCombineLayout layout = (ImgTextCombineLayout) content_linear.getChildAt(i);
			layout.setWaterMark(isVisible, "");
		}
	}
}
