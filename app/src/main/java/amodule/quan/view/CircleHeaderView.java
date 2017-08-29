package amodule.quan.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.xianghatest.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.AutoScrollADView;
import amodule.main.view.circle.CircleHeaderAllQuan;
import amodule.main.view.circle.CircleHeaderRecUser;
import amodule.quan.db.SubjectData;
import amodule.quan.view.CircleHeaderNoDataView.interfaceNoDataView;
import amodule.quan.view.CircleHeaderTopFakeView.interfaceTopFakeView;
import amodule.quan.view.CircleHeaderTopNomalView.interfaceTopNomalView;
import aplug.basic.LoadImage;

/**
 * 头部控件
 *
 * @author Administrator
 */
@SuppressLint("InflateParams")
public class CircleHeaderView extends LinearLayout {

    private Activity context;
    private ItemCallback delOnClick;
    public final int TAG_ID = R.string.tag;
    private static final int TOP_RECOMMED_USER = 2;//推荐用户
    private static final int ALL_QUAN_TAG = 4;//所有圈子
    private static final int STICKY_NEW_TAG = 6;//新置顶
    private static final int TOP_TAG = 8;//顶部类型的tag
    private static final int TOPCOMMON_TAG = 10;//顶部标准类型的tag
    private static final int MIDDLE_TAG = 12;//中间类型的tag
    private static final int BOTTOM_TAG = 14;//底部类型的tag
    private static final int FAKE_TAG = 16;//假界面类型tag
    private static final int NODATA_TAG = 18;//无数据类型显示tag
    private String stiaticID = "";

    public CircleHeaderView(Activity context) {
        super(context);
        this.setOrientation(VERTICAL);
        this.context = context;
    }

    public void setStiaticID(String stiaticID){
        this.stiaticID = stiaticID;
    }

    // *******************************************总逻辑控制start*************************************************
    private LinearLayout createViewLayout(int tag) {
        ArrayList<Integer> indexs = new ArrayList<Integer>();
        HashMap<String, View> maps = new HashMap<String, View>();
        int index = 0;
        int count = this.getChildCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                View view = this.getChildAt(i);
                int viewTag = Integer.parseInt(String.valueOf(view.getTag()));
                if (tag == viewTag)
                    return (LinearLayout) view;
            }
            //不存在当前view，并当前headers有view显示
            for (int i = 0; i < count; i++) {
                View view = this.getChildAt(i);
                int viewTag = Integer.parseInt(String.valueOf(view.getTag()));
                maps.put(String.valueOf(view.getTag()), view);
                indexs.add(viewTag);
            }
            //对view进行处理
            indexs.add(tag);
            Collections.sort(indexs);
            int size = indexs.size();
            for (int i = 0; i < size; i++) {
                if (tag == indexs.get(i))
                    index = i;
            }
            LinearLayout view = createLayout(tag);
            if (index == size - 1) {
                this.addView(view);
            } else {
                this.addView(view, index);
            }
            return view;
        } else {
            LinearLayout view = createLayout(tag);
            this.addView(view);
            return view;
        }

    }

    private LinearLayout createLayout(int tag) {
        LinearLayout Layout = new LinearLayout(context);
        LayoutParams lp = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        Layout.setOrientation(LinearLayout.VERTICAL);
        Layout.setLayoutParams(lp);
        Layout.setVisibility(View.GONE);
        Layout.setTag(tag);
        return Layout;
    }
    // *******************************************总逻辑控制end*************************************************

    // *******************************************推荐userstart*************************************************
    public void initRecUser(final ArrayList<Map<String, String>> jsonList) {
        if (jsonList != null) {
            LinearLayout layout = createViewLayout(TOP_RECOMMED_USER);
            layout.removeAllViews();
            CircleHeaderRecUser recUser = new CircleHeaderRecUser(getContext());
            recUser.setStiaticID(stiaticID);
            recUser.setOnItemClickListener(new CircleHeaderRecUser.OnItemClickListener() {
                @Override
                public void onClick(View view, int position) {
                    XHClick.mapStat(context,stiaticID,"顶部推荐关注","用户");
                    AppCommon.openUrl(context, jsonList.get(position).get("url"), true);
                }
            });
            recUser.setData(jsonList);
            layout.setVisibility(VISIBLE);
            layout.addView(recUser);
        }
    }

    // *******************************************推荐user end*************************************************

    // *******************************************所有圈子start*************************************************

    public void initAllQuan(ArrayList<Map<String, String>> jsonList) {
        if (jsonList != null) {
            LinearLayout layout = createViewLayout(ALL_QUAN_TAG);
            final int childCount = layout.getChildCount();
            if (childCount == 0) {
                createTopAd(jsonList);
            } else if (childCount == 1) {
                View view = layout.getChildAt(0);
                if (view instanceof CircleHeaderAllQuan) {
                    ((CircleHeaderAllQuan) view).setCircleData(jsonList);
                } else {
                    layout.removeAllViews();
                    createTopAd(jsonList);
                }
            } else {
                layout.removeAllViews();
                createTopAd(jsonList);
            }
        }
    }

    private void createTopAd(ArrayList<Map<String, String>> jsonList) {
        LinearLayout layout = createViewLayout(ALL_QUAN_TAG);
        CircleHeaderAllQuan allQuan = new CircleHeaderAllQuan(getContext());
        allQuan.setStiaticID(stiaticID);
        allQuan.setCircleData(jsonList);
        allQuan.setmOnItemClickCallback(new CircleHeaderAllQuan.OnItemClickCallback() {
            @Override
            public void onClick(View v, Map<String, String> map) {
                XHClick.mapStat(context,stiaticID,"圈子",map.get("name"));
                AppCommon.openUrl(context, map.get("url"), true);
            }
        });
        layout.setVisibility(View.VISIBLE);
        layout.addView(allQuan);
    }

    // *******************************************所有圈子end*************************************************

    // *******************************************新公告置顶start*************************************************


    public void initNewSticky(List<Map<String, String>> jsonList) {
        if (jsonList != null && jsonList.size() != 0) {
            LinearLayout layout = createViewLayout(STICKY_NEW_TAG);
            layout.removeAllViews();
            createnewSticky(jsonList);
        }
    }

    private void createnewSticky(List<Map<String, String>> jsonList) {
        LinearLayout layout = createViewLayout(STICKY_NEW_TAG);
        float height = Tools.getDimen(getContext(), R.dimen.dp_56);
        AutoScrollADView.ADViewAdapter adapter = new AutoScrollADView.ADViewAdapter(jsonList);
        adapter.setOnItemClickListener(new AutoScrollADView.ADViewAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, Map<String, String> data) {
                XHClick.mapStat(context,stiaticID,"公告","");
                AppCommon.openUrl(context,data.get("url"),true);
            }
        });
        final AutoScrollADView autoScrollADView = new AutoScrollADView(getContext());
        autoScrollADView.setStiaticID(stiaticID);
        autoScrollADView.setmAdverHeight(height);
        autoScrollADView.setAdapter(adapter);
        layout.setVisibility(VISIBLE);
        layout.addView(autoScrollADView, ViewGroup.LayoutParams.MATCH_PARENT, (int) height);
        View lineView = new View(getContext());
        lineView.setBackgroundColor(Color.parseColor(getResources().getString(R.color.common_bg)));
        layout.addView(lineView, ViewGroup.LayoutParams.MATCH_PARENT, Tools.getDimen(getContext(), R.dimen.dp_1));
        autoScrollADView.start();
    }

    // *******************************************新公告置顶end*************************************************

    // *******************************************公告置顶start*************************************************

    /**
     * 初始化中间
     */
    public void initMiddleView(ArrayList<Map<String, String>> jsonList) {
        if (jsonList != null) {
            LinearLayout view = createViewLayout(MIDDLE_TAG);
            if (view.getChildCount() > 0) {
                changeMiddleData(jsonList);
            } else
                for (int i = 0; i < jsonList.size(); i++) {
                    Map<String, String> map = jsonList.get(i);
                    createMiddleView(map);
                }
        }
    }

    /**
     * 改变中间数据
     *
     * @param changeList
     */
    private void changeMiddleData(ArrayList<Map<String, String>> changeList) {
        ArrayList<View> child_del = new ArrayList<View>();// 要被删除的view的index;
        ArrayList<Map<String, String>> like_list = new ArrayList<Map<String, String>>();// 要添加数据
        LinearLayout layout = createViewLayout(MIDDLE_TAG);
        for (int i = 0; i < layout.getChildCount(); i++) {
            String code = (String) layout.getChildAt(i).getTag();
            child_del.add(layout.getChildAt(i));
            for (int j = 0; j < changeList.size(); j++) {
                if (code.equals(changeList.get(j).get("code"))) {// code
                    // 相同为变化view数据
                    View view = layout.getChildAt(i);
                    if (view instanceof CircleHeaderTopAndNoticeView) {
                        ((CircleHeaderTopAndNoticeView) view).changeMiddleView(changeList.get(j), view);
                    }
                    child_del.remove(child_del.size() - 1);
                    break;
                }
            }
        }
        // 获取要添加数据
        for (int i = 0; i < changeList.size(); i++) {
            like_list.add(changeList.get(i));
            for (int j = 0; j < layout.getChildCount(); j++) {
                String code = (String) layout.getChildAt(j).getTag();
                if (code.equals(changeList.get(i).get("code"))) {
                    like_list.remove(like_list.size() - 1);
                    break;
                }
            }
        }

        deleteLayoutView(layout, child_del);
        for (int i = 0; i < like_list.size(); i++) {
            createMiddleView(like_list.get(i));
        }
    }

    /**
     * 创建中间新的View
     */
    private void createMiddleView(final Map<String, String> map) {
        CircleHeaderTopAndNoticeView circleHeaderTopAndNoticeView = new CircleHeaderTopAndNoticeView(context, map);
        LinearLayout layout = createViewLayout(MIDDLE_TAG);
        layout.addView(circleHeaderTopAndNoticeView);
        layout.setVisibility(View.VISIBLE);
    }
    // *******************************************公告置顶end*************************************************

    // *******************************************通用头start*************************************************

    /**
     * 设置top其他数据
     *
     * @param content        内容
     * @param backgroupColor 背景颜色
     * @param textColor      文字颜色
     * @return
     */
    public View initTopView(String content, String backgroupColor, String textColor) {
        return initTopView(content, backgroupColor, textColor, null, 0, false, false);
    }

    /**
     * 设置top其他数据
     *
     * @param content        内容
     * @param backgroupColor 背景
     * @param textColor      文字颜色
     * @param isOne          是否保证一个view
     * @return
     */
    public View initTopView(String content, String backgroupColor, String textColor, boolean isOne) {
        return initTopView(content, backgroupColor, textColor, null, 0, false, isOne);
    }

    /**
     * 设置top其他数据
     *
     * @param content        内容
     * @param backgroupColor 背景颜色
     * @param textColor      文字颜色
     * @param ItemOnClick    点击回调
     * @return
     */
    public View initTopView(String content, String backgroupColor, String textColor, final ItemCallback ItemOnClick) {
        return initTopView(content, backgroupColor, textColor, ItemOnClick, 0, false, false);
    }

    /**
     * 设置top其他数据
     *
     * @param content        内容
     * @param backgroupColor 背景颜色
     * @param textColor      文字颜色
     * @param ItemOnClick    点击回调
     * @param drawable       图片id
     * @return
     */
    public View initTopView(String content, String backgroupColor, String textColor, final ItemCallback ItemOnClick, int drawable) {
        return initTopView(content, backgroupColor, textColor, ItemOnClick, drawable, false, false);
    }

    /**
     * 设置top其他数据
     *
     * @param content        内容
     * @param backgroupColor 背景颜色
     * @param textColor      文字颜色
     * @param ItemOnClick    点击回调
     * @param isShowDelete   是否显示删除
     * @return
     */
    public View initTopView(String content, String backgroupColor, String textColor, final ItemCallback ItemOnClick, boolean isShowDelete) {
        return initTopView(content, backgroupColor, textColor, ItemOnClick, 0, isShowDelete, false);
    }

    /**
     * 设置其他数据
     *
     * @param content        内容
     * @param backgroupColor 背景颜色
     * @param textColor      内容文字意思
     * @param ItemOnClick    点击回调
     * @param drawable       中间显示图片
     * @param isShowDelete   是否显示删除控件
     */
    public View initTopView(String content, String backgroupColor, String textColor, final ItemCallback ItemOnClick, int drawable, boolean isShowDelete, boolean isOne) {

        final LinearLayout layout = createViewLayout(TOPCOMMON_TAG);
        if (isOne && layout.getChildCount() > 0 && layout.getVisibility() == View.VISIBLE) {
            View view = layout.getChildAt(0);
            ((TextView) view.findViewById(R.id.tv_content)).setText(content);
            return view;
        }
        CircleHeaderTopNomalView mCircleHeaderTopNomalView = new CircleHeaderTopNomalView(context);
        mCircleHeaderTopNomalView.initView(content, backgroupColor, textColor, drawable, isShowDelete);
        mCircleHeaderTopNomalView.setInterface(new interfaceTopNomalView() {
            @Override
            public void setClickDel(View view) {
                layout.removeView(view);
            }

            @Override
            public void setItemClick() {
                if (ItemOnClick != null)
                    ItemOnClick.onClick("");
            }
        });
        layout.addView(mCircleHeaderTopNomalView);
        layout.setVisibility(View.VISIBLE);
        return mCircleHeaderTopNomalView;
    }
    // *******************************************通用头ends*************************************************

    /**
     * 设置底部活动位置
     */
    public void initBottomView(Map<String, String> map, final ItemCallback itemCallback) {
        LinearLayout layout = createViewLayout(BOTTOM_TAG);
        if (layout.findViewWithTag(map.get("url")) == null) {
            int width = ToolsDevice.getWindowPx(context).widthPixels - Tools.getDimen(context, R.dimen.dp_20);// 20=10*2
            int height = width * 160 / 600;

            ImageView img = new ImageView(context);
            LayoutParams lp_gg = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, height);
            img.setLayoutParams(lp_gg);
            String imgUrl = map.get("img");
            if (imgUrl.matches("[\\d]+")) {
                img.setImageResource(Integer.parseInt(imgUrl));
            } else {
                // 加载图片
                setViewImage(img, img, imgUrl);
            }
            // 点击查看详情;
            img.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemCallback.onClick("");
                }
            });
            img.setTag(TAG_ID, map.get("url"));// 添加标示.
            layout.addView(img);
        }
        layout.setVisibility(View.VISIBLE);
    }

    /**
     * 加载图片
     *
     * @param view
     * @param iv
     * @param imgUrl
     */
    private void setViewImage(View view, final ImageView iv, String imgUrl) {
        iv.setBackgroundResource(R.drawable.i_nopic);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(context).load(imgUrl).build();
        if (bitmapRequest != null)
            bitmapRequest.into(iv);
    }

    // *******************************************假界面start*************************************************

    /**
     * 创建假界面
     *
     * @param uploadList  上传，成功的假界面
     * @param FailureList 失败的假界面
     */
    public void initFakeContentView(ArrayList<SubjectData> uploadList, ArrayList<SubjectData> FailureList) {
        FakeUploadContentView(uploadList);
        FakeFailureContentView(FailureList);
    }

    // *****************************************假界面中间贴子start*************************************************

    /**
     * 变化数据
     *
     * @param uploadList
     */
    private void FakeUploadContentView(ArrayList<SubjectData> uploadList) {
        LinearLayout layout = createViewLayout(FAKE_TAG);
        if (layout.getChildCount() > 0) {// 无数据直接创建
            FakeContrastContentView(uploadList);
        } else {
            for (int i = 0; i < uploadList.size(); i++) {
                createContentView(uploadList.get(i));
            }
        }
    }

    /**
     * 对比数据集合
     *
     * @param uploadList
     */
    private void FakeContrastContentView(ArrayList<SubjectData> uploadList) {
        LinearLayout layout = createViewLayout(FAKE_TAG);
        ArrayList<View> deleteList = getFakeDelView(layout, uploadList);

        ArrayList<SubjectData> addList = getAddFakeList(layout, uploadList);
        // 删除对应的view
        deleteLayoutView(layout, deleteList);
        // 添加view
        for (int i = 0; i < addList.size(); i++) {
            createContentView(addList.get(i));
        }
    }

    /**
     * 假界面数据体
     *
     * @param subjectData
     */
    public NormalContentView createContentView(SubjectData subjectData) {
        LinearLayout layout = createViewLayout(FAKE_TAG);
        Map<String, String> map = subjectToMap(subjectData);
        NormalContentView contentView = new NormalContentView(context);
        contentView.setTag(String.valueOf(subjectData.getId()));
        Log.i("subjectData.getId()", String.valueOf(subjectData.getId()));
        contentView.initView(map,0);
        layout.addView(contentView, 0);//添加到第一个位置
        layout.setVisibility(View.VISIBLE);
        return contentView;
    }

    /**
     * 处理数据获取要删除的数据
     *
     * @param layout
     * @param list
     * @return
     */
    private ArrayList<View> getFakeDelView(LinearLayout layout, ArrayList<SubjectData> list) {
        ArrayList<View> deleteList = new ArrayList<View>();
        // 要删除的数据
        for (int i = 0; i < layout.getChildCount(); i++) {
            String id = (String) layout.getChildAt(i).getTag();
            deleteList.add(layout.getChildAt(i));
            for (int j = 0; j < list.size(); j++) {
                if (id.equals(String.valueOf(list.get(j).getId()))) {
                    changeFakeNowContentView(list.get(j), layout.getChildAt(i));
                    deleteList.remove(deleteList.size() - 1);
                    break;
                }
            }
        }
        return deleteList;
    }

    private void changeFakeNowContentView(SubjectData subjectData, View view) {
        if (view instanceof NormalContentView) {
            if (SubjectData.UPLOAD_ING == subjectData.getUploadState()) {
                ((NormalContentView) view).setShowUpload(false);
            } else if (SubjectData.UPLOAD_SUCCESS == subjectData.getUploadState()) {
                ((NormalContentView) view).setCode(subjectData.getCode());
                ((NormalContentView) view).setShowUpload(true);
            }
        }
    }

    /**
     * 获取要添加数据
     *
     * @param layout
     * @param List
     * @return
     */
    private ArrayList<SubjectData> getAddFakeList(LinearLayout layout, ArrayList<SubjectData> List) {
        ArrayList<SubjectData> addList = new ArrayList<SubjectData>();
        for (int i = 0; i < List.size(); i++) {
            addList.add(List.get(i));
            for (int j = 0; j < layout.getChildCount(); j++) {
                String id = (String) layout.getChildAt(j).getTag();
                if (id.equals(String.valueOf(List.get(i).getId()))) {
                    addList.remove(addList.size() - 1);
                    break;
                }
            }
        }
        return addList;
    }
    // *****************************************假界面中间贴子end*************************************************

    // *****************************************假界面失败headerstart*********************************************

    /**
     * 处理 失败的数据体
     *
     * @param FailureList
     */
    private void FakeFailureContentView(ArrayList<SubjectData> FailureList) {
        LinearLayout layout = createViewLayout(TOP_TAG);
        if (layout.getChildCount() > 0) {
            fakeContrastTopView(FailureList);
        } else {
            for (int i = 0; i < FailureList.size(); i++) {
                createTopFakeView(FailureList.get(i));
            }
        }
    }

    /**
     * 处理 失败的数据体
     *
     * @param FailureList
     */
    private void fakeContrastTopView(ArrayList<SubjectData> FailureList) {
        LinearLayout layout = createViewLayout(TOP_TAG);
        ArrayList<View> deleteList = getFakeDelView(layout, FailureList);

        ArrayList<SubjectData> addList = getAddFakeList(layout, FailureList);
        // 删除对应的view
        deleteLayoutView(layout, deleteList);
        // 添加view
        for (int i = 0; i < addList.size(); i++) {
            createTopFakeView(addList.get(i));
        }
    }

    /**
     * 创建假数据失败
     *
     * @param subjectData
     */
    private void createTopFakeView(final SubjectData subjectData) {
        final LinearLayout layout = createViewLayout(TOP_TAG);
        CircleHeaderTopFakeView mCircleHeaderTopFakeView = new CircleHeaderTopFakeView(context, subjectData);
        mCircleHeaderTopFakeView.setInterfaceTopFakeView(new interfaceTopFakeView() {

            @Override
            public void clickDel(View view) {
                layout.removeView(view);
                if (delOnClick != null)
                    delOnClick.onClick(String.valueOf(view.getTag()));
            }
        });
        layout.addView(mCircleHeaderTopFakeView);
        layout.setVisibility(View.VISIBLE);

    }
    // *****************************************假界面失败headerend*********************************************

    /**
     * 设置删除回调
     *
     * @param callback
     */
    public void setFakeDelCallback(ItemCallback callback) {
        this.delOnClick = callback;
    }

    /**
     * 处理subject转map
     *
     * @param subjectData
     */
    private Map<String, String> subjectToMap(SubjectData subjectData) {
        Map<String, String> map = new HashMap<>();
        map.put("style", "1");
        map.put("showUpload", "true");
        map.put("imgs", "刚刚");
        map.put("isLike", "1");
        map.put("likeNum", "0");
        map.put("commentNum", "0");
        //处理video数据
        if(!TextUtils.isEmpty(subjectData.getVideo())
                && !TextUtils.isEmpty(subjectData.getVideoSImg())){
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("videoUrl",subjectData.getVideo());
                jsonObject.put("sImgUrl",subjectData.getVideoSImgLocal());
                jsonObject.put("gImgUrl","");
                jsonObject.put("videoTime","");
                jsonObject.put("type",subjectData.getVideoType());
                map.put("selfVideo",jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ArrayList<Map<String, String>> contentArray = subjectData.getContentArray();
        ArrayList<String> imgs = new ArrayList<>();
        StringBuffer contentStr = new StringBuffer();
        for (Map<String, String> content : contentArray) {
            if (!TextUtils.isEmpty(content.get("imgUrl"))) {
                imgs.add(content.get("imgUrl"));
            }
            contentStr.append(content.get("content"));
        }
        map.put("content", contentStr.toString());
        // map.put("cName", mPlateData.getName());
        String[] imgArray = new String[imgs.size()];
        map.put("imgs", Tools.changeArrayDateToJson(imgs.toArray(imgArray)));
        map.put("showUpload", contentStr.toString());
        Map<String, String> customer = LoginManager.userInfo;
        customer.put("img", customer.get("img"));
        map.put("customer", Tools.map2Json(customer));
        map.put("title", subjectData.getTitle());
        map.put("uploadState", String.valueOf(subjectData.getUploadState()));
        if (!TextUtils.isEmpty(subjectData.getCode())) {
            map.put("code", subjectData.getCode());
        }
        return map;
    }
// *******************************************假界面end*************************************************
//***************************无数数据显示界面start************************************************

    /**
     * 显示无数据显示的头部
     *
     * @param content
     * @param des
     * @param callback
     */
    public void showNoDataView(String content, String des, final ItemCallback callback) {
        LinearLayout layout = createViewLayout(NODATA_TAG);
        if (layout.getChildCount() > 0 && layout.getVisibility() == View.VISIBLE) {
            return;
        }

        CircleHeaderNoDataView mCircleHeaderNoDataView = new CircleHeaderNoDataView(context);
        mCircleHeaderNoDataView.initView(content, des);
        mCircleHeaderNoDataView.setInterface(new interfaceNoDataView() {

            @Override
            public void ItemClick() {
                callback.onClick("");
            }
        });
        layout.setVisibility(View.VISIBLE);
        layout.addView(mCircleHeaderNoDataView);
    }

    /**
     * 外部设置展示空数据view
     *
     * @param view
     */
    public void showMyViewNoData(View view) {
        LinearLayout layout = createViewLayout(NODATA_TAG);
        if (layout.getChildCount() > 0 && layout.getVisibility() == View.VISIBLE) {
            return;
        }
        layout.setVisibility(View.VISIBLE);
        layout.addView(view);
    }

    /**
     * 隐藏无数据显示的头部
     */
    public void hideNoDataView() {
        LinearLayout layout = createViewLayout(NODATA_TAG);
        if (layout.getChildCount() > 0) {
            layout.removeAllViews();
            this.removeView(layout);
        }
    }
//***************************无数数据显示界面end************************************************

    /**
     * item
     */
    public interface ItemCallback {
        public void onClick(String content);
    }

    /**
     * 删除假数据对应的view
     *
     * @param layout
     * @param List
     */
    private void deleteLayoutView(LinearLayout layout, ArrayList<View> List) {
        if (List.size() <= 0)
            return;
        for (int i = 0; i < List.size(); i++) {
            layout.removeView(List.get(i));
        }
    }
}
