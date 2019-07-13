/** 
 *  
 * @author	xuxl
 * @email	leoxuxl@163.com
 * @version  
 *     1.0 2016年1月18日 上午11:10:24 
 */
package com.smartdevice.testd3505;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.smartdevice.testd3505.R;
import com.smartdevicesdk.cscreen.CustomerScreenHelper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class CustomerScreenActivity extends Activity implements OnClickListener {
	CustomerScreenHelper cs;
	ImageView imageView1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cscreen);

		Button button_dot = (Button) findViewById(R.id.button_dot);
		button_dot.setOnClickListener(this);
		
		Button button_rgb565=(Button)findViewById(R.id.button_rgb565);
		button_rgb565.setOnClickListener(this);
		
		imageView1=(ImageView)findViewById(R.id.imageView1);
	}

	@Override
	protected void onResume() {
		String device = "/dev/ttyUSB1";// MainActivity.devInfo.getPrinterSerialport();
		int baudrate = 460800;// MainActivity.devInfo.getPrinterBaudrate();
		cs = new CustomerScreenHelper(device, baudrate);

		if (cs.open()) {
			cs.openBackLight((byte) 0x01);
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		cs.openBackLight((byte) 0x00);
		cs.close();
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_dot:
			if (colorIndex < colorArray.length - 1) {
				colorIndex++;
			} else {
				colorIndex = 0;
			}
			Bitmap bm= getBitmap();
			if(cs.ShowDotImage(Color.BLACK, colorArray[colorIndex],bm))
			{
				imageView1.setImageBitmap(bm);
			}
			break;
		case R.id.button_rgb565:
			Bitmap bmRGB565=BitmapFactory.decodeResource(getResources(), R.drawable.minions);
			if(cs.ShowRGB565Image(bmRGB565))
			{
				imageView1.setImageBitmap(bmRGB565);
			}
			break;
		default:
			break;
		}
	}

	int[] colorArray = new int[] { Color.BLUE, Color.GRAY, Color.GREEN, Color.LTGRAY, Color.MAGENTA,
			Color.RED, Color.YELLOW };
	int colorIndex = 0;

	private Bitmap getBitmap() {
		Bitmap bitmap = Bitmap.createBitmap(320, 240, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setTextSize(80);

		DateFormat df = new SimpleDateFormat("HH:mm:ss");
		String ss = df.format(new Date());
		canvas.drawColor(Color.WHITE);
		canvas.drawText(ss, 10, 100, paint);
		//imageView1.setImageBitmap(bitmap);
		return bitmap;
	}
}
