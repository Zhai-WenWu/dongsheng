package amodule.article.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;

import com.xiangha.R;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/19 09:46.
 * E_mail : ztanzeyu@gmail.com
 */

public class InputUrlDialog extends Dialog implements View.OnClickListener{

    private EditText urlEdit;
    private EditText descEdit;

    private OnReturnResultCallback mOnReturnResultCallback;

    public InputUrlDialog(@NonNull Context context) {
        super(context, R.style.dialog);
        setContentView(R.layout.a_article_view_link);
        urlEdit= (EditText) findViewById(R.id.url_edit);
        descEdit= (EditText) findViewById(R.id.desc_edit);

        findViewById(R.id.sure).setOnClickListener(this);
        findViewById(R.id.cannel).setOnClickListener(this);
        findViewById(R.id.dialog_root).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.cannel:
                if(mOnReturnResultCallback != null){
                    mOnReturnResultCallback.onCannel();
                }
            case R.id.dialog_root:
                dismiss();
                break;
            case R.id.sure:
                dismiss();
                String url = urlEdit.getText().toString().trim();
                String desc = descEdit.getText().toString().trim();
                if(mOnReturnResultCallback != null){
                    mOnReturnResultCallback.onSure(url,desc);
                }
                break;
        }
    }

    public interface OnReturnResultCallback{
        public void onSure(String url,String desc);
        public void onCannel();
    }

    public OnReturnResultCallback getOnReturnResultCallback() {
        return mOnReturnResultCallback;
    }

    public void setOnReturnResultCallback(OnReturnResultCallback mOnReturnResultCallback) {
        this.mOnReturnResultCallback = mOnReturnResultCallback;
    }
}
