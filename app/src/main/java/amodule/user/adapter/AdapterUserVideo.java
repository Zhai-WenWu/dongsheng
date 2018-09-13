package amodule.user.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import acore.widget.TagTextView;
import amodule.user.view.UserHomeItem;
import amodule.user.view.UserHomeTxtItem;
import amodule.user.view.UserHomeVideoItem;
import amodule.user.view.UserHomeViewRow;

import static amodule.dish.db.UploadDishData.UPLOAD_DRAF;
import static amodule.dish.db.UploadDishData.UPLOAD_FAIL;

/**
 * 我的页面：视频
 */
public class AdapterUserVideo extends AdapterSimple {

    private Activity mContext;
    private List<Map<String, String>> mData;

    public AdapterUserVideo(Activity con, View parent, List<Map<String, String>> data, int resource, String[] from, int[] to) {
        super(parent, data, resource, from, to);
        mContext = con;
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.size() / 3 + (mData.size() % 3 > 0 ? 1 : 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null || convertView.getTag() == null) {
            convertView = new UserHomeViewRow(mContext);
            holder = new ViewHolder((UserHomeViewRow) convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        List<Map<String, String>> listData = new ArrayList<>();
        int targetLength = (position + 1) * 3;
        int length = targetLength <= mData.size() ? targetLength : mData.size();
        for (int i = position * 3; i < length; i++) {
            listData.add(mData.get(i));
        }
        holder.setData(listData, position);
        return convertView;
    }

    public class ViewHolder {
        UserHomeViewRow itemView;

        public ViewHolder(UserHomeViewRow view) {
            this.itemView = view;
            if (itemView != null) {
//                itemView.setOnItemClickListener((itemView, dataMap) -> {
//                    if (mOnItemClickListener != null)
//                        mOnItemClickListener.onItemClick(itemView, dataMap);
//                });
//                itemView.setDeleteClickListener(data -> {
//                    if(mOnDeleteClickCallback != null){
//                        mOnDeleteClickCallback.onDeleteClick(data);
//                    }
//                });
            }

        }

        public void setData(final List<Map<String, String>> list, int position) {
            itemView.setCreateViewCallback(new UserHomeViewRow.CreateViewCallback() {
                @Override
                public View createView() {
                    return LayoutInflater.from(mContext).inflate(R.layout.item_personal_video, null);
                }

                @Override
                public void bindData(View view, Map<String, String> data) {
                    ImageView imageVew = view.findViewById(R.id.image);
                    ImageView deleteIcon = view.findViewById(R.id.delete_icon);
                    TagTextView uplaodState = view.findViewById(R.id.upload_state);
                    TagTextView descText = view.findViewById(R.id.desc_text);

                    //处理上传状态
                    String fromLocal = data.get("dataFrom");
                    if ("1".equals(fromLocal)) {
                        String path = data.get("imgPath");
                        if (imageVew != null && path != null) {
                            Glide.with(mContext)
                                    .load(path)
                                    .centerCrop()
                                    .into(imageVew);
                            imageVew.setVisibility(View.VISIBLE);
                        }
                        String uploadType = data.get("uploadType");
                        if (UPLOAD_FAIL.equals(uploadType)) {
                            uplaodState.setText("上传失败");
                            uplaodState.setDrawableL(R.drawable.icon_upload_fail);
                        } else if (UPLOAD_DRAF.equals(uploadType)) {
                            uplaodState.setText("草稿箱");
                            uplaodState.setDrawableL(R.drawable.icon_draf);
                        }
                        uplaodState.setVisibility(View.VISIBLE);
                        deleteIcon.setVisibility(View.VISIBLE);
                        descText.setVisibility(View.GONE);
                    } else {
                        String path = data.get("img");
                        if (imageVew != null && path != null) {
                            Glide.with(mContext)
                                    .load(path)
                                    .centerCrop()
                                    .into(imageVew);
                            imageVew.setVisibility(View.VISIBLE);
                        }
                        //网络数据
                        deleteIcon.setVisibility(View.GONE);
                        String statusInfo = data.get("status");
                        if ("1".equals(statusInfo)) {
                            uplaodState.setText("上传失败");
                            uplaodState.setDrawableL(R.drawable.icon_upload_fail);
                            uplaodState.setVisibility(View.VISIBLE);
                        } else {
                            uplaodState.setVisibility(View.GONE);
                        }
                        boolean isMe = "2".equals(data.get("isMe"));
                        if (uplaodState.getVisibility() == View.GONE) {
                            //正常数据显示
                            if(isMe){
                                descText.setText(data.get("allClick"));//likeNumber	String	0
                            }else {
                                String text = data.get("likeNumber");
                                if(!TextUtils.isEmpty(text) && !"0".equals(text)){
                                    descText.setText(text);
                                }else{
                                    descText.setText("");
                                }
                            }
                            descText.setDrawableL(isMe ? R.drawable.icon_play : R.drawable.icon_likex);
                            descText.setVisibility(View.VISIBLE);
                        }
                    }

                }
            });
            itemView.setData(list);
        }
    }

    private UserHomeItem.OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(UserHomeItem.OnItemClickListener clickListener) {
        this.mOnItemClickListener = clickListener;
    }

    private UserHomeTxtItem.OnDeleteClickCallback mOnDeleteClickCallback;

    public void setOnDeleteClickCallback(UserHomeTxtItem.OnDeleteClickCallback onDeleteClickCallback) {
        mOnDeleteClickCallback = onDeleteClickCallback;
    }
}
