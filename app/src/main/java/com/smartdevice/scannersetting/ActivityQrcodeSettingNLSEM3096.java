package com.smartdevice.scannersetting;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.smartdevice.testd3505.R;
import com.smartdevice.testd3505.ScannerActivity;

public class ActivityQrcodeSettingNLSEM3096 extends ListActivity {

	private static final String TAG = null;

	String defaultSetting2D = "NLS0001000;";
	
	String opensettingStr="NLS0006010;";
	String closesettingStr="NLS0006000;";

	
	String dataTypeFor2D = "NLS0310000=0x0D0A;";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_barcode_qrcodesetting);
		setListAdapter(new SimpleAdapter(this, getData("simple-list-item-2"),
				android.R.layout.simple_list_item_2, new String[] { "title",
						"description" }, new int[] { android.R.id.text1,
						android.R.id.text2 }));
	}

	protected void onListItemClick(ListView listView, View v, int position,
			long id) {
		switch (position) {
		case 0:
			try {
				ScannerActivity.scanner.sendCommand(defaultSetting2D.getBytes("US-ASCII"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				Toast toast = Toast.makeText(this,"success",
						Toast.LENGTH_LONG);
				toast.show();
			break;
		case 1:
			try {
				String dataStr=opensettingStr+dataTypeFor2D+closesettingStr;
				ScannerActivity.scanner.sendCommand(dataStr.getBytes("US-ASCII"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				toast = Toast.makeText(this,"success",
						Toast.LENGTH_LONG);
				toast.show();
			break;
		default:
			break;
		}
	}

	
	private List<Map<String, String>> getData(String title) {
		List<Map<String, String>> listData = new ArrayList<Map<String, String>>();

		// Reset
		Map<String, String> map = new HashMap<String, String>();
		map.put("title", getResources().getString(R.string.action_reset));
		map.put("description",
				getResources().getString(R.string.action_reset_desc));
		listData.add(map);

		// Set data type
		map = new HashMap<String, String>();
		map.put("title", getResources().getString(R.string.action_datatype));
		map.put("description",
				getResources().getString(R.string.action_datatype_desc));
		listData.add(map);

		return listData;
	}
}
