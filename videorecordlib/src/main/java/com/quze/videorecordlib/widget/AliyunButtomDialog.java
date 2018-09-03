//package com.quze.videorecordlib.widget;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.os.Bundle;
//import android.view.Gravity;
//import android.view.View;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.TextView;
//
//import com.quze.videorecordlib.R;
//
//
//public class AliyunButtomDialog extends Dialog implements View.OnClickListener {
//    private View view;
//    private TextView tv_refresh_record;
//    private TextView btn_quit;
//    private TextView btn_cancel;
//    OnBtnClickListener onBtnClickListener;
//
//    public void setOnBtnClickListener(OnBtnClickListener onBtnClickListener) {
//        this.onBtnClickListener = onBtnClickListener;
//    }
//
//    //这里的view其实可以替换直接传layout过来的 因为各种原因没传(lan)
//    public AliyunButtomDialog(Context context) {
//        super(context, R.style.AliyunBackDialog);
//        this.view = View.inflate(context, R.layout.dialog_aliyun_back,null);
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(view);//这行一定要写在前面
//        initView(view);
//        setCancelable(true);//点击外部不可dismiss
//        setCanceledOnTouchOutside(true);
//        Window window = this.getWindow();
//        window.setGravity(Gravity.BOTTOM);
//        WindowManager.LayoutParams params = window.getAttributes();
//        params.width = WindowManager.LayoutParams.MATCH_PARENT;
//        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        window.setAttributes(params);
//    }
//
//    private void initView(View view){
//        tv_refresh_record = view.findViewById(R.id.tv_refresh_record);
//        btn_quit = findViewById(R.id.btn_quit);
//        btn_cancel = findViewById(R.id.btn_cancel);
//
//        tv_refresh_record.setOnClickListener(this);
//        btn_quit.setOnClickListener(this);
//        btn_cancel.setOnClickListener(this);
//    }
//
//    @Override
//    public void onClick(View v) {
//        int i = v.getId();
//        if (i == R.id.btn_cancel) {
//            dismiss();
//
//        } else if (i == R.id.btn_quit) {
//            dismiss();
//            if (onBtnClickListener != null) {
//                onBtnClickListener.quit();
//            }
//
//        } else if (i == R.id.tv_refresh_record) {
//            dismiss();
//            if (onBtnClickListener != null) {
//                onBtnClickListener.refresh();
//            }
//
//        }
//    }
//
//    public interface OnBtnClickListener{
//        void quit();
//        void refresh();
//    }
//}
