package amodule.search.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.xiangha.R;

import java.util.List;
import java.util.Map;

import acore.override.adapter.AdapterSimple;
import acore.tools.Tools;

/**
 * Title:AdapterListSearch.java Copyright: Copyright (c) 2014~2017
 * 
 * @author zeyu_t
 * @date 2014年10月14日
 */
@SuppressLint("ResourceAsColor")
public class AdapterSearch extends AdapterSimple {
	public String[] searchWords;
	public boolean markRed = false;

	public AdapterSearch(View parent, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
		super(parent, data, resource, from, to);
	}

	public void setSearchWords(String[] searchWords) {
		this.searchWords = searchWords;
	}

	@Override
	public void setViewText(TextView v, String text) {
		v.setVisibility(View.VISIBLE);
		if (text.equals("hide") | text.equals("精") | text.equals("步骤图") | text.equals("删除")) {
			super.setViewText(v, text);
		} else if (text.indexOf("type2") == 0) {
			v.setBackgroundResource(R.drawable.bg_round_green_type1);
			super.setViewText(v, "宜搭");
		} else if (text.indexOf("type1") == 0) {
			v.setBackgroundResource(R.drawable.bg_round_red_type2);
			super.setViewText(v, "相克");
		} else if (text.indexOf("folState1") == 0) {// 1-自己，2-没有关注，3-已关注
			super.setViewText(v, "hide");
		} else if (text.indexOf("folState2") == 0) {
			String color = Tools.getColorStr(mParent.getContext(),R.color.comment_color);
			v.setTextColor(Color.parseColor(color));
			super.setViewText(v, "关注");
		} else if (text.indexOf("folState3") == 0) {
			v.setTextColor(Color.parseColor("#C9C9C9"));
			super.setViewText(v, "已关注");
		} else if (searchWords == null) {
			super.setViewText(v, text);
		} else {
			super.setViewText(v, text);
//					if (textMaxWidth > 0 && v.getId() != R.id.tv_itemBurden) {
//						v.setMaxWidth(textMaxWidth);
//					}
					// 按搜索词标红
//					 if (text.indexOf(searchWord) >= 0) {
//						 for (int j = text.indexOf(searchWord); j < text.length();) {
//							 if (searchWord.equals(text.substring(j, j + searchWord.length()))) {
//								String color = Tools.getColorStr(mParent.getContext(),R.color.comment_color);
//								 style.setSpan(new ForegroundColorSpan(Color.parseColor(color)), j, j + searchWord.length(),
//										 Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
//								 j++;
//							 } else {
//								 if (text.indexOf(searchWord) >= 0) {
//									 j = text.indexOf(searchWord);
//								 } else
//									 break;
//							 }
//						 }
//					 }
					//二次注释
//			SpannableStringBuilder style = new SpannableStringBuilder(text);
//			if (markRed) {
//				for (String searchWord : searchWords) {
//					for (int i = 0; i < searchWord.length(); i++) {
//						if (text.indexOf(searchWord.charAt(i)) > 0){
//							// 按搜索词逐个字标红
//							String keyWord = Character.toString(searchWord.charAt(i));
//							for (int j = text.indexOf(searchWord.charAt(i)); j < text.length(); j++) {
//								// 设置指定位置文字的颜色
//								if (keyWord.equals(Character.toString(text.charAt(j)))) {
//									String color = Tools.getColorStr(mParent.getContext(),R.color.comment_color);
//									style.setSpan(new ForegroundColorSpan(Color.parseColor(color)), j, j + 1,
//											Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
//								}
//							}
//						}
//					}
//				}
//			}
//			v.setText(style);
		}
	}
}
