package com.smartdevice.scannersetting;

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

public class ActivityQrcodeSettingME51xx extends ListActivity {

	private static final String TAG = null;

	/*
	 * 明德二维条码恢复出厂设置
	 */
	byte[] defaultSetting2D = new byte[] { 0x16, 0x4D, 0x0D,
			0x25, 0x25, 0x25, 0x44, 0x45, 0x46, 0x2E };

	/*
	 * 明德二维条码设置数据格式，回车换行
	 */
	byte[] dataTypeFor2D = new byte[] { 0x16, 0x4D, 0x0D, 0x38,
			0x32, 0x30, 0x32, 0x44, 0x30, 0x31, 0x2E };

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
				
				ScannerActivity.scanner.sendCommand(defaultSetting2D);
				ScannerActivity.scanner.sendCommand(new byte[]{0x16,0x54,0x0D});
				
				Toast toast = Toast.makeText(this,"success",
						Toast.LENGTH_LONG);
				toast.show();
			break;
		case 1:
			ScannerActivity.scanner.sendCommand(dataTypeFor2D);
				toast = Toast.makeText(this,"success",
						Toast.LENGTH_LONG);
				toast.show();
			break;
		default:
			break;
		}
	}

	/**
	 * ����SimpleAdapter�ĵڶ����������ΪList<Map<?,?>>
	 * 
	 * @param title
	 * @return
	 */
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
	
	 /**
	  * 将byte数组转换为字符串形式表示的十六进制数方便查看
	  */
	 public static StringBuffer bytesToString(byte[] bytes)
	 {
	  StringBuffer sBuffer = new StringBuffer();
	  for (int i = 0; i < bytes.length; i++)
	  {
	   String s = Integer.toHexString(bytes[i] & 0xff);
	   if (s.length() < 2)
	    sBuffer.append('0');
	   sBuffer.append(s + " ");
	  }
	  return sBuffer;
	 }


	 private static byte charToByte(char c)
	 {
	  return (byte) "0123456789abcdef".indexOf(c);
	 }
}
