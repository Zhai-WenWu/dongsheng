package amodule.quan.view;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseAppCompatActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.ImageViewVideo;
import acore.widget.multifunction.MentionStyleBulider;
import acore.widget.multifunction.view.MultifunctionTextView;
import amodule.quan.activity.ShowSubject;
import amodule.quan.tool.SubjectControl;
import amodule.quan.tool.SubjectFloorAdvertControl;
import amodule.quan.tool.SubjectHistroyControl;
import amodule.quan.view.ImgTextCombineLayout.ImgTextCallBack;
import amodule.user.activity.FriendHome;
import aplug.imageselector.ImgWallActivity;
import core.xiangha.emj.view.EditTextShow;
import third.video.VideoImagePlayerController;

public class BarSubjectFloorOwnerNew extends RelativeLayout implements OnClickListener {
    public final static int TAG_ID = R.string.tag;
    private BaseAppCompatActivity mAct;
    private Handler handler;
    private View headerView;
    private LinearLayout sb_header_linear_body_imgs;

    private String folState = "", title = "";
    private ArrayList<String> img_urls;
    /**头部楼主信息*/
    private SubjectHeaderUser headerUser;
    /**贴子的title*/
    private SubjectHeaderTitle headerTitle;
    /**来自哪个圈子*/
    private SubjectHeaderFromCircle fromCircle;
    /** 地理位置 */
    private SubjectHeaderAddress address;
    /** 底部view */
    private SubjectHeaderBottom bottomLayout;
    /** 视频layout，不全屏时 */
    private SubjectHeaderVideoLayout videoLayout;
    /**更多button的layout*/
    private SubjectHeaderMore headerMore;
    /**统计id*/
    public static String tongjiId = "a_post_detail_normal";
    private boolean isHasVideo = false;

    private boolean saveHistoryOver = false;

    public BarSubjectFloorOwnerNew(BaseAppCompatActivity mAct, Handler handler, RelativeLayout qpView) {
        super(mAct);
        this.mAct = mAct;
        this.handler = handler;
        headerView = LayoutInflater.from(mAct).inflate(R.layout.a_circle_header_subject_new, this, true);
        headerView.setVisibility(View.GONE);

        sb_header_linear_body_imgs = (LinearLayout) headerView.findViewById(R.id.sb_header_linear_body_imgs);

        headerUser = (SubjectHeaderUser) findViewById(R.id.subject_header_user);
        videoLayout = (SubjectHeaderVideoLayout) findViewById(R.id.sb_header_video_layout);
        headerMore = (SubjectHeaderMore) findViewById(R.id.subject_header_more);
        headerTitle = (SubjectHeaderTitle) findViewById(R.id.sb_header_tv_title_rela);
        fromCircle = (SubjectHeaderFromCircle) findViewById(R.id.rela_circle_from);
        address = (SubjectHeaderAddress) findViewById(R.id.address);
        bottomLayout = (SubjectHeaderBottom) findViewById(R.id.bottom_layout);

        headerTitle.setTitleOnClick(this);
        //初始化广告
        new SubjectFloorAdvertControl(mAct, this, tongjiId).initAd();
    }

    /**
     * 设置headerView的data
     *
     * @param subjectInfo  title的map数据
     * @param floorsOneInfo 一楼map数据
     */
    public boolean setData(final Map<String, String> subjectInfo, final Map<String, String> floorsOneInfo) {
        title = subjectInfo.get("title");
        folState = subjectInfo.get("folState");

        //设置来自的circle
        fromCircle.setData(subjectInfo.get("classId"));
        //设置user
        headerUser.setData(mAct,folState,floorsOneInfo);
        //设置title
        headerTitle.setTitle(title, subjectInfo.get("type"), subjectInfo.get("isJingHua"));
        //设置title的长按右键点击事件
        doDeleteLou(folState, floorsOneInfo.get("id"));
        //设置video
        videoLayout.setmAct(mAct);
        isHasVideo = videoLayout.setData(floorsOneInfo,mAct);
        if (isHasVideo) {
            tongjiId = "a_post_detail_video";
        }
        //设置more
        headerMore.setData(title, subjectInfo.get("type"));
        //获取菜谱数据
        if (subjectInfo.containsKey("dish")) {
            Map<String, String> dishMap = StringManager.getFirstMap(subjectInfo.get("dish"));
            if (dishMap != null) {
                headerMore.setDishInfo(dishMap.get("code"), dishMap.get("name"));
            }
        }

        //设置地址
        address.setData(subjectInfo.get("address"));
        //设置底部数据
        int likeNum = Integer.parseInt(subjectInfo.get("likeNum"));
        int replyNum = Integer.valueOf(subjectInfo.get("commentNum"));
        int clickNum = Integer.valueOf(subjectInfo.get("clickNum"));
        bottomLayout.setTopData(likeNum, replyNum, clickNum);
        bottomLayout.setLikesShow(StringManager.getListMapByJson(floorsOneInfo.get("likeList")));

        //新的数据展示
        setNewContentImgTv(StringManager.getListMapByJson(floorsOneInfo.get("content")), folState, floorsOneInfo.get("id"));

        headerView.setVisibility(View.VISIBLE);

        //保存浏览记录
        if (!saveHistoryOver) {
            saveHistoryOver = true;
            SubjectHistroyControl.getInance().saveHistoryToDB(mAct, subjectInfo, floorsOneInfo);
        }
        return isHasVideo;
    }

    public String getVideoImg(){
        return videoLayout.getVideoImg();
    }

    public boolean getIsHasVideo(){
        return isHasVideo;
    }

    /**
     * 删除本贴
     *
     * @param folState 关注状态 1是自己贴子
     * @param id 楼层id
     */
    private void doDeleteLou(String folState, final String id) {
        if (folState.equals("1")) {
            headerTitle.setTitleRightClick("删除", new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ToolsDevice.getNetActiveState(mAct)
                            && ToolsDevice.isNetworkAvailable(mAct)) {
                        deleteDialog(id);
                    } else {
                        Tools.showToast(mAct, "网络错误，请检查网络或重试");
                    }
                }
            });
        } else {
            headerTitle.setTitleRightClick("举报", new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage(ShowSubject.REPORT_CLICK, id);
                }
            });
        }
    }

    /**
     * 创建删除dialog
     * @param id 楼层id
     */
    private void deleteDialog(final String id) {
        String params = "type=delFloor&floorId=" + id;
        SubjectControl.getInstance().createDeleteDilog(mAct, params, "本贴",
                new SubjectControl.OnDeleteSuccessCallback() {
                    @Override
                    public void onDeleteSuccess(int flag, String url, Object returnObj) {
                        mAct.finish();
                        //统计删除贴(计算事件)
                        XHClick.onEventValue(mAct, "quanOperate", "quanOperate", "删除贴", 1);
                    }
                });
    }

    private void sendMessage(int what,String id){
        Message msg = handler.obtainMessage(what, id);
        handler.sendMessage(msg);
    }

    /**
     * 新的图文混排控件
     *
     * @param listMapByJson 内容的数据集合
     */
    private void setNewContentImgTv(ArrayList<Map<String, String>> listMapByJson, String folState, final String id) {
        sb_header_linear_body_imgs.removeAllViews();
        img_urls = new ArrayList<>();
        if (listMapByJson.size() != 0) {
            sb_header_linear_body_imgs.setVisibility(View.VISIBLE);
            for (int i = 0; i < listMapByJson.size(); i++) {
                String img_url = listMapByJson.get(i).get("img");
                String text = listMapByJson.get(i).get("text").trim();
                text = text.replace("\n", "").replace("\r", "");
                MultifunctionTextView.MultifunctionText multifunctionText = new MultifunctionTextView.MultifunctionText();
                MentionStyleBulider mentionStyleBulider = new MentionStyleBulider(mAct, text, new MentionStyleBulider.MentionClickCallback() {
                    @Override
                    public void onMentionClick(View v, String userCode) {
                        Intent intent = new Intent(getContext(), FriendHome.class);
                        intent.putExtra("code", userCode);
                        getContext().startActivity(intent);
                    }
                });
                multifunctionText.addStyle(mentionStyleBulider.getContent(), mentionStyleBulider.build());

                ImgTextCombineLayout layout = new ImgTextCombineLayout(mAct);
                layout.setImgTextCallBack(callback);
                layout.setImgText(text, img_url, false);
                layout.setSubjectStyle();
                layout.textview.setText(multifunctionText);
                if (!TextUtils.isEmpty(img_url))
                    img_urls.add(img_url);
                sb_header_linear_body_imgs.addView(layout);
                layout.textview.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Message msg2 = handler.obtainMessage(ShowSubject.REPLY_LZ_CLICK, null);
                        handler.sendMessage(msg2);
                    }
                });
                //处理点击显示的删除状态
                if (folState.equals("1")) {
                    layout.textview.setRightClicker("删除", new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ToolsDevice.getNetActiveState(mAct) && ToolsDevice.isNetworkAvailable(mAct)) {
                                deleteDialog(id);
                            } else {
                                Tools.showToast(mAct, "网络错误，请检查网络或重试");
                            }
                        }
                    });
                } else {
                    layout.textview.setRightClicker("举报", new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendMessage(ShowSubject.REPORT_CLICK, id);
                        }
                    });
                }
            }
        } else
            sb_header_linear_body_imgs.setVisibility(View.GONE);
    }

    /**
     * 点赞成功执行
     *
     * @param returnObj 点赞的返回数据
     */
    public void likeOver(Object returnObj) {
        if (bottomLayout != null) {
            bottomLayout.likeOver(returnObj);
        }
    }

    /** 回复成功 */
    public void replyOver() {
        if (bottomLayout != null) {
            bottomLayout.replyOver();
        }
    }

    // 启动ImgWall
    private void showImgWall(int index) {
        Intent intent = new Intent(mAct, ImgWallActivity.class);
        intent.putStringArrayListExtra("images", img_urls);
        intent.putExtra("index", index);
        mAct.startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //回复楼主
            case R.id.sb_header_tv_title:
            case R.id.sb_header_tv_content:
                Message msg2 = handler.obtainMessage(ShowSubject.REPLY_LZ_CLICK, null);
                handler.sendMessage(msg2);
                break;
        }
    }

    public ImageViewVideo getImageViewVideo() {
        if (videoLayout != null) {
            return videoLayout.getImageViewVideo();
        }
        return null;
    }

    public void onResume() {
        if (videoLayout != null) {
            videoLayout.onResume();
        }
    }

    public void onPause() {
        if (videoLayout != null) {
            videoLayout.onPause();
        }
    }

    public void onDestroy() {
        if (videoLayout != null) {
            videoLayout.onDestroy();
        }
    }

    public boolean onBackPressed(){
        if(videoLayout != null){
            return videoLayout.onBackPressed();
        }
        return false;
    }

    /** 回调 */
    private ImgTextCallBack callback = new ImgTextCallBack() {
        @Override
        public void onDelete(ImgTextCombineLayout layout) {
        }

        @Override
        public void onClick(ImgTextCombineLayout layout) {
        }

        @Override
        public int getWidth() {
            return ToolsDevice.getWindowPx(mAct).widthPixels;
        }

        @Override
        public void onImageClick(ImgTextCombineLayout layout) {
            int index = 0;
            int index_now = 0;
            if (layout != null) {
                for (int i = 0, length = sb_header_linear_body_imgs.getChildCount(); i < length; i++) {
                    if (layout == sb_header_linear_body_imgs.getChildAt(i)) index = i;
                }
                index_now = index;
                for (int i = 0, length = sb_header_linear_body_imgs.getChildCount(); i < length; i++) {
                    if (index >= i) {
                        if (TextUtils.isEmpty(((ImgTextCombineLayout) sb_header_linear_body_imgs.getChildAt(i)).getImgText().get(ImgTextCombineLayout.IMGEURL)))
                            index_now--;
                    }
                }
            }
            showImgWall(index_now);
        }

        @Override
        public void onFocusChange(EditTextShow editTextshow, boolean hasFocus, ImgTextCombineLayout layout) {
        }

        @Override
        public void initImgNull(ImgTextCombineLayout layout) {
        }
    };

    public VideoImagePlayerController getVideoPlayerController() {
        if (videoLayout != null) {
            return videoLayout.getVideoPlayerController();
        }
        return null;
    }

    /**
     * view滚动时调用
     */
    public void viewScroll(){
        if(isHasVideo) videoLayout.viewScroll();
    }


}