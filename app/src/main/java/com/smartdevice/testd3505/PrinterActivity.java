/** 
 *  
 * @author	xuxl
 * @email	xuxingliu922@163.com
 * @version  
 *     1.0 2015年12月22日 上午11:38:42 
 */
package com.smartdevice.testd3505;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.smartdevice.testd3505.printer.PrinterHelper;
import com.smartdevice.testd3505.printer.entity.SupermakerBill;
import com.smartdevicesdk.adapter.SpinnerManage;
import com.smartdevicesdk.printer.BarcodeCreater;
import com.smartdevicesdk.printer.PrintService;
import com.smartdevicesdk.printer.PrinterClassSerialPort3505;
import com.smartdevicesdk.printer.PrinterCommand;
import com.smartdevicesdk.utils.TypeConversion;

/**
 * This class is used for : Printer
 * 
 * @author xuxl
 * @email xuxingliu922@163.com
 * @version 1.0 2015年12月22日 上午11:38:42
 */
public class PrinterActivity extends Activity implements OnClickListener, OnItemSelectedListener{
	PrinterClassSerialPort3505 printerClass = null;
	List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
	private static final int REQUEST_EX = 1;
	protected static final String TAG = "PrintDemo";

	private int cutTimes = 1;

	private Thread autoprint_Thread;
	boolean isPrint = true;
	int times = 1500;// Automatic print time interval

	private ImageView iv = null;
	private boolean printFlag;

	private String picPath = "";
	private Bitmap btMap = null;
	private Button btnQrCode = null;
	private Button btnBarCode = null;
	private Button btnWordToPic = null;
	private Button btnUnicode;
	private Button btnOpenDevice;
	private Button btnPrint = null;
	private Button btnOpenPic = null;
	private Button btnPrintPic = null;
	private boolean printWeak = false;

	// private RadioButton radiobutton_cut,radiobutton_cutall;
	private RadioGroup radio_cut;

	private TextView textViewState = null, textView_msg;
	private EditText et_input = null;
	private CheckBox checkBoxAuto = null;

	private Spinner spinner_device;
	private Spinner spinner_baudrate;

	String thread = "readThread";
	String text = "abckefghijklmnopkrstuvwsyz1234567890打印测试";

	private String device = "/dev/ttyUSB1";
	private int baudrate = 115200;// 38400
	private boolean close_printer = true;
	
	private Button btnPrintModel1, btnPrintModel2, btnPrintModel3, btnPrintJump;
	
	 @SuppressLint("HandlerLeak")
	 Handler mhandler = new Handler() {
		@SuppressLint("SetTextI18n")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PrinterCommand.MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				Log.i(TAG, "readBuf:" + readBuf[0]);
				if (readBuf[0] == 0x13) {
					checkBoxAuto.setChecked(false);
					PrintService.isFUll = true;
					textViewState.setText(getResources().getString(
							R.string.str_printer_state)
							+ ":"
							+ getResources().getString(
									R.string.str_printer_bufferfull));
					printFlag = false;
				} else if (readBuf[0] == 0x11) {
					PrintService.isFUll = false;
					textViewState.setText(getResources().getString(
							R.string.str_printer_state)
							+ ":"
							+ getResources().getString(
									R.string.str_printer_buffernull));
					
				} else if (readBuf[0] == 0x08) {
					checkBoxAuto.setChecked(false);
					textViewState.setText(getResources().getString(
							R.string.str_printer_state)
							+ ":"
							+ getResources().getString(
									R.string.str_printer_nopaper));
				} else if (readBuf[0] == 0x01) {
					textViewState.setText(getResources().getString(
							R.string.str_printer_state)
							+ ":"
							+ getResources().getString(
									R.string.str_printer_printing));
					printFlag = false;
				} else if (readBuf[0] == 0x04) {
					checkBoxAuto.setChecked(false);
					textViewState.setText(getResources().getString(
							R.string.str_printer_state)
							+ ":"
							+ getResources().getString(
									R.string.str_printer_hightemperature));

				} else if (readBuf[0] == 0x02) {
					checkBoxAuto.setChecked(false);
					textViewState.setText(getResources().getString(
							R.string.str_printer_state)
							+ ":"
							+ getResources().getString(
									R.string.str_printer_lowpower));
				} else if (readBuf[0] == 0x00) {
					printFlag = true;
				} else {
					String readMessage = new String(readBuf, 0, msg.arg1);
					if (readMessage.contains("800"))// 80mm paper
					{
						PrintService.imageWidth = 72;
						Toast.makeText(getApplicationContext(), "80mm",
								Toast.LENGTH_SHORT).show();
					} else if (readMessage.contains("580"))// 58mm paper
					{
						PrintService.imageWidth = 48;
						Toast.makeText(getApplicationContext(), "58mm",
								Toast.LENGTH_SHORT).show();
					}
				}
				break;
			case PrinterCommand.MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case PrinterCommand.STATE_CONNECTED:
					break;
				case PrinterCommand.STATE_CONNECTING:
					Toast.makeText(getApplicationContext(),
							"STATE_CONNECTING", Toast.LENGTH_SHORT).show();
					break;
				case PrinterCommand.STATE_LISTEN:
				case PrinterCommand.STATE_NONE:
					break;
				case PrinterCommand.SUCCESS_CONNECT:
					printerClass.write(new byte[] { 0x1b, 0x2b });
					Toast.makeText(getApplicationContext(),
							"SUCCESS_CONNECT", Toast.LENGTH_SHORT).show();
					break;
				case PrinterCommand.FAILED_CONNECT:
					Toast.makeText(getApplicationContext(),
							"FAILED_CONNECT", Toast.LENGTH_SHORT).show();

					break;
				case PrinterCommand.LOSE_CONNECT:
					Toast.makeText(getApplicationContext(), "LOSE_CONNECT",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case PrinterCommand.MESSAGE_WRITE:

				break;
			case PrinterCommand.PERMIT_PRINTER:
				String result = (String) msg.obj;
				Toast.makeText(getApplicationContext(),R.string.permit_printer, Toast.LENGTH_SHORT).show();
				break;
			case PrinterCommand.FORBID_PRINTER:
				String forbid_print = (String) msg.obj;
				Toast.makeText(getApplicationContext(),R.string.forbid_print, Toast.LENGTH_SHORT).show();
				break;
			case PrinterCommand.TIMEOUT_PRINTER:
				String print_timeout = (String) msg.obj;
				Toast.makeText(getApplicationContext(),R.string.open_print_function, Toast.LENGTH_SHORT).show();
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_printer);
		getOverflowMenu();

		// 初始化视图 Initialization view
		initView();
		// 初始化数据 Initialization data
		initData();
		// 初始化事件 Initialization event
		initEvent();
		// 初始化打印类 Initialization print class
		printerClass = new PrinterClassSerialPort3505(device, baudrate, mhandler);
		// 启动自动打印线程 start the thread to auto print
		autoprint_Thread = new AutoPrintThread();
		autoprint_Thread.start();
		
		final IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(mBatInfoReceiver, filter);
	}

	private void initData() {
		// 自动生成待打印条码样例 
		//generate bar code waiting to print
		btnBarCode.performClick();
		// 设置默认打印测试文字 
		//set the default print text
		et_input.setText(text);
		// 获取当前默认的打印机的串口和波特率
		//get the default printer serial port and baud rate
		device = MainActivity.devInfo.getPrinterSerialport();
		baudrate = MainActivity.devInfo.getPrinterBaudrate();
		// 设置默认串口和波特率 
		//set the default printer serial port and baud rate
		SpinnerManage.setDefaultItem(spinner_device, device);
		SpinnerManage.setDefaultItem(spinner_baudrate, baudrate);
	}

	private void initView() {
		radio_cut = (RadioGroup) findViewById(R.id.radio_cut);
		textView_msg = (TextView) findViewById(R.id.textView_msg);
		textViewState = (TextView) findViewById(R.id.textViewState);
		et_input = (EditText) findViewById(R.id.editText1);
		btnUnicode = (Button) findViewById(R.id.btnUnicode);
		btnPrint = (Button) findViewById(R.id.btnPrint);
		btnPrintModel1 = (Button) findViewById(R.id.btnPrintModel1);
		btnPrintModel2 = (Button) findViewById(R.id.btnPrintModel2);
		btnPrintModel3 = (Button) findViewById(R.id.btnPrintModel3);
		btnPrintJump = (Button) findViewById(R.id.btnPrintJump);
		btnOpenPic = (Button) findViewById(R.id.btnOpenPic);
		btnPrintPic = (Button) findViewById(R.id.btnPrintPic);
		checkBoxAuto = (CheckBox) findViewById(R.id.checkBoxTimer);
		iv = (ImageView) findViewById(R.id.iv_test);
		btnQrCode = (Button) findViewById(R.id.btnQrCode);
		btnBarCode = (Button) findViewById(R.id.btnBarCode);
		btnWordToPic = (Button) findViewById(R.id.btnWordToPic);
		btnOpenDevice = (Button) findViewById(R.id.btnopendevice);
		spinner_device = (Spinner) findViewById(R.id.spinner1);
		spinner_baudrate = (Spinner) findViewById(R.id.spinner2);
	}

	private void initEvent() {
		btnPrintModel1.setOnClickListener(this);
		btnPrintModel2.setOnClickListener(this);
		btnPrintModel3.setOnClickListener(this);
		btnPrintJump.setOnClickListener(this);
		btnOpenDevice.setOnClickListener(this);
		btnQrCode.setOnClickListener(this);
		btnWordToPic.setOnClickListener(this);
		btnBarCode.setOnClickListener(this);
		btnPrint.setOnClickListener(this);
		btnUnicode.setOnClickListener(this);
		btnOpenPic.setOnClickListener(this);
		btnPrintPic.setOnClickListener(this);
		spinner_device.setOnItemSelectedListener(this);
		spinner_baudrate.setOnItemSelectedListener(this);
	}
	
	
	 class AutoPrintThread extends Thread{
		@Override
		public void run() {
			super.run();
			Bitmap mBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.order1);
			Bitmap myBitmap = resizeImage(mBitmap, 384, 748);
			// Bitmap printBitmap = getBitmapPrint(myBitmap);

			while (isPrint) {
				if (checkBoxAuto.isChecked()) {
					for (int i = 0; i < 10; i++) {
						printerClass.write(new byte[] { 0x1b, 0x76 });
						if (printFlag) {
							i = 10;
							printFlag = false;
						}
					}
					printerClass.printUnicode("H7UJ787-JU78UU6-JJ785J6-J876K76\n");
					printerClass.write(PrinterCommand.CMD_FONTSIZE_DOUBLE);
					printerClass.printUnicode("Quick Lotto (5/11)\n");
					printerClass.printUnicode("Terminal No: 85010002    SN:1\n");
					printerClass.printUnicode("Draw No: 20160503011\n");
					printerClass.printUnicode("Option3\n");
					printerClass.write(PrinterCommand.CMD_FONTSIZE_NORMAL);
					printerClass.printUnicode("A. 02 06 08 ₦200\n");
					printerClass.printUnicode("B. 02 06 08 ₦200\n");
					printerClass.printUnicode("C. 02 06 08 ₦200\n");
					printerClass.printUnicode("D. 02 06 08 ₦200\n");
					printerClass.printUnicode("E. 02 06 08 ₦200\n");
					printerClass.printUnicode("\n\n");
					printerClass.write(PrinterCommand.CMD_FONTSIZE_NORMAL);
				} else {
					cutTimes = 0;
				}
			}
		}
	}
    
	@Override
	protected void onResume() {
		super.onResume();
		close_printer = true;
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(close_printer){
			printerClass.close();
		}
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(mBatInfoReceiver);
		super.onDestroy();
	}
	
	private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (Intent.ACTION_SCREEN_ON.equals(action)) {
				printerClass.device = device;
				printerClass.baudrate = baudrate;
				printerClass.open();
			} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
				printerClass.close();

			}
		}

	};

	/**
	 * 调整图片大小以适应打印机图片大小
	 * @param bitmap
	 * @param w
	 * @param h
	 * @return
	 */
	public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
		Bitmap BitmapOrg = bitmap;
		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();
		int newWidth = w;
		int newHeight = h;

		if (width >= newWidth) {
			float scaleWidth = ((float) newWidth) / width;
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleWidth);
			Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
					height, matrix, true);
			return resizedBitmap;
		} else {
			Bitmap bitmap2 = Bitmap.createBitmap(newWidth, newHeight,
					bitmap.getConfig());
			Canvas canvas = new Canvas(bitmap2);
			canvas.drawColor(Color.WHITE);

			canvas.drawBitmap(BitmapOrg, (newWidth - width) / 2, 0, null);

			return bitmap2;
		}
	}

	/**
	 * 再次进入当前页面获取上一页面传递的值
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_EX && resultCode == RESULT_OK
				&& null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			picPath = picturePath;
			iv.setImageURI(selectedImage);
			btMap = BitmapFactory.decodeFile(picPath);
			if (btMap.getHeight() > 384) {
				btMap = BitmapFactory.decodeFile(picPath);
				iv.setImageBitmap(resizeImage(btMap, 384, 384));

			}
			cursor.close();
		}

	}

	/**
	 * 字符串转unicode编码的字节数组
	 * @param s
	 * @return
	 */
	static byte[] string2Unicode(String s) {
		try {
			byte[] bytes = s.getBytes("unicode");
			byte[] bt = new byte[bytes.length - 2];
			for (int i = 2, j = 0; i < bytes.length - 1; i += 2, j += 2) {
				bt[j] = (byte) (bytes[i + 1] & 0xff);
				bt[j + 1] = (byte) (bytes[i] & 0xff);
			}
			return bt;
		} catch (Exception e) {
			try {
				byte[] bt = s.getBytes("GBK");
				return bt;
			} catch (UnsupportedEncodingException e1) {
				Log.e(TAG, e.getMessage());
				return null;
			}
		}
	}

	/**
	 * 添加子菜单
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		Resources res = getResources();
		String[] cmdStr = res.getStringArray(R.array.cmd);
		for (int i = 0; i < cmdStr.length; i++) {
			String[] cmdArray = cmdStr[i].split(",");
			if (cmdArray.length == 2) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("title", cmdArray[0]);
				map.put("description", cmdArray[1]);
				menu.add(0, i, i, cmdArray[0]);
				listData.add(map);
			}
		}

		return true;
	}

	/**
	 * 显示菜单
	 */
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

	/**
	 * 验证相关打印指令
	 */
	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		Map map = listData.get(item.getItemId());
		String cmd = map.get("description").toString();

		byte[] bt = TypeConversion.hexStringToBytes(cmd);
		printerClass.write(bt);
		printerClass.printText(map.get("title").toString());
		Toast toast = Toast.makeText(this, "send success！", Toast.LENGTH_SHORT);
		toast.show();
		return false;
	}
	
	@Override
	public void onBackPressed() {
		printerClass.close();
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnopendevice:
			// 打开*关闭打印设备 open or close print device
			openAndcloseDevice();
			break;
		case R.id.btnQrCode:
			// 生成二维码图片 generate QR code picture
			generalQEcode("http://www.google.com");
			break;
		case R.id.btnWordToPic:
			// 文字转图片 word to picture
			woreToPic();
			break;
		case R.id.btnBarCode:
			// 生成一维条码图片 generate bar code
			generalBarcode("431D0E7D9CC19BC1FDAB7");
			break;
		case R.id.btnPrint:
			// 打印GBK编码文字 print text coding by GBK
			printGBKText();
			break;
		case R.id.btnUnicode:
			// 打印UNICODE编码文字 print text coding by UNICODE
			printUnicode();
			break;
		case R.id.btnOpenPic:
			// 选择打印图片 select picture to print
			openPic();
			break;
		case R.id.btnPrintPic:
			// 打印图片 print picture
			printPic();
			break;
		case R.id.btnPrintModel1:
			// 打印模板一 print mode one
			printPurcase(false, false);
			break;
		case R.id.btnPrintModel2:
			// 打印模板二 print mode two
			printPurcase(true, false);
			break;
		case R.id.btnPrintModel3:
			// 打印模板三 print mode three
			printPurcase(false, true);
			break;

		case R.id.btnPrintJump:
			// 打印模板三 print mode three
			jump2Select();
			break;

		default:
			break;
		}
		
	}

	private void jump2Select(){
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.setClass(PrinterActivity.this, ProjectSelectActivity.class);
		startActivity(intent);
	};

	private void printPurcase(boolean hasStartPic, boolean hasEndPic) {
		SupermakerBill bill = PrinterHelper.getInstance(this).getSupermakerBill(hasStartPic, hasEndPic);
		PrinterHelper.getInstance(this).printPurchaseBillModelOne(printerClass, bill);
	}

	private void printPic() {
		if (btMap != null) {
			printerClass.printImage(btMap);
		}
	}

	private void openPic() {
		close_printer = false;
		Intent intent = new Intent(
				Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, REQUEST_EX);
	}

	private void printUnicode() {
		String str = et_input.getText().toString();
		try {
			printerClass.printUnicode(str);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	private void printGBKText() {
		String str = et_input.getText().toString();
		try {
			printerClass.printText(str);

		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	private void generalBarcode(String message) {

//		if (message.getBytes().length > message.length()) {
//			Toast.makeText(PrinterActivity.this, "create error",
//					2000).show();
//			return;
//		}
		if (message.length() > 0) {
			btMap = BarcodeCreater.creatBarcode(
					PrinterActivity.this, message, 384, 50, false, BarcodeFormat.CODE_128);
			PrintService.imageWidth = 48;
			iv.setImageBitmap(btMap);
		}
		
	}

	private void woreToPic() {
		String str = et_input.getText().toString();
		btMap = Bitmap.createBitmap(384,
				et_input.getLineCount() * 25, Config.ARGB_8888);
		PrintService.imageWidth = 48;
		Canvas canvas = new Canvas(btMap);
		canvas.drawColor(Color.WHITE);
		TextPaint textPaint = new TextPaint();
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setColor(Color.BLACK);
		textPaint.setTextSize(25.0F);
		StaticLayout layout = new StaticLayout(str, textPaint,
				btMap.getWidth(), Alignment.ALIGN_NORMAL,
				(float) 1.0, (float) 0.0, true);

		layout.draw(canvas);
		iv.setImageBitmap(btMap);
	}

	private void generalQEcode(String message) {
		if (message.length() > 0) {
			try {
				message = new String(message.getBytes("utf8"));
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, e.getMessage());
			}
			btMap = BarcodeCreater.encode2dAsBitmap(message, 384, 384);
			PrintService.imageWidth = 48;
			iv.setImageBitmap(btMap);
		}
	}

	private void openAndcloseDevice() {
		if (printerClass.mSerialPort.isOpen) {
			printerClass.close();
			btnOpenDevice.setText(getResources().getString(
					R.string.opendevice));
		} else {
			printerClass.device = device;
			printerClass.baudrate = baudrate;
			printerClass.open();
			printerClass.write(new byte[] { 0x1b, 0x76 });
			btnOpenDevice.setText(getResources().getString(R.string.closedevice));
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if(parent.getId() == R.id.spinner1){
			device = spinner_device.getItemAtPosition(position).toString();
			if (printerClass.mSerialPort.isOpen) {
				printerClass.close();
				btnOpenDevice.setText(getResources().getString(
						R.string.opendevice));
			}
		}else if(parent.getId() == R.id.spinner2){
			
			String selectStr = spinner_baudrate.getItemAtPosition(
					position).toString();
			baudrate = Integer.parseInt(selectStr);
			if (printerClass.mSerialPort.isOpen) {
				printerClass.close();
				btnOpenDevice.setText(getResources().getString(
						R.string.opendevice));
			}
		}
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}

}