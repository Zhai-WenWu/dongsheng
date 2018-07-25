package amodule.topic.model;

/**
 * 话题列表item数据
 */
public class TopicItemModel {

    private String mVideoCode;//视频code
    private String mVideoName;//视频名称、描述
    private boolean mIsEssence;//是否精选
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
}
