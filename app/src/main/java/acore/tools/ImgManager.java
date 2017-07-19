package acore.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import xh.basic.tool.UtilFile;
import xh.basic.tool.UtilImage;
import xh.basic.tool.UtilLog;
import xh.basic.tool.UtilString;
import acore.override.XHApplication;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import aplug.basic.LoadImage;

import com.xiangha.R;

public class ImgManager extends UtilImage{
	
	// 获取圆角矩形背景图
	public static Drawable getRoundBackground(Context context, String color) {
		float dimenH = 22;
		float dimenW = 80;
		if (!context.getResources().getString(R.dimen.dp_22).equals("22.0dip")) {
			dimenH = (float) 35.3;
			dimenW = 128;
		}
		int height = ToolsDevice.dp2px(context, dimenH), width = ToolsDevice.dp2px(
				context, dimenW), round = ToolsDevice.dp2px(context, 3);
		// 新建一个新的输出图片
		Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		// 新建一个矩形
		RectF outerRect = new RectF(0, 0, width, height);
		// 产生一个红色的圆角矩形 或者任何有色颜色，不能是透明！
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.parseColor(color));
		canvas.drawRoundRect(outerRect, round, round, paint);
		return new BitmapDrawable(context.getResources(), output);
	}

	/**
	 * 判断图片是否长宽成比
	 * @param path  : 图片路径
	 * @param scale  : 比例
	 * @return
	 */
	public static boolean isQualified(String path, int scale) {
		// 配置bitmap，防止内存溢出
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		int wi = options.outWidth;
		int hei = options.outHeight;
		// Log.i("FRJ","wi:" + wi + "  hei:" + hei);
		return !(wi / hei >= scale || hei / wi >= scale);
	}

	/**
	 * 删除长期存储图片
	 * @param imgUrl
	 */
	public static void delImg(String imgUrl) {
		if (imgUrl.length() == 0)
			return;
		String name = UtilString.toMD5(imgUrl, false);
		UtilFile.delDirectoryOrFile(UtilFile.getSDDir() + LoadImage.SAVE_LONG + "/" + name, 0);
	}

	/**
	 * 长期存储图片到本地，需在多线程中调用
	 * @param imgUrl
	 * @param type
	 */
	public static void saveImg(String imgUrl, String type) {
		if (imgUrl.length() == 0)
			return;
		String name = UtilString.toMD5(imgUrl, false);
		// 图片不存在则下载
		if (UtilFile.ifFileModifyByCompletePath(UtilFile.getSDDir() + type + "/" + name, -1) == null) {
			LoadImage.with(XHApplication.in())
				.load(imgUrl)
				.setSaveType(type)
				.preload();
		}
	}
	
	/**
	 * 将Bitmap转换成InputStream
	 * 
	 * @param bitmap
	 * @return
	 */
	public static InputStream bitmapToInputStream(Bitmap bitmap, int kb) {
		int options = 99;
		InputStream is = null;
		byte[] theByte = null;
		int num = 1;
		Bitmap oldBitmap = bitmap;
		// 压缩
		while (bitmap != null && options >= 0) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				boolean isOk = bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
				while(!isOk && num < 2){
					UtilLog.reportError("bitmap.compress error:" + num, null);
					options = 99;
					bitmap = oldBitmap;
					isOk = bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
					num ++;
				}
				if(!isOk){
					oldBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				}
				theByte = baos.toByteArray();
				// LogManager.print("d", bitmap.getWidth() + "*" + bitmap.getHeight() + "正在压缩质量"
				// + options + "结果" + (theByte.length / 1024));
				if (theByte.length / 1024 < kb || kb == 0 || options <= 3 || !isOk) {
					is = new ByteArrayInputStream(theByte);
					baos.close();
					break;
				}
				baos.close();
				options -= 1000 / options;
				if (options <= 0)
					options = 3;
			} catch (Exception e) {
				UtilLog.reportError("图片压缩", e);
				break;
			}
		}
		return is;
	}

}
