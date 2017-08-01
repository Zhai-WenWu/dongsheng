package amodule.dish.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.logic.load.LoadManager;
import acore.override.XHApplication;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.PopWindowDialog;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.dish.db.DataOperate;
import amodule.dish.tools.OffDishToFavoriteControl;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import third.share.BarShare;
import third.share.ShareActivityDialog;
import third.share.ShareTools;
import third.video.VideoPlayerController;
import xh.basic.internet.UtilInternet;
import xh.windowview.BottomDialog;

import static amodule.dish.activity.DetailDish.tongjiId;
import static com.xiangha.R.id.back;
import static com.xiangha.R.id.fav_layout;
import static com.xiangha.R.id.modify_layout;
import static com.xiangha.R.id.share_layout;
import static xh.basic.tool.UtilString.getListMapByJson;

/**
 * Created by Administrator on 2016/9/21.
 */
public class DishTitleViewControlNew implements View.OnClickListener{
    private Context context;
    private DishWebView mDishWebView;
    private ImageView favImg;
    private TextView favText,titleView;
    private Activity detailDish;
    private String state,dishState;
    private Map<String, String> dishInfoMap;//dishInfo数据
    private VideoPlayerController mVideoPlayerController;
    private boolean loading = true;//收藏标示

    private String code;
    private boolean isHasVideo;
    private PopWindowDialog mFavePopWindowDialog;
    private LoadManager loadManager;

    private boolean isHasPower = true;

    private OnDishTitleControlListener mListener;

    public DishTitleViewControlNew(Context context,OnDishTitleControlListener listener){
        this.context= context;
        mListener = listener;
    }

    public void initView(Activity detailDish,DishWebView xhWebView) {
        this.detailDish= detailDish;
        mDishWebView = xhWebView;
        //处理标题
        titleView = (TextView)detailDish.findViewById(R.id.title);
        detailDish.findViewById(back).setOnClickListener(this);
        detailDish.findViewById(fav_layout).setOnClickListener(this);
        detailDish.findViewById(share_layout).setOnClickListener(this);
        detailDish.findViewById(R.id.more_layout).setOnClickListener(this);
        detailDish.findViewById(fav_layout).setVisibility(View.GONE);
        detailDish.findViewById(share_layout).setVisibility(View.INVISIBLE);
        detailDish.findViewById(R.id.more_layout).setVisibility(View.INVISIBLE);
        favText = (TextView) detailDish.findViewById(R.id.tv_fav);
        favImg = (ImageView) detailDish.findViewById(R.id.img_fav);
    }

    public void reset(){

    }

    /**
     * 设置视频控制器
     * @param mVideoPlayerControllers
     */
    public void setVideoContrl(VideoPlayerController mVideoPlayerControllers){
        this.mVideoPlayerController=mVideoPlayerControllers;
    }

    /**
     * 设置数据
     * @param dishInfoMaps-----数据集合
     * @param code------code菜谱
     * @param isHasVideo----是否是视频贴
     */
    public void setData(Map<String, String> dishInfoMaps,String code,boolean isHasVideo,String dishState,LoadManager loadManager){
        this.dishInfoMap=dishInfoMaps;
        this.code= code;
        this.isHasVideo=isHasVideo;
        this.dishState = dishState;
        this.loadManager= loadManager;
    }
    public PopWindowDialog getPopWindowDialog(){
        return mFavePopWindowDialog;
    }
    /**
     * 设置当前状态
     * @param states
     */
    public void setstate(String states){
        this.state= states;
    }

    /**
     * 初始化当前状态
     */
    public void setViewState(){
        //收藏
        if (dishInfoMap.get("isFav").equals("2")) {
            favImg.setImageResource(R.drawable.z_caipu_xiangqing_topbar_ico_fav_active);
            favText.setText("已收藏");
        }
        detailDish.findViewById(R.id.fav_layout).setVisibility(state != null ? View.GONE : View.VISIBLE);
        //分享处理:视频菜谱审核前不显示分享按钮，审核成功后显示分享按钮
//        if(state != null&&isHasVideo){
//            if("6".equals(dishState) || TextUtils.isEmpty(dishState)){//视频菜谱，并且审核通过了
//                detailDish.findViewById(share_layout).setVisibility(View.VISIBLE);
//            }else{
//                detailDish.findViewById(share_layout).setVisibility(View.GONE);
//            }
//        }else{
//            detailDish.findViewById(share_layout).setVisibility(View.VISIBLE);
//        }

        //编辑
        if(state != null){
            if(isHasVideo && ("6".equals(dishState) || TextUtils.isEmpty(dishState))){ //视频菜谱，并且审核通过了，则不允许编辑
                detailDish.findViewById(R.id.more_layout).setVisibility(View.GONE);
                detailDish.findViewById(share_layout).setVisibility(View.VISIBLE);
            }else{
                detailDish.findViewById(R.id.more_layout).setVisibility(View.VISIBLE);
            }
        }else{
            detailDish.findViewById(R.id.more_layout).setVisibility(View.GONE);
            detailDish.findViewById(share_layout).setVisibility(View.VISIBLE);
        }

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case back:
                XHClick.mapStat(detailDish, tongjiId, "顶部导航栏点击量", "返回点击量");
                detailDish.finish();
                break;
            case fav_layout://收藏
                if (detailDish != null)
                    XHClick.track(detailDish, "收藏菜谱");
                doFavorite();
                break;
            case share_layout:
                openShare();
                break;
            case R.id.more_layout: //查看更多按钮
                BottomDialog bottomDialog = new BottomDialog(context);
                bottomDialog.setTopButton("分享", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openShare();
                    }
                }).setBottomButton("编辑", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isHasVideo){
                            Tools.showToast(context,"请用香哈（视频版）编辑");
                        }else doModify();
                    }
                }).show();
                break;
            case modify_layout://编辑
                if(isHasVideo){
                    Tools.showToast(context,"请用香哈（视频版）编辑");
                }else doModify();
                break;
        }
    }
    /**
     * 修改菜谱
     */
    private void doModify() {
        if (state != null) {
            XHClick.onEventValue(detailDish.getApplicationContext(), "dishOperate", "dishOperate", "修改已发布", 1);
            Intent intent = new Intent(detailDish, UploadDishActivity.class);
            intent.putExtra("code", code);
            String dishTypeValue = UploadDishActivity.DISH_TYPE_NORMAL;
            if(isHasVideo)dishTypeValue = UploadDishActivity.DISH_TYPE_VIDEO;
            intent.putExtra(UploadDishActivity.DISH_TYPE_KEY, dishTypeValue);
            intent.putExtra("state", UploadDishActivity.UPLOAD_DISH_EDIT);
            intent.putExtra("titleName", "修改菜谱");
            detailDish.startActivity(intent);
            detailDish.finish();
        }
    }

    private void openShare() {
        if (detailDish != null)
            XHClick.track(detailDish, "分享菜谱");
        XHClick.mapStat(detailDish, "a_share400", "菜谱", "菜谱详情页");
        XHClick.mapStat(detailDish, tongjiId, "顶部导航栏点击量", "分享点击量");

        boolean isAuthor = false;
        String nickName = "",code = "";
        ArrayList<Map<String, String>> cusArray = getListMapByJson(dishInfoMap.get("customer"));
        if(cusArray.size()>0) {
            Map<String, String> cusMap = getListMapByJson(dishInfoMap.get("customer")).get(0);
            code = cusMap.get("code");
            nickName = cusMap.get("nickName");
            //登录并是自己的菜谱贴
            if (LoginManager.isLogin() && !TextUtils.isEmpty(code) && code.equals(LoginManager.userInfo.get("code"))) {
                isAuthor = true;
            }
        }
        Map<String, String> mapData = getShareData(isAuthor);
        Intent intent = new Intent(detailDish, ShareActivityDialog.class);
        intent.putExtra("tongjiId", isAuthor ? "a_my" : "a_user");
        intent.putExtra("isHasReport", !isAuthor);
        intent.putExtra("nickName", nickName);
        intent.putExtra("code", code);
        intent.putExtra("imgUrl", mapData.get("mImgUrl"));
        intent.putExtra("clickUrl", mapData.get("mClickUrl"));
        intent.putExtra("title", mapData.get("mTitle"));
        intent.putExtra("content", mapData.get("mContent"));
        intent.putExtra("type", mapData.get("mType"));
        intent.putExtra("shareFrom", "文章详情");
        detailDish.startActivity(intent);
    }

    public Map<String, String> getShareData(boolean isAuthor) {
        //点击显示数据
        String mType, mTitle, mClickUrl, mContent, mImgUrl,isVideo;
        //登录并是自己的菜谱贴
        if (isAuthor) {
            mTitle = "【香哈菜谱】我上传了" + dishInfoMap.get("name") + "的做法";
            mClickUrl = StringManager.wwwUrl + "caipu/" + dishInfoMap.get("code") + ".html";
            mContent = "我在香哈做出了史上最好吃的" + dishInfoMap.get("name") + "，进来请你免费享用！";
            mImgUrl = dishInfoMap.get("img");
            mType = BarShare.IMG_TYPE_WEB;
            isVideo="1";
            //不是自己的菜谱贴
        } else if (isHasVideo) {
            mTitle = "【香哈菜谱】看了" + dishInfoMap.get("name") + "的教学视频，我已经学会了，味道超赞！";
            mClickUrl = StringManager.wwwUrl + "caipu/" + dishInfoMap.get("code") + ".html";
            mContent = "顶级大厨的做菜视频，讲的真是太详细啦！想吃就赶快进来免费学习吧~ ";
            mImgUrl = dishInfoMap.get("img");
            mType = BarShare.IMG_TYPE_WEB;
            isVideo="2";
        } else {
            mTitle = "【香哈菜谱】" + dishInfoMap.get("name") + "的做法";
            mClickUrl = StringManager.wwwUrl + "caipu/" + dishInfoMap.get("code") + ".html";
            mContent = "我又学会了一道" + dishInfoMap.get("name") + "，太棒了，强烈推荐你也用香哈学做菜！";
            mImgUrl = dishInfoMap.get("img");
            mType = BarShare.IMG_TYPE_WEB;
            isVideo="1";
        }
        Map<String, String> map = new HashMap<>();
        map.put("mType", mType);
        map.put("mTitle", mTitle);
        map.put("mClickUrl", mClickUrl);
        map.put("mContent", mContent);
        map.put("mImgUrl", mImgUrl);
        map.put("isVideo",isVideo);
        return map;
    }

    /**
     * 离线菜谱
     */
    private void doBuyBurden(boolean state) {
        if(!state){
            if(DataOperate.buyBurden(detailDish.getApplicationContext(), dishInfoMap.get("code")).length() > 0)
                DataOperate.deleteBuyBurden(detailDish.getApplicationContext(), dishInfoMap.get("code"));
        }else if(OffDishToFavoriteControl.getIsAutoOffDish(detailDish.getApplicationContext())) {
            XHClick.mapStat(detailDish, tongjiId, "顶部导航栏点击量", "下载点击量");
            if (DataOperate.buyBurden(detailDish.getApplicationContext(), dishInfoMap.get("code")).length() == 0) {
                String dishJson = mListener.getOffDishJson();
                Log.i("DetailDish", "dishJson:" + dishJson);
                if (TextUtils.isEmpty(dishJson)) {
                    Tools.showToast(detailDish.getApplicationContext(), "离线失败");
                } else {
                    String mouldVersion = mDishWebView.getMouldVersion();
                    DataOperate.saveBuyBurden(detailDish.getApplicationContext(), dishJson, mouldVersion);
                    mDishWebView.saveDishData();
//                    Tools.showToast(detailDish.getApplicationContext(), "已成功离线");
                }
            }
        }
    }

    /**
     * 收藏
     */
    private void doFavorite() {
        if (LoginManager.userInfo.size() > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (loading) loadManager.startProgress("仍在进行");
                }
            }, 1000);
            AppCommon.onFavoriteClick(detailDish.getApplicationContext(), "favorites", code,
                    new InternetCallback(detailDish.getApplicationContext()) {
                        @Override
                        public void loaded(int flag, String url, Object returnObj) {
                            loading = false;
                            loadManager.dismissProgress();
                            if (flag >= UtilInternet.REQ_OK_STRING) {
                                Map<String, String> map = getListMapByJson(returnObj).get(0);
                                boolean nowFav = map.get("type").equals("2");
                                doBuyBurden(nowFav);
                                favText.setText(nowFav ? "已收藏" : "  收藏  ");
                                favImg.setImageResource(nowFav ? R.drawable.z_caipu_xiangqing_topbar_ico_fav_active : R.drawable.z_caipu_xiangqing_topbar_ico_fav);

                                //统计
                                XHClick.onEvent(detailDish.getApplicationContext(), "dishFav", nowFav ? "收藏" : "取消");
                                XHClick.mapStat(detailDish, tongjiId, "顶部导航栏点击量", "收藏点击量");
                                dishInfoMap.put("favNum", nowFav ? "2" : "1");
                                if (nowFav) {
                                    boolean isShow = PopWindowDialog.isShowPop(FileManager.xmlKey_shareShowPopDataFavDish, FileManager.xmlKey_shareShowPopNumFavDish);
                                    if (isShow) {
                                        mFavePopWindowDialog = new PopWindowDialog(XHApplication.in(), "收藏成功", "这道菜已经被多人分享过，分享给好友？");
                                        if (isHasVideo && mVideoPlayerController != null && mVideoPlayerController.getVideoImageView() != null) {
                                            String title = "【香哈菜谱】看了" + dishInfoMap.get("name") + "的教学视频，我已经学会了，味道超赞！";
                                            String clickUrl = StringManager.wwwUrl + "video/caipu/" + dishInfoMap.get("code");
                                            ;
                                            String content = "顶级大厨的做菜视频，讲的真是太详细啦！想吃就赶快进来免费学习吧~ " + clickUrl;
//                                            Bitmap bitmap = mVideoPlayerController.getVideoImageView().getBitmap();
//                                            String imgUrl = ShareTools.getBarShare(DetailDish.this).saveDrawable(bitmap, FileManager.save_cache + "/share_" + currentTimeMillis() + ".png");
                                            String type = BarShare.IMG_TYPE_WEB;
                                            String imgUrl = dishInfoMap.get("img");
                                            if (imgUrl == null) {
                                                type = ShareTools.IMG_TYPE_RES;
                                                imgUrl = "" + R.drawable.umen_share_launch;
                                            }
                                            mFavePopWindowDialog.show(type, title, clickUrl, content, imgUrl, "菜谱收藏成功后", "强化分享");
                                        } else {
                                            String type = BarShare.IMG_TYPE_WEB;
                                            String title = "【香哈菜谱】" +  dishInfoMap.get("name") + "的做法";
                                            String clickUrl = StringManager.wwwUrl + "caipu/" + dishInfoMap.get("code") + ".html";
                                            String content = "我又学会了一道" + dishInfoMap.get("name") + "，太棒了，强烈推荐你也用香哈学做菜！";
                                            String imgUrl = dishInfoMap.get("img");
                                            mFavePopWindowDialog.show(type, title, clickUrl, content, imgUrl, "菜谱收藏成功后", "强化分享");
                                        }
                                        XHClick.mapStat(XHApplication.in(), "a_share400", "强化分享", "菜谱收藏成功后");
                                    }
                                }
                            } else {
                                toastFaildRes(flag, true, returnObj);
                            }
                        }
                    });
        } else {
            Intent intent = new Intent(detailDish, LoginByAccout.class);
            detailDish.startActivity(intent);
        }
    }

    public void setOfflineLayoutVisibility(boolean isShow){
        isHasPower = isShow ? (state != null ? false : true) : false;
    }

    public interface OnDishTitleControlListener{
        public String getOffDishJson();
    }

//    /**
//     * 离线菜谱
//     */
//    private void doBuyBurden() {
//        XHClick.mapStat(detailDish, tongjiId, "顶部导航栏点击量", "下载点击量");
//        if (DataOperate.buyBurden(detailDish.getApplicationContext(), dishInfoMap.get("code")).length() == 0) {
//            //若已经下载的离线数量还没到达离线菜谱的上线
//            if (AppCommon.buyBurdenNum >= DataOperate.getDownDishLimit(detailDish.getApplicationContext())) {
//                // 到达极限值，提示再下载要删除一部分了。
//                if (AppCommon.buyBurdenNum == DataOperate.getDownDishLimit(detailDish.getApplicationContext()) && LoginManager.isLogin()) {
//                    new AlertDialog.Builder(detailDish)
//                            .setIcon(android.R.drawable.ic_dialog_alert)
//                            .setTitle("等级不足")
//                            .setMessage("离线清单已达到" + DataOperate.getDownDishLimit(detailDish.getApplicationContext()) + "个,您的等级提升后可下载更多")
//                            .setPositiveButton("查看等级", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    if (LoginManager.isLogin()) {
//                                        String url = StringManager.api_getCustomerRank + "?code=" + LoginManager.userInfo.get("code");
//                                        AppCommon.openUrl(detailDish, url, true);
//                                    } else {
//                                        detailDish.startActivity(new Intent(detailDish, LoginByAccout.class));
//                                    }
//                                }
//                            }).setNegativeButton("整理清单", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Intent intent = new Intent(detailDish, OfflineDish.class);
//                            detailDish.startActivity(intent);
//                            detailDish.finish();
//                        }
//                    }).create().show();
//                }else {// 提示登录可以下载更多菜谱。
//                    new AlertDialog.Builder(detailDish)
//                            .setIcon(android.R.drawable.ic_dialog_alert).setTitle("是否保存")
//                            .setMessage("离线清单已达到" + DataOperate.getDownDishLimit(detailDish.getApplicationContext()) + "个,想要下载更多菜谱请登录")
//                            .setPositiveButton("登录", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    Intent intent = new Intent(detailDish, LoginByAccout.class);
//                                    detailDish.startActivity(intent);
//                                }
//                            }).setNegativeButton("整理清单", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Intent intent = new Intent(detailDish, OfflineDish.class);
//                            detailDish.startActivity(intent);
//                            detailDish.finish();
//                        }
//                    }).create().show();
//                }
//            } else {
//                String dishJson = mListener.getOffDishJson();
//                Log.i("DetailDish","dishJson:" + dishJson);
//                if(TextUtils.isEmpty(dishJson)){
//                    Tools.showToast(detailDish.getApplicationContext(), "离线失败");
//                }else {
//                    DataOperate.saveBuyBurden(detailDish.getApplicationContext(), dishJson);
//                    mDishWebView.saveDishData();
//                    offImg.setImageResource(R.drawable.z_caipu_xiangqing_topbar_ico_offline_active);
//                    offText.setText("已下载");
//                    Tools.showToast(detailDish.getApplicationContext(), "已成功下载到离线清单中");
//                }
//            }
//        } else {
//            Intent intent = new Intent(detailDish, OfflineDish.class);
//            detailDish.startActivity(intent);
//        }
//    }
}
