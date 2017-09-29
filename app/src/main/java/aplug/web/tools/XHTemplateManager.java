package aplug.web.tools;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import acore.override.XHApplication;
import acore.override.helper.XHActivityManager;
import acore.tools.FileManager;
import acore.tools.StringManager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;
import third.mall.aplug.MallInternetCallback;
import third.mall.aplug.MallReqInternet;
import third.mall.aplug.MallStringManager;


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
    private ArrayList<Map<String,String>> mapArrayList = new ArrayList<>(); //处理全部模版集合数据


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

    /**
     * 更新全部模版数据
     */
    public void checkUplateAllTemplate(){
        checkXHUplateAllTemplate();
    }

    /**
     * 处理香哈全部模版更新
     */
    private void checkXHUplateAllTemplate(){
        String url=StringManager.API_TEMPLATE_AUTOLOADTEMPLATE;
        ReqEncyptInternet.in().doEncypt(url, "",new InternetCallback(XHActivityManager.getInstance().getCurrentActivity()) {
            @Override
            public void loaded(int flag, String url, Object msg) {
                if(flag>=ReqInternet.REQ_OK_STRING){
                    ArrayList<Map<String,String>> mapList= StringManager.getListMapByJson(msg);
                    if(mapList!=null&&mapList.size()>0){
                        int size = mapList.size();
                        for(int i=0;i<size;i++){
                            Map<String,String> map= mapList.get(i);
                            map.put("namekey","templateName");
                            map.put("versionkey","versionSign");
                            mapArrayList.add(map);
                        }
                    }
                }
                checkDsUplateAllTemplate();
            }
        });

    }
    /**
     * 处理电商全部模版更新
     */
    public void checkDsUplateAllTemplate(){
        String url= MallStringManager.mall_api_autoloadTemplate;
        MallReqInternet.in().doGet(url,new MallInternetCallback(XHActivityManager.getInstance().getCurrentActivity()) {
            @Override
            public void loadstat(int flag, String url, Object msg, Object... stat) {
                if(flag>= ReqInternet.REQ_OK_STRING){
                     ArrayList<Map<String,String>> mapList= StringManager.getListMapByJson(msg);
                    if(mapList!=null&&mapList.size()>0){
                        int size = mapList.size();
                        for(int i=0;i<size;i++){
                            Map<String,String> map= mapList.get(i);
                            map.put("namekey","template_name");
                            map.put("versionkey","version_sign");
                            mapArrayList.add(map);
                        }
                    }
                }
                handlerAllTemplateData();
            }
        });

    }

    /**
     * 处理更新模版数据
     */
    private void handlerAllTemplateData(){
        if(isLoad||mapArrayList==null||mapArrayList.size()<=0){
            return;
        }
        if(templateNum<=0) {
            templateNum = 0;
        }else if(templateNum>=mapArrayList.size()){
            templateNum=-1;
            isLoad=true;
            return;
        }
        Map<String,String> mapTemplate=mapArrayList.get(templateNum);
        if(mapTemplate!=null&&mapTemplate.containsKey(mapTemplate.get("namekey"))){
            //处理
            String requestMethod= mapTemplate.get(mapTemplate.get("namekey"));
            String versionSign= mapTemplate.get(mapTemplate.get("versionkey"));
            //获取模版数据
            final String path = FileManager.getSDDir() + "long/" + requestMethod;
            final String readStr = FileManager.readFile(path);
            //获取version
            String versionUrl = (String) FileManager.loadShared(XHApplication.in(), requestMethod, "version_sign");
            if(TextUtils.isEmpty(readStr))versionUrl = "";

            if(TextUtils.isEmpty(versionUrl) || !versionSign.equals(versionUrl)){
                getSingleAllTemplate(requestMethod, mapTemplate, path, readStr, mapTemplate.get("versionkey"), new TemplateWebViewControl.MouldCallBack() {
                    @Override
                    public void load(boolean isSuccess, String data, String requestMothed, String version) {
                        ++templateNum;
                        handlerAllTemplateData();
                    }
                });
            }else{
                Log.i("wyl","当前sign:正确::");
                ++templateNum;
                handlerAllTemplateData();
            }
        }else{
            templateNum=-1;
            isLoad=true;
            return;
        }
    }

    /**
     * 直接通过七牛url进行下载数据
     * @param requestMethod
     * @param msg
     * @param path
     * @param readStr
     * @param versionKey
     * @param mouldCallBack
     */
    private void getSingleAllTemplate(String requestMethod,Map<String,String> msg,final String path, final String readStr,String versionKey,TemplateWebViewControl.MouldCallBack mouldCallBack){
        if(templateWebViewControl==null){
            templateWebViewControl= new TemplateWebViewControl();
        }
        templateWebViewControl.setMouldCallBack(mouldCallBack);
        templateWebViewControl.handlerQiniuGetData(msg,requestMethod,path,readStr,versionKey);
    }
}
