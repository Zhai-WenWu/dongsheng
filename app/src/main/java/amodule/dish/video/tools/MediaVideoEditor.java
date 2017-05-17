package amodule.dish.video.tools;

import com.download.tools.FileUtils;
import com.lansosdk.videoeditor.VideoEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2016/10/17.
 */

public class MediaVideoEditor extends VideoEditor {


    /**
     * 对视频增加字幕
     * @param videoFile
     * @param text
     * @param dstFile
     */
    public int setVideoText(String videoFile,String text,String dstFile){
        if(fileExist(videoFile)){
            List<String> cmdList=new ArrayList<String>();

            cmdList.add("-i");
            cmdList.add(videoFile);

            cmdList.add("-vf");
            cmdList.add("subtitles="+text);
            cmdList.add(dstFile);

            String[] command=new String[cmdList.size()];
            for(int i=0;i<cmdList.size();i++){
                command[i]=(String)cmdList.get(i);
            }
            return  executeVideoEditor(command);

        }else{
            return VIDEO_EDITOR_EXECUTE_FAILED;
        }
    }

    /**
     * 音频裁剪,截取音频文件中的一段.
     * 需要注意到是: 尽量保持裁剪文件的后缀名和源音频的后缀名一致.
     * @param srcFile   源音频
     * @param dstFile  裁剪后的音频
     * @return
     */
    public int executeAudioAR(String srcFile,String dstFile)
    {
        if(fileExist(srcFile)){

            List<String> cmdList=new ArrayList<String>();

            cmdList.add("-i");
            cmdList.add(srcFile);

            cmdList.add("-ar");
            cmdList.add(String.valueOf("44100"));

            cmdList.add("-acodec");
            cmdList.add("libfaac");
            cmdList.add("-y");
            cmdList.add(dstFile);
            String[] command=new String[cmdList.size()];
            for(int i=0;i<cmdList.size();i++){
                command[i]=(String)cmdList.get(i);
            }
            return  executeVideoEditor(command);

        }else{
            return VIDEO_EDITOR_EXECUTE_FAILED;
        }
    }

    /**
     * 两个音频文件混合.
     * 混合后的文件压缩格式是aac格式, 故需要您dstPath的后缀是aac或m4a.
     *
     * @param audioPath1  主音频的完整路径
     * @param audioPath2  次音频的完整路径
     * @param value1  主音频的音量, 浮点类型, 大于1.0为放大音量, 小于1.0是减低音量.比如设置0.5则降低一倍.
     * @param value2  次音频的音量, 浮点类型.
     * @param dstPath  输出保存的完整路径.需要文件名的后缀是aac 或 m4a格式.
     * @return
     */
    public int executeAudioVolumeMix(String audioPath1,String audioPath2,float value1,float value2,String dstPath)
    {
        List<String> cmdList=new ArrayList<String>();

        String filter=String.format(Locale.getDefault(),"[0:a]volume=volume=%f[a1]; [1:a]volume=volume=%f[a2]; [a1][a2]amix=inputs=2:duration=first:dropout_transition=2",value1,value2);


        cmdList.add("-i");
        cmdList.add(audioPath1);

        cmdList.add("-i");
        cmdList.add(audioPath2);

        cmdList.add("-filter_complex");
        cmdList.add(filter);

        cmdList.add("-acodec");
        cmdList.add("libfaac");

        cmdList.add("-y");
        cmdList.add(dstPath);
        String[] command=new String[cmdList.size()];
        for(int i=0;i<cmdList.size();i++){
            command[i]=(String)cmdList.get(i);
        }
        return  executeVideoEditor(command);
    }

    /**
     * 合成音频
     * @return
     */
    public static int concatMp3(String start,String middle ,String dsPath)
    {
        List<String> cmdList=new ArrayList<String>();

        cmdList.add("-i");
        String concat="concat:"+start+"|"+middle;
//		String concat= "concat:/sdcard/xiangha/video/xiangha/audio_start.aac|/sdcard/xiangha/video/xiangha/audio_cut.aac";
        cmdList.add(concat);

        cmdList.add("-acodec");
        cmdList.add("copy");

        cmdList.add("-y");
//		String dsPath="/sdcard/xiangha/video/xiangha/ok.aac";
        cmdList.add(dsPath);
        String[] command=new String[cmdList.size()];
        for(int i=0;i<cmdList.size();i++){
            command[i]=(String)cmdList.get(i);
        }
        VideoEditor veditor=new VideoEditor();
        return veditor.executeVideoEditor(command);
    }

    /**
     * 合成音頻
     * @param tsArray
     * @param dstPath
     * @return
     */
    public int executemp3( String statr,String middle,String dstPath)
    {
        if(fileExist(statr)){

            List<String> cmdList=new ArrayList<String>();

            cmdList.add("–i");
            cmdList.add(statr);

            cmdList.add("–i");
            cmdList.add(middle);

//			cmdList.add("-filter_complex amix=inputs=2:duration=first:dropout_transition=2");
//			cmdList.add("–f");
//			cmdList.add("mp3");

            cmdList.add(dstPath);

            String[] command=new String[cmdList.size()];
            for(int i=0;i<cmdList.size();i++){
                command[i]=(String)cmdList.get(i);
            }
            return  executeVideoEditor(command);

        }else{
            return VIDEO_EDITOR_EXECUTE_FAILED;
        }
    }

    //ffmpeg -i input.wav -af volume=-3dB output.wav
//	ffmpeg -i xxx.aac -af "volume=volume=0.5" -acodec libfaac -y oooo.aac
//	ffmpeg -f s16le -ar 44.1k -ac 2 -i file.pcm -af "volume=volume=0.5" -f s16le -acode

    /**
     * 音頻减小
     * @param startAudio
     * @param dstPath
     * @return
     */
    public int executeAudioVolume( String startAudio,float values,String dstPath) {
        if (fileExist(startAudio)) {

            List<String> cmdList = new ArrayList<String>();


//			String filter=String.format(Locale.getDefault(),"volume=volume=%f",values);
            String filter=String.format(Locale.getDefault(),"volume='if(lt(t,%f),1,max(1-(t-%f)/9,0))':eval=frame",values,values);

            cmdList.add("-i");
            cmdList.add(startAudio);

            cmdList.add("-af");
            cmdList.add(filter);

            cmdList.add("-acodec");
            cmdList.add("libfaac");
            cmdList.add("-b:a");
            cmdList.add("64000");

            cmdList.add("-y");

            cmdList.add(dstPath);

            String[] command = new String[cmdList.size()];
            for (int i = 0; i < cmdList.size(); i++) {
                command[i] = (String) cmdList.get(i);
            }
            return executeVideoEditor(command);

        } else {
            return VIDEO_EDITOR_EXECUTE_FAILED;
        }
    }
//    /**
//     * 视频截图
//     *
//     * @param videoPath 视频路径
//     * @param outputPath 截图输出路径
//     * @param wh 截图画面尺寸，例如84x84
//     * @param ss 截图起始时间
//     * @return
//     */
//    public static boolean captureThumbnails(String videoPath, String outputPath, String wh, String ss) {
//        //ffmpeg -i /storage/emulated/0/DCIM/04.04.mp4 -s 84x84 -vframes 1 /storage/emulated/0/DCIM/Camera/miaopai/1388843007381.jpg
//        //ffmpeg -i eis-sample.mpg -s 40x40 -r 1/5 -vframes 10 %d.jpg
//        FileUtils.deleteFile(outputPath);
//        if (ss == null)
//            ss = "";
//        else
//            ss = " -ss " + ss;
//        String cmd = String.format("ffmpeg -d stdout -loglevel verbose -i \"%s\"%s -s %s -vframes 1 \"%s\"", videoPath, "1", 480+"x"+360, outputPath);
//        return UtilityAdapter.FFmpegRun("", cmd) == 0;
//    }
}
