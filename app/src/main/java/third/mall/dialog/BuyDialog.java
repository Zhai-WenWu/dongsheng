package third.mall.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.xianghatest.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import acore.tools.Tools;
import aplug.basic.ReqInternet;
import third.mall.activity.ShoppingActivity;
import third.mall.activity.ShoppingOrderActivity;
import third.mall.adapter.AdapterFavorable;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;
import xh.basic.internet.UtilInternet;
import xh.basic.tool.UtilString;

/**
 * 选择优惠券的dialog
 *
 * @author yujian
 */
public class BuyDialog extends SimpleDialog {

    private Context context;
    private ImageView item_commod_iv;
    private TextView item_commod_texts,item_commod_price,item_commod_num;
    private RelativeLayout item_commod_cut;
    private Map<String,String> mapData;
    private int productNum=1;

    public BuyDialog(Activity activity, Map<String,String> mapData,int productNum) {
        super(activity, R.layout.dialog_mall_buy);
        this.context = activity;
        this.mapData= mapData;
        this.productNum= productNum;
        setLatyoutHeight();
        init();
    }

    public void init() {
        item_commod_iv= (ImageView) findViewById(R.id.item_commod_iv);
        item_commod_texts= (TextView) findViewById(R.id.item_commod_texts);
        item_commod_price= (TextView) findViewById(R.id.item_commod_price);
        item_commod_num= (TextView) findViewById(R.id.item_commod_num);
        //减少数量
        findViewById(R.id.item_commod_cut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(productNum>1){
                    --productNum;
                    item_commod_num.setText(String.valueOf(productNum));

                }else{
                    Tools.showToast(context,"最少购买一个");
                }
            }
        });
        //增加数量
        findViewById(R.id.item_commod_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ++productNum;
                item_commod_num.setText(String.valueOf(productNum));
            }
        });
        //点击下一步
        findViewById(R.id.next_order).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRequestOrder(getOrderInfo());
            }
        });
    }

    private String getOrderInfo(){
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("product_code",mapData.get("product_code"));
            jsonObject.put("product_num",String.valueOf(productNum));
            return  jsonObject.toString();
        }catch (Exception e){
        }
        return "";
    }
    /**
     * 生产订单
     */
    private void setRequestOrder(final String orderInfo) {
        if(TextUtils.isEmpty(orderInfo)){
            return;
        }
        String param="order_info="+orderInfo;
        MallReqInternet.in().doPost(MallStringManager.mall_checkoutOrder_v2, param, new MallInternetCallback(context) {

            @Override
            public void loadstat(int flag, String url, Object msg, Object... stat) {
                if(flag>= UtilInternet.REQ_OK_STRING){
                    ArrayList<Map<String,String>> list_data=UtilString.getListMapByJson(UtilString.getListMapByJson(msg).get(0).get("sub_order"));
                    if(list_data.size()>0){
                        Intent intent = new Intent(context,ShoppingOrderActivity.class);
                        intent.putExtra("msg_order", msg.toString());
                        intent.putExtra("order_info", orderInfo);
                        intent.putExtra("url", MallStringManager.mall_checkoutOrder_v2);
                        if(stat!=null&&stat.length>0&& !TextUtils.isEmpty((String)stat[0])){
                            intent.putExtra("stat", (String) stat[0]);
                        }
                    }else{
                        setRequestOrder(orderInfo);
                    }
                }else if(flag==UtilInternet.REQ_CODE_ERROR && msg instanceof Map){
                    @SuppressWarnings("unchecked")
                    Map<String,String> map= (Map<String, String>) msg;
                    if("6000008".equals(map.get("code"))){
                        setRequestOrder(orderInfo);
                    }else{
                        Tools.showToast(context, map.get("msg")+"");
                    }
                }
            }
        });
    }

}
