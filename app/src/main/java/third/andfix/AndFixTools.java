package third.andfix;

import android.content.Context;
import android.text.TextUtils;

import com.alipay.euler.andfix.patch.PatchManager;
import com.qq.e.comm.util.Md5Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Map;

import acore.dialogManager.VersionOp;
import acore.logic.XHClick;
import acore.tools.FileManager;
import acore.tools.StringManager;
import aplug.basic.InternetCallback;
import aplug.basic.ReqInternet;
import xh.basic.tool.UtilString;

public class AndFixTools {
	private volatile static AndFixTools andFix;
	/**
	 * patch manager
	 */
	private PatchManager mPatchManager;
	private String mVersion;
	private Context mCon;

	private String tongjiId = "a_hotfix";
	
	private AndFixTools(){}
	
	public static AndFixTools getAndFix(){
		if(andFix == null){
			synchronized (AndFixTools.class) {
				if(andFix == null){
					andFix = new AndFixTools();
				}
			}
		}
		return andFix;
	}
	
	/**
	 * 初始化
	 * @param con
	 */
	public void initPatchManager(Context con){
		mCon = con;
		//初始化 PatchManager
		mPatchManager = new PatchManager(con);
		mVersion = VersionOp.getVerName(con);
		mPatchManager.init(mVersion);//current version
		// Load patch 加载补丁;你应该尽可能早的加载补丁，通常都是在Application的onCreate()方法中进行初始化。
		mPatchManager.loadPatch();
	}
	
	public void doGetFixFile(final Context con){
		String patchVersion = FileManager.loadShared(con, FileManager.file_andfix, mVersion).toString();
		if(TextUtils.isEmpty(patchVersion)){
			patchVersion = "0";
		}
		final String finalPatchVersion = patchVersion;
		ReqInternet.in().doGet(StringManager.api_andFix, new InternetCallback(con) {
			
			@Override
			public void loaded(int flag, String url, Object msg) {
				if(flag >= ReqInternet.REQ_OK_STRING){
					ArrayList<Map<String, String>> array = UtilString.getListMapByJson(msg);
					if(array != null && array.size() > 0){
						Map<String, String> map = array.get(0);
						String incNum = map.get("incNum");
						if(!finalPatchVersion.equals(incNum)){
							int oldNum = Integer.parseInt(finalPatchVersion);
							int newNum = Integer.parseInt(incNum);
							if(newNum > oldNum){
								loadPatchFile(con,map.get("incNum"),map.get("url"),map.get("checksum"));
							}
						}
					}
				}else{
					XHClick.mapStat(mCon,tongjiId ,"失败","请求andFix接口失败");
				}
			}
		});
	}
	
	private void loadPatchFile(final Context con, final String patchVersion, String patchUrl, final String checksum){
		ReqInternet.in().getInputStream(patchUrl, new InternetCallback(con){

			@Override
			public void loaded(int flag, String url, Object msg) {
				if(flag >= ReqInternet.REQ_OK_IS){
					String patchName = mVersion + "_" + patchVersion + ".apatch";
					String filePath = FileManager.getSDDir() + patchName;
					FileManager.saveFileToCompletePath(filePath, (InputStream)msg, false);
					boolean check = checkMd5(filePath,checksum,patchVersion);
					if(check) loadPatch(filePath,patchVersion);
					else XHClick.mapStat(mCon,tongjiId ,"失败","补丁包加密校验没通过");
					FileManager.saveShared(con, FileManager.file_andfix, mVersion, patchVersion);
				}else{
					XHClick.mapStat(mCon,tongjiId ,"失败","下载补丁包失败");
				}
			}
		});
	}
	
	/**
	 * 加载补丁文件，立即生效
	 * @param patch : 补丁包的绝对路径
	 */
	private void loadPatch(String patch,String patchVersion){
		try {
			//Add patch 添加补丁
			mPatchManager.addPatch(patch); //path:补丁文件下载到本地的路径。
			//当一个新的补丁文件被下载后，调用addPatch(path)就会立即生效
			XHClick.mapStat(mCon,tongjiId ,"成功",patchVersion);
		} catch (IOException e) {
			e.printStackTrace();
			XHClick.mapStat(mCon,tongjiId ,"失败","加载补丁包发送io异常");
		}catch (Exception e){
			XHClick.mapStat(mCon,tongjiId ,"失败","加载补丁包发送io异常");
			e.printStackTrace();
		}catch(Error error){
			XHClick.mapStat(mCon,tongjiId ,"失败","加载补丁包发送io异常");
			error.printStackTrace();
		}
	}

	/**
	 * md5（md5_file（增量文件）_appId_当前app版本号_增量文件版本号）
	 * @return
     */
	private boolean checkMd5(String patch,String checksum,String patchVersion){
		StringBuffer buffer = new StringBuffer(getFileMD5(patch));
		buffer.append("_1_");
		buffer.append(VersionOp.getVerName(mCon));
		buffer.append("_");
		buffer.append(patchVersion);
		String md5Local = Md5Util.encode(buffer.toString());
		return md5Local.equals(checksum);
	}

	/**
	 * 获取单个文件的MD5值！
	 * @param filePath
	 * @return
	 */

	private String getFileMD5(String filePath) {
		File file = new File(filePath);
		if (!file.isFile()) {
			XHClick.mapStat(mCon,tongjiId ,"失败","补丁包文件不存在");
			return "";
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[1024];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while ((len = in.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());
		return bigInt.toString(16);
	}
}
