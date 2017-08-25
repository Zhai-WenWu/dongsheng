package third.mall.activity;

import android.app.Dialog;
import android.content.DialogInterface;
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
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import aplug.web.ShowTemplateWeb;
import aplug.web.tools.XHTemplateManager;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;
import third.mall.adapter.AdapterEvalution;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;

public class PublishEvalutionMultiActivity extends BaseActivity {
    public static final int REQUEST_CODE_NEED_REFRESH = 0x2;
    public static final String STATISTICS_ID = "a_publish_comment";

    public static final String EXTRAS_ORDER_ID = "order_id";
    public static final String EXTRAS_POSITION = "position";
    public static final String EXTRAS_ID = "id";

    private PtrClassicFrameLayout refershLayout;
    private ListView commodList;

    private AdapterEvalution adapter;

    private List<Map<String, String>> commodData = new ArrayList<>();

    private String order_id = "";
    private int position = -1;
    private int id = -1;
    int status;
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
            order_id = intent.getStringExtra(EXTRAS_ORDER_ID);
            position = intent.getIntExtra(EXTRAS_POSITION,position);
            id = intent.getIntExtra(EXTRAS_ID,id);
        }
    }

    private void initView() {
        refershLayout = (PtrClassicFrameLayout) findViewById(R.id.refresh_list_view_frame);
        commodList = (ListView) findViewById(R.id.commod_list);

        TextView rightText = (TextView) findViewById(R.id.rightText);
        rightText.setText("发布");
        rightText.setVisibility(View.VISIBLE);
        rightText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XHClick.mapStat(PublishEvalutionMultiActivity.this, STATISTICS_ID,"点击发布按钮","");
                if(!ToolsDevice.isNetworkAvailable(PublishEvalutionMultiActivity.this)){
                    Tools.showToast(PublishEvalutionMultiActivity.this,"网络异常，请检查网络");
                    return;
                }
                publishMutilEvalution();
            }
        });
    }

    private void setLoading() {
        adapter = new AdapterEvalution(this, commodData, order_id);
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
                .append(order_id);
        MallReqInternet.in().doGet(
                params.toString(),
                new MallInternetCallback(this) {
                    @Override
                    public void loadstat(int flag, String url, Object msg, Object... stat) {
                        commodData.clear();
                        loadManager.hideProgressBar();
                        refershLayout.refreshComplete();
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
        MallReqInternet.in().doPost(MallStringManager.mall_addMuiltComment,
                getParams(),
                new MallInternetCallback(this) {
                    @Override
                    public void loadstat(int flag, String url, Object msg, Object... stat) {
                        cancelUploadingDialog();
                        if (flag >= MallReqInternet.REQ_OK_STRING) {
                            Map<String, String> data = StringManager.getFirstMap(msg);
                            if (data.containsKey("status") && "2".equals(data.get("status"))) {
                                //TODO 去评价成功
                                Intent intent = new Intent(PublishEvalutionMultiActivity.this,EvalutionSuccessActivity.class);
                                intent.putExtra(ShowTemplateWeb.REQUEST_METHOD, XHTemplateManager.XHDISH);
                                intent.putExtra(ShowTemplateWeb.NOW_DATA_ARR,new String[]{"94888485"});
                                intent.putExtra("url","http://m.xiangha.com");
                                startActivity(intent);
                            }
                        }
                    }
                });
    }

    private LinkedHashMap<String,String> getParams(){
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("type", "6");
        params.put("order_id", order_id);
        params.put("data", getCommentData());
        Log.i("tzy","params = " + params.toString());
        return params;
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
        } catch (JSONException ignored) {

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
            case OrderStateActivity.request_order:
                if (resultCode == OrderStateActivity.result_comment_success) {
                    status = requestCode;
                }
                break;
            default:break;
        }
    }

    @Override
    public void onBackPressed() {
        XHClick.mapStat(PublishEvalutionMultiActivity.this, STATISTICS_ID,"点击返回按钮","");
        super.onBackPressed();
    }

    @Override
    public void finish() {
        if(id != -1 && position != -1){
            Intent intent = new Intent();
            intent.putExtra("code", String.valueOf(id));
            intent.putExtra("position", String.valueOf(position));
            intent.putExtra("order_id",order_id);
            setResult(status, intent);
        }
        super.finish();
    }

    private Dialog mUploadingDialog;

    private void showUploadingDialog() {
        if (mUploadingDialog != null && mUploadingDialog.isShowing())
            return;
        if (mUploadingDialog == null) {
            mUploadingDialog = new Dialog(this, R.style.dialog);
            mUploadingDialog.setContentView(R.layout.ask_upload_dialoglayout);
//            mUploadingDialog.setCancelable(false);
            mUploadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    MallReqInternet.in().cancelRequset(new StringBuffer(MallStringManager.mall_addMuiltComment).append(getParams()).toString());
                }
            });
        }
        mUploadingDialog.show();
    }

    private void cancelUploadingDialog() {
        if (mUploadingDialog == null || !mUploadingDialog.isShowing())
            return;
        mUploadingDialog.cancel();
    }

}
