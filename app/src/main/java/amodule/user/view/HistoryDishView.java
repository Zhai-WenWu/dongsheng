package amodule.user.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xh.view.HButtonView;
import com.xh.view.TitleMessageView;
import com.xiangha.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.activity.DetailDish;
import amodule.dish.activity.GoodDish;
import amodule.user.db.BrowseHistorySqlite;
import aplug.basic.ReqInternet;

import static android.app.Activity.RESULT_OK;

/**
 * PackageName : amodule.user.view
 * Created by MrTrying on 2016/8/17 11:13.
 * E_mail : ztanzeyu@gmail.com
 */
public class HistoryDishView extends HistoryView {

    private BaseActivity mAct;

    private final int LOAD_OVER = 2;
    private final int REFRESH_OVER = 3;
    private Handler mHandler;
    private AdapterSimple mAdapter;
    private List<Map<String, String>> mData = new ArrayList<>();
    private int currentPage = 0;

    private boolean mIsChoose;

    public HistoryDishView(BaseActivity activity, boolean isChoose) {
        super(activity);
        mAct = activity;
        mIsChoose = isChoose;
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case LOAD_OVER:
                        mLoadManager.loadOver(50,mListView, msg.arg1);
                        mAdapter.notifyDataSetChanged();
                        mRoot.findViewById(R.id.noData_layout).setVisibility(mData.size() == 0 ? View.VISIBLE : View.GONE);
                        mListView.setVisibility(mData.size() == 0 ? View.GONE : View.VISIBLE);
                        break;
                    case REFRESH_OVER:
                        refreshLayout.refreshComplete();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void initView() {
        noDataText.setText("暂无浏览记录哦，快去逛逛吧~");
        if(mIsChoose){
            noDataBtn.setVisibility(View.GONE);
        }else{
            noDataBtn.setVisibility(View.VISIBLE);
        }
        noDataBtn.setText("去逛逛");
        noDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.finish();
                mContext.startActivity(new Intent(mContext, GoodDish.class));
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mIsChoose){
                    Intent it = new Intent();
                    it.putExtra("dishCode",mData.get(position).get("code"));
                    it.putExtra("dishName",mData.get(position).get("name"));
                    mAct.setResult(RESULT_OK,it);
                    mAct.finish();
                }else {
                    Intent intent = new Intent(mContext, DetailDish.class);
                    intent.putExtra("code", mData.get(position).get("code"));
                    intent.putExtra("img", mData.get(position).get("img"));
                    intent.putExtra("name", mData.get(position).get("name"));
                    intent.putExtra("dishInfo", getDishInfo(mData.get(position)));
                    mContext.startActivity(intent);
                }
            }
        });
        if (!mIsChoose)
            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    final DialogManager dialogManager = new DialogManager(mAct);
                    dialogManager.createDialog(new ViewManager(dialogManager)
                            .setView(new TitleMessageView(mAct).setText("确定删除该条浏览记录?"))
                            .setView(new HButtonView(mAct)
                                    .setNegativeText("取消", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialogManager.cancel();
                                        }
                                    })
                                    .setPositiveText("确定", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialogManager.cancel();
                                            Map<String, String> map = mData.get(position);
                                            BrowseHistorySqlite sqlite = new BrowseHistorySqlite(mContext);
                                            sqlite.deleteByCode(BrowseHistorySqlite.TB_DISH_NAME, map.get("code"));
                                            mData.remove(map);
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    }))).show();
                    return true;
                }
            });
    }

    private String getDishInfo(Map<String,String> data) {
        if (data == null || data.isEmpty())
            return "";
        String info = data.get("info");
        try{
            JSONObject dishInfoJson = new JSONObject();
            dishInfoJson.put("code",data.get("code"));
            dishInfoJson.put("name",data.get("name"));
            dishInfoJson.put("img",data.get("img"));
            dishInfoJson.put("type",TextUtils.equals(data.get("hasVideo"), "2") ? "2" : "1");
            dishInfoJson.put("favorites",data.get("favorites"));
            dishInfoJson.put("info", TextUtils.isEmpty(info) ? "" : info);
            JSONObject customerJson = new JSONObject();
            Map<String,String> userInfo = StringManager.getFirstMap(data.get("customer"));
            customerJson.put("customerCode",userInfo.get("code"));
            customerJson.put("nickName",userInfo.get("nickName"));
            customerJson.put("info",userInfo.get("info"));
            customerJson.put("img",userInfo.get("img"));
            dishInfoJson.put("customer",customerJson);
            return Uri.encode(dishInfoJson.toString());
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    @Override
    protected AdapterSimple getAdapter() {
        mAdapter = new AdapterSimple(mListView, mData,
                R.layout.c_search_result_caipu_item,
                new String[]{"img","name", "burdens", "nickName", "allClick", "exclusive","duration"},
                new int[]{R.id.iv_caipuCover,R.id.tv_caipu_name, R.id.tv_caipu_decrip,
                        R.id.tv_caipu_origin, R.id.tv_caipu_observed, R.id.iv_itemIsSolo,R.id.video_duration});
        mAdapter.urlKey = "imgShow";
        mAdapter.videoImgId = R.id.itemImg1;
        mAdapter.playImgWH = Tools.getDimen(mContext, R.dimen.dp_34);

        mAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view == null || data == null) return false;
                int id = view.getId();
                switch (id) {
                    case R.id.tv_caipu_origin:
                    case R.id.tv_caipu_name:
                    case R.id.tv_caipu_decrip:
                    case R.id.tv_caipu_observed:
                        int textMaxWidth = ToolsDevice.getWindowPx(mContext).widthPixels
                                - ToolsDevice.dp2px(mContext, 2 * 15 + 120 + 4 + 15 + 15);
                        ((TextView) view).setMaxWidth(textMaxWidth);
                        return false;
                    case R.id.iv_itemIsSolo:
                        RelativeLayout layout = (RelativeLayout) view.getParent();
                        layout.setVisibility(View.GONE);
                        return true;
                }
                return false;
            }
        });
        return mAdapter;
    }

    @Override
    protected void loadData(final boolean isRefresh) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isRefresh) {
                    currentPage = 0;
                    mData.clear();
                }
                BrowseHistorySqlite sqlite = new BrowseHistorySqlite(mContext);
                List<Map<String, String>> data = sqlite.loadByPage(BrowseHistorySqlite.TB_DISH_NAME, ++currentPage);
                for (int index = 0; index < data.size(); index++) {
                    Map<String, String> map = data.get(index);
                    String nickName = map.get("nickName");
                    if(!TextUtils.isEmpty(nickName) && nickName.length()>=8){
                        nickName = nickName.substring(0,7) + "...";
                    }
                    map.put("nickName",nickName);
                    map.put("imgShow", map.get("img"));
                    map.put("isMakeImg", map.get("isMakeImg").equals("2") ? "步骤图" : "hide");
                    map.put("allClick", map.get("allClick") + "浏览      " + map.get("favorites") + "收藏");
                    if (!map.containsKey("hasVideo")) {
                        map.put("hasVideo", "1");
                    }
                }
                mData.addAll(data);
                if (isRefresh) {
                    mHandler.sendEmptyMessage(REFRESH_OVER);
                }
                Message msg = mHandler.obtainMessage(LOAD_OVER, data.size(), 0);
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    @Override
    public void cleanData() {
        BrowseHistorySqlite sqlite = new BrowseHistorySqlite(mContext);
        sqlite.deleteByCode(BrowseHistorySqlite.TB_DISH_NAME, null);
        mData.clear();
        Message msg = mHandler.obtainMessage(LOAD_OVER, 0, 0);
        mHandler.sendMessage(msg);
    }

    @Override
    public boolean hasData() {
        return mData != null && mData.size() > 0;
    }
}
