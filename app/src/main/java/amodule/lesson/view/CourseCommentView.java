package amodule.lesson.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.load.LoadManager;
import acore.logic.stat.intefaces.OnClickListenerStat;
import acore.override.XHApplication;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.widget.DownRefreshList;
import acore.widget.KeyboardDialog;
import amodule.article.activity.ReportActivity;
import amodule.comment.CommentDialog;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import xh.basic.internet.UtilInternet;

import static xh.basic.tool.UtilString.getListMapByJson;

public class CourseCommentView extends RelativeLayout {
    private Context mContext;
    private List<String> mData;
    private ArrayList<Map<String, String>> listArray = new ArrayList<>();
    private final int KEYBOARD_OPTION_COMMENT = 1;
    private final int KEYBOARD_OPTION_REPLAY = 2;
    private int mKeyboardDialogOptionFrom = KEYBOARD_OPTION_COMMENT;
    private String mReplayText;
    private RelativeLayout mContentView;
    private LoadManager mLoadManager;
    private String type = "7";


    private DownRefreshList downRefreshList;
    private AdapterSimple adapterSimple;
    private String code;
    private int currentPage = 0, everyPage = 0;
    private int dropPage = 1, upDropPage = 1, downDropPage = 1, slide = 1, from = 1, source = 1;

    private TextView commend_write_tv;
    private LinearLayout titleLayout;
    private TextView titleTv;
    private ImageView close_img;

    private View mCommentHintView;

    private String gotoCommentId, gotoReplayId;

    private String mSendText;

    private String currentUrl = StringManager.api_addForum, currentParams, oldCommentId, mCurrentReplayId, mCurrentReplayName;

    private String contentTongjiId, likeTongjiId = "a_like", reportTongjiId = "a_report", deleteTongjiId = "a_delete";
    private String likeTwoLeven, reportTwoLeven, deleteTwoLeven;

    private StringBuffer commentIdStrBuffer;

    private int replayIndex;
    private boolean isSend = false, isAddForm;

    private int mCommentsNum = -1;
    private String mCommentsNumStr;

    private CommentDialog.CommentOptionSuccCallback mCommentOptionSuccCallback;
    private String mChapterCode;


    public CourseCommentView(Context context) {
        this(context, null);
    }

    public CourseCommentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
//        getCommentData(true);
    }

    public void initView() {
        commentIdStrBuffer = new StringBuffer();
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_course_list, this, true);
        downRefreshList = view.findViewById(R.id.comment_listview);
        downRefreshList.setRefreshEnable(false);
        mContentView = view.findViewById(R.id.activityLayout);
        mCommentHintView = view.findViewById(R.id.commend_hind);
        mLoadManager = new LoadManager(mContext, mContentView);
        TextView writeCommentTv = view.findViewById(R.id.commend_write_tv);
        writeCommentTv.setOnClickListener(new OnClickListenerStat() {
            @Override
            public void onClicked(View v) {
                String currentUrl = StringManager.api_addForum;
                mKeyboardDialogOptionFrom = KEYBOARD_OPTION_COMMENT;
                showCommentEdit();
            }
        });

        adapterSimple = new AdapterSimple(downRefreshList, listArray, R.layout.a_course_comment_item, new String[]{}, new int[]{}) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                CourseCommentItem viewCommentItem = (CourseCommentItem) view.findViewById(R.id.comment_item);
                viewCommentItem.setCommentItemListener(getCommentItenListener(viewCommentItem, position));
                viewCommentItem.setUserInforListenr(getUserInforListener());
                viewCommentItem.setData(listArray.get(position));
                return view;
            }
        };
        mLoadManager.showProgressBar();
        mLoadManager.setLoading(downRefreshList, adapterSimple, true, new OnClickListenerStat() {
            @Override
            public void onClicked(View v) {
                getCommentData(false);
            }
        });
    }

    private void showCommentEdit() {
        KeyboardDialog keyboardDialog = new KeyboardDialog(mContext);
        keyboardDialog.init(R.layout.course_comment_keyboard_layout);
        keyboardDialog.setTextLength(50);
        if (mKeyboardDialogOptionFrom == KEYBOARD_OPTION_REPLAY) {
            keyboardDialog.setContentStr(mReplayText);
            if (TextUtils.isEmpty(mReplayText)) {
                keyboardDialog.setHintStr("回复" + mCurrentReplayName);
            }
        } else if (mKeyboardDialogOptionFrom == KEYBOARD_OPTION_COMMENT) {
            keyboardDialog.setContentStr(mSendText);
            if (TextUtils.isEmpty(mSendText)) {
                keyboardDialog.setHintStr("写评论...");
            }
        }
        keyboardDialog.setOnSendClickListener(new OnClickListenerStat() {
            @Override
            public void onClicked(View v) {
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
                    mSendText = finalStr;
                } else if (mKeyboardDialogOptionFrom == KEYBOARD_OPTION_REPLAY) {
                    mReplayText = finalStr;
                }
            }
        });
        keyboardDialog.show();
    }

    private synchronized void sendData(String sendText) {
        if (isSend || TextUtils.isEmpty(sendText)) return;
        if (!LoginManager.isLogin()) {
            Intent intent = new Intent(mContext, LoginByAccout.class);
            mContext.startActivity(intent);
            return;
        }
        isSend = true;

        String newParams;
        isAddForm = false;
        if (StringManager.api_addForum.equals(currentUrl)) {
            isAddForm = true;
            JSONArray jsonArray = new JSONArray();
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("text", sendText);
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            newParams = "type=" + type + "&code=" + code + "&content=" + jsonArray.toString();
        } else {
            newParams = "type=" + type + "&code=" + code + currentParams + "&content=" + sendText;
        }
        newParams += "&commentIds=" + commentIdStrBuffer + "&firstCode=" + mChapterCode;
        Log.i("commentReplay", "sendData() newParams:" + newParams);
        ReqEncyptInternet.in().doEncypt(currentUrl, newParams, new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                Log.i("commentReplay", "sendData() flag:" + flag + "   o:" + o);
                if (flag >= ReqInternet.REQ_OK_STRING) {
                    if (isAddForm) {
                        ArrayList<Map<String, String>> arrayList = getListMapByJson(o);
                        Log.i("commentReplay", "sendData() arrayList:" + arrayList.size());
                        if (arrayList.size() > 0) {
                            Map<String, String> map = arrayList.get(0);
                            if (commentIdStrBuffer.length() != 0) commentIdStrBuffer.append(",");
                            commentIdStrBuffer.append(map.get("comment_id"));
                            listArray.add(0, arrayList.get(0));
                            if (listArray.size() == 1)
                                changeDataChange();
                        }
                        adapterSimple.notifyDataSetChanged();
                        downRefreshList.setSelection(0);
                    } else {
                        Log.i("commentReplay", "sendData() replayIndex:" + replayIndex);
                        Map<String, String> map = listArray.get(replayIndex);
                        String replay = map.get("replay");
                        Log.i("commentReplay", "sendData() replay:" + replay);
                        if (!TextUtils.isEmpty(replay)) {
                            JSONArray jsonArray = new JSONArray();
                            JSONObject jsonObject;
                            Map<String, String> replayMap;
                            ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(replay);
                            ArrayList<Map<String, String>> newList = StringManager.getListMapByJson(o);
                            arrayList.addAll(newList);
                            try {
                                for (int i = 0; i < arrayList.size(); i++) {
                                    jsonObject = new JSONObject();
                                    replayMap = arrayList.get(i);
                                    for (String key : replayMap.keySet()) {
                                        jsonObject.put(key, replayMap.get(key));
                                    }
                                    jsonArray.put(jsonObject);
                                }
                            } catch (JSONException exception) {
                                exception.printStackTrace();
                            }
                            map.put("replay", jsonArray.toString());
                            Log.i("commentReplay", "sendData() jsonArray:" + jsonArray.toString());
                            adapterSimple.notifyDataSetChanged();
                        }
                    }

                    try {
                        mCommentsNum = Integer.parseInt(mCommentsNumStr);
                    } catch (Exception e) {
                        mCommentsNum = -1;
                    }
                    if (mCommentsNum != -1) {
                        mCommentsNum++;
                        mCommentsNumStr = String.valueOf(mCommentsNum);
                    }
                    if (mCommentOptionSuccCallback != null)
                        mCommentOptionSuccCallback.onSendSucc();
                } else {
                    Tools.showToast(XHApplication.in(), String.valueOf(o));
                }
                isSend = false;
            }
        });
    }

    private void getCommentData(final boolean isForward) {
        if (isForward) {
            currentPage = 1;
            slide = 2;
            if (listArray.size() > 0) {
                gotoCommentId = listArray.get(0).get("comment_id");
            }
            dropPage = upDropPage;
        } else {
            currentPage++;
            slide = 1;
            if (listArray.size() > 0) {
                gotoCommentId = listArray.get(listArray.size() - 1).get("comment_id");
            }
            dropPage = downDropPage;
        }

        mLoadManager.loading(downRefreshList, listArray.size() == 0);
        String params = "type=" + type + "&code=" + code + "&from=" + "1" + "&slide=" + slide + "&dropPage=" + dropPage;
        Log.i("commentReplay", "getCommentData() params:" + params);
        ReqEncyptInternet.in().doEncypt(StringManager.api_forumList, params, new InternetCallback() {
            @Override
            public void loaded(int flag, String s, Object o) {
                int loadCount = 0;
                if (flag >= UtilInternet.REQ_OK_STRING) {
                    from = 1;
                    source = 1;
                    ArrayList<Map<String, String>> arrayList = getListMapByJson(o);
                    if (arrayList.size() > 0) {
                        Map<String, String> dataMap = arrayList.get(0);
                        String status = dataMap.get("status");
                        // TODO: 2018/12/19
                        if (!"2".equals(status)) {
//                            dismiss();
                            return;
                        }
                        String dataList = dataMap.get("list");
                        String dataPage = dataMap.get("page");
                        String titleInfo = dataMap.get("info");
                        arrayList = getListMapByJson(dataList);
                        if (isForward) {
                            if (dropPage == 1) {
                                listArray.clear();
                                Log.i("commentReplay", "清除数据，重新加载");
                            }
                            listArray.addAll(0, arrayList);
                        } else {
                            listArray.addAll(arrayList);
                        }
                        if (!TextUtils.isEmpty(dataPage) && !"null".equals(dataPage)) {
                            if (isForward)
                                upDropPage = Integer.parseInt(dataPage);
                            else
                                downDropPage = Integer.parseInt(dataPage);
                        }
                        if (!TextUtils.isEmpty(titleInfo)) {
                            ArrayList<Map<String, String>> titleArray = StringManager.getListMapByJson(titleInfo);
                            if (titleArray.size() > 0) {
                                Map<String, String> map = titleArray.get(0);
                                String title = map.get("title");
                                final String clickUrl = map.get("url");
                                if (!TextUtils.isEmpty(title)) {
                                    titleLayout.setVisibility(View.VISIBLE);
                                    titleTv.setText(title);
                                    if (!TextUtils.isEmpty(clickUrl)) {
                                        titleLayout.setOnClickListener(new OnClickListenerStat() {
                                            @Override
                                            public void onClicked(View v) {
                                                AppCommon.openUrl((Activity) mContext, clickUrl, true);
                                            }
                                        });
                                    }
                                }
                            }
                        }

                        adapterSimple.notifyDataSetChanged();
                        loadCount = arrayList.size();
                        if (everyPage == 0)
                            everyPage = loadCount;
                    }
                    changeDataChange();
                }
                mLoadManager.loadOver(flag, downRefreshList, loadCount, "没有更多评论啦");
            }
        });
    }

    private void changeDataChange() {
        Log.i("commentActivity", "changeDataChange() size:" + listArray.size());
        if (listArray.size() == 0) {
            downRefreshList.setVisibility(View.GONE);
            mCommentHintView.setVisibility(View.VISIBLE);
        } else {
            downRefreshList.setVisibility(View.VISIBLE);
            downRefreshList.onRefreshComplete();
            mCommentHintView.setVisibility(View.GONE);
        }
    }

    private CourseCommentItem.OnCommentItenListener getCommentItenListener(final CourseCommentItem viewCommentItem, final int position) {
        return new CourseCommentItem.OnCommentItenListener() {
            @Override
            public void onShowAllReplayClick(String comment_id) {
                ReqEncyptInternet.in().doEncypt(StringManager.api_replayList, "type=" + type + "&code=" + code + "&commentId=" + comment_id, new InternetCallback() {
                    @Override
                    public void loaded(int flag, String s, Object o) {
                        if (flag >= ReqInternet.REQ_OK_STRING) {
                            viewCommentItem.addReplayView(o.toString(), true);
                        }
                    }
                });
            }

            @Override
            public void onReportCommentClick(String comment_id, String comment_user_code, String comment_user_name, String reportContent, String reportType) {
                if (!LoginManager.isLogin()) {
                    mContext.startActivity(new Intent(mContext, LoginByAccout.class));
                    return;
                }
                if (LoginManager.isLogin()
                        && !TextUtils.isEmpty(LoginManager.userInfo.get("code"))
                        && !TextUtils.isEmpty(comment_user_code)
                        && !comment_user_code.equals(LoginManager.userInfo.get("code"))) {
//                    XHClick.mapStat(mContext, reportTongjiId, reportTwoLeven, reportType);
                    Intent intent = new Intent(mContext, ReportActivity.class);
                    intent.putExtra("type", type);
                    intent.putExtra("code", code);
                    intent.putExtra("commentId", comment_id);
                    intent.putExtra("userCode", comment_user_code);
                    intent.putExtra("reportName", comment_user_name);
                    intent.putExtra("reportContent", reportContent);
                    mContext.startActivity(intent);
                }
            }

            @Override
            public void onReportReplayClick(String comment_id, String replay_id, String replay_user_code, String replay_user_name, String reportContent) {
                Log.i("commentReplay", "onReportReplayClick()");
                if (!LoginManager.isLogin()) {
                    mContext.startActivity(new Intent(mContext, LoginByAccout.class));
                    return;
                }
                if (!TextUtils.isEmpty(LoginManager.userInfo.get("code"))
                        && !TextUtils.isEmpty(replay_user_code)
                        && !replay_user_code.equals(LoginManager.userInfo.get("code"))) {
//                    XHClick.mapStat(mContext, reportTongjiId, reportTwoLeven, "点击楼中楼的举报");
                    Intent intent = new Intent(mContext, ReportActivity.class);
                    intent.putExtra("type", type);
                    intent.putExtra("code", code);
                    intent.putExtra("commentId", comment_id);
                    intent.putExtra("replayId", replay_id);
                    intent.putExtra("userCode", replay_user_code);
                    intent.putExtra("reportName", replay_user_name);
                    intent.putExtra("reportContent", reportContent);
                    mContext.startActivity(intent);
                }
            }

            @Override
            public void onDeleteReplayClick(final int replayIndex, String comment_id, String replay_id) {
//                XHClick.mapStat(mContext, deleteTongjiId, deleteTwoLeven, "点击楼中楼的删除按钮");
                ReqEncyptInternet.in().doEncypt(StringManager.api_delReplay, "type=" + type + "&code=" + code + "&commentId=" + comment_id + "&replayId=" + replay_id,
                        new InternetCallback() {
                            @Override
                            public void loaded(int flag, String s, Object o) {
                                Log.i("commentReplay", "deleteReplay() flag:" + flag + "   obj:" + o);
//                        Log.i("commentReplay","deleteReplay() position:" + position + "  listArray.size():" + listArray.size() + "   replayIndex:" + replayIndex);
                                if (flag >= ReqInternet.REQ_OK_STRING) {
                                    if (position < listArray.size()) {
                                        Map<String, String> map = listArray.get(position);
                                        String replay = map.get("replay");
                                        ArrayList<Map<String, String>> arrayList = StringManager.getListMapByJson(replay);
//                                Log.i("commentReplay","deleteReplay() arrayList.size():" + arrayList.size());
                                        if (replayIndex < arrayList.size()) {
                                            arrayList.remove(replayIndex);
                                            map.put("replay", StringManager.getJsonByArrayList(arrayList).toString());
                                            viewCommentItem.addReplayView(arrayList, true);
                                        }
                                    }
                                }
                            }
                        });
            }

            @Override
            public void onDeleteCommentClick(final String comment_id, String deleteType) {
//                XHClick.mapStat(mContext, deleteTongjiId, deleteTwoLeven, deleteType);
                final DialogManager dialogManager = new DialogManager(mContext);
                dialogManager.createDialog(new ViewManager(dialogManager)
                        .setView(new TitleMessageView(mContext).setText("确认删除我的评论？"))
                        .setView(new HButtonView(mContext)
                                .setNegativeText("取消", new OnClickListenerStat() {
                                    @Override
                                    public void onClicked(View v) {
                                        dialogManager.cancel();
                                    }
                                })
                                .setPositiveText("确认", new OnClickListenerStat() {
                                    @Override
                                    public void onClicked(View v) {
                                        String params = "type=" + type + "&code=" + code + "&commentId=" + comment_id;
                                        ReqEncyptInternet.in().doEncypt(StringManager.api_delForum, params, new InternetCallback() {
                                            @Override
                                            public void loaded(int flag, String s, Object o) {
                                                if (flag >= ReqInternet.REQ_OK_STRING) {
                                                    listArray.remove(position);
                                                    adapterSimple.notifyDataSetChanged();
                                                    try {
                                                        mCommentsNum = Integer.parseInt(mCommentsNumStr);
                                                    } catch (Exception e) {
                                                        mCommentsNum = -1;
                                                    }
                                                    if (mCommentsNum != -1) {
                                                        mCommentsNum--;
                                                        mCommentsNum = Math.max(mCommentsNum, 0);
                                                        mCommentsNumStr = String.valueOf(mCommentsNum);
                                                    }
                                                    if (listArray.size() == 0) {
                                                        upDropPage = 1;
                                                        gotoCommentId = null;
                                                        gotoReplayId = null;
                                                        getCommentData(true);
                                                    } else {
                                                        changeDataChange();
                                                    }
                                                    if (mCommentOptionSuccCallback != null)
                                                        mCommentOptionSuccCallback.onDelSucc();
                                                }
                                            }
                                        });
                                        dialogManager.cancel();
                                    }
                                }))).show();
            }

            @Override
            public void onPraiseClick(String comment_id) {
                if (!LoginManager.isLogin()) {
                    Intent intent = new Intent(mContext, LoginByAccout.class);
                    mContext.startActivity(intent);
                    return;
                }
//                XHClick.mapStat(mContext, contentTongjiId, "点赞", "");
//                XHClick.mapStat(mContext, likeTongjiId, likeTwoLeven, "");
                String params = "type=" + type + "&code=" + code + "&commentId=" + comment_id + "&firstCode=" + mChapterCode;
                ReqEncyptInternet.in().doEncypt(StringManager.api_likeForum, params, new InternetCallback() {
                    @Override
                    public void loaded(int i, String s, Object o) {
                        if (i >= ReqInternet.REQ_OK_STRING) {
                            ArrayList<Map<String, String>> arrayList = getListMapByJson(o);
                            if (arrayList.size() > 0) {
                                Map<String, String> map = listArray.get(position);
                                map.put("is_fabulous", "2");
                                map.put("fabulous_num", arrayList.get(0).get("num"));
                                adapterSimple.notifyDataSetChanged();
                            }
                        }
                    }
                });
            }

            @Override
            public void onContentReplayClick(String comment_id, String replay_id, String replay_code, String replay_name, String type, boolean isShowKeyBoard, boolean isMyselft) {
                if (isMyselft) return;
                if (!TextUtils.equals(mCurrentReplayId, replay_code)) {
                    mReplayText = null;
                }
                currentParams = "&commentId=" + comment_id + "&replayUcode=" + replay_code;
                replayIndex = position;
                currentUrl = StringManager.api_addReplay;
                oldCommentId = comment_id;
                mCurrentReplayId = replay_code;
                mCurrentReplayName = replay_name;
                mKeyboardDialogOptionFrom = KEYBOARD_OPTION_REPLAY;
                showCommentEdit();
//                XHClick.mapStat(mContext, contentTongjiId, "回复", type);
            }
        };
    }

    private CourseCommentItem.OnUserInforListener getUserInforListener() {
        return new CourseCommentItem.OnUserInforListener() {
            @Override
            public void onReplayUserNameClick(boolean isAuther, String userName) {
//                XHClick.mapStat(mContext, contentTongjiId, "用户信息", isAuther ? "点击楼层作者用户名" : "点击楼层其他用户名");
            }

            @Override
            public void onCommentUserNameClick(String userName) {
//                XHClick.mapStat(mContext, contentTongjiId, "用户信息", "点击评论用户名");
            }

            @Override
            public void onCommentUserIconClick() {
//                XHClick.mapStat(mContext, contentTongjiId, "用户信息", "点击用户头像");
            }

            @Override
            public void onCommentUserVipClick() {
//                XHClick.mapStat(mContext, contentTongjiId, "用户信息", "点击会员icon");
            }
        };
    }


    public DownRefreshList getListView() {
        return downRefreshList;
    }

    public void setCode(String code, String chapterCode) {
        this.code = code;
        this.mChapterCode = chapterCode;
        initView();
    }

}
