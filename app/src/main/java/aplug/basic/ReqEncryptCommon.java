package aplug.basic;

import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONObject;

import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import acore.override.XHApplication;
import acore.tools.RSAUtils;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;

/**
 * 网络加密公共部分
 * xhCode,加密AEC加密成token
 */
public class ReqEncryptCommon {
    public final static String password ="yUskz2#7:Xa,]KCu";//本地数据写死key
    public final static String IVKEY = "ZxwG,OO8:LDqs&8u";//与服务端约定死
    private String GY="";//公钥
    private boolean isencrypt=false;//是否加密
    private String sign="";//sign值
    private long timeLength=-1;//时间长度,当前是秒，使用要*1000
    private long nowTime=-1;//当前获取公钥成功的请求时间
    private static ReqEncryptCommon reqEncryptCommon=null;
    public static ReqEncryptCommon getInstance(){
        if(reqEncryptCommon==null){
            synchronized (ReqEncryptCommon.class){
                if(reqEncryptCommon==null)reqEncryptCommon=new ReqEncryptCommon();
            }
        }
        return reqEncryptCommon;
    }
    /**
     * 获取token
     */
    public String getToken(){
        //香哈code
        try {
            String xhcode = ToolsDevice.getXhIMEI(XHApplication.in());
            return encrypt(xhcode,password); // 加密
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     * AES加密
     * @param data
     * @param key
     * @return
     */
    public static String encrypt(String data,String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();
            SecretKeySpec keyspec = new SecretKeySpec(fullZore(key,blockSize), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(fullZore(IVKEY,blockSize));
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(fullZore(data,blockSize));
            return new String(Base64.encode(encrypted, Base64.DEFAULT)).trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * AES解密
     * @param data
     * @param key
     * @return
     */
    public static String decrypt(String data,String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();
            SecretKeySpec keyspec = new SecretKeySpec(fullZore(key,blockSize), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(fullZore(IVKEY,blockSize));
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            byte[] decrypted = cipher.doFinal(Base64. decode(data, Base64.DEFAULT));
            return new String(decrypted).trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    /**
     * java AES加解密补零操作
     * @param data
     * @param blockSize
     * @return
     */
    public static byte[] fullZore(String data,int blockSize){
        byte[] dataBytes = data.getBytes();
        int plaintextLength = dataBytes.length;
        if (plaintextLength % blockSize != 0) {
            plaintextLength = plaintextLength + (blockSize-(plaintextLength % blockSize));
        }
        byte[] plaintext = new byte[plaintextLength];
        System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
        return plaintext;
    }


    public String getData(String params)  {
        try {
            Map<String,String> map=null;
            if(!TextUtils.isEmpty(params)){
                map=StringManager.getMapByString(params,"&","=");
            }
            JSONObject jsonObject = new JSONObject();
            if(map!=null){
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    jsonObject.put(entry.getKey(),entry.getValue());
               }
            }
            jsonObject.put("sign",sign);
            GY=GY.replace("-----BEGIN PUBLIC KEY-----","");
            GY=GY.replace("-----END PUBLIC KEY-----","");
            byte[] data=jsonObject.toString().getBytes();

            return RSAUtils.encryptByPublicKey(data,GY);

        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public String getGY() {
        return GY;
    }

    public void setGY(String GY) {
        this.GY = GY;
    }

    public boolean isencrypt() {
        return isencrypt;
    }

    public void setIsencrypt(boolean isencrypt) {
        this.isencrypt = isencrypt;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public long getTimeLength() {
        return timeLength;
    }

    public void setTimeLength(long timeLength) {
        this.timeLength = timeLength;
    }

    public long getNowTime() {
        return nowTime;
    }

    public void setNowTime(long nowTime) {
        this.nowTime = nowTime;
    }
}
