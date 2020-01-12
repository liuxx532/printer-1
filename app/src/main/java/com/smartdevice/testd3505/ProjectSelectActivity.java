package com.smartdevice.testd3505;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.smartdevicesdk.adapter.SpinnerManage;
import com.smartdevicesdk.printer.PrinterClassSerialPort3505;
import com.smartdevicesdk.printer.PrinterCommand;

import java.util.Date;

import static com.smartdevice.testd3505.PrinterActivity.resizeImage;

/**
 * Created by lgx on 2018/3/13.
 */

public class ProjectSelectActivity extends Activity {

    private EditText spinner1, spinner2, spinner3;
    private String spinnerNum1, spinnerNum2, spinnerNum3;
    private Button printerBtn;
    private TextView currentTimeText;
    private String currentTime;
    private ImageView img;
    private Bitmap btMap = null;


    private PrinterClassSerialPort3505 printerClass = null;
    private String device = "/dev/ttyUSB1";
    private int baudrate = 115200;// 38400
    private boolean close_printer = true;
    private String[] arrayString;
    private Thread autoprint_Thread;

    @SuppressLint("HandlerLeak")
    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PrinterCommand.MESSAGE_READ:
                case PrinterCommand.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case PrinterCommand.SUCCESS_CONNECT:
                            printerClass.write(new byte[]{0x1b, 0x2b});
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
            }
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_select);
        spinner1 = (EditText) findViewById(R.id.spinner1);
        spinner2 = (EditText) findViewById(R.id.spinner2);
        spinner3 = (EditText) findViewById(R.id.spinner3);
        printerBtn = (Button) findViewById(R.id.printerBtn);
        currentTimeText = (TextView) findViewById(R.id.currentTimeText);
        img = (ImageView) findViewById(R.id.img);

        String currentTimeDay = DateUtils.formatDateTime(this, System.currentTimeMillis(), 4);
        String currentTimeHour = DateUtils.formatDateTime(this, System.currentTimeMillis(), 5);
        currentTime = currentTimeDay + " " + currentTimeHour;
        currentTimeText.setText(currentTime);

        setOnClick();

        // 获取当前默认的打印机的串口和波特率
        //get the default printer serial port and baud rate
        device = MainActivity.devInfo.getPrinterSerialport();
        baudrate = MainActivity.devInfo.getPrinterBaudrate();

        // 初始化打印类 Initialization print class
        printerClass = new PrinterClassSerialPort3505(device, baudrate, mhandler);
        // 启动自动打印线程 start the thread to auto print
        autoprint_Thread = new AutoPrintThread();
        autoprint_Thread.start();


        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mBatInfoReceiver, filter);

        arrayString = getResources().getStringArray(R.array.spinner_array);

        openAndcloseDevice();
    }


    private void openAndcloseDevice() {
        if (printerClass.mSerialPort.isOpen) {
            printerClass.close();
        } else {
            printerClass.device = device;
            printerClass.baudrate = baudrate;
            printerClass.open();
            printerClass.write(new byte[]{0x1b, 0x76});
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        close_printer = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (close_printer) {
            printerClass.close();
        }
    }

    private void setOnClick() {
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPic();
            }
        });
//
//
//        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                spinnerNum1 = arrayString[position];
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                spinnerNum2 = arrayString[position];
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                spinnerNum3 = arrayString[position];
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
        printerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printer();
            }
        });
    }

    @Override
    public void onBackPressed() {
        printerClass.close();
        super.onBackPressed();
    }

    private void printer() {

        printerClass.write(PrinterCommand.CMD_FONTSIZE_DOUBLE);
        printerClass.printText("肉             " + spinner1.getText().toString() + "\n");
        printerClass.printText("肠子           " + spinner2.getText().toString() + "\n");
        printerClass.printText("焖子           " + spinner3.getText().toString() + "\n");
        printerClass.write(PrinterCommand.CMD_SET_FONT_16x16);
        printerClass.printText("尚家柳肉食 " + "\n");
        printerClass.printText("联系电话: 13930896169" + "\n");
        printerClass.printText("日期: " + currentTime + "\n");
        printerClass.printText("欢 ……(^-^) " + "\n");
        printerClass.printText("迎 ……(^-^) " + "\n");
        printerClass.printText("下 ……(^-^) " + "\n");
        printerClass.printText("次 ……(^-^) " + "\n");

        printerClass.printImage(btMap);
//        printerClass.printUnicode("\n\n");
//        printerClass.printUnicode("\n\n");
//        printerClass.printUnicode("\n\n");
//        printerClass.printUnicode("\n\n");
//        printerClass.printUnicode("\n\n");
//        printerClass.printUnicode("\n\n");

    }

    class AutoPrintThread extends Thread {
        @Override
        public void run() {
            super.run();
            for (int i = 0; i < 10; i++) {
                printerClass.write(new byte[]{0x1b, 0x76});
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

        }
    }
    private static final int REQUEST_EX = 1;
    private void openPic() {
        close_printer = false;
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_EX);
    }
    private String picPath = "";
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
            img.setImageURI(selectedImage);
            btMap = BitmapFactory.decodeFile(picPath);
            if (btMap.getHeight() > 384) {
                btMap = BitmapFactory.decodeFile(picPath);
                img.setImageBitmap(resizeImage(btMap, 384, 384));

            }
            cursor.close();
        }

    }
}
