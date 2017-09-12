package aplug.web.tools;

import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * 香哈模版更新
 */

public class XHTemplateManager {
    public final static String XHDISHLAYOUT = "xhDishLayout";
    public final static String DSPRODUCTINFO = "DsProductInfo";//商品详情页
    public final static String DSUNDERSCOREPRODUCTINFO = "DsUnderscoreProductInfo";//详情页下划页
    public final static String DSSUCCESSCOMMENT = "DsSuccessComment";//评论成功页
    public final static String DSCOMMENTLIST = "DsCommentList";//评论列表页
    public final static Map<String,String[]> TEMPLATE_MATCHING = new HashMap<>();
    //初始化
    static {
        TEMPLATE_MATCHING.put(XHDISHLAYOUT , new String[]{"<{code}>"});//菜谱模板
        TEMPLATE_MATCHING.put(DSPRODUCTINFO , new String[]{"<{product_code}>"});//商品详情页
        TEMPLATE_MATCHING.put(DSUNDERSCOREPRODUCTINFO , new String[]{"<{product_code}>"});//商品详情页介绍
        TEMPLATE_MATCHING.put(DSCOMMENTLIST , new String[]{"<{product_code}>","<{comment_id}>","<{from}>"});//评价列表
    }

    public static long starttime;
    //模版更新集合
    private String[] templates=new String[]{XHDISHLAYOUT,DSPRODUCTINFO,DSUNDERSCOREPRODUCTINFO,DSSUCCESSCOMMENT,DSCOMMENTLIST};
    private boolean isLoad= false;
    private int templateNum=-1;
    private TemplateWebViewControl templateWebViewControl;
    public XHTemplateManager(){
        starttime= System.currentTimeMillis();
        templateNum=0;
    }

    /**
     * 检查全部更新
     */
    public void CheckUpdataAllTemplate(){
        if(isLoad){
            return;
        }
        if(templateNum<=0) {
            templateNum = 0;
        }else if(templateNum>=templates.length){
            templateNum=-1;
            isLoad=true;
            return;
        }
        String requestMethod=templates[templateNum];
        if(!TextUtils.isEmpty(requestMethod)){
            getSingleTemplate(requestMethod, new TemplateWebViewControl.MouldCallBack() {
                @Override
                public void load(boolean isSuccess, String data, String requestMothed, String version) {
                    ++templateNum;
                    CheckUpdataAllTemplate();
                }
            });
        }else{
            templateNum=-1;
            isLoad=true;
            return;
        }
    }

    /**
     * 获取单个模版
     * @param requestMethod
     * @param mouldCallBack
     */
    public void getSingleTemplate(String requestMethod,TemplateWebViewControl.MouldCallBack mouldCallBack){
        if(templateWebViewControl==null){
            templateWebViewControl= new TemplateWebViewControl();
        }
        templateWebViewControl.setMouldCallBack(mouldCallBack);
        templateWebViewControl.getH5MDWithRequestMed(requestMethod);
    }
}
