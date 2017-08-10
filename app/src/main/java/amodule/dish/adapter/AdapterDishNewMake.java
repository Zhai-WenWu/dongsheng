package amodule.dish.adapter;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xianghatest.R;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.widget.ImageViewVideo;
import amodule.dish.activity.upload.UploadDishActivity;
import amodule.dish.tools.UploadDishSpeechTools;
import amodule.dish.tools.upload.UploadDishVideoControl;
import aplug.recordervideo.activity.ChooseVideoActivity;
import aplug.recordervideo.tools.FileToolsCammer;

import static amodule.dish.tools.upload.UploadDishVideoControl.MAKE_INFO_MAX_TEXT;
import static amodule.dish.tools.upload.UploadDishVideoControl.MAKE_VIDEO_MAX_S;
import static amodule.dish.tools.upload.UploadDishVideoControl.MAKE_VIDEO_S_TEXT;

/**
 * Created by Fang Ruijiao on 2016/10/25.
 */
public class AdapterDishNewMake extends AdapterSimple {
    private BaseActivity mAct;
    private ArrayList<Map<String, String>> mData;
    private LayoutInflater mLayoutInflater;

    public AdapterDishNewMake(BaseActivity act,View parent, ArrayList<Map<String, String>> data, int resource, String[] from, int[] to) {
        super(parent, data, resource, from, to);
        mAct = act;
        mData = data;
        mLayoutInflater = LayoutInflater.from(parent.getContext());
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Map<String, String> map = mData.get(position);
        ViewCache viewCache;
        if (convertView == null) {
            viewCache = new ViewCache();
            convertView =mLayoutInflater.inflate(R.layout.a_dish_upload_make_video_item,parent, false);
            int[] viewId = new int[] {R.id.iv_makes_back0,R.id.tv_makeStep,R.id.dish_up_make_title,R.id.tv_make_path,R.id.video_makes_item_video_img,
                                    R.id.dish_up_speech_make_title,R.id.dish_make_video_relase,R.id.dish_make_video_cut,R.id.dish_make_opration
                                    ,R.id.video_makes_parent,R.id.dish_make_video_title_hint};
            viewCache.setView(convertView, viewId);
            viewCache.etMakeTitle.setTag(position);
            viewCache.etMakeTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    // 关键代码
                    v.dispatchWindowFocusChanged(hasFocus);
                }
            });
            final TextView tvTitleHint = viewCache.tv_title_hint;
            viewCache.etMakeTitle.addTextChangedListener(new MyTextWatcher(viewCache) {
                @Override
                public void afterTextChanged(Editable s, ViewCache holder) {
                    int position = (Integer) holder.etMakeTitle.getTag();
                    Map<String, String> n = mData.get(position);
                    String inputStr = s.toString().replaceAll("\n","");
                    n.put("makesInfo",inputStr);
                    String videoInfo = n.get("videoInfo");
                    String curTime = "0";
                    if(!TextUtils.isEmpty(videoInfo)){
                        ArrayList<Map<String, String>> videoInfoArray = StringManager.getListMapByJson(videoInfo);
                        if(videoInfoArray.size() > 0){
                            curTime = videoInfoArray.get(0).get("cutTime");
                        }
                    }
                    oprtionTitleHintTv(inputStr,curTime,tvTitleHint);
                    mData.set(position, n);
                }
            });
            convertView.setTag(viewCache);
        } else {
            viewCache = (ViewCache) convertView.getTag();
            viewCache.etMakeTitle.setTag(position);
        }
        viewCache.setValue(map,position);
        return convertView;
    }

    class ViewCache {
        private View makesBackLayout;
        private TextView tv_step;
        private EditText etMakeTitle;
        private TextView tv_make_path,tv_title_hint;
        private ImageViewVideo iv_make_video_img;
        private View speechMakeTitle,videoRelase,videoCut,dishMakeOpration;
        private LinearLayout video_makes_parent;

        public void setView(View view, int... param) {
            makesBackLayout = view.findViewById(param[0]);
            tv_step = (TextView) view.findViewById(param[1]);
            etMakeTitle = (EditText) view.findViewById(param[2]);
            tv_make_path = (TextView) view.findViewById(param[3]);
            iv_make_video_img = (ImageViewVideo) view.findViewById(param[4]);
            speechMakeTitle = view.findViewById(param[5]);
            videoRelase = view.findViewById(param[6]);
            videoCut = view.findViewById(param[7]);
            dishMakeOpration = view.findViewById(param[8]);
            video_makes_parent = (LinearLayout) view.findViewById(param[9]);
            tv_title_hint = (TextView) view.findViewById(param[10]);
        }

        public void setValue(final Map<String, String> map, final int index) {
            tv_step.setText(map.get("makesStep"));
            etMakeTitle.setText(map.get("makesInfo"));
            //选择视频
            View.OnClickListener addMake = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    XHClick.mapStat(mAct, UploadDishVideoControl.tongjiId,"视频编辑","更换");
                    Intent intent = new Intent(mAct, ChooseVideoActivity.class);
                    intent.putExtra("postion",index);
                    mAct.startActivityForResult(intent, UploadDishActivity.DISH_ADD_MAKE);
                }
            };
            makesBackLayout.setOnClickListener(addMake);
            videoRelase.setOnClickListener(addMake);
            String videoInfo = map.get("videoInfo");
            String videoPath = null,imgPath = null;
            if(!TextUtils.isEmpty(videoInfo)) {
                try {
                    JSONObject jsonObject = new JSONObject(videoInfo);
                    videoPath = jsonObject.getString("path");
                    imgPath = jsonObject.getString("imgPath");
                    String cutTime = jsonObject.getString("cutTime");
                    oprtionTitleHintTv(map.get("makesInfo"),cutTime,tv_title_hint);
                } catch (Exception e) {
                }
            }
            if(TextUtils.isEmpty(videoPath) || TextUtils.isEmpty(imgPath)){
                iv_make_video_img.setVisibility(View.GONE);
                dishMakeOpration.setVisibility(View.GONE);
            }else{
                iv_make_video_img.setVisibility(View.VISIBLE);
                iv_make_video_img.playImgWH = playImgWH;
                if(!new File(imgPath).exists()){
                    FileToolsCammer.getImgPath(videoPath);
                }
                iv_make_video_img.parseItemImg(imgPath, true, true);
//                iv_make_video_img.setImageBitmap(FileToolsCammer.getBitmapByImgPath(videoPath,imgPath));
                dishMakeOpration.setVisibility(View.VISIBLE);
            }
            iv_make_video_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnStarListener.OnClickListener(index);
                }
            });
            speechMakeTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    XHClick.mapStat(mAct, UploadDishActivity.STATISTICS_ID, "语音", "直接点击语音按钮");
                    etMakeTitle.requestFocus();
                    UploadDishSpeechTools.createUploadDishSpeechTools().startSpeech(etMakeTitle);
                }
            });
            //裁剪
            videoCut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnCutListener != null)
                        mOnCutListener.OnCut(index);
                }
            });
            if(video_makes_parent.getChildCount() > 0)
                video_makes_parent.removeAllViews();
        }
    }

    private void oprtionTitleHintTv(String makeInfo,String cutTime,TextView tv_title_hint){
        if(TextUtils.isEmpty(cutTime)){
            if(!TextUtils.isEmpty(makeInfo) && makeInfo.length() > MAKE_INFO_MAX_TEXT){
                tv_title_hint.setText("每步最多" + MAKE_INFO_MAX_TEXT + "个字");
                return;
            }
        }else{
            Float vidoTime = Float.parseFloat(cutTime);
            if(vidoTime > MAKE_VIDEO_MAX_S){
                tv_title_hint.setText("视频不能超过" + MAKE_VIDEO_MAX_S + "秒");
                return;
            }
            if(!TextUtils.isEmpty(makeInfo) && makeInfo.length() > MAKE_INFO_MAX_TEXT){
                tv_title_hint.setText("每步最多" + MAKE_INFO_MAX_TEXT + "个字");
                return;
            }
            if(vidoTime > 0 && !TextUtils.isEmpty(makeInfo) && vidoTime * MAKE_VIDEO_S_TEXT < makeInfo.length()){
                tv_title_hint.setText("本步骤视频" + vidoTime + "秒，描述最多" + ((int)(vidoTime * MAKE_VIDEO_S_TEXT)) + "个字");
            }else{
                tv_title_hint.setText("");
            }
        }
    }

    private abstract class MyTextWatcher implements TextWatcher {
        private ViewCache mHolder;
//        private int start = 0,newCount = 0;

        public MyTextWatcher(ViewCache holder) {
            this.mHolder = holder;
        }
        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1,int arg2,int arg3) {
        }
        @Override
        public void onTextChanged(CharSequence arg0, int start, int beforeCount,int newCount) {
//            this.start = start;
//            this.newCount = newCount;
        }
        @Override
        public void afterTextChanged(Editable s) {
//            if(s.length() > 50){
//                Toast.makeText(mAct,"超过最大限制",Toast.LENGTH_SHORT).show();
//                s = s.delete(start,start + newCount);
//            }
            afterTextChanged(s, mHolder);
        }
        public abstract void afterTextChanged(Editable s, ViewCache holder);
    }

    private OnCutListener mOnCutListener;
    public void setOnCutListener(OnCutListener onCutListener){
        mOnCutListener = onCutListener;
    }

    private OnStarListener mOnStarListener;
    public void setOnStarListener(OnStarListener onStarListener){
        mOnStarListener = onStarListener;
    }

    public interface OnCutListener{
        public void OnCut(int index);
    }
    public interface OnStarListener{
        public void OnClickListener(int index);
    }
}
