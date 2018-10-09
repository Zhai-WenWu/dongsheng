package amodule.dish.video.module;

import java.util.Objects;

import amodule.topic.model.AddressModel;
import amodule.topic.model.CustomerModel;
import amodule.topic.model.ImageModel;
import amodule.topic.model.TopicModel;
import amodule.topic.model.VideoModel;

public class ShortVideoDetailModule {
    protected String mCode;
    protected String mName;
    protected boolean isEssence;
    protected boolean isFav;
    protected boolean isLike;
    protected String mFavNum;
    protected String playMode;
    protected String mCommentNum;
    protected String mLikeNum;
    protected String mShareNum;
    protected String mClickNum;
    protected VideoModel mVideoModel;
    protected ImageModel mImageModel;
    protected CustomerModel mCustomerModel;
    protected TopicModel mTopicModel;
    protected AddressModel mAddressModel;
    protected amodule.dish.video.module.ShareModule mShareModule;
    protected String statJson;

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
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShortVideoDetailModule that = (ShortVideoDetailModule) o;
        return isEssence == that.isEssence &&
                isFav == that.isFav &&
                isLike == that.isLike &&
                Objects.equals(mCode, that.mCode) &&
                Objects.equals(mName, that.mName) &&
                Objects.equals(mFavNum, that.mFavNum) &&
                Objects.equals(mCommentNum, that.mCommentNum) &&
                Objects.equals(mLikeNum, that.mLikeNum) &&
                Objects.equals(mShareNum, that.mShareNum) &&
                Objects.equals(mClickNum, that.mClickNum) &&
                Objects.equals(mVideoModel, that.mVideoModel) &&
                Objects.equals(mImageModel, that.mImageModel) &&
                Objects.equals(mCustomerModel, that.mCustomerModel) &&
                Objects.equals(mTopicModel, that.mTopicModel) &&
                Objects.equals(mAddressModel, that.mAddressModel) &&
                Objects.equals(mShareModule, that.mShareModule);
    }

    @Override
    public int hashCode() {

        return Objects.hash(mCode, mName, isEssence, isFav, isLike, mFavNum, mCommentNum, mLikeNum, mShareNum, mClickNum, mVideoModel, mImageModel, mCustomerModel, mTopicModel, mAddressModel, mShareModule);
    }

    public String getPlayMode() {
        return playMode;
    }

    public void setPlayMode(String playMode) {
        this.playMode = playMode;
    }
}
