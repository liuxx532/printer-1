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

public class ActivityBarcodeSettingUE966 extends ListActivity {
	/**
	 * 明德一维条码恢复出厂设置
	 */
	byte[] defaultSetting1D = new byte[] { 0x04, (byte) 0xC8, 0x04, 0x00,
			(byte) 0xFF, 0x30 };

	/**
	 * 明德一维条码设置数据格式，回车换行
	 */
	byte[] dataTypeFor1D = new byte[] { 0x07, (byte) 0xC6, 0x04, 0x08, 0x00,
			(byte) 0xEB, 0x07, (byte) 0xFE, 0x35 };

	/**
	 * 明德一维条码，禁止ACK/NAK握手
	 */
	byte[] ack_nak_protocol1D = new byte[] { 0x07, (byte) 0xC6, 0x04, 0x08,
			0x00, (byte) 0x9F, 0x00, (byte) 0xFE, (byte) 0x88 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_barcode_barcodesetting);
		setListAdapter(new SimpleAdapter(this, getData("simple-list-item-2"),
				android.R.layout.simple_list_item_2, new String[] { "title",
						"description" }, new int[] { android.R.id.text1,
						android.R.id.text2 }));
	}

	protected void onListItemClick(ListView listView, View v, int position,
			long id) {
		switch (position) {
		case 0:
			ScannerActivity.scanner.sendCommand(defaultSetting1D);

			// 明德模块 设置主机模式07C60408008A08FE95

			// 明德 设置主机模式
			ScannerActivity.scanner.sendCommand(new byte[] { 0x07, (byte) 0xC6, 0x04, 0x08, 0x00,
					(byte) 0x8a, 0x08, (byte) 0xFE, (byte) 0x95 });
			
			// 明德 设置回车换行
			ScannerActivity.scanner.sendCommand(new byte[] { 0x07, (byte) 0xC6, 0x04, 0x08, 0x00,
					(byte) 0xEB, 0x07, (byte) 0xFE, 0x35 });
			
			ScannerActivity.scanner.sendCommand(ack_nak_protocol1D);
			
			Toast toast = Toast.makeText(this, "success", Toast.LENGTH_LONG);
			toast.show();
			break;
		case 1:
			ScannerActivity.scanner.sendCommand(dataTypeFor1D);
			ScannerActivity.scanner.sendCommand(ack_nak_protocol1D);
			toast = Toast.makeText(this, "success", Toast.LENGTH_LONG);
			toast.show();
			break;
		default:
			break;
		}
	}

	private List<Map<String, String>> getData(String title) {
		List<Map<String, String>> listData = new ArrayList<Map<String, String>>();

		Map<String, String> map = new HashMap<String, String>();
		map.put("title", getResources().getString(R.string.action_reset));
		map.put("description",
				getResources().getString(R.string.action_reset_desc));
		listData.add(map);

		map = new HashMap<String, String>();
		map.put("title", getResources().getString(R.string.action_datatype));
		map.put("description",
				getResources().getString(R.string.action_datatype_desc));
		listData.add(map);

		return listData;
	}
}
