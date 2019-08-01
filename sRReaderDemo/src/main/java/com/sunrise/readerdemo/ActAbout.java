package com.sunrise.readerdemo;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

public class ActAbout extends Activity {
	private TextView mTvSoftVer;
	private TextView mTvCorp;
	private TextView mTvSoftVerjar;
	private TextView mDriveceID;
	private TextView mTvApiVer;

	private String deviceId = "";
	private String sofeVer = "";
	private String apiVer = "";
	      
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);   
		setContentView(R.layout.act_about2);

		Bundle bundle = getIntent().getExtras();
		if(!bundle.isEmpty()){
			apiVer = bundle.getString("apiVersion");
			deviceId = bundle.getString("drivceID");
			sofeVer = bundle.getString("softVersion");
		}

		mTvSoftVer = (TextView) findViewById(R.id.tvSoftVer);
		mTvCorp = (TextView) findViewById(R.id.tvCorp);
		mTvSoftVerjar = (TextView) findViewById(R.id.tvSoftVerjar);
		mDriveceID = (TextView) findViewById(R.id.tv_drvice_id);
		mTvApiVer = (TextView) findViewById(R.id.tvApiVer);


		PackageManager pm = getPackageManager();

		PackageInfo pinfo = null;
		try {
			pinfo = pm.getPackageInfo(this.getPackageName(), PackageManager.GET_CONFIGURATIONS);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		int versionCode = pinfo.versionCode;
		mTvSoftVer.setText("App版本信息:" + versionCode);
		mTvSoftVerjar.setText("驱动版本:" + sofeVer);
		mDriveceID.setText("设备ID:" + deviceId);
		mTvApiVer.setText("接口版本:" + apiVer);

	}
        
	          
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {   
				finish();			  
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
