package amodule.dish.video.module;

import amodule.topic.model.AddressModel;
import amodule.topic.model.CustomerModel;
import amodule.topic.model.ImageModel;
import amodule.topic.model.TopicModel;
import amodule.topic.model.VideoModel;

public class ShortVideoDetailModule {

    private String mCode;
    private String mName;
    private boolean isEssence;
    private boolean isFav;
    private boolean isLike;
    private String mFavNum;
    private String mCommentNum;
    private String mLikeNum;
    private String mShareNum;
    private String mClickNum;
    private VideoModel mVideoModel;
    private ImageModel mImageModel;
    private CustomerModel mCustomerModel;
    private TopicModel mTopicModel;
    private AddressModel mAddressModel;
    private amodule.dish.video.module.ShareModule mShareModule;
    private String statJson;
    private String rId;

    public String getCode() {
        return mCode;
    }

    public void setCode(String code) {
        mCode = code;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getLikeNum() {
        return mLikeNum;
    }

    public void setLikeNum(String likeNum) {
        mLikeNum = likeNum;
    }

    public String getCommentNum() {
        return mCommentNum;
    }

    public void setCommentNum(String commentNum) {
        mCommentNum = commentNum;
    }

    public boolean isEssence() {
        return isEssence;
    }

    public void setEssence(boolean essence) {
        isEssence = essence;
    }

    public boolean isFav() {
        return isFav;
    }

    public void setFav(boolean fav) {
        isFav = fav;
    }

    public boolean isLike() {
        return isLike;
    }

    public void setLike(boolean like) {
        isLike = like;
    }

    public String getFavNum() {
        return mFavNum;
    }

    public void setFavNum(String favNum) {
        mFavNum = favNum;
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

    public TopicModel getTopicModel() {
        return mTopicModel;
    }

    public void setTopicModel(TopicModel topicModel) {
        mTopicModel = topicModel;
    }

    public AddressModel getAddressModel() {
        return mAddressModel;
    }

    public void setAddressModel(AddressModel addressModel) {
        mAddressModel = addressModel;
    }

    public ShareModule getShareModule() {
        return mShareModule;
    }

    public void setShareModule(ShareModule shareModule) {
        mShareModule = shareModule;
    }

    public void setStatJson(String statJson) {
        this.statJson = statJson;
    }

    public String getStatJson() {
        return statJson;
    }

    public void setrId(String rId) {
        this.rId = rId;
    }

    public String getrId() {
        return rId;
    }
}
