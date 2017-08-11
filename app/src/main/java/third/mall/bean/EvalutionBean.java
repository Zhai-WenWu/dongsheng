package third.mall.bean;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * PackageName : third.mall.bean
 * Created by MrTrying on 2017/8/8 21:37.
 * E_mail : ztanzeyu@gmail.com
 */

public class EvalutionBean {

    public String code;
    /**评分*/
    public int score;
    /**内容*/
    public String content;
    /**图片集合*/
    public ArrayList<String> images = new ArrayList<>();
    /**是否可以分享到朋友圈*/
    public boolean canShare = false;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public void addImage(@NonNull String imageUrl){
        images.add(imageUrl);
    }

    public void replaceImage(String oldUrl,String newUrl){
        images.set(images.indexOf(oldUrl),newUrl);
    }

    public void removeImage(@NonNull String key){
        images.remove(key);
    }

    public boolean isCanShare() {
        return canShare;
    }

    public void setCanShare(boolean canShare) {
        this.canShare = canShare;
    }
}
