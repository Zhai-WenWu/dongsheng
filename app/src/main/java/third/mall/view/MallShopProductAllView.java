package third.mall.view;

import java.util.ArrayList;

import third.mall.bean.ProductBean;
import third.mall.view.MallShopProductView.InterProudct;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

/**
 * 处理商品view 集合
 * 
 * @author yujian
 *
 */
public class MallShopProductAllView extends LinearLayout {

	private Context context;
	private InterProudct interProudct;

	public MallShopProductAllView(Context context) {
		super(context);
		this.context = context;
		this.setOrientation(1);
	}

	public MallShopProductAllView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		this.setOrientation(1);
		for (int i = 0; i < 3; i++) {
			MallShopProductView productView = new MallShopProductView(context);
			productView.setInterface(new InterProudct() {
				@Override
				public void setChangeSucess() {
					if (interProudct != null) {
						interProudct.setChangeSucess();
					}
				}
			});
			this.addView(productView);
		}
	}

	public void setInterface(InterProudct interProudct) {
		this.interProudct = interProudct;
	}

	public void setData(ArrayList<ProductBean> list,String url,String mall_stat_statistic) {
		Log.i("android view ::;", "setData");
		// 删除数据
		if(this.getChildCount()>3){
			this.removeViews(3, this.getChildCount()-3);
		}
		if (list.size() > 3) {
			for (int i = 0, size = list.size(); i < size; i++) {
				if(i<3){
					MallShopProductView productView= (MallShopProductView) this.getChildAt(i);
					productView.setVisibility(View.VISIBLE);
					productView.setValue(list.get(i));
					productView.setUrl(url,mall_stat_statistic);
				}else{
					MallShopProductView productView = new MallShopProductView(context);
					productView.setValue(list.get(i));
					productView.setUrl(url,mall_stat_statistic);
					productView.setTag(list.get(i).getCode());
					productView.setInterface(new InterProudct() {
						@Override
						public void setChangeSucess() {
							if (interProudct != null) {
								interProudct.setChangeSucess();
							}
						}
					});
					this.addView(productView);
				}
			}
		}else{
			for (int i = 0; i < 3; i++) {
				if(list.size()>=i+1){
					((MallShopProductView) this.getChildAt(i)).setVisibility(View.VISIBLE);
					((MallShopProductView) this.getChildAt(i)).setValue(list.get(i));
					((MallShopProductView) this.getChildAt(i)).setUrl(url,mall_stat_statistic);
					((MallShopProductView) this.getChildAt(i)).setTag(list.get(i).getCode());
				}else{
					((MallShopProductView) this.getChildAt(i)).setVisibility(View.GONE);
				}
			}
		}

	}

	public void setChangeView(ArrayList<ProductBean> list) {
		Log.i("android view ::;", "setChangeView");
		for (int i = 0, size = list.size(); i < size; i++) {
			MallShopProductView productView = (MallShopProductView) this.getChildAt(i);
			productView.setValue(list.get(i));
		}
	}
}
