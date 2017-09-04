/*
 * 
 * @author intBird 20140213.
 * 
 */
package amodule.nous.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.XHClick;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.FileManager;
import acore.tools.StringManager;
import acore.tools.ToolsDevice;

public class AdapterNousHome extends AdapterSimple {

	public int contentWidth = 0;
	private BaseActivity act;
	private List<? extends Map<String, ?>> data;

	public AdapterNousHome(BaseActivity act, View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
		this.data = data;
		this.act = act;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) data.get(position);
		// 缓存视图
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(act).inflate(R.layout.a_nous_item, parent, false);
			viewHolder.setView(convertView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.setData(map, position);
		return convertView;
	}

	public class ViewHolder{
		public RelativeLayout search_fake_layout,relativeLayout2,content_layout;
		public ImageView nous_image,firstImage,iv_nousCover;
		public TextView firstTitle,firstClick,tv_soruce,tv_nousTitle;
		public void setView(View view){
			content_layout=(RelativeLayout) view.findViewById(R.id.content_layout);
			search_fake_layout= (RelativeLayout) view.findViewById(R.id.search_fake_layout);
			relativeLayout2= (RelativeLayout) view.findViewById(R.id.relativeLayout2);
			nous_image= (ImageView) view.findViewById(R.id.nous_image);
			iv_nousCover= (ImageView) view.findViewById(R.id.iv_nousCover);
			firstTitle = (TextView) view.findViewById(R.id.first_title_nous);
			firstClick = (TextView) view.findViewById(R.id.tv_allClick);
			firstImage = (ImageView) view.findViewById(R.id.first_pic_nous);
			tv_soruce = (TextView) view.findViewById(R.id.tv_soruce);
			tv_nousTitle = (TextView) view.findViewById(R.id.tv_nousTitle);
		}
		public void setData(Map<String, String> map, int position){
			if (map.size() == 4) {
				search_fake_layout.setVisibility(View.GONE);
				relativeLayout2.setVisibility(View.GONE);
				setViewImage(nous_image, map.get("img"));
			} else {
				if(position == -1){//以前在第一个位置显示特殊的view样式，现在去掉。
					search_fake_layout.setVisibility(View.GONE);
					nous_image.setVisibility(View.GONE);
					relativeLayout2.setVisibility(View.VISIBLE);
					
					LayoutParams lp = firstImage.getLayoutParams();
					lp.width = ToolsDevice.getWindowPx(act).widthPixels;
					lp.height = ToolsDevice.getWindowPx(act).widthPixels * 2 / 3;
					firstImage.setLayoutParams(lp);
					setViewImage(firstImage, map.get("img"));
					setViewText(firstTitle, map.get("title"));
					setViewText(firstClick, map.get("allClick"));
				}else {
					search_fake_layout.setVisibility(View.VISIBLE);
					setViewImage(iv_nousCover, map.get("img"));
					relativeLayout2.setVisibility(View.GONE);
					nous_image.setVisibility(View.GONE);
//					TextView tv_nousContent = (TextView) view.findViewById(R.id.tv_nousContent1);
					tv_nousTitle.setTextColor(act.getResources().getColor(R.color.nous_item_text_color));
					setViewText(tv_nousTitle, map.get("title"));
					setViewText(firstClick, map.get("allClick"));
//					setViewText(tv_soruce, map.get("soruce"));
					tv_soruce.setText(map.get("soruce"));
					//数据变灰
					String code= map.get("code");
					tv_nousTitle.setTag(code+position);
					
					String[] strs= FileManager.getSharedPreference(act, FileManager.USERCHECK);
					for (String str: strs) {
						if(tv_nousTitle.getTag().equals(str+position)&&map.get("code").equals(str)){
							tv_nousTitle.setTextColor(act.getResources().getColor(R.color.c_gray_999999));
						}
					}
					
//					String content = map.get("content");
//					int number = contentWidth / ToolsDevice.sp2px(act, Tools.getDimen(act,R.dimen.dp_15));
//					int number2 = number + (number / 2);
//					if (content.length() >= number) {
//						tv_nousContent.setText(content.substring(0, number));
//						if (content.length()<= number2 ) {
//							tv_nousContent2.setText(content.substring(number, content.length()));
//						} else {
//							tv_nousContent2.setText(content.substring(number, number2) + "...");
//						}
//					}else if (content.length() > 0 && content.length() < number) {
//						tv_nousContent.setText(content);
//					}
				}
			}
			setOnclick(map, content_layout, position);
		}
	}
	// 绑定点击动作
	private void setOnclick(final Map<String, String> map, final View view, final int i) {
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TextView tv_nousTitle = (TextView) view.findViewById(R.id.tv_nousTitle);
				tv_nousTitle.setTextColor(act.getResources().getColor(R.color.c_gray_999999));
				XHClick.mapStat(act, "a_nouse", "内容点击数", "");
//				AdapterNousHome.this.notifyDataSetChanged();
				if(map.size()==3){
					if(map.containsKey("url")&&map.get("url").length()!=0){
						AppCommon.openUrl(act, map.get("url"), true);
					}
				}else{
					AppCommon.openUrl(act, StringManager.api_nouseInfo + map.get("code"), true);
//					AppCommon.openUrl(act, "nousInfo.app?code=" + map.get("code"), true);
					FileManager.setSharedPreference(act,FileManager.USERCHECK , map.get("code"));
				}
			}
		});
	}
}
