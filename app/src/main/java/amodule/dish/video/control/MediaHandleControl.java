package amodule.dish.video.control;

/**
 * 视频裁剪操作具体控制类
 * 大图片转视频20%
 * 食材转视频20%
 * 裁剪视频 15%
 * 视频转ts 15%
 * ts合成视频 15%
 * 视频加bmg 15%
 */
public class MediaHandleControl {
//    private String time;//视频时间控制
//    private MediaVideoEditor editor = new MediaVideoEditor();
//    private Context context;
//    private UploadDishData uploadDishData;
//    private boolean isStop=false;//是否暂停 true暂停
//    private Typeface typeFace;
//    private String now_path;
//    private String  now_path_img;//图片路径
//    private String  now_path_paper;//裁剪视频路径
//    private String  now_path_paper_text;//裁剪视频路径
//    private String  now_path_ts;//转码视频路径
//    private String  now_path_sucess;//合成视频路径
//    private String  now_path_audio;//音频数据
//    private String  now_path_pcm;//音频pcm数据
//    private String  now_path_sucess_ts;//合成视频路径
//    private String now_path_code;//片头片尾处理后的数据存储位置
//    public static int codeRate=125*1000*100;
//    private ArrayList<SpeechBean> listBeans;//合成音频数据
//    private String path;
//    private float audioHandlerTime;
//    private float vtime;
//    private float scr_multiple;
//    private String imagePath="";//处理大图
//    public MediaHandleControl(Context context, UploadDishData uploadDishData) {
//        this.context = context;
//        this.uploadDishData= uploadDishData;
//        init();
//    }
//
//    /**
//     * 测试：初始化
//     * @param context
//     */
//    public MediaHandleControl(Context context){
//        this.context = context;
//    }
//
//    /**
//     * 测试调用
//     */
//    public void audio() {
//        try{
////        SpeechHandlerControl speechHandlerControl22 = new SpeechHandlerControl("/sdcard/texts.pcm", context);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
////                editor.executeAudioCutOut(path_voide+"/xiangha_bgm.mp3",path_voide+"/audio_cut.mp3",0,allCutTime);
////                now_path_img=viewPath_cancel;
////                DishCreateViewControl dishCreateViewControl = new DishCreateViewControl(context,0,0);
////                setViewToBitmap(dishCreateViewControl.setContent(uploadDishData), "imgdish.png");
////                setViewToBitmap(dishCreateViewControl.setContent(null), "imgtemp.png");
////                String[] toArray = new String[]{viewPath_cancel+"3/path_audio"+"/audio_start.mp3",viewPath_cancel+"3/path_audio"+"/audio_cut.mp3"};
////                int state=editor.executemp3(toArray,viewPath_cancel+"3/path_audio"+"/audio.mp3");
////                int state_1=editor.executemp3(viewPath_cancel+"3/path_audio"+"/audio_start.mp3",viewPath_cancel+"3/path_audio"+"/audio_cut.mp3",viewPath_cancel+"3/path_audio"+"/audio_1.mp3");
////                editor.concatMp3("/sdcard/a2.mp3","/sdcard/a6.mp3","/sdcard/c3.mp3");
////                Log.i(state_1+"::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::"+state);
////                successHandlerAudio();
////                handlerBgmAudio();
////                executeCrop();
////                editor.executePcmEncodeAac("/sdcard/step_2.pcm",8000,2,"/sdcard/step_new.aac");
////                editor.executeAudioDelayMix("/sdcard/audio_start.aac","/sdcard/step_new.aac",10,10,"/sdcard/audio_cut_new1.aac");
////                editor.executeAudioCutOut(path_voide + "/xiangha_bgm.aac", "/sdcard/audio_cut_new.aac", 0, 37);
////                editor.executeAudioCutOut(path_voide + "/xiangha_bgm.aac", path_voide+"/_new.aac", 0, 37);
////                handlerAudioShade();
////                editor.executeAudioAR("/sdcard/audio_start.aac","/sdcard/step_new.aac");
////                editor.executeAudioDelayMix("/sdcard/xiangha_bgm.aac", "/sdcard/step_new.aac", 10*1000, 10*1000, "/sdcard/audio_new_0.aac");
////                editor.executeAudioDelayMix("/sdcard/audio_new_0.aac", "/sdcard/step_new.aac", 30*1000, 30*1000, "/sdcard/audio_new_1.aac");
////                editor.executeAudioDelayMix("/sdcard/audio_new_1.aac", "/sdcard/step_new.aac", 50*1000, 50*1000, "/sdcard/audio_new_2.aac");
////                editor.executeAudioDelayMix("/sdcard/audio_new_2.aac", "/sdcard/step_new.aac", 70*1000, 70*1000, "/sdcard/audio_new_3.aac");
//                    //图片标准是：1920*1080
//                    DishCreateViewControl dishCreateViewControl = new DishCreateViewControl(context,0,0);
//                    dishCreateViewControl.setContent(null);
//                }
//            }).start();
//        }catch (Exception e){
//
//        }
//
//    }
//    /**
//     * 初始化数据
//     */
//    private void init() {
//        time = String.valueOf(System.currentTimeMillis());
//        now_path= viewPath_cancel+uploadDishData.getId()+"/";
//        now_path_img=now_path+MediaControl.path_img;
//        now_path_paper=now_path+MediaControl.path_paper;
//        now_path_paper_text=now_path+MediaControl.path_paper_text;
//        now_path_ts= now_path+MediaControl.path_ts;
//        now_path_sucess=now_path+MediaControl.path_sucess;
//        now_path_sucess_ts=now_path+MediaControl.path_sucess_ts;
//        now_path_audio=now_path+MediaControl.path_audio;
//        now_path_pcm=now_path+MediaControl.path_pcm;
//        now_path_code=now_path+MediaControl.path_code;
//        //删除数据
//        delAllMediaHandlerData(uploadDishData.getId());
//// 应用字体
//        if (new File(MediaControl.path_voide + "/fonts/font.ttf").exists()) {
//            typeFace=Typeface.createFromFile(MediaControl.path_voide + "/fonts/font.ttf");
//        }
//        DisplayMetrics metric = new DisplayMetrics();
//        ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metric);
////            int width = metric.widthPixels;  // 屏幕宽度（像素）
//        int heightScr = metric.heightPixels;  // 屏幕高度（像素）
//        //处理当前视频差
//        scr_multiple= (float) 1920/heightScr;
//        //处理大图
//        handlerCover();
//    }
//
//    /**
//     * 开始视频的处理
//     */
//    public void startVideo() {
//        System.gc();//通知系统去回收内存
////        startSpeechHandler();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                //大图
//                if(!isStop)startOneViewToVideo();
//                //食材
//                if(!isStop)setViewToBitmapDish();
//                //裁剪视频
//                if(!isStop)startPaperMedia();
//                if(!isStop)startPaperMediaText();
//                //视频转ts
//                if(!isStop)startVideoToTs();
//                //合成步骤
//                if(!isStop)startCompound();
//                //视频绿线变成黑线
//                if(!isStop)videoShowBack();
////               //背景音乐处理
//                if(!isStop)audioHandler();
//                //合成数据
////                if(!isStop)speechHandlerAudio();
//                //片头片尾，合成视频转ts
//                if(!isStop)startVideoSuccessToTs();
//                //合成
//                if(!isStop)startSuccessCompoundVideo();
//                //加背景音樂
//                if(!isStop)successHandler();
//
////                if(!isStop)successHandlerAudio();
//            }
//        }).start();
//    }
//
//    /**
//     * 处理合成音频的数据
//     */
//    private void startSpeechHandler(){
//        try {
//            //创建文件路径
//            File file = new File(now_path_pcm);
//            if (!file.exists()) {
//                file.mkdirs();
//            }
//            listBeans = new ArrayList<>();
//            ArrayList<Map<String, String>> lists = UtilString.getListMapByJson(uploadDishData.getMakes());
//            int size = lists.size();
//            //标题
//            SpeechBean speechTitle = new SpeechBean();
//            speechTitle.setText(uploadDishData.getName());
//
//            JSONObject jsonObject_title = new JSONObject(lists.get(size - 1).get("videoInfo"));
//            MediaPaperBean bean_title = new MediaPaperBean();
//            bean_title.jsonToBean(jsonObject_title);
//            float oneTime = bean_title.getCutTime() > 3 ? 3 : bean_title.getCutTime();
//            speechTitle.setvCutTime(oneTime);//时间要根据真实的时间来确定
//            speechTitle.setPath(now_path_pcm+"/title.pcm");
//            speechTitle.setDif_time(100);
//            listBeans.add(speechTitle);
//            //用料
//            SpeechBean speechMaterial = new SpeechBean();
//            speechMaterial.setText("用料");
//            speechMaterial.setvCutTime(3);//时间要根据真实的时间来确定
//            speechMaterial.setPath(now_path_pcm+"/material.pcm");
//            listBeans.add(speechMaterial);
//            //做法
//            SpeechBean speechMethod = new SpeechBean();
//            speechMethod.setText("做法");
//            speechMethod.setvCutTime(3);//时间要根据真实的时间来确定
//            speechMethod.setPath(now_path_pcm+"/method.pcm");
//            speechMethod.setDif_time(-500);
//            listBeans.add(speechMethod);
//
//            //对步骤的处理
//            for (int i = 0; i < size; i++) {
//                SpeechBean speechBean = new SpeechBean();
//                speechBean.setText(lists.get(i).get("makesInfo"));
//                JSONObject jsonObject = new JSONObject(lists.get(i).get("videoInfo"));
//                MediaPaperBean bean = new MediaPaperBean();
//                bean.jsonToBean(jsonObject);
//                speechBean.setvCutTime(bean.getCutTime());
//                speechBean.setPath(now_path_pcm+"/step_"+i+".pcm");
//                listBeans.add(speechBean);
//            }
//            SpeechHandlerControl speechHandlerControl = new SpeechHandlerControl(listBeans,context);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//    /**
//     * 第一步:
//     * (1)开始大图转视频,因为需求开子线程读取文件，必须在主线程中执行。
//     */
//    public void startOneViewToVideo(){
//        try {
//            //获取底部背景宽高
//            ArrayList<Map<String, String>> lists = UtilString.getListMapByJson(uploadDishData.getMakes());
//            int size = lists.size();
//            JSONObject jsonObject = new JSONObject(lists.get(size - 1).get("videoInfo"));
//            MediaPaperBean bean = new MediaPaperBean();
//            bean.jsonToBean(jsonObject);
//            //获取当前视频
//            MediaInfo mediaInfo = new MediaInfo(bean.getPath());
//            mediaInfo.prepare();
//            //图片标准是：1920*1080
//            int height=mediaInfo.vHeight;
//            int waith=mediaInfo.vWidth;
//
//            View view_one = LayoutInflater.from(context).inflate(R.layout.media_one, null);
//            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(waith, height);
//            view_one.setLayoutParams(layoutParams);
//            view_one.setDrawingCacheEnabled(true);
//            RelativeLayout layout = (RelativeLayout) view_one.findViewById(R.id.media_img);
//            layout.setLayoutParams(layoutParams);
//            RelativeLayout media_middle_rela= (RelativeLayout) view_one.findViewById(R.id.media_middle_rela);
//            media_middle_rela.setPadding(16,16,16,16);
//            LinearLayout media_middle_linear= (LinearLayout) view_one.findViewById(R.id.media_middle_linear);
//            media_middle_linear.setPadding(96,30,96,36);
//            TextView media_middle_title= (TextView) view_one.findViewById(R.id.media_middle_title);
//            TextView media_middle_name= (TextView) view_one.findViewById(R.id.media_middle_name);
//            String name= uploadDishData.getName();
//            if(name.contains("#")){
//                int start_index= name.indexOf("#");
//                int end_index= name.lastIndexOf("#");
//                if(start_index!=end_index){
//                    name=name.replace(name.substring(start_index,end_index+1),"");
//                }else{//当前就一个#。
//                    name=name.replace("#","");
//                }
//            }
//            media_middle_title.setText(name);
//            if(typeFace!=null)media_middle_title.setTypeface(typeFace);
//            media_middle_title.setTextSize(44*scr_multiple);
//            TextPaint tp = media_middle_title.getPaint();
//            tp.setFakeBoldText(true);
//            media_middle_name.setText("香哈・"+LoginManager.userInfo.get("nickName"));
//
//            if(typeFace!=null)media_middle_name.setTypeface(typeFace);
//            media_middle_name.setTextSize(18*scr_multiple);
//
//            setViewToBitmap(view_one, "imgOne.png");
//
//            editor.executeDeleteAudio(bean.getPath(),now_path_img + "/one_no.mp4");
//            if(paperPathState(now_path_img + "/imgOne.png",now_path_paper_text)) {
//                //因为ffmpeg命令不能精确裁剪，减小误差使用该东西
//                if(bean.getCutTime()>=3){
//                    editor.executeVideoCutOverlay(now_path_img + "/one_no.mp4", mediaInfo.aCodecName, now_path_img + "/imgOne.png", (float) (bean.getEndTime()-3), 3f, 0, 0, now_path_paper_text + "/one.mp4", codeRate);
//                }else{
//                    editor.executeVideoCutOverlay(now_path_img + "/one_no.mp4", mediaInfo.aCodecName, now_path_img + "/imgOne.png", bean.getStartTime(), bean.getCutTime(), 0, 0, now_path_paper_text + "/one.mp4", codeRate);
//                }
//                FileManager.delDirectoryOrFile(now_path_img + "/imgOne.png");
//                if(paperPathState(now_path_paper_text + "/one.mp4",now_path_paper)){
//                    copy(now_path_paper_text + "/one.mp4",now_path_paper ,  "one.mp4");
//                }
//                FileManager.delDirectoryOrFile(now_path_img + "/one_no.mp4");
//            }
//            handlerDataCallBack.setCallBack(30);
//        }catch (Exception e){
//            e.printStackTrace();
//            handlerDataCallBack.CallBackError();
//        }
//    }
//
//    /**
//     * 第一步：
//     * (2)生成食材页面
//     */
//    private void setViewToBitmapDish(){
//        //获取底部背景宽高
//        try {
//            ArrayList<Map<String, String>> lists = UtilString.getListMapByJson(uploadDishData.getMakes());
//            int size = lists.size();
//            JSONObject jsonObject = new JSONObject(lists.get(size - 1).get("videoInfo"));
//            MediaPaperBean bean = new MediaPaperBean();
//            bean.jsonToBean(jsonObject);
//            //获取当前视频
//            MediaInfo mediaInfo = new MediaInfo(bean.getPath());
//            mediaInfo.prepare();
//            DishCreateViewControl dishCreateViewControl = new DishCreateViewControl(context,mediaInfo.vWidth,mediaInfo.vHeight);
//            setViewToBitmap(dishCreateViewControl.setContent(uploadDishData), "imgdish.png");
//            editor.executeDeleteAudio(bean.getPath(),now_path_img + "/"+time + "_no.mp4");
//            if (paperPathState(now_path_img + "/imgdish.png", now_path_paper_text)) {
//                editor.executeVideoCutOverlay(now_path_img + "/"+time + "_no.mp4", mediaInfo.aCodecName, now_path_img + "/imgdish.png", 1, 5f, 0, 0, now_path_paper_text + "/dish.mp4", codeRate);
//                if(paperPathState(now_path_paper_text + "/dish.mp4",now_path_paper)){
//                    copy(now_path_paper_text + "/dish.mp4",now_path_paper ,  "dish.mp4");
//                }
//            }
//            handlerDataCallBack.setCallBack(70);
//        }catch (Exception e){
//            e.printStackTrace();
//            handlerDataCallBack.CallBackError();
//        }
//    }
//    /**
//     * 第二步：
//     * (1)裁剪视频
//     */
//    public void startPaperMedia() {
//        try {
//            Log.i("zhangyujian","开始裁剪");
//            ArrayList<Map<String, String>> lists = UtilString.getListMapByJson(uploadDishData.getMakes());
//            int size = lists.size();
//            Log.i("zhangyujian","size:::"+size);
//            JSONArray jsonArray = new JSONArray();
//            for (int i = 0; i < size; i++) {
//                //数据处理
//                JSONObject jsonObject_data= new JSONObject();
//                jsonObject_data.put("makesStep",lists.get(i).get("makesStep"));
//                jsonObject_data.put("makesInfo",lists.get(i).get("makesInfo"));
//
//                //处理裁剪
//                JSONObject jsonObject = new JSONObject(lists.get(i).get("videoInfo"));
//                MediaPaperBean bean = new MediaPaperBean();
//                bean.jsonToBean(jsonObject);
//                Log.i("zhangyujian","bean.getPath()::"+bean.getPath());
//
//                DecimalFormat df   = new DecimalFormat("######0.0");
//                editor.executeDeleteAudio(bean.getPath(),now_path_img + "/"+time + "_parper_" + i + ".mp4");
//                //第二次执行，避免第一次文件未生成-----------------------------
//                Log.i("zhangyujian", i+"::当前文件paper是否存在::"+new File(now_path_img + "/"+time + "_parper_" + i + ".mp4").exists());
//                if(!new File(now_path_img + "/"+time + "_parper_" + i + ".mp4").exists()){
//                    editor.executeDeleteAudio(bean.getPath(),now_path_img + "/"+time + "_parper_" + i + ".mp4");
//                }
//                if (bean.getCutTime() >= bean.getAllTime()) {//未进行裁剪---拷贝文件
//                    if(paperPathState(now_path_img + "/"+time + "_parper_" + i + ".mp4", now_path_paper)){
////                        editor.executeAddWaterMark(bean.getPath(),now_path_img + "/text_"+i+".png",0,0,now_path_paper + "/" + time + "_" + i + ".mp4",codeRate);
//                        copy(now_path_img + "/"+time + "_parper_" + i + ".mp4",now_path_paper ,  time + "_" + i + ".mp4");
//                        bean.setCutPath(now_path_paper + "/" + time + "_" + i + ".mp4");
//                    }
//                } else {//进行裁剪
//                    if (paperPathState(now_path_img + "/"+time + "_parper_" + i + ".mp4", now_path_paper)) {
//                        Log.i("zhangyujian","开始裁剪：：：" + i);
////                        editor.executeVideoCutOverlay(bean.getPath(), mediaInfo.aCodecName, now_path_img + "/text_"+i+".png", bean.getStartTime(), bean.getCutTime(), 0, 0, now_path_paper + "/" + time + "_" + i + ".mp4", codeRate);
//                        editor.executeVideoCutOut(now_path_img + "/"+time + "_parper_" + i + ".mp4",now_path_paper + "/" + time + "_" + i + ".mp4", bean.getStartTime(), bean.getCutTime());
//                        bean.setCutPath(now_path_paper + "/" + time + "_" + i + ".mp4");
//                    }
//                }
//                //第二次执行，避免第一次文件未生成---------------------------------------------
//                Log.i("zhangyujian", i+"::当前文件paper是否存在::"+new File(now_path_paper + "/" + time + "_" + i + ".mp4").exists());
//                if(!new File(now_path_paper + "/" + time + "_" + i + ".mp4").exists()){
//                    if (bean.getCutTime() >= bean.getAllTime()) {//未进行裁剪---拷贝文件
//                        if(paperPathState(now_path_img + "/"+time + "_parper_" + i + ".mp4", now_path_paper)){
////                        editor.executeAddWaterMark(bean.getPath(),now_path_img + "/text_"+i+".png",0,0,now_path_paper + "/" + time + "_" + i + ".mp4",codeRate);
//                            copy(now_path_img + "/"+time + "_parper_" + i + ".mp4",now_path_paper ,  time + "_" + i + ".mp4");
//                            bean.setCutPath(now_path_paper + "/" + time + "_" + i + ".mp4");
//                        }
//                    } else {//进行裁剪
//                        if (paperPathState(now_path_img + "/"+time + "_parper_" + i + ".mp4", now_path_paper)) {
//                            Log.i("zhangyujian","开始裁剪：：：" + i);
////                        editor.executeVideoCutOverlay(bean.getPath(), mediaInfo.aCodecName, now_path_img + "/text_"+i+".png", bean.getStartTime(), bean.getCutTime(), 0, 0, now_path_paper + "/" + time + "_" + i + ".mp4", codeRate);
//                            editor.executeVideoCutOut(now_path_img + "/"+time + "_parper_" + i + ".mp4",now_path_paper + "/" + time + "_" + i + ".mp4", bean.getStartTime(), bean.getCutTime());
//                            bean.setCutPath(now_path_paper + "/" + time + "_" + i + ".mp4");
//                        }
//                    }
//                }
//                FileManager.delDirectoryOrFile(now_path_img + "/"+time + "_parper_" + i + ".mp4");
//                jsonObject_data.put("videoInfo",bean.beanToJson());
//                jsonArray.put(jsonObject_data);
//            }
//            Log.i("zhangyujian",uploadDishData.getId()+"::步骤数据处理::"+jsonArray.toString());
//            UploadDishSqlite uploadDishSqlite= new UploadDishSqlite(context);
//            uploadDishSqlite.update(uploadDishData.getId(),UploadDishData.ds_makes,jsonArray.toString());
//            handlerDataCallBack.setCallBack(95);
//        }catch (Exception e){
//            handlerDataCallBack.CallBackError();
//        }
//
//    }
//
//    /**第二步
//     * (2)对裁剪页面加字幕
//     */
//    private void startPaperMediaText(){
//        try {
//            Log.i("zhangyujian","裁剪加字幕");
//            ArrayList<Map<String, String>> lists = UtilString.getListMapByJson(uploadDishData.getMakes());
//            int size = lists.size();
//            for (int i = 0; i < size; i++) {
//
//                //处理裁剪
//                JSONObject jsonObject = new JSONObject(lists.get(i).get("videoInfo"));
//                MediaPaperBean bean = new MediaPaperBean();
//                bean.jsonToBean(jsonObject);
//                //获取当前视频
//                MediaInfo mediaInfo = new MediaInfo(now_path_paper + "/" + time + "_" + i + ".mp4");
//                mediaInfo.prepare();
//                //图片标准是：1920*1080
//                int height=mediaInfo.vHeight;
//                int waith=mediaInfo.vWidth;
////                int heightScr= DeviceUtils.getScreenHeight(context);
//
//                //字幕处理
//                View view_text= LayoutInflater.from(context).inflate(R.layout.media_text,null);
//                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(waith,height);
//                view_text.setLayoutParams(layoutParams);
//                view_text.setDrawingCacheEnabled(true);
//                RelativeLayout text_linear= (RelativeLayout) view_text.findViewById(R.id.text_linear);
//                text_linear.setLayoutParams(layoutParams);
//                text_linear.setBackgroundColor(context.getResources().getColor(R.color.transparent));
//
//                LinearLayout linear_text= (LinearLayout) view_text.findViewById(R.id.linear_text);
//                RelativeLayout.LayoutParams layoutParams_linear = new RelativeLayout.LayoutParams(waith, ViewGroup.LayoutParams.WRAP_CONTENT);
//                layoutParams_linear.addRule(RelativeLayout.CENTER_HORIZONTAL);
//                layoutParams_linear.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//                linear_text.setLayoutParams(layoutParams_linear);
//
//                TextView text_1= (TextView) view_text.findViewById(R.id.text_1);
//                TextView text_2= (TextView) view_text.findViewById(R.id.text_2);
//                LinearLayout.LayoutParams layoutParams_text= new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
////                text.setTextSize(Tools.getDimenSp(context,R.dimen.dp_27)*multiple);
//                layoutParams_text.gravity=Gravity.CENTER_HORIZONTAL;
//                text_1.setLayoutParams(layoutParams_text);
//                text_2.setLayoutParams(layoutParams_text);
//                String info=lists.get(i).get("makesInfo");
//                if(info.length()<=15){
//                    text_1.setVisibility(View.INVISIBLE);
//                    text_2.setVisibility(View.VISIBLE);
//                    text_2.setText(info);
//                }else if(info.length()==16){
//                    text_1.setVisibility(View.VISIBLE);
//                    text_2.setVisibility(View.VISIBLE);
//                    text_1.setText(info.substring(0,6));
//                    text_2.setText(info.substring(6,info.length()));
//                }else if(info.length()==17){
//                    text_1.setVisibility(View.VISIBLE);
//                    text_2.setVisibility(View.VISIBLE);
//                    text_1.setText(info.substring(0,7));
//                    text_2.setText(info.substring(7,info.length()));
//                }else{
//                    int num= (int) (info.length()*0.4);
//                    text_1.setVisibility(View.VISIBLE);
//                    text_2.setVisibility(View.VISIBLE);
//                    text_1.setText(info.substring(0,num));
//                    text_2.setText(info.substring(num,info.length()));
//                }
////                text.setText(lists.get(i).get("makesInfo"));
//                text_1.setPadding(0,0,0, (int) (2*scr_multiple));
//                text_2.setPadding(0,0,0, (int) (40*scr_multiple));
////
//                text_1.setTextSize(22*scr_multiple);
//                text_1.setMaxEms(15);
////                text_1.setSingleLine();
//                if(typeFace!=null)text_1.setTypeface(typeFace);
//                text_1.setTextColor(Color.parseColor("#fffffe"));
//                text_1.setGravity(Gravity.CENTER_HORIZONTAL);
////
//                text_2.setTextSize(22*scr_multiple);
//                text_2.setMaxEms(15);
////                text_2.setSingleLine();
//                if(typeFace!=null)text_2.setTypeface(typeFace);
//                text_2.setTextColor(Color.parseColor("#fffffe"));
//                text_2.setGravity(Gravity.CENTER_HORIZONTAL);
//
//                setViewToBitmap(view_text, "text_"+i+".png");
//                editor.executeDeleteAudio(bean.getPath(),now_path_img + "/"+time + "_text_" + i + ".mp4");
//                //第二次执行，避免第一次文件未生成------------------
//                Log.i("zhangyujian", i+"::当前文件webp是否存在::"+new File(now_path_img + "/"+time + "_text_" + i + ".mp4").exists());
//                if(!new File(now_path_img + "/"+time + "_text_" + i + ".mp4").exists()){//文件未生成，再进行一次操作
//                    editor.executeDeleteAudio(bean.getPath(),now_path_img + "/"+time + "_text_" + i + ".mp4");
//                }
//                if(bean.getCutTime() >= bean.getAllTime()){
//                    if(paperPathState(now_path_img + "/"+time + "_text_" + i + ".mp4",now_path_paper_text)){
//                        editor.executeVideoCutOverlay(now_path_img + "/"+time + "_text_" + i + ".mp4",mediaInfo.aCodecName,now_path_img + "/text_"+i+".png",
//                                0,mediaInfo.vDuration,0,0,now_path_paper_text + "/" + time + "_" + i + ".mp4",codeRate);
//                    }
//                }else{
//                    if(paperPathState(now_path_img + "/"+time + "_text_" + i + ".mp4",now_path_paper_text)){
//                        editor.executeVideoCutOverlay(now_path_img + "/"+time + "_text_" + i + ".mp4",mediaInfo.aCodecName,now_path_img + "/text_"+i+".png",
//                                bean.getStartTime(),bean.getCutTime(),0,0,now_path_paper_text + "/" + time + "_" + i + ".mp4",codeRate);
//                    }
//                }
//                //第二次执行，避免第一次文件未生成------------------
//                Log.i("zhangyujian", i+"::当前文件webp是否存在::"+new File(now_path_img + "/"+time + "_text_" + i + ".mp4").exists());
//                if(!new File(now_path_img + "/"+time + "_text_" + i + ".mp4").exists()){//文件未生成，再进行一次操作
//                    if(bean.getCutTime() >= bean.getAllTime()){
//                        if(paperPathState(now_path_img + "/"+time + "_text_" + i + ".mp4",now_path_paper_text)){
//                            editor.executeVideoCutOverlay(now_path_img + "/"+time + "_text_" + i + ".mp4",mediaInfo.aCodecName,now_path_img + "/text_"+i+".png",
//                                    0,mediaInfo.vDuration,0,0,now_path_paper_text + "/" + time + "_" + i + ".mp4",codeRate);
//                        }
//                    }else{
//                        if(paperPathState(now_path_img + "/"+time + "_text_" + i + ".mp4",now_path_paper_text)){
//                            editor.executeVideoCutOverlay(now_path_img + "/"+time + "_text_" + i + ".mp4",mediaInfo.aCodecName,now_path_img + "/text_"+i+".png",
//                                    bean.getStartTime(),bean.getCutTime(),0,0,now_path_paper_text + "/" + time + "_" + i + ".mp4",codeRate);
//                        }
//                    }
//                }
//                FileManager.delDirectoryOrFile(now_path_img + "/"+time + "_text_" + i + ".mp4");
//            }
//            handlerDataCallBack.setCallBack(96);
//        }catch (Exception e){
//            e.printStackTrace();
//            handlerDataCallBack.CallBackError();
//        }
//    }
//
//
//    /**第三步：
//     * 视频转ts格式,path_paper转视频
//     */
//    private void startVideoToTs(){
//        try {
//            Log.i("zhangyujian","开始转ts");
//            File file = new File(now_path_paper_text);
//            File[] tempList = file.listFiles();
//            for (int i = 0; i < tempList.length; i++) {
//                if (tempList[i].isFile()) {
//                    if (paperPathState(tempList[i].getAbsolutePath(), now_path_ts)) {
//                        Log.i("zhangyujian","开始转ts:::" + i);
//                        editor.executeConvertMp4toTs(tempList[i].getAbsolutePath(), now_path_ts + "/" + time + "_" + i + ".ts");
//                    }
//                }
//            }
//            handlerDataCallBack.setCallBack(97);
//        }catch (Exception e){
//            e.printStackTrace();
//            handlerDataCallBack.CallBackError();
//        }
//    }
//    /**第四步：
//     * 设置开启合成
//     */
//    public void startCompound() {
//        try {
//            Log.i("zhangyujian","开始合成");
//            File file = new File(now_path_ts);
//            File[] tempList = file.listFiles();
//            ArrayList<String> lists = new ArrayList<>();
//            for (int i = 0; i < tempList.length; i++) {
//                if (tempList[i].isFile()) {
//                    if (paperPathState(tempList[i].getAbsolutePath(), now_path_sucess)) {
//                        lists.add(tempList[i].getAbsolutePath());
//                    }
//                }
//            }
//            String[] tss = (String[]) lists.toArray(new String[lists.size()]);
//            editor.executeConvertTsToMp4(tss, now_path_sucess + "/" + time + "_no.mp4");
//            handlerDataCallBack.setCallBack(98);
//            //删除文件
////            FileManager.delDirectoryOrFile(now_path_ts);
////            FileManager.delDirectoryOrFile(now_path_paper_text);
//        }catch (Exception e){
//            handlerDataCallBack.CallBackError();
//        }
//    }
//
//    /**
//     * 黑图片处理
//     */
//    private void  videoShowBack(){
//        MediaInfo mediaInfo = new MediaInfo(now_path_sucess + "/" + time + "_no.mp4");
//        mediaInfo.prepare();
//        //图片标准是：1920*1080
//        int height=mediaInfo.vHeight;
//        int waith=mediaInfo.vWidth;
//        View view_back= LayoutInflater.from(context).inflate(R.layout.media_back,null);
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(waith,height);
//        view_back.setLayoutParams(layoutParams);
//        view_back.setDrawingCacheEnabled(true);
//        RelativeLayout text_linear= (RelativeLayout) view_back.findViewById(R.id.text_linear);
//        text_linear.setLayoutParams(layoutParams);
//        text_linear.setBackgroundColor(context.getResources().getColor(R.color.transparent));
//
//        ImageView image_back= (ImageView) view_back.findViewById(R.id.image_back);
//        RelativeLayout.LayoutParams layoutParams_back= new RelativeLayout.LayoutParams(waith, 20);
//        layoutParams_back.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        image_back.setLayoutParams(layoutParams_back);
//        image_back.setBackgroundColor(Color.parseColor("#000000"));
//        setViewToBitmap(view_back, "success.png");
//        if(paperPathState(now_path_sucess + "/" + time + "_no.mp4",now_path_sucess)){
//            editor.executeVideoCutOverlay(now_path_sucess + "/" + time + "_no.mp4",mediaInfo.aCodecName,now_path_img + "/success.png",
//                    0,mediaInfo.vDuration,0,0,now_path_sucess + "/" + time + ".mp4",codeRate);
//        }
//
//    }
//
//    /**
//     * 对音频进行统一处理
//     *  1、先合成一样包含片尾时间的背景音乐
//     *  2、对背景进行渐变、
//     *  3、加标题，加用料，加做法，加步骤，加片尾。
//     *  4、只有片头背景音乐进行添加
//     */
//    private void audioHandler(){
//        //获取要求的背景音乐
//        handlerAudioTime();
//        //背景音乐进行渐变
//        handlerAudioShade();
//        //背景音乐加声音数据
//        addDataToAudio();
//        //对背景音乐进行合成
//        handlerAudioMerge();
//    }
//
//    /**
//     * 获取标准时间的背景音乐
//     */
//    private void handlerAudioTime(){
//        try {
//            //步骤
//            MediaInfo mediaInfo = new MediaInfo(now_path_sucess + "/" + time + ".mp4");
//            mediaInfo.prepare();
//            vtime = mediaInfo.vDuration;
//            //片尾
//            if (paperPathState(path_voide + "/xiangha_end.mp4", now_path_audio))
//                editor.executeDeleteVideo(path_voide + "/xiangha_end.mp4", now_path_audio+"/audio_end.aac");
//            MediaPlayer mediaPlayer = new MediaPlayer();
//            mediaPlayer.setDataSource(context, Uri.parse(now_path_audio+"/audio_end.aac"));
//            mediaPlayer.prepare();
//            float endtime=mediaPlayer.getDuration()/1000;
//            audioHandlerTime=vtime+endtime;
//            Log.i("zhangyujian", "时常：" + audioHandlerTime);
//            if (paperPathState(path_voide + "/xiangha_bgm.aac", now_path_audio)) {
//                editor.executeAudioCutOut(path_voide + "/xiangha_bgm.aac", now_path_audio+"/audio_cut_new.aac", 0, audioHandlerTime);
//            }
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//
//    }
//
//    /**
//     * 背景音乐进行渐变
//     */
//    private void handlerAudioShade(){
//        try {
//            //  渐变音量
//            if (paperPathState(now_path_audio+"/audio_cut_new.aac", now_path_audio))
//                editor.executeAudioVolume(now_path_audio+"/audio_cut_new.aac", audioHandlerTime- 9, now_path_audio+"/audio_cut_shade.aac");
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 背景音乐加声音数据
//     */
//    private void addDataToAudio(){
//        //合成标题，用料，做法
//        try {
//            path = now_path_audio+"/audio_cut_shade.aac";
//            //先这样写，要对增加数据严谨性。
////            ArrayList<Map<String, String>> lists = UtilString.getListMapByJson(uploadDishData.getMakes());
////            int size = lists.size();
////            JSONObject jsonObject = new JSONObject(lists.get(size - 1).get("videoInfo"));
////            MediaPaperBean bean = new MediaPaperBean();
////            bean.jsonToBean(jsonObject);
////            float oneTime = bean.getCutTime() > 3 ? 3 : bean.getCutTime();
////            int indexTime = (int) (3000 + oneTime * 1000);
////            int startTime=0;
////            //对标题，用料，做法进行合成
////            for(int i=0;i<3;i++){
////                editor.executePcmEncodeAac(listBeans.get(i).getPath(), 8000, 2, now_path_pcm + "/step_" + i + ".aac");
////                editor.executeAudioAR(now_path_pcm + "/step_" + i + ".aac",now_path_pcm + "/step_new_" + i + ".aac");
////                listBeans.get(i).setPathAac(now_path_pcm + "/step_new_" + i + ".aac");
////                startTime+=listBeans.get(i).getDif_time();
////                editor.executeAudioDelayMix(path, listBeans.get(i).getPathAac(), startTime, startTime, now_path_audio+"/audio_cut_new_" + i + ".aac");
////                path = now_path_audio+"/audio_cut_new_" + i + ".aac";
////                startTime += listBeans.get(i).getvCutTime() * 1000;
////            }
////            //对步骤音频拼接
////            for (int i = 3; i < listBeans.size(); i++) {
////                editor.executePcmEncodeAac(listBeans.get(i).getPath(), 8000, 2, now_path_pcm + "/step_" + i + ".aac");
////                editor.executeAudioAR(now_path_pcm + "/step_" + i + ".aac",now_path_pcm + "/step_new_" + i + ".aac");
////                listBeans.get(i).setPathAac(now_path_pcm + "/step_new_" + i + ".aac");
////                editor.executeAudioDelayMix(path, listBeans.get(i).getPathAac(), indexTime, indexTime, now_path_audio+"/audio_cut_new_" + i + ".aac");
//////                deleteFile(path);
////                path = now_path_audio+"/audio_cut_new_" + i + ".aac";
////                indexTime += listBeans.get(i).getvCutTime() * 1000;
////            }
//            //对背景音乐进行合成
//            editor.executeAudioDelayMix(path, now_path_audio+"/audio_end.aac", (int)(vtime*1000), (int)(vtime*1000), path_voide+"/audio_cut.aac");
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 对背景音乐进行合成
//     */
//    private void handlerAudioMerge(){
//        //從視頻中獲取mp3
//        if(paperPathState(path_voide+"/xiangha_start.mp4",now_path_audio))
//            editor.executeDeleteVideo(path_voide+"/xiangha_start.mp4",path_voide+"/audio_start.aac");
//        editor.concatMp3(path_voide+"/audio_start.aac",path_voide+"/audio_cut.aac",path_voide+"/ok.aac");
//        handlerDataCallBack.setCallBack(99);
//    }
//
//    /**
//     * 合成视频转ts
//     */
//    private void startVideoSuccessToTs(){
//        Log.i("zhangyujian","合成视频转ts");
//
//        try {
//            //去除音頻
//            if (paperPathState(path_voide+"/xiangha_start.mp4", now_path_code))
//                editor.executeDeleteAudio(path_voide+"/xiangha_start.mp4", now_path_code + "/start_no.mp4");
//
//            if (paperPathState(path_voide+"/xiangha_end.mp4", now_path_code))
//                editor.executeDeleteAudio(path_voide+"/xiangha_end.mp4", now_path_code + "/end_no.mp4");
//            //处理视频第一帧
//            startOneVideoImage();
//            //視頻轉ts
//            if(paperPathState(now_path_code+"/start_no_cover.mp4", now_path_sucess_ts)){
//                editor.executeConvertMp4toTs(now_path_code+"/start_no_cover.mp4", now_path_sucess_ts + "/start.ts");
//            }else{
//                if (paperPathState(now_path_code+"/start_no.mp4", now_path_sucess_ts))
//                    editor.executeConvertMp4toTs(now_path_code+"/start_no.mp4", now_path_sucess_ts + "/start.ts");
//            }
//
//            if (paperPathState(now_path_sucess + "/" + time + ".mp4", now_path_sucess_ts))
//                editor.executeConvertMp4toTs(now_path_sucess + "/" + time + ".mp4", now_path_sucess_ts + "/middle.ts");
//
//            if (paperPathState(now_path_code+"/end_no.mp4", now_path_sucess_ts))
//                editor.executeConvertMp4toTs(now_path_code+"/end_no.mp4", now_path_sucess_ts + "/end.ts");
//            handlerDataCallBack.setCallBack(99);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 处理视频的第一张图片
//     * 原则上是图片的转视频进行切割划分
//     */
//    private void startOneVideoImage(){
//        Log.i("zhangyujian","视频放入第一帧图片");
//        try{
//            String cover = uploadDishData.getCover();//对大图
//            Log.i("zhangyujian","cover:"+cover);
//            MediaInfo mediaInfo = new MediaInfo(now_path_code + "/start_no.mp4");
//            mediaInfo.prepare();
//
//            if(paperPathState(cover, now_path_code)){
//                MediaImageCover mediaImageCover = new MediaImageCover(context,R.layout.media_cover_url);
//                mediaImageCover.setCoverView(cover,1920,1080);
//                if(mediaImageCover!=null) {
//                    setViewToBitmap(mediaImageCover, "imgCover.png");
//                    if (paperPathState(now_path_code + "/start_no.mp4", now_path_code)) {
//                        editor.executeAddWaterMark(now_path_code + "/start_no.mp4",now_path_img + "/imgCover.png",0,0.1f,0,0,now_path_code + "/start_no_cover.mp4",codeRate);
//                    }
//                }
//            }
//        }catch (Exception e){
//
//        }
//    }
//
//
//    /**
//     * 合成包含片头片尾的视频
//     */
//    private void startSuccessCompoundVideo(){
//        try {
//            Log.i("zhangyujian","片头片尾的视频::开始合成");
//            File file = new File(now_path_sucess_ts);
//            File[] tempList = file.listFiles();
//            ArrayList<String> lists = new ArrayList<>();
//            for (int i = 0; i < tempList.length; i++) {
//                if (tempList[i].isFile()) {
//                    if (paperPathState(tempList[i].getAbsolutePath(), now_path)) {
//                        lists.add(tempList[i].getAbsolutePath());
//                    }
//                }
//            }
//            String[] tss = (String[]) lists.toArray(new String[lists.size()]);
//            editor.executeConvertTsToMp4(tss, now_path + "success.mp4");
//
////            handlerDataCallBack.setCallBack(100);
////            successCallback(now_path + "success.mp4");
//        }catch (Exception e){
//            handlerDataCallBack.CallBackError();
//        }
//    }
//
//    /**
//     * 最后完成
//     */
//    private void successHandler(){
//        Log.i("zhangyujian","去除背景音乐，加背景音乐，最后合成");
//        editor.executeDeleteAudio(now_path + "success.mp4",now_path + "success_no.mp4");
//        String videoFile=now_path + "success_no.mp4";
//        String audioFile=path_voide+"/ok.aac";
//        String dstFile=now_path + "/"+time+"_success.mp4";
//        editor.executeVideoMergeAudio(videoFile,audioFile,dstFile);
////        deleteFiles();
//        successCallback(dstFile);
//        handlerDataCallBack.setCallBack(100);
//
//    }
//
//    /**
//     * 删除文件数据(全部数据)
//     */
//    private void deleteFiles(){
//        deleteDirectory(now_path_img);
//        deleteDirectory(now_path_paper_text);
//        deleteDirectory(now_path_ts);
//        deleteDirectory(now_path_sucess);
//        deleteDirectory(now_path_sucess_ts);
//        deleteDirectory(now_path_audio);
//        deleteDirectory(now_path_pcm);
//        deleteDirectory(now_path_code);
//        deleteFile(path_voide+"/audio_start.aac");
//        deleteFile(path_voide+"/audio_end.aac");
//        deleteFile(path_voide+"/audio_cut.aac");
//
//        deleteFile(path_voide+"/ok.aac");
//        deleteFile(now_path + "success.mp4");
//        deleteFile(now_path + "success_no.mp4");
//    }
//
//    /**
//     * 处理视频成功
//     * @param pathurl
//     */
//    private void successCallback(String pathurl){
//        try {
//            UploadDishSqlite uploadDishSqlite = new UploadDishSqlite(context);
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("videoPath", pathurl);
//            uploadDishSqlite.update(uploadDishData.getId(), UploadDishData.ds_capture, jsonObject.toString());
//            handlerDataCallBack.callBackSucess(pathurl, uploadDishData.getId(),time,imagePath);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 处理cover大图
//     */
//    private void handlerCover(){
//        String cover= uploadDishData.getCover();
//        if(cover!=null&& !TextUtils.isEmpty(cover)){
//            File file= new File(cover);
//            String name=file.getName();
//            if(paperPathState(cover,now_path_paper)){
//                copy(cover,now_path_paper,name);
//                imagePath=now_path_paper+"/"+name;
//            }
//        }
//    }
//
//    /**
//     * 判断当前路径是否可用
//     *
//     * @param decPath:原始视频路径
//     * @param path：导出视频要存储的路径
//     */
//    private boolean paperPathState(String decPath, String path) {
//        if (VideoEditor.fileExist(decPath)) {
//            File file = new File(path);
//            if (!file.exists()) {
//                file.mkdirs();
//            }
//        } else {
//            return false;
//        }
//        return true;
//    }
//
//
//    /**
//     * 终止
//     */
//    public void stop(){
//        isStop=true;
//    }
//    public interface HandlerDataCallBack{
//        public void setCallBack(int progress);
//        public void CallBackError();
//        public void callBackSucess(String path,int id,String time,String imagepath);
//    }
//    private HandlerDataCallBack handlerDataCallBack;
//    public void setHandlerDataCallBack(HandlerDataCallBack handlerDataCallBack){
//        this.handlerDataCallBack= handlerDataCallBack;
//    }
//
//    /**
//     * 拷贝文件
//     * @param oldPath
//     * @param savePath
//     * @param saveName
//     */
//    public static void copy( String oldPath,String savePath, String saveName) {
//        if(!new File(oldPath).exists()){//当前文件存在，直接中断
//            return;
//        }
//        String filename = savePath + "/" + saveName;
//
//        File dir = new File(savePath);
//        // 如果目录不中存在，创建这个目录
//        if (!dir.exists())
//            dir.mkdir();
//        try {
//            if (!(new File(filename)).exists()) {
//                InputStream is = new FileInputStream(oldPath); //读入原文件
//                FileOutputStream fos = new FileOutputStream(filename);
//                byte[] buffer = new byte[2048];
//                int count = 0;
//                while ((count = is.read(buffer)) > 0) {
//                    fos.write(buffer, 0, count);
//                }
//                fos.close();
//                is.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public boolean isStop() {
//        return isStop;
//    }
//
//    public void setStop(boolean stop) {
//        isStop = stop;
//    }
//
//    /**
//     * view转图片，存储到本地
//     * @param view_one
//     * @param imgName----------path_img
//     */
//    private void setViewToBitmap(View view_one,String imgName){
//        view_one.measure(
//                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//        view_one.layout(0, 0, view_one.getMeasuredWidth(), view_one.getMeasuredHeight());
//        //下面的代码和上面的是一样的
//        Bitmap bitmap = getViewBitmap(view_one);
//
//        // 把一个View转换成图片
//        File file = new File(now_path_img);
//        if (!file.exists()) file.mkdirs();
//        File files = new File(now_path_img + "/"+imgName);
//        boolean imge = false;
//        try {
//            imge = bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(files));
//        } catch (Exception e) {
//        }
//        if (imge) {
////            Tools.showToast(context, "死去看图片");
////            context.startActivity(new Intent(context, TestActivity.class));
//        }
//    }
//
//    /**
//     * 创建view
//     * @param view
//     * @return
//     */
//    public static Bitmap getViewBitmap(View view) {
//        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        view.draw(canvas);
//        return bitmap;
//    }
//
//    /**
//     * 删除指定草稿箱全部数据合成视频数据
//     * @param  id 草稿箱id
//     */
//    public static void delAllMediaHandlerData(int id){
//        if(id>-1){
//            deleteDirectory(viewPath_cancel+id);
//        }
//    }
//
//
//    /**
//     * 删除目录（文件夹）以及目录下的文件
//     *
//     * @param sPath
//     *            被删除目录的文件路径
//     * @return 目录删除成功返回true，否则返回false
//     */
//    public static boolean deleteDirectory(String sPath) {
//        // 如果sPath不以文件分隔符结尾，自动添加文件分隔符
//        if (!sPath.endsWith(File.separator)) {
//            sPath = sPath + File.separator;
//        }
//        File dirFile = new File(sPath);
//        // 如果dir对应的文件不存在，或者不是一个目录，则退出
//        if (!dirFile.exists() || !dirFile.isDirectory()) {
//            return false;
//        }
//        Boolean flag = true;
//        // 删除文件夹下的所有文件(包括子目录)
//        File[] files = dirFile.listFiles();
//        for (int i = 0; i < files.length; i++) {
//            // 删除子文件
//            if (files[i].isFile()) {
//                flag = deleteFile(files[i].getAbsolutePath());
//                if (!flag)
//                    break;
//            } // 删除子目录
//            else {
//                flag = deleteDirectory(files[i].getAbsolutePath());
//                if (!flag)
//                    break;
//            }
//        }
//        if (!flag)
//            return false;
//        // 删除当前目录
//        if (dirFile.delete()) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    /**
//     * 删除单个文件
//     *
//     * @param sPath
//     *            被删除文件的路径+文件名
//     * @return 单个文件删除成功返回true，否则返回false
//     */
//    public static boolean deleteFile(String sPath) {
//        Boolean flag = false;
//        File file = new File(sPath);
//        // 路径为文件且不为空则进行删除
//        if (file.isFile() && file.exists()) {
//            file.delete();
//            flag = true;
//        }
//        return flag;
//    }
//
//    private void copyAsset(String savePath,String saveName,String assetName){
//
//        String filename = savePath + "/" + saveName;
//
//        File dir = new File(savePath);
//        // 如果目录不中存在，创建这个目录
//        if (!dir.exists())
//            dir.mkdir();
//        try {
//            if (!(new File(filename)).exists()) {
//                InputStream is = context.getAssets().open(assetName);
//                FileOutputStream fos = new FileOutputStream(filename);
//                byte[] buffer = new byte[2048];
//                int count = 0;
//                while ((count = is.read(buffer)) > 0) {
//                    fos.write(buffer, 0, count);
//                }
//                fos.close();
//                is.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
