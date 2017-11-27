package aplug.imageselector;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import java.util.ArrayList;

import acore.override.activity.base.BaseActivity;
import acore.tools.Tools;
import aplug.basic.LoadImage;
import aplug.basic.SubBitmapTarget;
import aplug.imageselector.adapter.AdapterImgWall;
import aplug.imageselector.constant.ImageSelectorConstant;

/**
 * Title:ImgWallActivity.java Copyright: Copyright (c) 2014~2017
 *
 * @author zeyu_t
 * @date 2014年9月16日
 */
@SuppressLint("ResourceAsColor")
public class ImgWallActivity extends BaseActivity implements OnClickListener {
    private ViewPager mImageViewPager;
    private int mIndex;
    private ArrayList<String> mData = new ArrayList<>();
    private ArrayList<String> resultList = new ArrayList<>();
    private ArrayList<String> notSelectedList = new ArrayList<>();
    //	private TextView mImgCurrentPage;//save,
    private TextView mImageCount;
    private ImageView indicator;
    private Button commit;
    private int mMode = 0, mMaxCount = 0;
    public static final int MODE_BROWE = 0;
    public static final int MODE_EDIT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initActivity("", 2, 0, 0, R.layout.c_view_img_wall);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            mMode = bundle.getInt("mode", 0);
            mData = bundle.getStringArrayList("images");
            if (bundle.containsKey("defaultImgs")) {
                resultList = bundle.getStringArrayList("defaultImgs");
            }
            if (bundle.containsKey(ImageSelectorConstant.EXTRA_NOT_SELECTED_LIST)) {
                notSelectedList = bundle.getStringArrayList(ImageSelectorConstant.EXTRA_NOT_SELECTED_LIST);
            }
            mIndex = bundle.getInt("index", 0);
            mMaxCount = bundle.getInt(ImageSelectorConstant.EXTRA_SELECT_COUNT);
        }
        if (mData == null || mData.size() <= 0) {
            Tools.showToast(this, "您选择的图片错误");
            finish();
            return;
        }
        // 初始化接口，应用启动的时候调用
        initView();
        setClick();

        AdapterImgWall mAdapter = new AdapterImgWall(this, mData);

        mImageViewPager.setAdapter(mAdapter);
        mImageViewPager.setClickable(true);
        mImageViewPager.setLongClickable(true);
        mAdapter.notifyDataSetChanged();
//		String img_index = (mIndex + 1) + "/" + mData.size();
//		mImgCurrentPage.setText(img_index);

        loadManager.hideProgressBar();
        if (mIndex == 0) {
            if (resultList != null
                    && resultList.contains(mData.get(0))) {
                indicator.setSelected(true);
                indicator.setImageResource(R.drawable.btn_selected);
            }
            if (notSelectedList != null
                    && notSelectedList.contains(mData.get(0))) {
                indicator.setVisibility(View.GONE);
            }
        } else {
            mImageViewPager.setCurrentItem(mIndex);
        }
    }

    private void initView() {
        RelativeLayout barTitle = (RelativeLayout) findViewById(R.id.barTitle);
        barTitle.setBackgroundColor(android.R.color.transparent);
        mImageCount = (TextView) findViewById(R.id.img_count);
//		mImgCurrentPage = (TextView) this.findViewById(R.id.img_page);
        mImageViewPager = (ViewPager) this.findViewById(R.id.imgviewPager);
        indicator = (ImageView) findViewById(R.id.img_select_img);
        commit = (Button) findViewById(R.id.img_commit);
        commit.setVisibility(mMode == 0 ? View.GONE : View.VISIBLE);
        indicator.setVisibility(mMode == 0 ? View.GONE : View.VISIBLE);
        setCommitText();
        findViewById(R.id.img_bottom_layout).setVisibility(mMode == 0 ? View.GONE : View.VISIBLE);
        ImageView leftImgBtn = (ImageView) findViewById(R.id.leftImgBtn);
        leftImgBtn.setImageResource(R.drawable.z_z_topbar_ico_back_white);
    }

    private void setCommitText() {
        if (resultList.size() > 0) {
            mImageCount.setText(String.valueOf(resultList.size()));
            mImageCount.setVisibility(View.VISIBLE);
        } else {
            mImageCount.setText("");
            mImageCount.setVisibility(View.GONE);
        }
    }

    private void setClick() {
        findViewById(R.id.save_img).setOnClickListener(this);
        findViewById(R.id.save_Cancel).setOnClickListener(this);
        findViewById(R.id.save_root).setOnClickListener(this);
        findViewById(R.id.img_commit).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.img_select_img).setOnClickListener(this);
        setViewPagerListener();

    }

    // 设置gallery监听
    private void setViewPagerListener() {
        mImageViewPager.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
//				String img_index = (position + 1) + "/" + mData.size();
//				mImgCurrentPage.setText(img_index);
                final String dir = mData.get(position);
                for (String selectDir : resultList) {
                    boolean selected = dir.equals(selectDir);
                    indicator.setSelected(selected);
                    if (selected) {
                        indicator.setImageResource(R.drawable.btn_selected);
                        break;
                    } else {
                        indicator.setImageResource(R.drawable.btn_unselected);
                    }
                }
                for (String notSelectDir : notSelectedList) {
                    boolean notSelected = dir.equals(notSelectDir);
                    if (notSelected) {
                        indicator.setVisibility(View.GONE);
                        break;
                    } else {
                        indicator.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                findViewById(R.id.img_save).setVisibility(View.GONE);
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_commit:
                back(true);
                break;
            case R.id.back:
                onBackPressed();
                break;
            case R.id.img_edit_layout:
                findViewById(R.id.img_edit_layout).setVisibility(View.GONE);
                break;
            //保存图片部分代码
            case R.id.save_img:
                saveImg();
            case R.id.save_root:
            case R.id.save_Cancel:
                findViewById(R.id.img_save).setVisibility(View.GONE);
                break;
            case R.id.img_select_img:
                boolean selected = indicator.isSelected();
                int currentIndex = mImageViewPager.getCurrentItem();

                if (selected) {
                    resultList.remove(mData.get(currentIndex));
                    indicator.setImageResource(R.drawable.btn_unselected);
                    indicator.setSelected(false);
                } else if (resultList.size() >= mMaxCount) {
                    final DialogManager dialogManager = new DialogManager(this);
                    dialogManager.createDialog(new ViewManager(dialogManager)
                            .setView(new TitleMessageView(this).setText("最多可以选择" + mMaxCount + "张图片"))
                            .setView(new HButtonView(this)
                                    .setNegativeText("我知道了", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialogManager.cancel();
                                        }
                                    }))).show();
                    return;
                } else {
                    resultList.add(mData.get(currentIndex));
                    indicator.setImageResource(R.drawable.btn_selected);
                    indicator.setSelected(true);
                }
                setCommitText();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        back(false);
    }

    public void back(boolean isCommit) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(ImageSelectorConstant.EXTRA_RESULT, resultList);
        bundle.putBoolean(ImageSelectorConstant.EXTRA_IS_COMMIT, isCommit);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    //图片保存
    private void saveImg() {
        int position = mImageViewPager.getCurrentItem();
        String url = mData.get(position);
//		final String fileName = url.split("/")[url.split("/").length - 1];
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(this).load(url).build();
        if (bitmapRequest != null)
            bitmapRequest.into(new SubBitmapTarget() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> arg1) {
                    ContentResolver cr = getContentResolver();
                    MediaStore.Images.Media.insertImage(cr, bitmap, "", "");
                    Tools.showToast(ImgWallActivity.this, "图片已保存");
                }
            });
    }

}