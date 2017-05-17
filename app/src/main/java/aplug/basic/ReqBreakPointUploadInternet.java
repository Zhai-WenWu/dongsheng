package aplug.basic;

import android.text.TextUtils;
import android.util.Log;


/**
 *断点上传类
 */
public class ReqBreakPointUploadInternet extends BreakPointUploadInternet{


    public ReqBreakPointUploadInternet() {
        super();
    }

    @Override
    public void breakPointUpload(String filePath, String key, String token, BreakPointUploadCallBack callBack) {
        if(TextUtils.isEmpty(key)||TextUtils.isEmpty(token)){
            Log.i("qiniu",  "key或token数据为null ");
            return ;
        }
        super.breakPointUpload(filePath, key, token, callBack);
    }
}
