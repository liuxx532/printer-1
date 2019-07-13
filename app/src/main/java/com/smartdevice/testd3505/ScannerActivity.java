/** 
 *  
 * @author	xuxl
 * @email	leoxuxl@163.com
 * @version  
 *     1.0 2015年12月23日 下午5:50:17 
 */ 
package com.smartdevice.testd3505;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.TextView;

import com.smartdevice.scannersetting.ActivityBarCodeCMD;
import com.smartdevice.scannersetting.ActivityBarcodeSettingUE966;
import com.smartdevice.scannersetting.ActivityQrcodeSettingME51xx;
import com.smartdevice.scannersetting.ActivityQrcodeSettingNLSEM3096;
import com.smartdevice.testd3505.media.TipSound;
import com.smartdevicesdk.scanner.ScannerHelper3505;
import com.smartdevicesdk.utils.HandlerMessage;

/** 
 * This class is used for : 
 *  
 * @author	xuxl
 * @email	leoxuxl@163.com
 * @version  
 *     1.0 2015年12月23日 下午5:50:17 
 */
public class ScannerActivity extends Activity {
	private static final String TAG = "ScannerActivity";
	public static ScannerHelper3505 scanner=null;
	Button btnScan;
	TextView textView;
	private boolean cannotReceive = true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scanner);
		
		btnScan=(Button)findViewById(R.id.btnScan);
		btnScan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cannotReceive = false;
				textView.setText("please on the bar code");
				scanner.scan();
			}
		});
		
		textView=(TextView)findViewById(R.id.textView_scan);
		
		Handler handler=new Handler(){
			public void handleMessage(android.os.Message msg) {
				if(msg.what==HandlerMessage.SCANNER_DATA_MSG)
				{
					if(cannotReceive){
						cannotReceive = false;
						return;
					}
					TipSound.playScanSound(getBaseContext());
					textView.setText(msg.obj.toString());
				}
			};
		};

		String device=MainActivity.devInfo.getScannerSerialport();
		int baudrate=MainActivity.devInfo.getScannerBaudrate();
		scanner=new ScannerHelper3505(device, baudrate, handler);
	}
	
	private void getOverflowMenu() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, 1, 1, R.string.action_1d);
		menu.add(0, 2, 2, R.string.action_2d);
		menu.add(0, 3, 3, R.string.action_2d_nls_em3096);
		menu.add(0, 4, 4, R.string.action_send_cmd);
		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 1) {
			Intent intent = new Intent();
			intent.setClass(ScannerActivity.this, ActivityBarcodeSettingUE966.class);
			startActivity(intent);
		} else if (item.getItemId() == 2) {
			Intent intent = new Intent();
			intent.setClass(ScannerActivity.this, ActivityQrcodeSettingME51xx.class);
			startActivity(intent);
		} else if (item.getItemId() == 3) {
			Intent intent = new Intent();
			intent.setClass(ScannerActivity.this, ActivityQrcodeSettingNLSEM3096.class);
			startActivity(intent);
		} else if (item.getItemId() == 4) {
			Intent intent = new Intent();
			intent.setClass(ScannerActivity.this, ActivityBarCodeCMD.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.i(TAG, "key press code is "+keyCode);
		if(keyCode== 135){
			cannotReceive = false;
			textView.setText("please on the bar code");
			scanner.scan();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		scanner.close();
	}
}
