package third.mall.activity;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.xianghatest.R;

import acore.logic.LoginManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.CommentBar;
import amodule.user.activity.login.LoginByAccout;
import aplug.web.ShowTemplateWeb;
import aplug.web.tools.XHTemplateManager;

/**
 * PackageName : third.mall.activity
 * Created by MrTrying on 2017/8/22 13:55.
 * E_mail : ztanzeyu@gmail.com
 */

public class EvalutionListActivity extends ShowTemplateWeb {

    protected CommentBar commentBar;
    protected RelativeLayout editControlerLayout;

    private String callbackName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent  = getIntent();
        intent.putExtra(EvalutionListActivity.REQUEST_METHOD, XHTemplateManager.DSCOMMENTLIST);
        setIntent(intent);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.a_evalution_list_template_webview;
    }

    @Override
    protected void initUI() {
        super.initUI();
        title.setText("评价");
        initCommentBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected int heightDifference = -1;
    protected void initCommentBar() {
        editControlerLayout = (RelativeLayout) findViewById(R.id.edit_controler_layout);
        editControlerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        resetCommentBar();
                        break;
                }
                return false;
            }
        });
        commentBar = (CommentBar) findViewById(R.id.comment_bar);
        commentBar.setVisibility(View.GONE);
        commentBar.setOnPublishCommentCallback(new CommentBar.OnPublishCommentCallback() {
            @Override
            public boolean onPrePublishComment() {
                if(!LoginManager.isLogin()){
                    Tools.showToast(EvalutionListActivity.this,"请登录");
                    startActivity(new Intent(EvalutionListActivity.this, LoginByAccout.class));
                    return true;
                }
                if(!ToolsDevice.isNetworkAvailable(EvalutionListActivity.this)){
                    Tools.showToast(EvalutionListActivity.this,"请检查网络连接");
                    return true;
                }
                return false;
            }

            @Override
            public void onPublishComment(String content) {
                if(templateWebView != null)
                    templateWebView.loadUrl("Javascript:"+callbackName+"(\""+content+"\")");
                resetCommentBar();
            }
        });
        setOnKeyBoardListener(new OnKeyBoardListener() {
            @Override
            public void show() {
                if(heightDifference != -1){
                    int heightDiff = rl.getRootView().getHeight() - rl.getHeight();
                    Rect r = new Rect();
                    rl.getWindowVisibleDisplayFrame(r);
                    int screenHeight = rl.getRootView().getHeight();
                    heightDifference = screenHeight - (r.bottom - r.top);
                    boolean isKeyboradShow = heightDifference > 200;
                    heightDifference = isKeyboradShow ? heightDifference - heightDiff : 0;
                }
                editControlerLayout.setPadding(0, 0, 0, heightDifference);
            }

            @Override
            public void hint() {
                editControlerLayout.setPadding(0, 0, 0, 0);
            }
        });
    }

    public void showCommentBar(String userName,String callbackName){//dsShowCommentBarCallback
        this.callbackName = callbackName;
        commentBar.setVisibility(View.VISIBLE);
        commentBar.setEditTextHint("回复 ：" + userName);
    }

    public void resetCommentBar(){
        commentBar.setEditTextHint("回复 ");
        commentBar.resetEdit();
        commentBar.hide();
        callbackName = "";
    }
}
