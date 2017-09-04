package aplug.imageselector;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import acore.logic.XHClick;
import acore.override.activity.base.BaseFragmentActivity;
import acore.tools.FileManager;
import acore.tools.Tools;
import amodule.answer.activity.BaseEditActivity;
import aplug.imageselector.adapter.FolderAdapter;
import aplug.imageselector.adapter.ImageGridAdapter;
import aplug.imageselector.bean.Folder;
import aplug.imageselector.bean.Image;
import aplug.imageselector.constant.ImageSelectorConstant;
import xh.basic.tool.UtilLog;

/**
 * @author Eva
 */
public class ImageSelectorActivity extends BaseFragmentActivity implements OnClickListener {
	// 不同loader定义
	private static final int LOADER_ALL = 0;
	private static final int LOADER_CATEGORY = 1;
	// 请求加载系统照相机
	private static final int REQUEST_CAMERA = 100;
	// 请求预览
	private static final int REQUEST_PREVIEW = 101;

	/** 选中数据集合 */
	private ArrayList<String> notSelectedList = new ArrayList<>();
	/** 选中数据集合 */
	private ArrayList<String> resultList = new ArrayList<>();
	// 文件夹数据
	private ArrayList<Folder> mResultFolder = new ArrayList<>();
	// 图片Grid
	private GridView mGridView;
	private RelativeLayout mGridViewLayout;
	private Button mSubmitButton;
	 // 预览按钮
    private Button mPreviewBtn;

	/** 默认可选图片maxCount */
	private int mDefaultCount = 0;

	private ImageGridAdapter mImageAdapter;
	private FolderAdapter mFolderAdapter;

	private ListView mFolderListView;

	// 时间线
	private TextView mTimeLineText;
	// 类别
	private TextView mCategoryText;

	private TextView mImageCount;
	private TextView mTitle;

	private boolean hasFolderGened = false;
	private boolean mIsShowCamera = false;

	private int mGridWidth, mGridHeight;

	private File mTmpFile;

	private int mode;
	private int loaderId = 0;

	/**
	 * 当没有选择图片的时候是否可以退出这个界面
	 */
	private boolean isCanBackOnNoChoose = true;
	private String mTjId;
	private String mTag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_seletor_activity_default);
		// 初始化数据
		initData();
		initView();
		setListener();

		// 设置加载callback
		getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);
		init();

	}

	/**
	 * 初始化区分数据模块
	 */
	private void init() {
		if(Tools.isShowTitle()) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			int dp_45 = Tools.getDimen(this, R.dimen.dp_45);
			int height = dp_45 + Tools.getStatusBarHeight(this);

			RelativeLayout bar_title = (RelativeLayout) findViewById(R.id.title_rela_all);
			RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
			bar_title.setLayoutParams(layout);
			bar_title.setPadding(0, Tools.getStatusBarHeight(this), 0, 0);
		}
	}
	private void initView() {
		if(!isCanBackOnNoChoose) findViewById(R.id.btn_back).setVisibility(View.GONE);

		// 初始化，先隐藏当前timeline
		mTimeLineText = (TextView) findViewById(R.id.timeline_area);
		mTimeLineText.setVisibility(View.GONE);

		mCategoryText = (TextView) findViewById(R.id.category_btn);

		mGridViewLayout = (RelativeLayout) findViewById(R.id.grid_layout);
		mGridView = (GridView) findViewById(R.id.grid);
		mImageAdapter = new ImageGridAdapter(this, mIsShowCamera);
		// 是否显示选择指示器
		mImageAdapter.showSelectIndicator(mode == ImageSelectorConstant.MODE_MULTI);
		mGridView.setAdapter(mImageAdapter);

		mImageCount = (TextView) findViewById(R.id.img_count);
		mImageCount.setText(resultList.size() + "/" + mDefaultCount);
		mTitle = (TextView) findViewById(R.id.title);
		mSubmitButton = (Button) findViewById(R.id.commit);
		if (resultList == null || resultList.size() == 0) {
			mSubmitButton.setEnabled(false);
			mSubmitButton.setBackgroundResource(R.drawable.bg_image_unselected_commit);
		} else {
			mSubmitButton.setEnabled(true);
			mSubmitButton.setBackgroundResource(R.drawable.bg_image_selected_commit);
		}
		
		mPreviewBtn = (Button) findViewById(R.id.preview);
        // 初始化，按钮状态初始化
        if(resultList == null || resultList.size()<=0){
        	mPreviewBtn.setTextColor(Color.parseColor("#999999"));
        }else{
        	mPreviewBtn.setTextColor(Color.parseColor("#333333"));
        }
        
        if(mode == ImageSelectorConstant.MODE_SINGLE){
        	findViewById(R.id.footer_bar).setVisibility(View.GONE);
        }
		mFolderListView = (ListView) findViewById(R.id.category_list);
		mFolderListView.setAdapter(mFolderAdapter);
	}

	// 初始化数据
	private void initData() {
		Intent intent = getIntent();
		isCanBackOnNoChoose = intent.getBooleanExtra(ImageSelectorConstant.IS_CAN_BACK_ON_NO_CHOOSE,true);
		mTjId = intent.getStringExtra("tjId");
		mTag = intent.getStringExtra("tag");

		// 选择图片数量
		mDefaultCount = intent.getIntExtra(ImageSelectorConstant.EXTRA_SELECT_COUNT , 1);
		// 图片选择模式
		mode = intent.getIntExtra(ImageSelectorConstant.EXTRA_SELECT_MODE, ImageSelectorConstant.MODE_MULTI);
		// 默认选择
		if (mode == ImageSelectorConstant.MODE_MULTI
				&& intent.hasExtra(ImageSelectorConstant.EXTRA_DEFAULT_SELECTED_LIST)) {
			resultList = intent.getStringArrayListExtra(ImageSelectorConstant.EXTRA_DEFAULT_SELECTED_LIST);
		}
		notSelectedList = intent.getStringArrayListExtra(ImageSelectorConstant.EXTRA_NOT_SELECTED_LIST);
//		if(resultList != null && resultList.size() > 0){
//			notSelectedList.addAll(resultList);
//			resultList.clear();
//		}
		// 是否显示照相机
		mIsShowCamera = intent.getBooleanExtra(ImageSelectorConstant.EXTRA_SHOW_CAMERA, true);

		mFolderAdapter = new FolderAdapter(this);
	}

	private void setListener() {
		findViewById(R.id.btn_back).setOnClickListener(this);
		mSubmitButton.setOnClickListener(this);
		mPreviewBtn.setOnClickListener(this);
		mCategoryText.setOnClickListener(this);

		mFolderListView.setOnItemClickListener(mFolderItemClickListener);
		mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int state) {
				//根据滑动状态设置图片的加载状态
				if (state == SCROLL_STATE_IDLE || state == SCROLL_STATE_TOUCH_SCROLL) {
					Glide.with(ImageSelectorActivity.this).resumeRequests();
				} else {
					Glide.with(ImageSelectorActivity.this).pauseRequests();
				}
				//设置日期指示器的显示状态；停止滑动，日期指示器消失
				if (state == SCROLL_STATE_IDLE) {
					mTimeLineText.setVisibility(View.GONE);
				} else if (state == SCROLL_STATE_FLING) {
					mTimeLineText.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (mTimeLineText.getVisibility() == View.VISIBLE) {
					int index = firstVisibleItem + 1 == view.getAdapter().getCount() ? view.getAdapter().getCount() - 1
							: firstVisibleItem + 1;
					Image image = (Image) view.getAdapter().getItem(index);
					if (image != null) {
						mTimeLineText.setText(Tools.formatPhotoDate(image.path));
					}
				}
			}
		});

		mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {

				final int width = mGridView.getWidth();
				final int height = mGridView.getHeight();

				mGridWidth = width;
				mGridHeight = height;

				final int columnSpace = Tools.getDimen(ImageSelectorActivity.this, R.dimen.dp_3);
				int columnWidth = (int) ((width - columnSpace * 5) / 4f);
				mImageAdapter.setItemSize(columnWidth);

				mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				if (mImageAdapter.isShowCamera()) {
					// 如果显示照相机，则第一个Grid显示为照相机，处理特殊逻辑
					if (i == 0) {
						showCameraAction();
					} else {
						// 正常操作
						selectImageFromGrid(adapterView, i, mode, view);
					}
				} else {
					// 正常操作
					selectImageFromGrid(adapterView, i, mode, view);
				}
			}
		});
		mImageAdapter.setIndicatorClick(new ImageGridAdapter.OnIndicatorClickListener() {
			@Override
			public void onClick(ImageGridAdapter adapter, int position, View view) {
				selectImageFromIndicator(adapter, position, view);
			}
		});
	}

	private boolean mIsOpenAnim = false;
	/**打开相册列表*/
	private void openCategoryList(){
		if (mFolderListView.getVisibility() == View.VISIBLE || mIsOpenAnim)
			return;
		excuteAnim(true);
	}

	private boolean mIsCloseAnim = false;
	/**关闭相册列表*/
	private void closeCategoryList() {
		if (mFolderListView.getVisibility() != View.VISIBLE || mIsCloseAnim)
			return;
		excuteAnim(false);
	}

	private void excuteAnim(final boolean isOpen) {
		AnimationSet animSet = new AnimationSet(true);
		animSet.setDuration(200);
		animSet.setRepeatCount(0);
		animSet.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				if (isOpen) {
					mFolderListView.setVisibility(View.VISIBLE);
					mIsOpenAnim = true;
				} else
					mIsCloseAnim = true;
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (isOpen)
					mIsOpenAnim = false;
				else {
					mFolderListView.setVisibility(View.GONE);
					mIsCloseAnim = false;
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		Animation scaleAnim = new ScaleAnimation(isOpen ? 0.5f : 1.0f, isOpen ? 1.0f : 0.5f, isOpen ? 0.5f : 1.0f, isOpen ? 1.0f : 0.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		Animation alphaAnim = new AlphaAnimation(isOpen ? 0.0f : 1.0f, isOpen ? 1.0f : 0.0f);
		animSet.addAnimation(scaleAnim);
		animSet.addAnimation(alphaAnim);
		mFolderListView.clearAnimation();
		mFolderListView.startAnimation(animSet);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 相机拍照完成后，返回图片路径
		if (requestCode == REQUEST_CAMERA) {
			if (resultCode == Activity.RESULT_OK) {
				if (mTmpFile != null) {
					onCameraShot(mTmpFile);
//					saveImage(mTmpFile.getAbsolutePath());
//					getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);
				}
			} else {
				if (mTmpFile != null && mTmpFile.exists()) {
					mTmpFile.delete();
				}
			}
		} else if (requestCode == REQUEST_PREVIEW) {
			if (resultCode == Activity.RESULT_OK && data != null) {
				ArrayList<String> currentArray = data.getStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT);
				// 设定默认选择
				if (currentArray != null && currentArray.size() > 0) {
					mImageAdapter.setDefaultSelected(currentArray);
					for (String path : currentArray) {
						onImageSelected(path);
					}
					ArrayList<String> array = new ArrayList<String>();
					array.addAll(resultList);
					for (String path : array) {
						if (!currentArray.contains(path)) {
							onImageUnselected(path);
						}
					}
					//判断是否需要提交，若需要提交则关闭该页面，返回上一级页面
					boolean isCommit = data.getBooleanExtra(ImageSelectorConstant.EXTRA_IS_COMMIT, false);
					if(isCommit){
						//提交
						commit();
					}
				}
				resultList = currentArray;
			}
		}
	}
	
	/** 更觉文件路径保存相片 */
	private void saveImage(String imagePath){
		try{
			ContentResolver contentResolver = getContentResolver();
			MediaStore.Images.Media.insertImage(contentResolver, imagePath, "", "");
			if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
				MediaScannerConnection.scanFile(this, new String[] {Environment.getExternalStorageDirectory().getPath()}, null, null);
			}else{
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, 
						Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
			}
		}catch(Exception e){
			Tools.showToast(this, "图片保存失败");
			UtilLog.reportError("相机拍照,保存到相册失败", e);
		}
	}

	/**
	 * 选择相机
	 */
	private void showCameraAction() {
		// 跳转到系统照相机
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (cameraIntent.resolveActivity(this.getPackageManager()) != null) {
			// 设置系统相机拍照后的输出路径
			// 创建临时文件
			mTmpFile = FileManager.createTmpFile(this);
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
			startActivityForResult(cameraIntent, REQUEST_CAMERA);
		} else {
			Tools.showToast(this, getResources().getString(R.string.msg_no_camera));
		}
	}

	/**
	 * 选择图片操作
	 * @param adapterView
	 * @param position
	 * @param mode
	 * @param view
	 */
	private void selectImageFromGrid(AdapterView<?> adapterView, int position, int mode, View view) {
		Image image = (Image) adapterView.getAdapter().getItem(position);
		if (image != null) {
			if (image.path != null && image.path.endsWith(".webp")) {
				Tools.showToast(getApplicationContext(), "不支持webp格式");
				return;
			}
			// 多选模式
			if (mode == ImageSelectorConstant.MODE_MULTI) {
				Intent intent = new Intent(this, ImgWallActivity.class);
				intent.putStringArrayListExtra("images", mImageAdapter.getAllImagePath());
				intent.putExtra("index", mIsShowCamera ? position - 1 : position);
				intent.putExtra("mode", ImgWallActivity.MODE_EDIT);
				intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_COUNT, mDefaultCount);
				// 设定默认选择
				if (resultList != null && resultList.size() > 0) {
					intent.putStringArrayListExtra("defaultImgs", resultList);
				}
				if (notSelectedList != null && notSelectedList.size() > 0) {
					intent.putStringArrayListExtra(ImageSelectorConstant.EXTRA_NOT_SELECTED_LIST, notSelectedList);
				}
				startActivityForResult(intent, REQUEST_PREVIEW);
			} else if (mode == ImageSelectorConstant.MODE_SINGLE) {
				if(view.findViewById(R.id.mask).getVisibility() == View.GONE){
					// 单选模式
					onSingleImageSelected(image.path);
				}else{
					Tools.showToast(getApplicationContext(), "此图已选中，请重新选择");
				}
			}
		}
	}

	private void selectImageFromIndicator(ImageGridAdapter adapter, int position, View view) {
		Image image = (Image) adapter.getItem(position);
		if (image != null) {
			if (view.getId() == R.id.checkmark) {
				if (resultList.contains(image.path)) {
					onImageUnselected(image.path);
					mImageAdapter.select(image);
				} else {
					// 判断选择数量问题
					if (mDefaultCount == resultList.size()) {
						Tools.showToast(this, "最多选" + mDefaultCount + "张图片", Gravity.CENTER);
						return;
					}
					onImageSelected(image.path);
					mImageAdapter.select(image);
				}
			}
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

		private final String[] IMAGE_PROJECTION = { MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME,
				MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media._ID };

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			if (id == LOADER_ALL) {
				CursorLoader cursorLoader = new CursorLoader(ImageSelectorActivity.this,
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, null, null,
						IMAGE_PROJECTION[2] + " DESC");
				return cursorLoader;
			} else if (id == LOADER_CATEGORY) {
				CursorLoader cursorLoader = new CursorLoader(ImageSelectorActivity.this,
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
						IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'", null,
						IMAGE_PROJECTION[2] + " DESC");
				return cursorLoader;
			}
			return null;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			if (data != null) {
				List<Image> images = new ArrayList<>();
				int count = data.getCount();
				if (count > 0) {
					data.moveToFirst();
					do {
						String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
						String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
						long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
						if(path ==null || name == null){
							continue;
						}
						Image image = new Image(path, name, dateTime);
						images.add(image);
						if (!hasFolderGened) {
							// 获取文件夹名称
							File imageFile = new File(path);
							File folderFile = imageFile.getParentFile();
							Folder folder = new Folder();
							folder.name = folderFile.getName();
							folder.path = folderFile.getAbsolutePath();
							folder.cover = image;
							if (!mResultFolder.contains(folder)) {
								List<Image> imageList = new ArrayList<>();
								imageList.add(image);
								folder.images = imageList;
								mResultFolder.add(folder);
							} else {
								// 更新
								Folder f = mResultFolder.get(mResultFolder.indexOf(folder));
								f.images.add(image);
							}
						}
					} while (data.moveToNext());

					mImageAdapter.setData(images);
					//配合选择图片以后需要保存并选中图片是使用
//					if(mTmpFile != null){
//						onImageSelected(images.get(0).path);
//						mTmpFile = null;
//					}
					
					// 设定默认选择
					if (resultList != null && resultList.size() > 0) {
						mImageAdapter.setDefaultSelected(resultList);
					}
					// 设定默认不能选择
					if (notSelectedList != null && notSelectedList.size() > 0) {
						mImageAdapter.setDefaultNotSelected(notSelectedList);
					}

					mFolderAdapter.setData(mResultFolder);
					hasFolderGened = true;
					
				}
			}
		}

		@Override public void onLoaderReset(Loader<Cursor> loader) {}
	};

	/** 单张图片选中回调 */
	public void onSingleImageSelected(String path) {
		Intent data = new Intent();
		resultList.add(path);
		data.putStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT, resultList);
		setResult(RESULT_OK, data);
		finish();
	}

	/** 图片选中回调 */
	public void onImageSelected(String path) {
		if (!resultList.contains(path)) {
			resultList.add(path);
		}
        if(resultList.size() == 0){
        	mPreviewBtn.setTextColor(Color.parseColor("#999999"));
			mSubmitButton.setEnabled(false);
        	mSubmitButton.setBackgroundResource(R.drawable.bg_image_unselected_commit);
        }else{
        	mImageCount.setText(resultList.size() + "/" + mDefaultCount);
        	mPreviewBtn.setTextColor(Color.parseColor("#333333"));
			mSubmitButton.setEnabled(true);
        	mSubmitButton.setBackgroundResource(R.drawable.bg_image_selected_commit);
        }
	}

	/** 取消图片选中回调 */
	public void onImageUnselected(String path) {
		if (resultList.contains(path)) {
			resultList.remove(path);
		}
		mImageCount.setText(resultList.size() + "/" + mDefaultCount);
		// 当为选择图片时候的状态
        if(resultList.size() == 0){
        	mPreviewBtn.setTextColor(Color.parseColor("#999999"));
			mSubmitButton.setEnabled(false);
        	mSubmitButton.setBackgroundResource(R.drawable.bg_image_unselected_commit);
        }else{
        	mPreviewBtn.setTextColor(Color.parseColor("#333333"));
			mSubmitButton.setEnabled(true);
        	mSubmitButton.setBackgroundResource(R.drawable.bg_image_selected_commit);
        }
	}

	/** 相机回调 */
	public void onCameraShot(File imageFile) {
		if (imageFile != null) {
			Intent data = new Intent();
			resultList.add(imageFile.getAbsolutePath());
			data.putStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT, resultList);
			setResult(RESULT_OK, data);
			finish();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 打开相册目录
		case R.id.category_btn:
			if (mFolderListView.getVisibility() == View.VISIBLE) {
				closeCategoryList();
			} else {
				openCategoryList();
				int index = mFolderAdapter.getSelectIndex();
				index = index == 0 ? index : index - 1;
				mFolderListView.setSelection(index);
			}
			break;
		// 预览
		case R.id.preview:
			if (resultList != null && resultList.size() > 0) {
				Intent intent = new Intent(this, ImgWallActivity.class);
				intent.putStringArrayListExtra("defaultImgs", resultList);
				intent.putStringArrayListExtra("images", resultList);
				intent.putExtra("index", 0);
				intent.putExtra("mode", ImgWallActivity.MODE_EDIT);
				intent.putExtra(ImageSelectorConstant.EXTRA_SELECT_COUNT, mDefaultCount);
				// 设定默认选择
				startActivityForResult(intent, REQUEST_PREVIEW);
			}else{
				Tools.showToast(this, "请选择图片");
			}
			break;
		// 退出
		case R.id.btn_back:
			if (BaseEditActivity.TAG.equals(mTag)) {
				XHClick.mapStat(this, mTjId, "点击图片按钮", "点击返回按钮");
			}
			setResult(RESULT_CANCELED);
			onBackPressed();
			break;
		// 提交按钮
		case R.id.commit:
			if (resultList != null && resultList.size() > 0) {
				if (BaseEditActivity.TAG.equals(mTag)) {
					XHClick.mapStat(this, mTjId, "点击图片按钮", (resultList.size() < 3 ? "只" : "") + "选择" + resultList.size() + "张图（点击完成）");
				}
				commit();
			}
			break;
		}
	}

	private void commit() {
		// 返回已选择的图片数据
		Intent data = new Intent();
		data.putStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT, resultList);
		setResult(RESULT_OK, data);
		finish();
	}

	@Override
	public void onBackPressed() {
		if(!isCanBackOnNoChoose)
			return;
		if (loaderId != 0) {
			loaderId = 0;
			mTitle.setText("全部图片");
			mCategoryText.setVisibility(View.VISIBLE);
			getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
			mFolderAdapter.setSelectIndex(0);
		} else {
			Intent data = new Intent();
			resultList.clear();
			data.putStringArrayListExtra(ImageSelectorConstant.EXTRA_RESULT, resultList);
			setResult(RESULT_OK, data);
			finish();
		}
	}
	
	AdapterView.OnItemClickListener mFolderItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
			mFolderAdapter.setSelectIndex(i);
			int index = i;
			closeCategoryList();
			loaderId = index;
			if (index == 0) {
				mTitle.setText("全部图片");
				mCategoryText.setVisibility(View.VISIBLE);
				ImageSelectorActivity.this.getSupportLoaderManager().restartLoader(LOADER_ALL, null,mLoaderCallback);
				mIsShowCamera = true;
				mImageAdapter.setShowCamera(mIsShowCamera);
			} else {
				Folder folder = mFolderAdapter.getItem(index);
				mTitle.setText(folder.name);
				mCategoryText.setVisibility(View.GONE);
				if (null != folder) {
					mImageAdapter.setData(folder.images);
					// 设定默认选择
					if (resultList != null && resultList.size() > 0) {
						mImageAdapter.setDefaultSelected(resultList);
					}
				}
				mIsShowCamera = false;
				mImageAdapter.setShowCamera(mIsShowCamera);
			}
			// 滑动到最初始位置
			mGridView.smoothScrollToPosition(0);
		}
	};

}
