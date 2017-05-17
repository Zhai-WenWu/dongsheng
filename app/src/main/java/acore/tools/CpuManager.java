package acore.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

public class CpuManager {

        // 获取CPU最大频率（单位KHZ）
     // "/system/bin/cat" 命令行
     // "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" 存储最大频率的文件的路径
        public static String getMaxCpuFreq() {
            String result = "";
            ProcessBuilder cmd;
            try {
                    String[] args = { "/system/bin/cat",
                                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" };
                    cmd = new ProcessBuilder(args);
                    Process process = cmd.start();
                    InputStream in = process.getInputStream();
                    byte[] re = new byte[24];
                    while (in.read(re) != -1) {
                            result = result + new String(re);
                    }
                    in.close();
            } catch (IOException ex) {
                    result = "N/A";
            }
            return result.trim();
        }

         // 获取CPU最小频率（单位KHZ）
        public static String getMinCpuFreq() {
            String result = "";
            ProcessBuilder cmd;
            try {
                    String[] args = { "/system/bin/cat",
                                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq" };
                    cmd = new ProcessBuilder(args);
                    Process process = cmd.start();
                    InputStream in = process.getInputStream();
                    byte[] re = new byte[24];
                    while (in.read(re) != -1) {
                            result = result + new String(re);
                    }
                    in.close();
            } catch (IOException ex) {
                    result = "N/A";
            }
            return result.trim();
        }

         // 实时获取CPU当前频率（单位KHZ）
        public static String getCurCpuFreq() {
            String result = "N/A";
            try {
                    FileReader fr = new FileReader(
                                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
                    @SuppressWarnings("resource")
					BufferedReader br = new BufferedReader(fr);
                    String text = br.readLine();
                    result = text.trim();
            } catch (FileNotFoundException ignored) {
            } catch (IOException ignored) {
            }
            return result;
        }

        // 获取CPU名字
        public static String getCpuName() {
            try {
                FileReader fr = new FileReader("/proc/cpuinfo");
                @SuppressWarnings("resource")
				BufferedReader br = new BufferedReader(fr);
                String text = br.readLine();
                String[] array = text.split(":\\s+", 2);
                for (int i = 0; i < array.length; i++) {
                }
                return array[1];
            } catch (FileNotFoundException ignored) {
            } catch (IOException ignored) {
            }
            return null;
        }
}
