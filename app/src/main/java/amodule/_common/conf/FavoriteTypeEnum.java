package amodule._common.conf;

public enum FavoriteTypeEnum {
    TYPE_DISH_ImageNText("1"),//图文菜谱
    TYPE_DISH_VIDEO("2"),//视频菜谱
    TYPE_NOUS("3"),//小知识
    TYPE_MENU("4"),//专辑（菜单）
    TYPE_SUBJECT("5"),//美食帖
    TYPE_ARTICLE("6"),//文章
    TYPE_VIDEO("7");//短视频

    private String mType;
    FavoriteTypeEnum(String type) {
        mType = type;
    }

    public String getType() {
        return mType;
    }

    public static FavoriteTypeEnum getTypeEnumByStr(String typeStr) {
        if (typeStr == null)
            return null;
        switch (typeStr) {
            case "1":
                return TYPE_DISH_ImageNText;
            case "2":
                return TYPE_DISH_VIDEO;
            case "3":
                return TYPE_NOUS;
            case "4":
                return TYPE_MENU;
            case "5":
                return TYPE_SUBJECT;
            case "6":
                return TYPE_ARTICLE;
            case "7":
                return TYPE_VIDEO;
            default:
                return null;
        }
    }
}
