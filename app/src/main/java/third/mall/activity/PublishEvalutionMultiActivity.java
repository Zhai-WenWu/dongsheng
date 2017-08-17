package third.mall.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.xianghatest.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;
import third.mall.adapter.AdapterEvalution;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;

public class PublishEvalutionMultiActivity extends BaseActivity {
    public static final int REQUEST_CODE_NEED_REFRESH = 0x2;
    public static final String STATISTICS_ID = "a_publish_comment";

    public static final String EXTRAS_ORDER_ID = "order_id";

    private TextView rightText;
    private PtrClassicFrameLayout refershLayout;
    private ListView commodList;

    private AdapterEvalution adapter;

    private List<Map<String, String>> commodData = new ArrayList<>();

    private String orderId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity("发布评价", 6, 0, R.layout.c_view_bar_title, R.layout.activity_publish_evalution_multi);
        initData();
        initView();
        setLoading();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            orderId = intent.getStringExtra(EXTRAS_ORDER_ID);
        }
        //TODO  101382361，101382360 ， 101382362
        orderId="101382362";
    }

    private void initView() {
        rightText = (TextView) findViewById(R.id.rightText);
        refershLayout = (PtrClassicFrameLayout) findViewById(R.id.refresh_list_view_frame);
        commodList = (ListView) findViewById(R.id.commod_list);

        rightText.setText("发布");
        rightText.setVisibility(View.VISIBLE);
        rightText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(PublishEvalutionMultiActivity.this, STATISTICS_ID,"点击发布按钮","");
                publishMutilEvalution();
            }
        });
    }

    private void setLoading() {
        adapter = new AdapterEvalution(this, commodData,orderId);
        loadManager.setLoading(refershLayout, commodList, adapter, true,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refersh();
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadData();
                    }
                });
        loadManager.getSingleLoadMore(commodList).setText("没有更多了");
    }

    private void refersh() {
        loadData();
    }

    private void loadData() {
        loadManager.showProgressBar();
        loadManager.changeMoreBtn(MallReqInternet.REQ_OK_STRING,-1,-1,1,true);
        StringBuilder params = new StringBuilder(MallStringManager.mall_toComment)
                .append("?order_id=")
                .append(orderId);
        MallReqInternet.in().doGet(
                params.toString(),
                new MallInternetCallback(this) {
                    @Override
                    public void loadstat(int flag, String url, Object msg, Object... stat) {
                        commodData.clear();
                        loadManager.hideProgressBar();
                        refershLayout.refreshComplete();
                        Log.i("tzy","msg = " + msg);
                        if (flag >= MallReqInternet.REQ_OK_STRING) {
                            List<Map<String, String>> datas = StringManager.getListMapByJson(msg);
                            commodData.addAll(datas);
                            adapter.notifyDataSetChanged();
                        }
                        loadManager.changeMoreBtn(flag,commodData.size(),0,2,false);
                        commodList.setVisibility(commodData.size() > 0 ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void publishMutilEvalution() {
        showUploadingDialog();
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("type", "6");
        params.put("order_id", orderId);
        params.put("data", getCommentData());
        Log.i("tzy","params = " + params.toString());
        MallReqInternet.in().doPost(MallStringManager.mall_addMuiltComment,
                params,
                new MallInternetCallback(this) {
                    @Override
                    public void loadstat(int flag, String url, Object msg, Object... stat) {
                        cancelUploadingDialog();
                        if (flag >= MallReqInternet.REQ_OK_STRING) {
                            Map<String, String> data = StringManager.getFirstMap(msg);
                            if (data.containsKey("status") && "2".equals(data.get("status"))) {
                                //去评价成功
                                Intent intent = new Intent(PublishEvalutionMultiActivity.this,EvalutionSuccessActivity.class);
                                intent.putExtra("url","http://m.xiangha.com");
                                startActivity(intent);
                            } else {

                            }
                        }
                    }
                });
    }

    private String getCommentData() {
        JSONArray jsonArray = new JSONArray();
        try {
            for (Map<String, String> data : commodData) {
                if("1".equals(data.get("status"))){
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("product_code", data.get("product_code"));
                    jsonObj.put("score", data.get("score"));
                    jsonArray.put(jsonObj);
                }
            }
        } catch (JSONException e) {

        }
        return jsonArray.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_CODE_NEED_REFRESH:
                if(resultCode == RESULT_OK){
                    refersh();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        XHClick.mapStat(PublishEvalutionMultiActivity.this, STATISTICS_ID,"点击返回按钮","");
        super.onBackPressed();
    }

    private Dialog mUploadingDialog;

    private void showUploadingDialog() {
        if (mUploadingDialog != null && mUploadingDialog.isShowing())
            return;
        if (mUploadingDialog == null) {
            mUploadingDialog = new Dialog(this, R.style.dialog);
            mUploadingDialog.setContentView(R.layout.ask_upload_dialoglayout);
            mUploadingDialog.setCancelable(false);
        }
        mUploadingDialog.show();
    }

    private void cancelUploadingDialog() {
        if (mUploadingDialog == null || !mUploadingDialog.isShowing())
            return;
        mUploadingDialog.cancel();
    }

}
