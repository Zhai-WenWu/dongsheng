package third.mall.bean;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * PackageName : third.mall.bean
 * Created by MrTrying on 2017/8/8 21:37.
 * E_mail : ztanzeyu@gmail.com
 */

public class EvalutionBean {

    public String orderId;
    public String productId;
    /**评分*/
    public int score;
    /**内容*/
    public String content;
    /**图片集合*/
    public ArrayList<String> images = new ArrayList<>();
    /**是否可以分享到朋友圈*/
    public boolean canShare = false;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String code) {
        this.productId = code;
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
        if(images.contains(oldUrl) && images.indexOf(oldUrl) < images.size()
                && images.contains(newUrl)){
            images.set(images.indexOf(oldUrl),newUrl);
        }else{
            addImage(newUrl);
        }
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
