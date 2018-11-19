package amodule.topic.model;

import android.graphics.Bitmap;

/**
 * 话题列表item数据
 */
public class TopicItemModel {

    public static final int TAB_HOT = 1;
    public static final int TAB_NEW = 2;
    private String mVideoCode;//视频code
    private String mVideoName;//视频名称、描述
    private boolean mIsEssence;//是否精选
    private boolean mIsHot;//是否热度排序
    private boolean mIsFav;//是否收藏
    private boolean mIsLike;//是否点赞
    private String mFavNum;//收藏量
    private String mLikeNum;//点赞量
    private String mShareNum;//分享量
    private String mClickNum;//浏览量
    private String mGotoUrl;
    private VideoModel mVideoModel;
    private ImageModel mImageModel;
    private CustomerModel mCustomerModel;
    private AddressModel mAddressModel;
    private LabelModel mLabelModel;
    private Bitmap mBitmap;
    private boolean isShow;
    private int mItemType;//1,活动二图片2,tab3,视频列表
    //statJson	String	{"type":"7","code":"647470"}
    private String statJson;
    private int mTabTag;
    private int mHotNo;

    public int getHotNo() {
        return mHotNo;
    }

    public void setHotNo(int mHotNo) {
        this.mHotNo = mHotNo;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public String getStatJson() {
        return statJson;
    }

    public void setStatJson(String statJson) {
        this.statJson = statJson;
    }

    public boolean getIsShow() {
        return isShow;
    }

    public void setIsShow(boolean show) {
        isShow = show;
    }

    public boolean getIsHot() {
        return mIsHot;
    }

    public void setIsHot(boolean mIsHot) {
        this.mIsHot = mIsHot;
    }

    public String getVideoCode() {
        return mVideoCode;
    }

    public void setVideoCode(String videoCode) {
        mVideoCode = videoCode;
    }

    public String getVideoName() {
        return mVideoName;
    }

    public void setVideoName(String videoName) {
        mVideoName = videoName;
    }

    public boolean isEssence() {
        return mIsEssence;
    }

    public void setEssence(boolean essence) {
        mIsEssence = essence;
    }

    public boolean isFav() {
        return mIsFav;
    }

    public void setFav(boolean fav) {
        mIsFav = fav;
    }

    public boolean isLike() {
        return mIsLike;
    }

    public void setLike(boolean like) {
        mIsLike = like;
    }

    public String getFavNum() {
        return mFavNum;
    }

    public void setFavNum(String favNum) {
        mFavNum = favNum;
    }

    public String getLikeNum() {
        return mLikeNum;
    }

    public void setLikeNum(String likeNum) {
        mLikeNum = likeNum;
    }

    public String getShareNum() {
        return mShareNum;
    }

    public void setShareNum(String shareNum) {
        mShareNum = shareNum;
    }

    public String getClickNum() {
        return mClickNum;
    }

    public void setClickNum(String clickNum) {
        mClickNum = clickNum;
    }

    public VideoModel getVideoModel() {
        return mVideoModel;
    }

    public void setVideoModel(VideoModel videoModel) {
        mVideoModel = videoModel;
    }

    public ImageModel getImageModel() {
        return mImageModel;
    }

    public void setImageModel(ImageModel imageModel) {
        mImageModel = imageModel;
    }

    public CustomerModel getCustomerModel() {
        return mCustomerModel;
    }

    public void setCustomerModel(CustomerModel customerModel) {
        mCustomerModel = customerModel;
    }

    public AddressModel getAddressModel() {
        return mAddressModel;
    }

    public void setAddressModel(AddressModel addressModel) {
        mAddressModel = addressModel;
    }

    public LabelModel getLabelModel() {
        return mLabelModel;
    }

    public void setLabelModel(LabelModel labelModel) {
        mLabelModel = labelModel;
    }

    public String getGotoUrl() {
        return mGotoUrl;
    }

    public void setGotoUrl(String gotoUrl) {
        mGotoUrl = gotoUrl;
    }

    public int getItemType() {
        return mItemType;
    }

    public void setItemType(int itemType) {
        this.mItemType = itemType;
    }

    public void setTabTag(int tabTag) {
        this.mTabTag = tabTag;
    }

    public int getTabTag() {
        return mTabTag;
    }
}
