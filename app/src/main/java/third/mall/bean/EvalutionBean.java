package third.mall.bean;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.LinkedHashMap;

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
    public LinkedHashMap<String,String> images = new LinkedHashMap <>();
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

    public LinkedHashMap <String,String> getImages() {
        return images;
    }

    public void addImage(@NonNull String imageUrl){
        images.put(imageUrl,"");
//       //YLKLog.i("tzy","images = " + images.toString());
    }

    public void replaceImage(String oldUrl,String newUrl){
        if(!TextUtils.isEmpty(oldUrl) && !TextUtils.isEmpty(newUrl)){
            if(images.containsKey(oldUrl)){
                images.put(oldUrl,newUrl);
            }else{
                addImage(oldUrl);
            }
        }
//       //YLKLog.i("tzy","images = " + images.toString());
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
