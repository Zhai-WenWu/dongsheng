package acore.tools;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import xh.basic.tool.UtilLog;

public class CPUTool{
    private final static String kCpuInfoMaxFreqFilePath = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";
//获取CPU最大频率
    public static int getMaxCpuFreq(){
        int result = 0;
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(kCpuInfoMaxFreqFilePath);
            br = new BufferedReader(fr);
            String text = br.readLine();
            if(!TextUtils.isEmpty(text))
                result = Integer.parseInt(text.trim());
        } catch (FileNotFoundException e){
        	UtilLog.reportError("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq 文件未找到", e);
        } catch (IOException e){
        	UtilLog.reportError("IO异常", e);
        } finally {
            if (fr != null)
                try {
                    fr.close();
                } catch (IOException e) {
                	UtilLog.reportError("IO异常", e);
                }
            if (br != null)
                try {
                    br.close();
                } catch (IOException e){
                	UtilLog.reportError("IO异常", e);
                }
        }

        return result;
    }

}