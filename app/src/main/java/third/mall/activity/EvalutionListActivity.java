package third.mall.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import acore.logic.LoginManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.CommentBar;
import amodule.user.activity.login.LoginByAccout;
import aplug.web.ShowTemplateWeb;
import aplug.web.tools.XHTemplateManager;
import third.mall.aplug.MallCommon;

/**
 * PackageName : third.mall.activity
 * Created by MrTrying on 2017/8/22 13:55.
 * E_mail : ztanzeyu@gmail.com
 */

public class EvalutionListActivity extends ShowTemplateWeb {

    protected CommentBar commentBar;
    protected RelativeLayout editControlerLayout;
    private TextView goShopping, bubbleSmall, bubbleBig;

    private String callbackName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
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
        initCommentBar();
    }

    @Override
    protected void initTitleView() {
        super.initTitleView();
        title.setText("评价");
        //初始化购物车
//        initShopping();
    }

    /** 初始化购物车 */
    private void initShopping() {
        goShopping = (TextView) findViewById(R.id.rightText);
        bubbleSmall = (TextView) findViewById(R.id.right_bubble_small);
        bubbleBig = (TextView) findViewById(R.id.right_bubble_big);
        setShoppingNumber();

        goShopping.setText("购物车");
        goShopping.setVisibility(View.VISIBLE);
        goShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EvalutionListActivity.this, ShoppingActivity.class));
            }
        });
    }

    /** 设置购物车气泡 */
    public void setShoppingNumber(){
        if(MallCommon.num_shopcat > 0
                && MallCommon.num_shopcat <= 9){
            bubbleSmall.setText(String.valueOf(MallCommon.num_shopcat));
            bubbleSmall.setVisibility(View.VISIBLE);
            bubbleBig.setVisibility(View.GONE);
        }else if(MallCommon.num_shopcat > 9
                && MallCommon.num_shopcat <= 99){
            bubbleBig.setText(String.valueOf(MallCommon.num_shopcat));
            bubbleSmall.setVisibility(View.GONE);
            bubbleBig.setVisibility(View.VISIBLE);
        }else if(MallCommon.num_shopcat > 99){
            bubbleBig.setText("99+");
            bubbleSmall.setVisibility(View.GONE);
            bubbleBig.setVisibility(View.VISIBLE);
        }else{
            bubbleSmall.setVisibility(View.GONE);
            bubbleBig.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        setShoppingNumber();
    }

    protected void initCommentBar() {
        editControlerLayout = (RelativeLayout) findViewById(R.id.edit_controler_layout);
        editControlerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
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
                if (!LoginManager.isLogin()) {
                    Tools.showToast(EvalutionListActivity.this, "请登录");
                    startActivity(new Intent(EvalutionListActivity.this, LoginByAccout.class));
                    return true;
                }
                if (!ToolsDevice.isNetworkAvailable(EvalutionListActivity.this)) {
                    Tools.showToast(EvalutionListActivity.this, "请检查网络连接");
                    return true;
                }
                return false;
            }

            @Override
            public void onPublishComment(String content) {
                if (templateWebView != null)
                    templateWebView.loadUrl("javascript:" + callbackName + "(\"" + content + "\")");
                resetCommentBar();
            }
        });

        final int statusBarHeight = Tools.getStatusBarHeight(this);
        setOnKeyBoardListener(new OnKeyBoardListener() {
            @Override
            public void show() {
                int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
                int heightDifference = usableHeightSansKeyboard - computeUsableHeight();
                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
            }

            @Override
            public void hint() {
                frameLayoutParams.height = mChildOfContent.getRootView().getHeight() - statusBarHeight;
            }
        });
    }


    public void showCommentBar(String userName, String callbackName) {//dsShowCommentBarCallback
        this.callbackName = callbackName;
        commentBar.setEditTextHint("回复 ：" + userName);
        commentBar.show();
    }

    public void resetCommentBar() {
        commentBar.setEditTextHint("回复 ");
        commentBar.resetEdit();
        commentBar.hide();
        callbackName = "";
    }
}
