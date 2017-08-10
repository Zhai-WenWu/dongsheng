package amodule.user.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.xianghatest.R;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import acore.override.activity.base.BaseActivity;
import acore.override.adapter.AdapterSimple;
import xh.basic.tool.UtilLog;

public class CountryListActivity extends BaseActivity {
	// 国家号码规则
	private ArrayList<Map<String, String>> countryList;

	private ListView country_list;

	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity("国家和地区", 2, 0, R.layout.c_view_bar_title, R.layout.user_country_list);
		intiView();
		initData();
		setListener();
	}

	private void intiView() {
		country_list = (ListView) findViewById(R.id.country_list);
	}

	private void initData() {
		countryList = new ArrayList<>();
		AdapterSimple adapter = new AdapterSimple(country_list, countryList,
				R.layout.user_country_list_item,
				new String[] { "name" }, 
				new int[] {R.id.country_name});
		country_list.setAdapter(adapter);
		InputStream is = null;
		try {
			is = this.getResources().getAssets().open("country.xml");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); // 取得DocumentBuilderFactory实例
			DocumentBuilder builder = factory.newDocumentBuilder(); // 从factory获取DocumentBuilder实例
			Document doc = builder.parse(is); // 解析输入流 得到Document实例
			Element rootElement = doc.getDocumentElement();
			NodeList items = rootElement.getElementsByTagName("country");
			for (int i = 0; i < items.getLength(); i++) {
				Node property = items.item(i);
				String content = property.getFirstChild().getNodeValue();
				if(content != null){
					Map<String, String> map = new HashMap<String, String>();
					map.put("name", content.substring(0, content.lastIndexOf("+")));
					map.put("countryId", content.substring(content.lastIndexOf("+")+1, content.length()));
					countryList.add(map);
				}
			}
		} catch (IOException e) {
			UtilLog.reportError("IO异常", e);
		} catch (ParserConfigurationException e) {
			UtilLog.reportError("xml parser异常", e);
		} catch (SAXException e) {
			UtilLog.reportError("SAX异常", e);
		}finally{
			try {
				if(is != null) is.close();
			} catch (IOException e) {
				UtilLog.reportError("IO异常", e);
			}
		}
	}
	
	private void setListener() {
		country_list.setOnItemClickListener(new OnItemClickListener() {
			@Override 
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent();
				intent.putExtra("countryId", countryList.get(position).get("countryId"));
				intent.putExtra("country", countryList.get(position).get("name"));
				setResult(100, intent);
				finish();
			}
		});
	}
}
