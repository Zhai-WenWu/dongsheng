package amodule.main.bean;

import java.io.Serializable;

/**
 * 首页所用的数据结构
 */
public class HomeModuleBean implements Serializable{
    private String title;//一级标题
    private String type;//一级类型
    private String twoType;//二级类型
    private String twoTitle;//二级标题
    private int position;//当前位置
    private int twoTypeIndex=0;//当前位置是在0
    private String twoData;//二级数据
    private String webUrl;//加载页面的链接
    private String isSelf;//是否为站外链接 1：否 2：是
    private String openSelf;//是否在当前WebView页面打开 1：否 2：是

    public String getOpenSelf() {
        return openSelf;
    }

    public void setOpenSelf(String openSelf) {
        this.openSelf = openSelf;
    }

    public void setIsSelf(String isSelf) {
        this.isSelf = isSelf;
    }

    public String getIsSelf() {
        return isSelf;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTwoType() {
        return twoType;
    }

    public void setTwoType(String twoType) {
        this.twoType = twoType;
    }

    public String getTwoTitle() {
        return twoTitle;
    }

    public void setTwoTitle(String twoTitle) {
        this.twoTitle = twoTitle;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getTwoTypeIndex() {
        return twoTypeIndex;
    }

    public void setTwoTypeIndex(int twoTypeIndex) {
        this.twoTypeIndex = twoTypeIndex;
    }

    public String getTwoData() {
        return twoData;
    }

    public void setTwoData(String twoData) {
        this.twoData = twoData;
    }

    public String toString(){
        return "title="+title+"&type="+type+"&twoType="+twoType+"&twoTitle="+twoTitle+"&position="+position;
    }
}
