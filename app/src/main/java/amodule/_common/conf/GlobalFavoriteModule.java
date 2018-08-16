package amodule._common.conf;

public class GlobalFavoriteModule {

    private String mFavCode;
    private boolean mIsFav;
    private String mFavNum;
    private FavoriteTypeEnum mFavType;

    public String getFavCode() {
        return mFavCode;
    }

    public void setFavCode(String favCode) {
        mFavCode = favCode;
    }

    public boolean isFav() {
        return mIsFav;
    }

    public void setFav(boolean fav) {
        mIsFav = fav;
    }

    public String getFavNum() {
        return mFavNum;
    }

    public void setFavNum(String favNum) {
        mFavNum = favNum;
    }

    public FavoriteTypeEnum getFavType() {
        return mFavType;
    }

    public void setFavType(FavoriteTypeEnum favType) {
        mFavType = favType;
    }
}
