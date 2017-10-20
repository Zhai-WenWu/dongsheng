package acore.logic.login.widget;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

/**
 * Created by sll on 2017/10/11.
 */

public class MsgNotifyDialog extends Dialog {

    private TextView mTitleView;
    private TextView mContentView;
    private Button mSingleBtn;

    public MsgNotifyDialog(@NonNull Context context) {
        this(context, R.style.dialog);
    }

    public MsgNotifyDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        initView(context);
    }

    protected MsgNotifyDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initView(context);
    }

    private void initView(Context context) {
        View content = LayoutInflater.from(context).inflate(R.layout.msg_dialog_layout, null);
        mTitleView = (TextView) content.findViewById(R.id.title);
        mContentView = (TextView) content.findViewById(R.id.desc);
        mSingleBtn = (Button) content.findViewById(R.id.know);
        setContentView(content);
    }

    public void show(String title, String desc, String btnText) {
        mTitleView.setText(title == null ? "" : title);
        mContentView.setText(desc == null ? "" : desc);
        mSingleBtn.setText(btnText == null ? "" : btnText);
        show();
    }

    public void setMsgBtnClickListener(View.OnClickListener clickListener) {
        if (mSingleBtn != null && clickListener != null) {
            mSingleBtn.setOnClickListener(clickListener);
        }
    }
}
