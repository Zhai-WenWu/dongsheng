package amodule.answer.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.tools.Tools;
import amodule.upload.bean.UploadItemData;

/**
 * Created by sll on 2017/7/20.
 */

public class AskAnswerUploadAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Map<String, String>> mDatas;

    public AskAnswerUploadAdapter(Context context) {
        mContext = context;
    }

    public void setData (ArrayList<Map<String, String>> datas) {
        mDatas = datas;
        if (mDatas == null || mDatas.isEmpty())
            return;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public Map<String, String> getItem(int position) {
        return (mDatas == null || mDatas.isEmpty()) ? null : mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.c_upload_dishvideo_item, null, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.setData(position, getItem(position));
        return viewHolder.mItemView;
    }

    private class ViewHolder {
        private int mPosition;
        private View mItemView;
        private RelativeLayout mUploadInfo;
        private TextView mUploadState;
        private TextView mUploadSize;
        private TextView mUploadTitle;
        private ProgressBar mUploadProgress;
        private ProgressBar mUploadProgressPause;
        private LinearLayout mUploadSuccess;
        private LinearLayout mUploadFail;
        private ImageView mUploadCover;
        private ImageView mUploadCoverLast;

        public ViewHolder(View itemView) {
            if (itemView == null)
                return;
            this.mItemView = itemView;
            mUploadInfo = (RelativeLayout) itemView.findViewById(R.id.rl_upload_info);
            mUploadTitle = (TextView) itemView.findViewById(R.id.tv_tilte);
            mUploadSize = (TextView) itemView.findViewById(R.id.tv_sizs);
            mUploadState = (TextView) itemView.findViewById(R.id.tv_upload_state);
            mUploadProgress = (ProgressBar) itemView.findViewById(R.id.pb_progress);
            mUploadProgressPause = (ProgressBar) itemView.findViewById(R.id.pb_progress_pause);
            mUploadSuccess = (LinearLayout) itemView.findViewById(R.id.ll_upload_success_item);
            mUploadFail = (LinearLayout) itemView.findViewById(R.id.ll_upload_fail_item);
            mUploadCover = (ImageView) itemView.findViewById(R.id.iv_cover_dish);
            mUploadCoverLast = (ImageView) itemView.findViewById(R.id.iv_cover_dish_last);
        }

        public void setData(int position, Map<String, String> data) {
            if (data == null)
                return;
            this.mPosition = position;
            String title = data.get("makeStep");
            if (!TextUtils.isEmpty(title))
                mUploadTitle.setText(title);
            String stateInfo = data.get("stateInfo");
            if (!TextUtils.isEmpty(stateInfo))
                mUploadState.setText(stateInfo);
            String totalLength = data.get("totleLength");
            if (totalLength != null)
                mUploadSize.setText(totalLength);
            switch (Integer.valueOf(data.get("state"))) {
                case UploadItemData.STATE_RUNNING:
                    mUploadInfo.setVisibility(View.VISIBLE);
                    mUploadState.setVisibility(View.VISIBLE);
                    mUploadSize.setVisibility(View.VISIBLE);
                    mUploadProgress.setVisibility(View.VISIBLE);
                    mUploadProgressPause.setVisibility(View.GONE);
                    mUploadSuccess.setVisibility(View.GONE);
                    mUploadFail.setVisibility(View.GONE);
                    break;
                case UploadItemData.STATE_FAILD:
                    mUploadInfo.setVisibility(View.GONE);
                    mUploadSize.setVisibility(View.VISIBLE);
                    mUploadSuccess.setVisibility(View.GONE);
                    mUploadFail.setVisibility(View.VISIBLE);
                    break;
                case UploadItemData.STATE_SUCCESS:
                    mUploadInfo.setVisibility(View.GONE);
                    mUploadSize.setVisibility(View.GONE);
                    mUploadSuccess.setVisibility(View.VISIBLE);
                    mUploadFail.setVisibility(View.GONE);
                    break;
                case UploadItemData.STATE_PAUSE:
                    mUploadInfo.setVisibility(View.VISIBLE);
                    mUploadSize.setVisibility(View.VISIBLE);
                    mUploadProgress.setVisibility(View.GONE);
                    mUploadProgressPause.setVisibility(View.VISIBLE);
                    mUploadSuccess.setVisibility(View.GONE);
                    mUploadFail.setVisibility(View.GONE);
                    break;
                case UploadItemData.STATE_WAITING:
                    mUploadInfo.setVisibility(View.VISIBLE);
                    mUploadSize.setVisibility(View.VISIBLE);
                    mUploadProgress.setVisibility(View.GONE);
                    mUploadProgressPause.setVisibility(View.VISIBLE);
                    mUploadSuccess.setVisibility(View.GONE);
                    mUploadFail.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
            if (position == mDatas.size() - 1) {
                mUploadCover.setVisibility(View.GONE);
                mUploadCoverLast.setVisibility(View.VISIBLE);
            }else{
                String imgPath = data.get("path");
                if(String.valueOf(UploadItemData.TYPE_VIDEO).equals(data.get("type")))
                    imgPath = data.get("videoImage");
                mUploadCover.setVisibility(View.VISIBLE);
                mUploadCoverLast.setVisibility(View.GONE);
                Glide.with(mContext).load(imgPath)
                        .override(Tools.getDimen(mContext, R.dimen.dp_123)
                                ,Tools.getDimen(mContext, R.dimen.dp_69))
                        .into(mUploadCover);
            }
            mUploadProgress.setProgress(Integer.parseInt(data.get("progress")));
            mUploadProgressPause.setProgress(Integer.parseInt(data.get("progress")));
        }
    }

}
