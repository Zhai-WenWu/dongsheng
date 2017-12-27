package third.mall.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.xh.manager.DialogManager;
import com.xh.manager.ViewManager;
import com.xiangha.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import acore.logic.XHClick;
import acore.tools.ObserverManager;
import acore.tools.StringManager;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import amodule.answer.view.UploadingView;
import cn.srain.cube.views.ptr.PtrClassicFrameLayout;
import third.mall.adapter.AdapterEvalution;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import third.mall.override.MallBaseActivity;

/**
 * 商品发布评价
 */
public class PublishEvalutionMultiActivity extends MallBaseActivity {
    public static final int REQUEST_CODE_NEED_REFRESH = 0x2;
    public static final String STATISTICS_ID = "a_publish_comment";

    public static final String EXTRAS_ORDER_ID = "order_id";
    public static final String EXTRAS_POSITION = "position";
    public static final String EXTRAS_ID = "id";

    private PtrClassicFrameLayout refershLayout;
    private ListView commodList;
    private TextView rightText;
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

        rightText = (TextView) findViewById(R.id.rightText);
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
        adapter.setIdAndPosition(id,position);
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
                new MallInternetCallback() {
                    @Override
                    public void loadstat(int flag, String url, Object msg, Object... stat) {
                        commodData.clear();
                        loadManager.hideProgressBar();
                        refershLayout.refreshComplete();
                        if (flag >= MallReqInternet.REQ_OK_STRING) {
                            List<Map<String, String>> datas = StringManager.getListMapByJson(msg);
                            commodData.addAll(datas);
                            adapter.notifyDataSetChanged();

                            setPulishStatus();
                        }
                        loadManager.changeMoreBtn(flag,commodData.size(),0,2,false);
                        commodList.setVisibility(commodData.size() > 0 ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void setPulishStatus(){
        boolean canPulish = false;
        Map<String,String> map;
        for(int index = 0 ; index < commodData.size() ; index ++){
            map = commodData.get(index);
            if("1".equals(map.get("status"))){
                canPulish = true;
                break;
            }
        }
        rightText.setVisibility(canPulish?View.VISIBLE:View.GONE);
    }

    private void publishMutilEvalution() {
        if(getParams().size() <= 1){
            //参数有问题
            return;
        }
        showUploadingDialog();
        MallReqInternet.in().doPost(MallStringManager.mall_addMuiltComment,
                getParams(),
                new MallInternetCallback() {
                    @Override
                    public void loadstat(int flag, String url, Object msg, Object... stat) {
                        cancelUploadingDialog();
                        if (flag >= MallReqInternet.REQ_OK_STRING) {
                            Map<String, String> data = StringManager.getFirstMap(msg);
                            if (data.containsKey("status") && "2".equals(data.get("status"))) {
                                if(id == -1 && position == -1){
                                    ObserverManager.getInstence().notify(ObserverManager.NOTIFY_COMMENT_SUCCESS,"",order_id);
                                }
                                startActivityForResult(
                                        new Intent(PublishEvalutionMultiActivity.this, EvalutionSuccessActivity.class)
                                                .putExtra(EvalutionSuccessActivity.EXTRAS_ID,id)
                                                .putExtra(EvalutionSuccessActivity.EXTRAS_POSITION,position),
                                        OrderStateActivity.request_order
                                );
                            }
                        }
                    }
                });
    }

    private LinkedHashMap<String,String> getParams(){
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("order_id", order_id);
        Map<String,String> map;
        for(int index = 0 ; index < commodData.size() ; index ++){
            map = commodData.get(index);
            if("1".equals(map.get("status"))){
                params.put("data[" + index + "][product_code]",map.get("product_code"));
                params.put("data[" + index + "][score]",map.get("score"));
            }
        }
//        Log.i("tzy","params = " + params.toString());
        return params;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_CODE_NEED_REFRESH:
                if(resultCode == OrderStateActivity.result_comment_success
                        || resultCode == OrderStateActivity.result_comment_part_success){
                    status = resultCode;
                    refersh();
                }
                break;
            case OrderStateActivity.request_order:
                if (resultCode == OrderStateActivity.result_comment_success
                        || resultCode == OrderStateActivity.result_comment_part_success) {
                    status = resultCode;
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

    private DialogManager mUploadingDialog;
    private void showUploadingDialog() {
        if (mUploadingDialog != null && mUploadingDialog.isShowing())
            return;
        if (mUploadingDialog == null) {
            mUploadingDialog = new DialogManager(this);
            mUploadingDialog.createDialog(new ViewManager(mUploadingDialog)
            .setView(new UploadingView(this))).noPadding().setOnCancelListener(new DialogInterface.OnCancelListener() {
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
