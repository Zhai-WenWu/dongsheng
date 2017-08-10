/**
 * 
 * @author intBird 20140213.
 * 
 */
package amodule.quan.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.xianghatest.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;
import acore.widget.TextViewShow;

public class AdapterQuanFriend extends AdapterSimple {
	private List<? extends Map<String, ?>> data;
	private List<String> list = new ArrayList<String>();
	private BaseActivity act;
	private String value;

	public AdapterQuanFriend(BaseActivity act, View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to,
			String value) {
		super(parent, data, resource, from, to);
		this.data = data;
		this.act = act;
		this.value = value;
		init();
	}

	@SuppressLint("HandlerLeak")
	private void init() {
		list.clear();
		ArrayList<String> indexList = TextViewShow.getStringList(value, '@', ' ');
		for (int i = 0; i < indexList.size(); i++) {
			list.add(indexList.get(i));
		}
	}

	public String getChooseList(){
		return list.toString();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = super.getView(position, convertView, parent);
		ImageView iv = (ImageView) convertView.findViewById(R.id.friend_iv_choose);
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) data.get(position);
		setOnclick(map, convertView, position);
		String nickName = map.get("nickName");
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(nickName)) {
				map.put("flag", "true");
				break;
			} else
				map.put("flag", "false");
		}
		if (map.get("flag").equals("true"))
			iv.setImageResource(R.drawable.i_ico_ok);
		else
			iv.setImageResource(R.drawable.i_ico_nook);
		ImageView friend_tv_lv = (ImageView) convertView.findViewById(R.id.friend_tv_lv);
		AppCommon.setLvImage(Integer.valueOf(map.get("lv")), friend_tv_lv);
		
		return convertView;
	}

	// 绑定点击动作
	private void setOnclick(final Map<String, String> map, final View view, final int i) {
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String name = map.get("nickName");
				boolean isHave = false;
				for (int k = 0; k < list.size(); k++) {
					if (list.get(k).equals(name)) {
						((ImageView) view.findViewById(R.id.friend_iv_choose)).setImageResource(R.drawable.i_ico_nook);
						map.put("flag", "false");
						list.remove(k);
						isHave = true;
						break;
					}
				}
				if (!isHave && list.size() == 5) {
					Tools.showToast(act, "最多可以@5个人");
				} else if (!isHave) {
					((ImageView) view.findViewById(R.id.friend_iv_choose)).setImageResource(R.drawable.i_ico_ok);
					map.put("flag", "true");
					list.add(name);
				}
			}
		});

	}
}
