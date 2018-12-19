package amodule.lesson.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.LoginManager;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.widget.DownRefreshList;
import acore.widget.KeyboardDialog;
import amodule.comment.CommentListSave;
import amodule.user.activity.login.LoginByAccout;

public class SecondPagerCommentView extends RelativeLayout {
    private Context mContext;
    private Activity mActivity;
    private List<String> mData;
    private ArrayList<Map<String, String>> mCommentList;
    private final int KEYBOARD_OPTION_COMMENT = 1;
    private final int KEYBOARD_OPTION_REPLAY = 2;
    private String currentUrl;
    private int mKeyboardDialogOptionFrom = KEYBOARD_OPTION_COMMENT;
    private String mReplayText;
    private String mCommentText;
    private DownRefreshList listView;

    public SecondPagerCommentView(Context context) {
        this(context, null);
    }

    public SecondPagerCommentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        setViewGroup();
    }

    public View setViewGroup() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_course_list, this, true);
        listView = view.findViewById(R.id.comment_listview);
        initList(listView);
        TextView writeCommentTv = view.findViewById(R.id.commend_write_tv);

        writeCommentTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentUrl = StringManager.api_addForum;
                mKeyboardDialogOptionFrom = KEYBOARD_OPTION_COMMENT;
                showCommentEdit();
            }
        });
        return this;
    }

    private void initList(DownRefreshList listView) {
        mCommentList = CommentListSave.mList;
        AdapterSimple adapterSimple = new AdapterSimple(listView, mCommentList, R.layout.a_course_comment_item, new String[]{}, new int[]{}) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                CourseCommentItem viewCommentItem = (CourseCommentItem) view.findViewById(R.id.comment_item);

//                    viewCommentItem.setCommentItemListener(getCommentItenListener(viewCommentItem,position));
//                    viewCommentItem.setUserInforListenr(getUserInforListener());
                viewCommentItem.setData(mCommentList.get(position));
                return view;
            }
        };
        listView.setAdapter(adapterSimple);
        adapterSimple.notifyDataSetChanged();
    }

    private void showCommentEdit() {
        KeyboardDialog keyboardDialog = new KeyboardDialog(mActivity);
        keyboardDialog.init(R.layout.course_comment_keyboard_layout);
        keyboardDialog.setTextLength(50);
        if (mKeyboardDialogOptionFrom == KEYBOARD_OPTION_REPLAY) {
            keyboardDialog.setContentStr(mReplayText);
            if (TextUtils.isEmpty(mReplayText)) {
                keyboardDialog.setHintStr("回复:.....");
            }
        } else if (mKeyboardDialogOptionFrom == KEYBOARD_OPTION_COMMENT) {
            keyboardDialog.setContentStr(mCommentText);
        }
        keyboardDialog.setOnSendClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboardDialog.cancel();
                String sendText = keyboardDialog.getText();
                if (LoginManager.isLogin()) {
                    keyboardDialog.setContentStr(null);
                }
                sendData(sendText);
            }
        });
        keyboardDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                String finalStr = keyboardDialog.getText();
                if (mKeyboardDialogOptionFrom == KEYBOARD_OPTION_COMMENT) {
                    mCommentText = finalStr;
                } else if (mKeyboardDialogOptionFrom == KEYBOARD_OPTION_REPLAY) {
                    mReplayText = finalStr;
                }
            }
        });
        keyboardDialog.show();
    }

    private void sendData(String sendText) {
        if (!LoginManager.isLogin()) {
            Intent intent = new Intent(mActivity, LoginByAccout.class);
            mActivity.startActivity(intent);
            return;
        }
    }

    public DownRefreshList getListView() {
        return listView;
    }
}
