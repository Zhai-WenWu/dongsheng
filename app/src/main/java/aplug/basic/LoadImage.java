package aplug.basic;

import xh.basic.internet.img.UtilLoadImage;
import android.app.Activity;
import android.content.Context;

public class LoadImage extends UtilLoadImage {
	public static Context initContext = null;
	private static volatile LoadImage instance = null;

	private LoadImage(Context context){
		super(context);
	}
	
	/**初始化*/
	public static LoadImage init(Context context){
		initContext = context;
		return getInstance();
	}
	
	public static LoadImage getInstance() {
		synchronized (LoadImage.class) {
			if (instance == null) {
				instance = new LoadImage(initContext);
			}
			return instance;
		}
	}
	
	public static Builder with(Activity activity){
		return getInstance().getBuilder(activity);
	}
	
	public static Builder with(Context context){
		return getInstance().getBuilder(context);
	}
}
