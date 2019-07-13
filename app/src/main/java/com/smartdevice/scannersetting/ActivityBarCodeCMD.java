/** 
 *  
 * @author	xuxl
 * @version  
 *     1.0 2016年7月21日 下午5:04:09 
 */ 
package com.smartdevice.scannersetting;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.smartdevice.testd3505.R;
import com.smartdevice.testd3505.ScannerActivity;
import com.smartdevicesdk.utils.StringUtility;

/** 
 * This class is used for : 
 *  
 * @author	xuxl
 * @version  
 *     1.0 2016年7月21日 下午5:04:09 
 */
public class ActivityBarCodeCMD extends Activity {
	TextView textView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_barcode_cmd);
		
		textView=(TextView)findViewById(R.id.textView_info);
		
		Button button_send=(Button)findViewById(R.id.button_send);
		button_send.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				EditText editText_cmd=(EditText)findViewById(R.id.editText_cmd);
				
				String str=editText_cmd.getText().toString();
				byte[] bt=StringUtility.StringToByteArray(str);
				if(bt!=null&&bt.length>0){
					byte[] btRec= ScannerActivity.scanner.sendCommand(new byte[]{0x4e, 0x4c, 0x53, 0x30, 0x30, 0x30, 0x31, 0x30, 0x30, 0x30});
					String hexString=StringUtility.ByteArrayToString(btRec, btRec.length);
					String textString = null;
					try {
						textString = new String(btRec, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					textView.setText("HEX:"+hexString+"\r\nTEXT:"+textString);
				}
			}
		});
	}
}
