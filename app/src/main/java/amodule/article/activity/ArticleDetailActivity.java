package amodule.article.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.tools.StringManager;
import acore.tools.Tools;
import amodule.article.adapter.ArticleDetailAdapter;
import amodule.article.view.ArticleHeaderView;
import amodule.article.view.CommodityItemView;
import amodule.article.view.DishItemView;
import amodule.article.view.ImageShowView;
import amodule.article.view.VideoShowView;
import amodule.article.view.richtext.RichParser;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;

import static amodule.article.adapter.ArticleDetailAdapter.Type_recommed;

/**
 * 文章详情
 */
public class ArticleDetailActivity extends BaseActivity {
    private boolean initUiSuccess=false;//ui初始化完成
    private String code="";//请求数据的code
    private int page= 0;//相关推荐的page
    private ArticleDetailAdapter detailAdapter;
    private ArrayList<Map<String,String>> otherListMap= new ArrayList<>();//评论列表和推荐列表对数据集合

    private ListView listview;
    private LinearLayout layout,linearLayoutOne,linearLayoutTwo,linearLayoutThree;//头部view
    private TextView title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle= this.getIntent().getExtras();
        if(bundle!=null){
            code= bundle.getString("code");
        }
        code="123456";
        init();
    }

    /**初始化**/
    private void init(){
        initActivity("文章详情页",2,0,0, R.layout.a_article_detail);
        initView();
        initData();
    }
    /**View部分初始化**/
    private void initView(){
        TextView title = (TextView) findViewById(R.id.title);
        listview = (ListView) findViewById(R.id.listview);
        initHeaderView();
        listview.addHeaderView(layout);
    }

    /**
     * 初始化header布局
     */
    private void initHeaderView(){
        //initHeaderView
        layout= new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        linearLayoutOne= new LinearLayout(this);
        linearLayoutOne.setOrientation(LinearLayout.VERTICAL);
        linearLayoutTwo= new LinearLayout(this);
        linearLayoutTwo.setOrientation(LinearLayout.VERTICAL);
        linearLayoutTwo.setPadding(Tools.getDimen(this,R.dimen.dp_20),0,Tools.getDimen(this,R.dimen.dp_20),0);
        linearLayoutThree= new LinearLayout(this);
        linearLayoutThree.setOrientation(LinearLayout.VERTICAL);
        linearLayoutOne.setVisibility(View.GONE);
        linearLayoutTwo.setVisibility(View.GONE);
        linearLayoutThree.setVisibility(View.GONE);
        layout.addView(linearLayoutOne);
        layout.addView(linearLayoutTwo);
        layout.addView(linearLayoutThree);
    }
    /**数据初始化**/
    private void initData(){
        if(TextUtils.isEmpty(code)){
            Tools.showToast(this,"当前数据错误，请重新请求");
            return;
        }
        detailAdapter= new ArticleDetailAdapter(otherListMap);
        listview.setAdapter(detailAdapter);
        requestArticleData();

    }
    /**请求网络**/
    private void requestArticleData(){
        String url= StringManager.api_getArticleInfo;
        String params= TextUtils.isEmpty(code)?"":"code="+code;
        loadManager.showProgressBar();
        ReqEncyptInternet.in().doEncypt(url, params, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object object) {
                if(flag>= ReqInternet.REQ_OK_STRING){
                    ArrayList<Map<String,String>> listMap=StringManager.getListMapByJson(object);
                    analysArticleData(listMap.get(0));
                }else{
                    toastFaildRes(flag,true,object);
                }
//                requestRelateData();
                loadManager.hideProgressBar();
            }
        });
    }

    /**
     * 解析文章数据
     * @param mapArticle
     */
    private void analysArticleData(@NonNull Map<String,String> mapArticle){
        if(mapArticle.isEmpty())return;
        findViewById(R.id.rightImgBtn2).setVisibility(View.VISIBLE);
        ArticleHeaderView headerView= new ArticleHeaderView(ArticleDetailActivity.this);
        headerView.setData(mapArticle);
        linearLayoutOne.addView(headerView);
        linearLayoutOne.setVisibility(View.VISIBLE);
        detailAdapter.notifyDataSetChanged();
        listview.setVisibility(View.VISIBLE);
        String content = mapArticle.get("content");
        analysArticleContent(content);
    }

    /**
     * 解析图文混排数据
     * @param content
     */
    private void analysArticleContent(String content){
        if(TextUtils.isEmpty(content))return;
        ArrayList<Map<String,String>> listContent = StringManager.getListMapByJson(content);
        int size= listContent.size();
        if(size>0)linearLayoutTwo.setVisibility(View.VISIBLE);
        for(int i=0;i<size;i++){
            String type= listContent.get(i).get("type");
            if("text".equals(type)){//文章
                String html= listContent.get(i).get("html");
                if(!TextUtils.isEmpty(html)){
                    TextView textView = new TextView(this);
                    textView.setText(RichParser.fromHtml(html));
                    linearLayoutTwo.addView(textView);
                }
            }else if("image".equals(type)){//图片
                String imageUrl= listContent.get(i).get("imageurl");
                if(!TextUtils.isEmpty(imageUrl)) {
                    ImageShowView imageShowView = new ImageShowView(this);
                    imageShowView.setImageUrl(imageUrl);
                    linearLayoutTwo.addView(imageShowView);
                }
            }else if("video".equals(type)){//视频
                String videoUrl= listContent.get(i).get("videourl");
                String videoimageurl= listContent.get(i).get("videosimageurl");
                if(!TextUtils.isEmpty(videoUrl)&&!TextUtils.isEmpty(videoimageurl)){
                    VideoShowView videoShowView= new VideoShowView(this);
                    videoShowView.setVideoData(videoimageurl,videoUrl);
                    linearLayoutTwo.addView(videoShowView);
                }

            }else if("xiangha".equals(type)){//自定义演示。ds，电商，caipu，菜谱
                String json = listContent.get(i).get("json");
                if(!TextUtils.isEmpty(json)){
                    Map<String,String> jsonMap=  StringManager.getFirstMap(json);
                    if(jsonMap.containsKey("type")&&!TextUtils.isEmpty(jsonMap.get("type"))){
                        String datatype= jsonMap.get("type");
                        if("ds".equals(datatype)){
                            CommodityItemView commodityItemView= new CommodityItemView(this);
                            linearLayoutTwo.addView(commodityItemView);
                        }else if("caipu".equals(datatype)){
                            DishItemView dishItemView= new DishItemView(this);
                            linearLayoutTwo.addView(dishItemView);
                        }
                    }
                }
            }
        }
        detailAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus&&!initUiSuccess){
            initUiSuccess=true;
        }
    }

    /**
     * 请求推荐列表
     */
    private void requestRelateData(){
        String url=StringManager.api_getRelated;
        String param= "page="+page+"&pagesize=10";
        ReqEncyptInternet.in().doEncypt(url, param, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object object) {
                if(flag>=ReqInternet.REQ_OK_STRING){
                    ArrayList<Map<String,String>> listMap = StringManager.getListMapByJson(object);
                    int size= listMap.size();
                    for(int i=0;i<size;i++){
                        listMap.get(i).put("datatype",String.valueOf(Type_recommed));
                    }
                    analysRelateData(listMap);
                }else{
                    toastFaildRes(flag,true,object);
                }
            }
        });
    }
    /**
     * 解析文章数据
     * @param ArrayRelate
     */
    private void analysRelateData(@NonNull ArrayList<Map<String,String>> ArrayRelate){
        if(ArrayRelate.isEmpty())return;
        otherListMap.addAll(ArrayRelate);
        detailAdapter.notifyDataSetChanged();
    }
    /**
     * 请求评论列表
     */
    private void requestForumData(){
        String url=StringManager.api_forumList;
        String param= "";
        ReqEncyptInternet.in().doEncypt(url, param, new InternetCallback(this) {
            @Override
            public void loaded(int flag, String url, Object object) {
                if(flag>=ReqInternet.REQ_OK_STRING){

                }else{
                    toastFaildRes(flag,true,object);
                }
            }
        });
    }
}
