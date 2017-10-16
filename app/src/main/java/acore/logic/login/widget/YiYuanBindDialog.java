package acore.logic.login.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.xiangha.R;

import acore.logic.LoginManager;
import acore.logic.XHClick;
import amodule.user.activity.login.LoginByAccout;

/**
 * 一元VIP的弹窗
 * Created by sll on 2017/10/10.
 */

public class YiYuanBindDialog extends Dialog {

    private TextView mTitleView;
    private TextView mDescView;
    private Button mBindBtn;
    private Context mContext;

    public YiYuanBindDialog(@NonNull Context context) {
        this(context, R.style.dialog);
        mContext = context;
    }

    public YiYuanBindDialog(@NonNull Context context, @StyleRes int themeResId) {
        this(context, true, null);
    }

    protected YiYuanBindDialog(@NonNull final Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        View contentView = LayoutInflater.from(context).inflate(R.layout.yiyuan_dialog_content, null);
        setContentView(contentView);
        getWindow().setBackgroundDrawableResource(R.drawable.bg_round_white13);

        mTitleView = (TextView) contentView.findViewById(R.id.title );
        mDescView = (TextView) contentView.findViewById(R.id.desc);
        mBindBtn = (Button) contentView.findViewById(R.id.btn1);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn1:
                        if (LoginManager.isLogin()) {
                            LoginManager.setAutoBindYiYuanVIP(true);
                            LoginManager.bindYiYuanVIP(mContext);
                            XHClick.mapStat(context, "a_vip_thismove", "转移到本账号", "");
                        } else {
                            LoginManager.setAutoBindYiYuanVIP(true);
                            Intent intent = new Intent(mContext, LoginByAccout.class);
                            mContext.startActivity(intent);
                            XHClick.mapStat(context, "a_vip_newmove", "转移到香哈账号", "");
                        }
                        YiYuanBindDialog.this.cancel();
                        break;
                    case R.id.btn2:
                        YiYuanBindDialog.this.cancel();
                        if (LoginManager.isLogin()) {
                            XHClick.mapStat(context, "a_vip_thismove","取消", "");
                        } else {
                            XHClick.mapStat(context, "a_vip_newmove","取消", "");
                        }
                        break;
                }
            }
        };
        contentView.findViewById(R.id.btn1).setOnClickListener(listener);
        contentView.findViewById(R.id.btn2).setOnClickListener(listener);
    }

    public void show(String title, String desc) {
        mTitleView.setText(title);
        mDescView.setText(desc);
        mBindBtn.setText(mContext.getString(LoginManager.isLogin() ? R.string.vip_transfer_this : R.string.vip_transfer_xh));
        show();
    }

}
