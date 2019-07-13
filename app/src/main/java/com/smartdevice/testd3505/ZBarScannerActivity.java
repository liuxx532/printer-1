package com.smartdevice.testd3505;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.smartdevice.testd3505.media.TipSound;
import com.smartdevicesdk.camerascanner.CameraCheck;
import com.smartdevicesdk.camerascanner.CameraPreview;
import com.smartdevicesdk.camerascanner.ZBarConstants;

public class ZBarScannerActivity extends Activity {

	private static final String TAG = "ZBarScannerActivity";
	public static Camera mCamera;
	public static boolean isScanOpen = false;
	private CameraPreview mPreview;
	private Handler autoFocusHandler;
	ImageScanner scanner;
	private boolean previewing = true;
	boolean isStartScan = false;
	FrameLayout preview;
	/**
	 * 摄像头位置
	 */
	int cameraInfoState = 0;

	/**
	 * 条码信息
	 */
	private String codeMessage = null;

	/**
	 * 解码库
	 */
	static {
		System.loadLibrary("iconv");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 去掉标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.zbar_capture);

		preview = (FrameLayout) findViewById(R.id.cameraPreview);

		isScanOpen = true;

		ImageView imageView_change = (ImageView) findViewById(R.id.imageView_change);
		imageView_change.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (cameraInfoState == CameraInfo.CAMERA_FACING_BACK) {
					cameraInfoState = CameraInfo.CAMERA_FACING_FRONT;
				} else {
					cameraInfoState = CameraInfo.CAMERA_FACING_BACK;
				}
				initCamera(cameraInfoState);
			}
		});

		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {// SDK版本必须大于2.3
			int cameraCount = Camera.getNumberOfCameras();
			if (cameraCount < 2) {// 多余两个摄像头，显示切换按钮
				imageView_change.setVisibility(View.GONE);
			}
		}
	}

	@Override
	protected void onResume() {
		if (CameraCheck.hasBackFacingCamera()) {
			cameraInfoState = CameraInfo.CAMERA_FACING_BACK;
		} else if (CameraCheck.hasFrontFacingCamera()) {
			cameraInfoState = CameraInfo.CAMERA_FACING_FRONT;
		} else {
			Toast.makeText(getApplicationContext(),
				"无法连接到摄像头\r\nUnable to connect to the camera", 300).show();
			finish();
		}
		initCamera(cameraInfoState);
		super.onResume();
	}

	private void initCamera(int cameraInfo) {
		isStartScan = true;
		try {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			autoFocusHandler = new Handler();

			/* Instance barcode scanner */
			scanner = new ImageScanner();
			scanner.setConfig(0, Config.X_DENSITY, 3);
			scanner.setConfig(0, Config.Y_DENSITY, 3);
			preview.removeAllViews();

			mCamera = Camera.open(cameraInfo);

			mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);

			preview.addView(mPreview);

			mCamera.setPreviewCallback(previewCb);

			mCamera.startPreview();
			previewing = true;
			mCamera.autoFocus(autoFocusCB);
			
			mCamera.setDisplayOrientation(90);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPause() {
		releaseCamera();
		isScanOpen = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		isScanOpen = false;
		super.onDestroy();
	}

	private void releaseCamera() {
		if (mCamera != null) {
			isStartScan = false;
			previewing = false;
			mCamera.stopPreview();
		}
	}

	// 自动对焦
	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (previewing)
				mCamera.autoFocus(autoFocusCB);
		}
	};

	PreviewCallback previewCb = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			Camera.Parameters parameters = camera.getParameters();
			Size size = parameters.getPreviewSize();
			if (isStartScan) {
				Image barcode = new Image(size.width, size.height, "Y800");
				barcode.setData(data);

				int result = scanner.scanImage(barcode);

				if (result != 0) {

					previewing = false;
					// 获取扫描数据
					SymbolSet syms = scanner.getResults();
					for (Symbol sym : syms) {
						codeMessage = sym.getData();
						if (codeMessage != "") {
							isStartScan = false;
							Toast.makeText(getApplicationContext(),
									codeMessage, 300).show();
							Intent dataIntent = new Intent();
							dataIntent.putExtra(ZBarConstants.SCAN_RESULT,
									codeMessage);
							dataIntent.putExtra(ZBarConstants.SCAN_RESULT_TYPE,
									sym.getType());
							setResult(Activity.RESULT_OK, dataIntent);
							TipSound.playScanSound(ZBarScannerActivity.this);
							finish();
							break;
						}
					}
				}
			}
		}
	};

	/**
	 * 自动对焦回调
	 */
	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};
}
