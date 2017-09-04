package amodule.dish.video.control;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import amodule.dish.db.UploadDishData;
import xh.basic.tool.UtilString;


/**
 * 清单
 */
public class DishCreateViewControl {
    private int waith=1920;//当前view的宽度
    private int height= 1080;//当前view的默认高度。
    private Context context;
    private Typeface typeFace;
    private String now_path_img=MediaControl.viewPath_cancel;
    private int num_residuum=0;
    private int multiple=1;
    private float scr_multiple;
    public DishCreateViewControl(Context context,int vWaith,int vHeight){
        this.context= context;
        if (new File(MediaControl.path_voide + "/fonts/font.ttf").exists()) {
            typeFace=Typeface.createFromFile(MediaControl.path_voide + "/fonts/font.ttf");
        }
        num_residuum=0;
//        if(vWaith>0){
//            this.waith=vWaith;
//            multiple=waith/1920;
//        }
//        if(vHeight>0)this.height=vHeight;
        DisplayMetrics metric = new DisplayMetrics();
        ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metric);
//            int width = metric.widthPixels;  // 屏幕宽度（像素）
        int heightScr = metric.heightPixels;  // 屏幕高度（像素）
        //处理当前视频差
        scr_multiple= (float) 1920/heightScr;
    }

    /**
     * View生成器
     * @param left
     * @param right
     * @param top
     * @param bottom
     * @param oneTextColor
     * @param oneTextsize
     * @param twoTextColor
     * @param twoTextSize
     * @param numColums
     * @param lineColums
     */
    public View setContentView(int left, int top,int right,  int bottom, String oneTextColor, float oneTextsize,
                           String twoTextColor, float twoTextSize, int numColums, int lineColums,int drawableId,
                               int item_left,int item_top,int item_right,int item_bottom,
                               ArrayList<Map<String,String>> listmaps){
        View viewMedia = LayoutInflater.from(context).inflate(R.layout.media_dish_new, null);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(waith,height);
        viewMedia.setLayoutParams(layoutParams);

        viewMedia.setDrawingCacheEnabled(true);
        RelativeLayout media_dish= (RelativeLayout) viewMedia.findViewById(R.id.media_dish);
        media_dish.setLayoutParams(layoutParams);
        ImageView image_backgroup= (ImageView) viewMedia.findViewById(R.id.image_backgroup);
        image_backgroup.setLayoutParams(layoutParams);
        image_backgroup.setBackgroundResource(drawableId);

        //两种类型
        LinearLayout media_dish_linear= (LinearLayout) viewMedia.findViewById(R.id.media_dish_linear);
        RelativeLayout.LayoutParams layoutParams_dish = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams_dish.setMargins(left,top,right,bottom);
        media_dish_linear.setLayoutParams(layoutParams_dish);
        media_dish_linear.setOrientation(LinearLayout.HORIZONTAL);
        num_residuum=listmaps.size()%numColums;
        int num_temp=num_residuum;
        System.out.println(":num_temp:::"+num_temp+"::listmaps.size():::"+listmaps.size()+"::;numColums::"+numColums);
        int now_line=(listmaps.size()-num_temp)/numColums;
        for(int i=0;i<numColums;i++){
            if(i!=0){
                int height_line=0;
                int tempText= (int) (oneTextsize+4.5);
                if(num_temp>0){height_line= (int) (tempText*(now_line+1)+item_bottom*(now_line+1));}
                else height_line= (int) (tempText*(now_line)+item_bottom*(now_line));
                ImageView imageView = new ImageView(context);
                LinearLayout.LayoutParams layoutParams_image= new LinearLayout.LayoutParams(2, height_line*2);
                layoutParams_image.setMargins(0, (int) oneTextsize,0,0);
                imageView.setLayoutParams(layoutParams_image);
                imageView.setBackgroundResource(R.drawable.bg_dish_media_line);
                media_dish_linear.addView(imageView);
            }
            LinearLayout linearLayout = new LinearLayout(context);
            RelativeLayout.LayoutParams layoutParams_linear= new RelativeLayout.LayoutParams((waith-left-right)/numColums,height-top-bottom);
            linearLayout.setLayoutParams(layoutParams_linear);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            int now_num=now_line;
            if(num_temp>0){
                now_num++;
                num_temp--;
            }
            System.out.println(i+":::now_line:::"+now_line+"::now_num:::"+now_num);
            for(int j=0;j<now_num;j++){
//                if(i*now_line+j>=listmaps.size()){//超出数据break;
//                    break;
//                }
                View view_item=LayoutInflater.from(context).inflate(R.layout.media_dish_item,null);
                int now_Index=0;
                if(numColums==1){now_Index=3;
                }else{now_Index=numColums;}
                RelativeLayout.LayoutParams layoutParams_item = new RelativeLayout.LayoutParams( (waith-left-right)/now_Index, ViewGroup.LayoutParams.WRAP_CONTENT);
                view_item.setLayoutParams(layoutParams_item);
                RelativeLayout dish_item= (RelativeLayout) view_item.findViewById(R.id.dish_item);
                dish_item.setLayoutParams(layoutParams_item);

                LinearLayout text_linear= (LinearLayout) view_item.findViewById(R.id.text_linear);
                RelativeLayout.LayoutParams linear_layoutparams= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                linear_layoutparams.setMargins(item_left,item_top,item_right,item_bottom);
                text_linear.setLayoutParams(linear_layoutparams);

                TextView text_name= (TextView) view_item.findViewById(R.id.text_name);
                if(typeFace!=null)text_name.setTypeface(typeFace);
                if(oneTextsize>0)text_name.setTextSize(oneTextsize);
               if(!TextUtils.isEmpty(oneTextColor)) text_name.setTextColor(Color.parseColor(oneTextColor));
                text_name.setMaxEms(7);

                TextView text_content= (TextView) view_item.findViewById(R.id.text_content);
                if(numColums==1){
                    text_content.setPadding(0,0,0,0);
                }
                if(!TextUtils.isEmpty(twoTextColor))text_content.setTextColor(Color.parseColor(twoTextColor));
                if(twoTextSize>0)text_content.setTextSize(twoTextSize);
                if(typeFace!=null)text_content.setTypeface(typeFace);
                int temp=0;
                if(num_residuum>0&&num_residuum>=i){
                    temp=i;
                }
                String name= listmaps.get(i*now_line+temp+j).get("name");
                if(name.length()>7){
                    name=name.substring(0,7)+"...";
                }
                String number = listmaps.get(i*now_line+temp+j).get("number");
//                if(number.length()>5){
//                    number=number.substring(0,5)+"...";
//                }
                text_name.setText(name);
                text_content.setText(number);
                linearLayout.addView(view_item);
            }
            media_dish_linear.addView(linearLayout);
        }
//        setViewToBitmap(viewMedia,"imgdish12.png");
        return viewMedia;
    }

    /**
     *
     *
     */
    public View setContent(UploadDishData uploadDishData){
        //根据数据生成对应的生成器
        ArrayList<Map<String,String>> listmaps= new ArrayList<>();
        listmaps.addAll(UtilString.getListMapByJson(uploadDishData.getFood()));
        listmaps.addAll(UtilString.getListMapByJson(uploadDishData.getBurden()));
//        for(int i=0; i<22;i++){
//            Map<String,String> map = new HashMap<>();
//            map.put("name","测试数测试数据"+i);
//            map.put("number","媳妇一定的");
//            listmaps.add(map);
//        }
        int size= listmaps.size();

        //当前样式
        //0-14,一种样式结构，行数7，
        //15-30 二种样式，行数10
        //逻辑放弃
//        if(size<=14){
//            if(size<=7){
//               return setContentView(0,264,0,0,"#000000",16*scr_multiple,"#666666",16*scr_multiple,
//                        1,7,R.drawable.media_dish_backgroup,
//                        0,0,0,20,listmaps);
//            }else{
//               return setContentView(116,264,116,0,"#000000",16*scr_multiple,"#666666",16*scr_multiple,
//                        2,7,R.drawable.media_dish_backgroup,
//                       72,0,4,20,listmaps);
//            }
//        }else{
//            if(size<=20){
//               return setContentView(0,264,0,0,"#000000",14*scr_multiple,"#666666",14*scr_multiple,
//                        2,10,R.drawable.media_dish_backgroup,
//                       106,0,0,14,listmaps);
//            }else {
//             return setContentView(0,264,0,0,"#000000",14*scr_multiple,"#666666",14*scr_multiple,
//                        3,10,R.drawable.media_dish_backgroup,
//                     32,0,10,14,listmaps);
//            }
//        }
        return null;
    }

    /**
     * view转图片，存储到本地
     * @param view_one
     * @param imgName----------path_img
     */
    private void setViewToBitmap(View view_one,String imgName){
        view_one.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view_one.layout(0, 0, view_one.getMeasuredWidth(), view_one.getMeasuredHeight());
        //下面的代码和上面的是一样的
//        Bitmap bitmap = getViewBitmap(view_one);
        // 把一个View转换成图片
        File file = new File(now_path_img);
        if (!file.exists()) file.mkdirs();
        File files = new File(now_path_img + "/"+imgName);
        boolean imge = false;
        try {
//            imge = bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(files));
        } catch (Exception e) {
        }
        if (imge) {
//            Tools.showToast(context, "死去看图片");
//            context.startActivity(new Intent(context, TestActivity.class));
        }
    }
}

