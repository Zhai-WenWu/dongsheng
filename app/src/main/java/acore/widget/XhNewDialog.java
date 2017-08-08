package acore.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.xianghatest.R;

import acore.logic.XHClick;

/**
 * Created by Fang Ruijiao on 2017/2/24.
 */
public class XhNewDialog {

    private Context mCon;
    private Dialog mDialog;
    private Window mWindow;
    private String tongjiId;
    private String twoLevel;


    public XhNewDialog(Context context,int layoutId,String tongjiId,String twoLevel) {
        mCon = context;
        this.tongjiId = tongjiId;
        this.twoLevel = twoLevel;
        mDialog = new Dialog(context, R.style.dialog);
        mDialog.setContentView(layoutId);
        mWindow = mDialog.getWindow();
    }

    public void show(){
        mDialog.show();
    }

    public void cancel(){
        mDialog.cancel();
    }

    public XhNewDialog setTitle(String title,String titleColor){
        TextView titleTv = (TextView)mWindow.findViewById(R.id.dialog_title);
        titleTv.setText(title);
        if(!TextUtils.isEmpty(titleColor)){
            titleTv.setTextColor(Color.parseColor(titleColor));
        }
        return this;
    }

    public XhNewDialog setMessage(String message,String messageColor){
        TextView messageTv = (TextView)mWindow.findViewById(R.id.dialog_message);
        messageTv.setText(message);
        if(!TextUtils.isEmpty(messageColor)){
            messageTv.setTextColor(Color.parseColor(messageColor));
        }
        return this;
    }

    public XhNewDialog setCanselButton(final String text, String textColor, boolean isBold, final View.OnClickListener listener) {
        TextView cancelTv = (TextView)mWindow.findViewById(R.id.dialog_cancel);
        cancelTv.setText(text);
        if(!TextUtils.isEmpty(textColor)){
            cancelTv.setTextColor(Color.parseColor(textColor));
        }
        if(isBold){
            TextPaint paint = cancelTv.getPaint();
            paint.setFakeBoldText(true);
        }
        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v);
                if(!TextUtils.isEmpty(tongjiId))
                    XHClick.mapStat(mCon,tongjiId,twoLevel,text);
            }
        });
        return this;
    }

    public XhNewDialog setSureButton(final String text, String textColor, boolean isBold, final View.OnClickListener listener) {
        TextView sureTv = (TextView)mWindow.findViewById(R.id.dialog_sure);
        sureTv.setText(text);
        if(!TextUtils.isEmpty(textColor)){
            sureTv.setTextColor(Color.parseColor(textColor));
        }
        if(isBold){
            TextPaint paint = sureTv.getPaint();
            paint.setFakeBoldText(true);
        }
        sureTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(tongjiId))
                    XHClick.mapStat(mCon,tongjiId,twoLevel,text);
                listener.onClick(v);
            }
        });
        return this;
    }



//    public class Builder{
//        private Context mContext;
//
//        private String title,message,cancelText,sureText,cancelColor,sureColor;
//        private boolean isCancelBold,isSureBold;
//
//        public Builder(Context context){
//            mContext = context;
//        }
//
//        public Dialog build(){
//            XhNewDialog xhNewDialog = new XhNewDialog(mContext);
//
//            return xhNewDialog.getDialog();
//        }
//    }
}
