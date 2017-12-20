package amodule.article.upload;

import android.content.Context;

import acore.tools.StringManager;
import amodule.comment.activity.PublishCommentActivity;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * PackageName : amodule.article.upload
 * Created by MrTrying on 2017/5/26 15:38.
 * E_mail : ztanzeyu@gmail.com
 */

public class CommentUpload {

    private static volatile CommentUpload mInstance= null;

    private CommentUpload(){}

    public synchronized static CommentUpload getInstance(){
        if(mInstance==null){
            synchronized (CommentUpload.class){
                if(mInstance == null){
                    mInstance = new CommentUpload();
                }
            }
        }
        return mInstance;
    }

    public void uploadComment(final Context context,String type, String code, String content){
        //拼接参数
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append(PublishCommentActivity.EXTRAS_TYPE).append("=").append(type)
                .append("&").append(PublishCommentActivity.EXTRAS_CODE).append("=").append(code)
                .append("&").append("content").append("=").append(content);
        //发请求
        ReqEncyptInternet.in().doEncypt(StringManager.api_addForum, sbuilder.toString(),
                new InternetCallback() {
                    @Override
                    public void loaded(int i, String s, Object o) {
                        if (i >= ReqEncyptInternet.REQ_OK_STRING) {
                            if(mOnCommentResult != null){
                                mOnCommentResult.onSuccess(o.toString());
                            }
                        } else {
                            if(mOnCommentResult != null){
                                mOnCommentResult.onFailed(o.toString());
                            }
                        }
                    }
                });
    }

    private OnCommentResult mOnCommentResult;
    public interface OnCommentResult{
        public void onSuccess(String msg);
        public void onFailed(String msg);
    }

    public OnCommentResult getOnCommentResult() {
        return mOnCommentResult;
    }

    public void setOnCommentResult(OnCommentResult mOnCommentResult) {
        this.mOnCommentResult = mOnCommentResult;
    }
}
