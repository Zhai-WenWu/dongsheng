package amodule.answer.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import amodule.answer.model.QAMsgModel;

/**
 * Created by sll on 2017/7/31.
 */

public class NumTabController {

    private Context mContext;
    private ViewGroup mParentView;

    private ArrayList<QAMsgModel> mModels;

    public NumTabController(Context mContext, ViewGroup mParentView) {
        this.mContext = mContext;
        this.mParentView = mParentView;
    }

    public void setData(ArrayList<QAMsgModel> models) {
        if (models == null || models.isEmpty() || mParentView == null || mContext == null)
            return;
        mModels = models;
        for (int i = 0; i < mModels.size(); i ++) {
            QAMsgModel model = mModels.get(i);
            String title = model.getmTitle();
            String num = model.getmMsgNum();
            if (!TextUtils.isEmpty(num)) {
                try {
                    int n = Integer.parseInt(num);
                    final NumTabStripView view = new NumTabStripView(mContext);
                    view.setData(n, title);
                    view.setPosition(i);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            view.hideNum();
                            if (mOnItemClickListener != null)
                                mOnItemClickListener.onClick(view);
                        }
                    });
                    mParentView.addView(view);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private View.OnClickListener mOnItemClickListener;
    public void setOnTabClickListener(View.OnClickListener listener) {
        mOnItemClickListener = listener;
    }
}
