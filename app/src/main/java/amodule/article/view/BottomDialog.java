package amodule.article.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangha.R;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/6/2 11:24.
 * E_mail : ztanzeyu@gmail.com
 */
public class BottomDialog extends Dialog {

    private LinearLayout buttonLayout;
    private TextView cannleButton;

    private View.OnClickListener cannleClick;

    public BottomDialog(@NonNull Context context) {
        super(context, R.style.dialog);
        setContentView(R.layout.view_bottom_dialog);
        initView();
    }

    private void initView() {
        buttonLayout = (LinearLayout) findViewById(R.id.bottom_dialog_button_layout);
        cannleButton = (TextView) findViewById(R.id.bottom_dialog_cancel);
        buttonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        cannleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cannleClick != null){
                    cannleClick.onClick(v);
                }
                dismiss();
            }
        });
    }

    public BottomDialog setCannleClick(String buttonText, View.OnClickListener onClickListener){
        if(!TextUtils.isEmpty(buttonText)){
            cannleButton.setText(buttonText);
        }
        this.cannleClick = onClickListener;
        return this;
    }

    public BottomDialog addButton(@NonNull String buttonText, final View.OnClickListener onClickListener ){
        if(buttonLayout.getChildCount() != 0){
            addLineView();
        }
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_bottom_dialog_button,null);
        TextView textView = (TextView) view.findViewById(R.id.bottom_dialog_bottom);
        textView.setText(buttonText);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onClickListener != null){
                    onClickListener.onClick(v);
                }
                dismiss();
            }
        });
        buttonLayout.addView(view);
        return this;
    }

    private void addLineView() {
        View view = new View(getContext());
        view.setBackgroundColor(Color.parseColor("#e0e0e0"));
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,1));
        buttonLayout.addView(view);
    }

}