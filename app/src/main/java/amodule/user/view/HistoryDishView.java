package amodule.user.view;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.dish.activity.DetailDish;
import amodule.dish.activity.GoodDish;
import amodule.user.db.BrowseHistorySqlite;
import aplug.basic.ReqInternet;
import xh.windowview.XhDialog;

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
                        mLoadManager.changeMoreBtn(mListView, ReqInternet.REQ_OK_STRING, 10, msg.arg1, currentPage, true);
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
                    mContext.startActivity(intent);
                }
            }
        });
        if (!mIsChoose)
            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    final XhDialog dialog = new XhDialog(mContext);
                    dialog.setTitle("确定删除该条浏览记录?")
                            .setCanselButton("确定", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Map<String, String> map = mData.get(position);
                                    BrowseHistorySqlite sqlite = new BrowseHistorySqlite(mContext);
                                    sqlite.deleteByCode(BrowseHistorySqlite.TB_DISH_NAME, map.get("code"));
                                    mData.remove(map);
                                    mAdapter.notifyDataSetChanged();
                                    dialog.cancel();
                                }
                            })
                            .setSureButtonTextColor("#333333")
                            .setSureButton("取消", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.cancel();
                                }
                            });
                    dialog.show();
                    return true;
                }
            });
    }

    @Override
    protected AdapterSimple getAdapter() {
        mAdapter = new AdapterSimple(mListView, mData,
                R.layout.a_history_dish_item_list,
                new String[]{"name", "burdens", "isFine", /*"isMakeImg",*/ "allClick", "exclusive"},
                new int[]{R.id.tv_itemDishName, R.id.tv_itemBurden, R.id.iv_itemIsFine, /*R.id.tv_item_make,*/ R.id.allclick, R.id.tag_exclusive});
        mAdapter.urlKey = "imgShow";
        mAdapter.videoImgId = R.id.itemImg1;
        mAdapter.playImgWH = Tools.getDimen(mContext, R.dimen.dp_34);

        mAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view == null || data == null) return false;
                int id = view.getId();
                switch (id) {
                    case R.id.tv_itemDishName:
                    case R.id.tv_item_make:
                    case R.id.allclick:
                        int textMaxWidth = ToolsDevice.getWindowPx(mContext).widthPixels
                                - ToolsDevice.dp2px(mContext, 2 * 15 + 120 + 4 + 15 + 15);
                        ((TextView) view).setMaxWidth(textMaxWidth);
                        return false;
                    case R.id.tag_exclusive:
                        RelativeLayout layout = (RelativeLayout) view.getParent();
                        layout.setVisibility("2".equals(data.toString()) ? View.VISIBLE : View.GONE);
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
                    map.put("imgShow", map.get("img"));
                    map.put("isFine", map.get("isFine").equals("2") ? "精" : "hide");
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
