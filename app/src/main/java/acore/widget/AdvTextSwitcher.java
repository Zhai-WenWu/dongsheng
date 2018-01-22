//package acore.widget;
//
//import android.content.Context;
//import android.os.Handler;
//import android.os.Message;
//import android.text.Html;
//import android.util.AttributeSet;
//import android.view.View;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.TextSwitcher;
//import android.widget.TextView;
//
//import com.xiangha.R;
//
//import java.util.ArrayList;
//import java.util.Timer;
//import java.util.TimerTask;
//
//import acore.tools.Tools;
//
//import static amodule.main.Main.timer;
//
//public class AdvTextSwitcher extends TextSwitcher{
//    private Context mContext;
//    private ArrayList<String> mTexts = new ArrayList<String>();
//    private int index=-1;
//    private Timer timer_text;
//    private AdvTextSwitcher.Callback mCallback;
//    private Handler mHandler = new Handler() {
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case 1:
//                    AdvTextSwitcher.this.next();
//                    break;
//            }
//        };
//    };
//    private class MyTask extends TimerTask {
//        @Override
//        public void run() {
//            mHandler.sendEmptyMessage(1);
//        }
//    }
//    public void setTextStillTime(long time) {
//        if (timer_text == null) {
//            timer_text = new Timer();
//        } else {
//            timer_text.scheduleAtFixedRate(new MyTask(),0,time);
//        }
//    }
//
//    public AdvTextSwitcher(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        this.mContext = context;
//        if (timer_text == null)
//            timer_text = new Timer();
//        Animation animIn = AnimationUtils.loadAnimation(mContext, R.anim.fade_in_slide_in);
//        Animation animOut = AnimationUtils.loadAnimation(mContext, R.anim.fade_out_slide_out);
//
//        this.setInAnimation(animIn);
//        this.setOutAnimation(animOut);
//        this.setFactory(new ViewFactory() {
//            @Override
//            public View makeView() {
//                TextView innerText = new TextView(mContext);
//                innerText.setTextSize(Tools.getDimenSp(mContext, R.dimen.sp_14));
//                innerText.setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onClick(View p1) {
//                        if(mCallback != null)
//                            mCallback.onItemClick(index);
//                    }
//                });
//                return innerText;
//            }
//        });
//    }
//    public void setTexts(ArrayList<String> texts) {
//        if (texts.size() > 0) {
//            this.mTexts = texts;
//        }
//    }
//
//    public void setCallback(Callback callback) {
//        this.mCallback = callback;
//    }
//
//    public void next() {
//        if (mTexts.size() > 0) {
//            if (index < mTexts.size() - 1) {
//                index++;
//            } else {
//                index = 0;
//            }
//            updateDisp();
//        }
//    }
//
//
//    public interface Callback {
//        public void onItemClick(int position);
//    }
//
//    private void updateDisp() {
//        if(mTexts.size()>index)
//            this.setText(Html.fromHtml(mTexts.get(index)));
//    }
//
//    /**
//     * 添加数据
//     * @param texts
//     */
//    public void addNewArray(ArrayList<String> texts){
//        for (int i=0,size=texts.size();i<size;i++){
//            mTexts.add(texts.get(i));
//        }
//    }
//
//    public void clearData(){
//        mTexts.clear();
//        index=-1;
//        timer=null;
//        mCallback=new Callback() {
//            @Override
//            public void onItemClick(int position) {
//
//            }
//        };
//    }
//
//}