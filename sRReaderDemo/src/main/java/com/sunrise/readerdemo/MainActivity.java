/**
 * @(#)MainActivity.java 2016年01月07日
 * 版权所有 (c) 2008-2016 广州市森锐电子科技有限公司
 * @author 黄进捷
 */

package com.sunrise.readerdemo;

import java.text.SimpleDateFormat;
import java.util.*;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.*;
import android.view.*;
import android.widget.*;

import com.sunrise.reader.IDecodeIDServerListener;
import com.sunrise.reader.ReaderManager;
import com.sunrise.reader.ReaderManagerService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sunrise.DeviceInfo;
import sunrise.IDImageUtil;
import sunrise.bluetooth.SRBluetoothCardReader;
import sunrise.pos.POSCardReader;
import sunrise.nfc.SRnfcCardReader;
import sunrise.otg.SRotgCardReader;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import com.sunrise.icardreader.helper.ConsantHelper;
import com.sunrise.icardreader.model.IdentityCardZ;

import static com.sunrise.reader.ReaderManagerService.SHAREDPREFERENCE_NAME;

@SuppressLint("UseValueOf")
public class MainActivity extends Activity {

    private TextView tv_info;
    private TextView nameTextView;
    private TextView sexTextView;
    private TextView folkTextView;
    private TextView birthTextView;
    private TextView addrTextView;
    private TextView codeTextView;
    private TextView policyTextView;
    private TextView validDateTextView;
    private TextView samidTextView;
    private ImageView photoView;
    private Button buttonNFC, buttonBT;
    private TextView mplaceHolder;
    private CheckBox mCkbOtg;

    private String deviceId = "";
    private String softVersion = "";
    private String apiVersion = "";

    /**
     * 计时
     */
    private TextView timeTextView;
    private TextView endTimeTextView;
    private TextView timeCountTextView;
    private Date startTime;
    private Date stopTime;
    private SimpleDateFormat mFormatter;

    //IP
    private String server_address = "";
    //端口
    private int server_port = 0;

    //回调handler
    public static Handler uiHandler;

    //普通NFC方式接口类
    private SRnfcCardReader mNFCReaderHelper;
    //OTG方式接口类
    private SRotgCardReader mOTGReaderHelper;
    //蓝牙方式接口类
    private SRBluetoothCardReader mBlueReaderHelper;
    //特殊POS接口类
    private POSCardReader posCardReader;

    // ----蓝牙功能有关的变量----
    private BluetoothAdapter mBluetoothAdapter = null; // /蓝牙适配器

    //蓝牙地址
    private String Blueaddress = null;
    //蓝牙名称
    private String btName = null;

    private static final String TAG = "SunriseDemo";
    private static final int READ_ICCID_CONNECT_DEVICE = 3;
    private static final int READ_IDCARD_CONNECT_DEVICE = 4;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_SET_SERVER = 2;
    private static final int REQUEST_REPEAT =5;

    //是否是otg方式
    private boolean isOtg;

    private AlertDialog alertDialog;

    //是否绑定蓝牙设备
    boolean isRegisterBT = false;
    //是否绑定OTG设备
    boolean isRegisterOTG = false;

    private Context context;

    //连接蓝牙
    private static final int REGISTER_BLUETOOTH = 50;
    //连接OTG
    private static final int REGISTER_OTG = 51;
    //NFC开始读取
    private static final int NFC_START = 52;

    //已读身份证次数
    private int iDReadingCount = 0;
    //读身份证次数(用户设定)
    private int iDReadingNum = 1;

    //是否是标准NFC方式
    private boolean isNFC = false;

    //NFC回调
    NfcAdapter.ReaderCallback nfcCallBack;

    private int actionType;

    //显示服务器
    private TextView serverTv;
    private static final int SERVER_TV = 5555;

    //读身份证成功次数
    private int readIDSuccessCount = 0;
    private TextView successTv;

    private ProgressDialog processDia; //加载框

    //绑定NFC回调函数, SDK需要Lv.19（android 4.4）以上
    private void initReaderCallback() {
        try {

            nfcCallBack = new NfcAdapter.ReaderCallback() {
                @Override
                public void onTagDiscovered(final Tag tag) {
                    isNFC = true;
                    Message message = new Message();
                    message.what = NFC_START;
                    message.obj = tag;
                    uiHandler.sendMessage(message);
                }
            };
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "android version too low, can not use nfc");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        uiHandler = new MyHandler(this);

        /**
         * 某些6.0系统搜索蓝牙时需要用户提供权限
         */
        if (Build.VERSION.SDK_INT >= 6.0) {
            RequestPermissionUtil.request(this, new String[]{Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_SETTINGS});
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
        }

        mFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS",
                Locale.getDefault());

        alertDialog = new AlertDialog.Builder(this).setTitle("提示").setMessage("请把身份证慢慢贴近NFC模块...").create();


        // 初始化阅读器,
        // test01,
        // 12315aA..1,
        // FE870B0163113409C134283661490AEF
        // 为测试账号、密码、密钥,
        // 正式使用时应向我们申请正式的账号、密码、密钥
        mNFCReaderHelper = new SRnfcCardReader(uiHandler, this, "test03", "12315aA..1", "FE870B0163113409C134283661490AEF");
        mOTGReaderHelper = new SRotgCardReader(uiHandler, this, "test03", "12315aA..1", "FE870B0163113409C134283661490AEF");
        mBlueReaderHelper = new SRBluetoothCardReader(uiHandler, this, "test03", "12315aA..1", "FE870B0163113409C134283661490AEF");
        posCardReader = new POSCardReader(uiHandler, this, "test03", "12315aA..1", "FE870B0163113409C134283661490AEF");

        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initViews();

        deviceId = mBlueReaderHelper.getDeviceId();
        softVersion = mBlueReaderHelper.getSoftVersion();
        apiVersion = mBlueReaderHelper.getAPIVersion();

        Blueaddress = null;

        //initShareReference();

//        mNFCReaderHelper.setIdentity(identity);

        initReaderCallback();

        //解密服务监听器，可监听连接的是哪台解密服务器
        mBlueReaderHelper.setDecodeServerListener(new IDecodeIDServerListener() {
            @Override
            public void getThisServer(String ip, int port) {
                uiHandler.obtainMessage(SERVER_TV, ip + "-" + port).sendToTarget();
            }
        });
        mOTGReaderHelper.setDecodeServerListener(new IDecodeIDServerListener() {
            @Override
            public void getThisServer(String ip, int port) {
                uiHandler.obtainMessage(SERVER_TV, ip + "-" + port).sendToTarget();
            }
        });
        mNFCReaderHelper.setDecodeServerListener(new IDecodeIDServerListener() {
            @Override
            public void getThisServer(String ip, int port) {
                uiHandler.obtainMessage(SERVER_TV, ip + "-" + port).sendToTarget();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // If BT is not on, request that it be enabled.
        Log.e("blue", "activity onStart");
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Log.e("blue", "activity isNotEnabled");
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 2);
            // Otherwise, setup the chat session
        }
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
    }

    //    private boolean boundBlueToothDevice() {
//        Log.d("Blueaddress", Blueaddress);
//        return mBlueReaderHelper.boundBlueToothDevice(Blueaddress);
//    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        actionType=requestCode;
        Log.e("MAIN", "onActivityResult: requestCode=" + requestCode
                + ", resultCode=" + resultCode);
        switch (requestCode) {

            //选择蓝牙设备后
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {

                    Blueaddress = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    btName = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_NAME);
                    if (!Blueaddress
                            .matches("([0-9a-fA-F][0-9a-fA-F]:){5}([0-9a-fA-F][0-9a-fA-F])")) {
                        tv_info.setText("address:" + Blueaddress
                                + " is wrong, length = " + Blueaddress.length());
                        return;
                    }
                }
                break;

            //选择服务器后
            case REQUEST_SET_SERVER:
                if (resultCode == 100) {

                    server_address = data.getExtras().getString("address");
                    server_port = data.getExtras().getInt("port");

                    Log.e("MAIN", "onActivityResult: " + server_address);
                    Log.e("MAIN", "onActivityResult: " + server_port);

                    initShareReference();
                }
                break;

            //蓝牙读IC卡前,选择蓝牙设备结果
            case READ_ICCID_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    Blueaddress = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    if (!Blueaddress
                            .matches("([0-9a-fA-F][0-9a-fA-F]:){5}([0-9a-fA-F][0-9a-fA-F])")) {
                        tv_info.setText("address:" + Blueaddress
                                + " is wrong, length = " + Blueaddress.length());
                        return;
                    }

                    readICCIDTask();
                }

                break;


            //蓝牙读身份证前,选择蓝牙设备结果
            case READ_IDCARD_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    Blueaddress = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    btName = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_NAME);
                    if (!Blueaddress
                            .matches("([0-9a-fA-F][0-9a-fA-F]:){5}([0-9a-fA-F][0-9a-fA-F])")) {
                        tv_info.setText("address:" + Blueaddress
                                + " is wrong, length = " + Blueaddress.length());
                        return;
                    }

                   readIDCardBlueTooth();

                }

                break;

            //设定读取次数(蓝牙或者OTG方式用)
            case REQUEST_REPEAT:
                if(resultCode == 100){
                    iDReadingNum = data.getIntExtra("readingNum", 1);
                }
                break;
        }
    }

    private void initShareReference() {
        // 设置特定的解密服务器

        if(!server_address.equals("") && server_port != 0) {
            //设置特定的解密服务器, 第一步先把管控设为空；第二步设入特定的服务器
            //先把管控设为空
            mBlueReaderHelper.enableAutoServer(false);
            //设入特定的服务器
            mNFCReaderHelper.setTheServer(this.server_address, this.server_port);

            mOTGReaderHelper.setTheServer(this.server_address, this.server_port);

            mBlueReaderHelper.setTheServer(this.server_address, this.server_port);
        }
    }

    @SuppressWarnings("deprecation")
    private void initViews() {
        tv_info = (TextView) findViewById(R.id.tv_info);
//        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        nameTextView = (TextView) findViewById(R.id.tv_name);
        sexTextView = (TextView) findViewById(R.id.tv_sex);
        folkTextView = (TextView) findViewById(R.id.tv_ehtnic);
        birthTextView = (TextView) findViewById(R.id.tv_birthday);
        addrTextView = (TextView) findViewById(R.id.tv_address);
        codeTextView = (TextView) findViewById(R.id.tv_number);
        policyTextView = (TextView) findViewById(R.id.tv_signed);
        validDateTextView = (TextView) findViewById(R.id.tv_validate);
        samidTextView = (TextView) findViewById(R.id.tv_samid);
        timeTextView = (TextView) findViewById(R.id.tv_time);
        endTimeTextView = (TextView) findViewById(R.id.tv_time2);
        photoView = (ImageView) findViewById(R.id.iv_photo);
        buttonNFC = (Button) findViewById(R.id.buttonNFC);
        buttonBT = (Button) findViewById(R.id.buttonBT);
        mplaceHolder = (TextView) findViewById(R.id.placeHolder);
        mCkbOtg = (CheckBox) findViewById(R.id.ckb_otg);
        timeCountTextView = (TextView) findViewById(R.id.tv_timecount);
        serverTv = (TextView) findViewById(R.id.tv_server);
        successTv = (TextView) findViewById(R.id.tv_success);

        processDia= new ProgressDialog(context);
        //点击提示框外面是否取消提示框
        processDia.setCanceledOnTouchOutside(false);
        //点击返回键是否取消提示框
        processDia.setCancelable(false);
        processDia.setIndeterminate(true);
        processDia.setMessage("加载中..");

        // 屏幕大小
        WindowManager wm = this.getWindowManager();
        int height = wm.getDefaultDisplay().getHeight();
        ViewGroup.LayoutParams p = mplaceHolder.getLayoutParams();
        p.height = height / 6;
        mplaceHolder.setLayoutParams(p);

        int width = wm.getDefaultDisplay().getWidth();
        ViewGroup.LayoutParams w = addrTextView.getLayoutParams();
        w.width = (width / 2) - 10;
        addrTextView.setLayoutParams(w);

        tv_info.setTextColor(Color.rgb(240, 65, 85));

        mCkbOtg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isOtg = mCkbOtg.isChecked();
            }
        });

        //NFC读身份证
        buttonNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // NFC读卡
                clearInfo();
                buttonNFC.setEnabled(false);
                buttonBT.setEnabled(false);
                readIDSuccessCount = 0;
                successTv.setText(readIDSuccessCount + "次");

                readIDCardNFC();

                alertDialog.show();
                isNFC = true;
            }
        });
        //读身份证
        buttonBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clearInfo();

                iDReadingCount = 0;
                readIDSuccessCount = 0;
                successTv.setText(readIDSuccessCount + "次");

                if (isOtg) {
                    //otg读身份证
                    readIDCardOTG();
                } else {
                    // 蓝牙读身份证
                    if (Blueaddress == null) {
                        Intent serverIntent = new Intent(MainActivity.this,
                                DeviceListActivity.class);
                        startActivityForResult(serverIntent,
                                READ_IDCARD_CONNECT_DEVICE);
                    } else {
                        readIDCardBlueTooth();
                    }
                }

            }
        });

        //读sim卡
        findViewById(R.id.buttonICCID).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readSimCard();
            }
        });

        //写sim卡
        findViewById(R.id.buttonDXZX).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        View dialog_view = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog, null);
                        final EditText imei = (EditText)dialog_view.findViewById(R.id.ed_imei);
                        final EditText number = (EditText)dialog_view.findViewById(R.id.ed_number);

                        if(isOtg){
                            if (!mOTGReaderHelper.registerOTGCard()) {
                                Toast.makeText(MainActivity.this, "请确认USB设备已经连接并且已授权，再读卡!", Toast.LENGTH_LONG)
                                        .show();
                            } else {
                                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                                        .setView(dialog_view)
                                        .setNegativeButton("写卡", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (imei.getText().toString().equals("") || number.getText().toString().equals("")){
                                                    Toast.makeText(v.getContext(), "输入为空", Toast.LENGTH_SHORT).show();
                                                }else {
                                                    writeICCIDTask(imei.getText().toString(), number.getText().toString());
                                                }
                                            }
                                        })
                                        .setPositiveButton("关闭", null).create();
                                alertDialog.show();
                            }
                        }else {
                            if (Blueaddress == null) {
                                Intent serverIntent = new Intent(MainActivity.this,
                                        DeviceListActivity.class);
                                startActivityForResult(serverIntent,
                                        REQUEST_CONNECT_DEVICE);
                                return;
                            }
                            if (mBlueReaderHelper.registerBlueCard(Blueaddress)) {
                                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                                        .setView(dialog_view)
                                        .setNegativeButton("写卡", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (imei.getText().toString().equals("") || number.getText().toString().equals("")){
                                                    Toast.makeText(v.getContext(), "输入为空", Toast.LENGTH_SHORT).show();
                                                }else {
                                                    writeICCIDTask(imei.getText().toString(), number.getText().toString());
                                                }
                                            }
                                        })
                                        .setPositiveButton("关闭", null).create();
                                alertDialog.show();
                            }
                        }
                    }
                });
        //读阅读器信息
        findViewById(R.id.button_drives).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        readerInfoByBTorOTG();
                    }
                });

    }


    /**
     * 读sim卡
     */
    protected void readICCIDTask() {
        final ProgressDialog progressDialog = new ProgressDialog(
                MainActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("正在读取卡号...");
        progressDialog.show();

        final byte[] cardNum = new byte[20];

        // 读取ICCID
        new AsyncTask<Object, Object, Integer>() {
            @Override
            protected Integer doInBackground(
                    Object... arg0) {
                if (isOtg) {
                    // 打开otg
                    if (mOTGReaderHelper.registerOTGCard()) {
                        return mOTGReaderHelper.readSimICCID(cardNum);
                    } else {
                        return -1;
                    }

                } else {
                    if (mBlueReaderHelper.registerBlueCard(Blueaddress)) {
                        return mBlueReaderHelper.readSimICCID(cardNum);
                    } else {
                        return -1;
                    }
                }

            }

            @Override
            protected void onPostExecute(Integer result) {
                progressDialog.dismiss();
                Log.e("",result+"");

                if(result == 0) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("此卡是白卡").setMessage(new String(cardNum))
                            .setPositiveButton("关闭", null).create()
                            .show();
                }else if(result == 1){
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("此卡是成卡").setMessage(new String(cardNum))
                            .setPositiveButton("关闭", null).create()
                            .show();
                }else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("错误").setMessage("读卡错误")
                            .setPositiveButton("关闭", null).create()
                            .show();
                }
                super.onPostExecute(result);
            }
        }.execute();
    }

    /**
     * 传入IMEI和短信中心号写sim卡
     */
    protected void writeICCIDTask(final String imei, final String number) {
        final ProgressDialog progressDialog = new ProgressDialog(
                MainActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("正在写卡...");
        progressDialog.show();

        new AsyncTask<Object, Object, Boolean>() {
            @Override
            protected Boolean doInBackground(
                    Object... arg0) {
                if (isOtg) {
                    return mOTGReaderHelper.writeSimCard(imei, number);
                } else {
                    return mBlueReaderHelper.writeSimCard(imei, number);
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                progressDialog.dismiss();

                if(result) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("提示").setMessage("写卡成功")
                            .setPositiveButton("关闭", null).create()
                            .show();
                }else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("提示").setMessage("写卡失败")
                            .setPositiveButton("关闭", null).create()
                            .show();
                }
                super.onPostExecute(result);
            }
        }.execute();
    }


    /**
     * NFC 方式寻找身份证
     */
    protected void readIDCardNFC() {
        if(posCardReader.isEnable){
            //特殊POS方式打开NFC监听
            posCardReader.openReader(nfcCallBack);
        }else {
            //普通NFC类打开NFC监听
            mNFCReaderHelper.EnableSystemNFCMessage(nfcCallBack);
        }
        isNFC = true;
    }


    /**
     * OTG读取身份证
     */
    protected void readIDCardOTG() {
        isNFC = false;

        //开始时间
        startTime();

        buttonNFC.setEnabled(false);
        buttonBT.setEnabled(false);

        new Thread(new Runnable() {
            public void run() {
                iDReadingCount++;
                isRegisterOTG = mOTGReaderHelper.registerOTGCard();
                uiHandler.sendEmptyMessage(REGISTER_OTG);
            }
        }).start();

    }

    /**
     * 蓝牙读身份证方式
     */
    protected void readIDCardBlueTooth() {

        isNFC = false;

        //开始时间
        startTime();

        buttonNFC.setEnabled(false);
        buttonBT.setEnabled(false);

        if (Blueaddress == null) {
            stopTime();
            Toast.makeText(this, "请选择蓝牙设备，再读卡!", Toast.LENGTH_LONG).show();
            return;
        }

        if (Blueaddress.length() <= 0) {
            stopTime();
            Toast.makeText(this, "请选择蓝牙设备，再读卡!", Toast.LENGTH_LONG).show();
            return;
        }

        new Thread(new Runnable() {
            public void run() {
                iDReadingCount++;
                //连接设备
                isRegisterBT = mBlueReaderHelper.registerBlueCard(Blueaddress);
                uiHandler.sendEmptyMessage(REGISTER_BLUETOOTH);
            }
        }).start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * 菜单
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //清除信息
            case R.id.action_clear:
                buttonNFC.setEnabled(true);
                buttonBT.setEnabled(true);
                clearInfo();
                break;
            //选择服务器
            case R.id.action_server:

                ArrayList<String> serverSet = getServerList();

                Intent intents = new Intent();
                intents.setClass(MainActivity.this, ActServerConfig.class);
                intents.putStringArrayListExtra("server", serverSet);
                startActivityForResult(intents, REQUEST_SET_SERVER);
                break;
            //关于
            case R.id.action_about:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ActAbout.class);
                intent.putExtra("softVersion", softVersion);
                intent.putExtra("apiVersion", apiVersion);
                intent.putExtra("drivceID", deviceId);
                this.startActivity(intent);
                break;
            //选择蓝牙阅读器
            case R.id.action_blue:
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                break;
            //设定读取次数
            case R.id.action_set_num:
                Intent numIntent = new Intent(this, ActSetReadIDNum.class);
                startActivityForResult(numIntent, 5);
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    protected void onStop() {
        //关闭管控线程，防止报错
        ReaderManagerService.stopServer();
        super.onStop();
    }

    /**
     * 消息handle,包括绑定蓝牙消息、绑定OTG消息、读身份证结果返回消息
     */
    class MyHandler extends Handler {
        private MainActivity activity;

        @SuppressLint("HandlerLeak")
        MyHandler(MainActivity activity) {
            this.activity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //成功
                case ConsantHelper.READ_CARD_SUCCESS:
                    handleReturnSuccessMsg(msg);
                    break;

                //错误-1
                case ConsantHelper.READ_CARD_NO_READ:
                    handleReturnErrorMsg(msg);
                    break;

                //错误-2
                case ConsantHelper.READ_CARD_BUSY:
                    handleReturnErrorMsg(msg);
                    break;

                //错误-3
                case ConsantHelper.READ_CARD_NET_ERR:
                    handleReturnErrorMsg(msg);
                    break;

                //错误-4
                case ConsantHelper.READ_CARD_NO_CARD:
                    handleReturnErrorMsg(msg);
                    break;

                //错误-5
                case ConsantHelper.READ_CARD_SAM_ERR:
                    handleReturnErrorMsg(msg);
                    break;

                //错误-6
                case ConsantHelper.READ_CARD_OTHER_ERR:
                    handleReturnErrorMsg(msg);
                    break;

                //错误-7
                case ConsantHelper.READ_CARD_NEED_TRY:
                    handleReturnErrorMsg(msg);
                    break;

                //错误-8
                case ConsantHelper.READ_CARD_OPEN_FAILED:
                    handleReturnErrorMsg(msg);
                    break;

                //错误-9
                case ConsantHelper.READ_CARD_NO_CONNECT:
                    handleReturnErrorMsg(msg);
                    break;

                //错误-10
                case ConsantHelper.READ_CARD_NO_SERVER:
                    handleReturnErrorMsg(msg);
                    break;

                //错误-11
                case ConsantHelper.READ_CARD_FAILED:
                    handleReturnErrorMsg(msg);
                    break;

                //错误-12
                case ConsantHelper.READ_CARD_SN_ERR:
                    handleReturnErrorMsg(msg);
                    break;

                //开始交互身份证
                case ConsantHelper.READ_CARD_START:
                    activity.clearInfo();
                    activity.tv_info.setText("开始读卡......");

                    break;

                //绑定蓝牙
                case REGISTER_BLUETOOTH:
                    if (isRegisterBT) {

                        buttonNFC.setEnabled(false);
                        buttonBT.setEnabled(false);

                        showLoadingDialog();

                        //开始读身份证
                        new Thread() {
                            public void run() {
                                //蓝牙读身份证
                                mBlueReaderHelper.readCard(30);
                            }
                        }.start();
                    } else {
                        stopTime();
                        buttonNFC.setEnabled(true);
                        buttonBT.setEnabled(true);
                        Toast.makeText(context, "请确认蓝牙设备已经连接，再读卡!", Toast.LENGTH_LONG).show();
                    }
                    break;

                //绑定OTG
                case REGISTER_OTG:
                    if (isRegisterOTG) {
                        buttonNFC.setEnabled(false);
                        buttonBT.setEnabled(false);

                        showLoadingDialog();

                        new Thread() {
                            public void run() {
                                //OTG读身份证
                                mOTGReaderHelper.readCard(3000);
                            }
                        }.start();

                    } else {
                        buttonNFC.setEnabled(true);
                        buttonBT.setEnabled(true);
                        stopTime();
                        Toast.makeText(context, "请确认USB设备已经连接并且已授权，再读卡!", Toast.LENGTH_LONG).show();
                    }
                    break;

                //NFC开始读取
                case NFC_START:
                    if(alertDialog.isShowing()){
                        alertDialog.cancel();
                    }
                    Log.e("MainActivity", "NFC 返回调用");

                    //清除信息
                    clearInfo();
                    //开始计时
                    startTime();
                    //显示加载框
                    showLoadingDialog();

                    //根据特殊POS的属性值来判断是使用特殊POS的NFC方式还是普通的NFC方式
                    if(posCardReader.isEnable) {
                        //特殊POS读身份证
                        posCardReader.readCardWithIntent();
                    }else if (mNFCReaderHelper.isNFC((Tag)msg.obj)) {
                        // NFC读取身份证
                        mNFCReaderHelper.readIDCard();
                    } else {
                        cancelLoadingDialog();
                        stopTime();
                        buttonNFC.setEnabled(true);
                        buttonBT.setEnabled(true);
                        tv_info.setText("不是身份证");
                        Log.e("MainActivity", "返回的Tag不可用");
                    }
                    break;

                //进度
                case ConsantHelper.READ_CARD_PROGRESS:
                    Log.e("MainActivity", msg.obj.toString());
                    break;

                case ConsantHelper.BLUETOOTH_BOUND_END:
                    switch (actionType){
                        case READ_ICCID_CONNECT_DEVICE:
                            readICCIDTask();
                            break;
                        case READ_IDCARD_CONNECT_DEVICE:
                            readIDCardBlueTooth();
                            break;
                    }

                    break;

                //NFC方式需要授权设备，使用此demo时不用
                //授权方式：在拨号界面按*#06#，把显示的第一条串号（IMEI或者MEID）给我们授权
                case 6:
                    Message message = new Message();
                    message.obj = "设备未授权";
                    handleReturnErrorMsg(message);
                    break;

                //显示服务器
                case SERVER_TV:
                    serverTv.setText(msg.obj.toString());
                    break;
            }
        }

    }

    /**
     * 读卡成功，显示信息
     * @param identityCard
     */
    private void readCardSuccess(IdentityCardZ identityCard) {
        if (identityCard != null) {
//            Toast.makeText(this,identityCard.originalString,Toast.LENGTH_LONG).show();
            nameTextView.setText(identityCard.name);
            sexTextView.setText(identityCard.sex);
            folkTextView.setText(identityCard.ethnicity);

            birthTextView.setText(identityCard.birth);
            codeTextView.setText(identityCard.cardNo);
            policyTextView.setText(identityCard.authority);
            addrTextView.setText(identityCard.address);
            validDateTextView.setText(identityCard.period);
            samidTextView.setText(identityCard.dn);

//            IDImageUtil.dealIDImage(byte[]) 头像图片解码接口,有加密则解码,没有则不处理
            photoView.setImageBitmap(IDImageUtil.dealIDImage(identityCard.avatar));
            if(identityCard.UUID != null && !identityCard.UUID.equals("")){
                Toast.makeText(context, "UUID: " + identityCard.UUID + " timeTag: " + identityCard.timeTag + " sign: " + identityCard.nfcSignature, Toast.LENGTH_LONG).show();
            }

            Log.e(TAG, "读卡成功!");

            //IDImageUtil.makeIDImage(this,mIdentityCardZ);

            readIDSuccessCount++;
            successTv.setText(readIDSuccessCount + "次");

        }else{
            Log.e("DEBUG", "数据为空,无法显示");
            //tv_info.setText("读卡失败!");
        }
        buttonNFC.setEnabled(true);
        buttonBT.setEnabled(true);

    }

    /**
     * 清除显示信息
     */
    private void clearInfo() {
        nameTextView.setText("");
        sexTextView.setText("");
        folkTextView.setText("");

        birthTextView.setText("");
        codeTextView.setText("");
        policyTextView.setText("");
        addrTextView.setText("");
        validDateTextView.setText("");
        samidTextView.setText("");
        photoView.setImageBitmap(null);

        tv_info.setText("");
    }

    /**
     * 记录开始时间
     */
    private void startTime(){
        startTime = new Date();
        String s = mFormatter.format(startTime);
        timeTextView.setText(s);
        endTimeTextView.setText("00:00:00:000");
        timeCountTextView.setText("0ms");
    }

    /**
     * 记录结束时间
     */
    private void stopTime(){
        try {
            stopTime = new Date();
            String s = mFormatter.format(stopTime);
            endTimeTextView.setText(s);
            long readTimeCount = stopTime.getTime() - startTime.getTime();
            timeCountTextView.setText(readTimeCount + "ms");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 返回成功信息后的处理
     * @param message
     */
    private void handleReturnSuccessMsg(Message message){
        cancelLoadingDialog();
        stopTime();
        tv_info.setText("读取成功!");
        buttonNFC.setEnabled(true);
        buttonBT.setEnabled(true);

        // 身份证读取成功，返回所有信息
        readCardSuccess((IdentityCardZ) message.obj);

        Log.i(TAG, "READ_CARD_SUCCESS:" + ((IdentityCardZ) message.obj).name);
        Log.i(TAG, "读取" + iDReadingCount + "次");

        //根据阅读次数重复读取
        repeatRead();
    }

    /**
     * 返回错误信息后的处理
     * @param message
     */
    private void handleReturnErrorMsg(Message message){
        //取消加载框
        cancelLoadingDialog();
        //停止计时
        stopTime();
        Object err = message.obj;
        tv_info.setText(err.toString() + "(错误代码: " + message.what + ")");

        buttonNFC.setEnabled(true);
        buttonBT.setEnabled(true);
        //根据阅读次数重复读取
        repeatRead();
    }

    /**
     * 显示加载框
     */
    private void showLoadingDialog(){
        if(!processDia.isShowing()) {
            processDia.show();
        }
    }

    /**
     * 隐藏加载框
     */
    private void cancelLoadingDialog(){
        if(processDia.isShowing()) {
            processDia.cancel();
        }
    }

    /**
     * 根据阅读次数重复读取
     */
    private void repeatRead(){
        if(!isNFC) {
            if (iDReadingCount < iDReadingNum) {
                iDReadingCount++;

                if (isOtg) {
                    //OTG
                    isRegisterOTG = mOTGReaderHelper.registerOTGCard();
                    uiHandler.sendEmptyMessage(REGISTER_OTG);
                } else {
                    //蓝牙
                    isRegisterBT = mBlueReaderHelper.registerBlueCard(Blueaddress);
                    uiHandler.sendEmptyMessage(REGISTER_BLUETOOTH);
                }
            }
        }
    }

    /**
     * 获取阅读器设备信息
     */
    private void readerInfoByBTorOTG(){
        if(isOtg) {
            //OTG方式
            if (!mOTGReaderHelper.registerOTGCard()) {
                Toast.makeText(MainActivity.this, "请确认USB设备已经连接并且已授权，再读卡!", Toast.LENGTH_LONG)
                        .show();
            } else {
                DeviceInfo info = mOTGReaderHelper.getDeviceInfo();
                String s = new String(info.id) + "\n" + "0" + String.valueOf(info.factoryNo) + "\n" + info.useFlg + "\n" + info.deviceType + "\n" + info.softVersion + "\n" + info.hardwareVersion;
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("DrivesInfo").setMessage(s)
                        .setPositiveButton("关闭", null).create()
                        .show();
            }
        }else {
            //蓝牙方式
            if (Blueaddress == null) {
                Intent serverIntent = new Intent(MainActivity.this,
                        DeviceListActivity.class);
                startActivityForResult(serverIntent,
                        REQUEST_CONNECT_DEVICE);
            } else {
                mBlueReaderHelper.registerBlueCard(Blueaddress);
                // 蓝牙读设备信息
                DeviceInfo info = mBlueReaderHelper.getDeviceInfo();
                String s = new String(info.id) + "\n" + "0" + String.valueOf(info.factoryNo)+ "\n" + info.useFlg + "\n" + info.deviceType + "\n" + info.softVersion + "\n" + info.hardwareVersion;
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("DrivesInfo").setMessage(s)
                        .setPositiveButton("关闭", null).create()
                        .show();
            }
        }
    }

    /**
     * 读sim卡
     */
    private void readSimCard(){
        if (isOtg) {
            //OTG方式
            if (!mOTGReaderHelper.registerOTGCard()) {
                Toast.makeText(MainActivity.this, "请确认USB设备已经连接并且已授权，再读卡!", Toast.LENGTH_LONG)
                        .show();
            } else {
                readICCIDTask();
            }
        } else{
            //蓝牙方式
            if(Blueaddress==null){
                Intent serverIntent = new Intent(MainActivity.this,
                        DeviceListActivity.class);
                startActivityForResult(serverIntent,
                        READ_ICCID_CONNECT_DEVICE);
            }else {
                readICCIDTask();
            }

        }
    }

    /**
     * 获取解密服务器列表，等下传递到服务器设置页面供选择
     * @return
     */
    private ArrayList<String> getServerList() {
        ArrayList<String> set = new ArrayList<String>();
        SharedPreferences preferences = context.getSharedPreferences(SHAREDPREFERENCE_NAME, Activity.MODE_PRIVATE);
        String json = preferences.getString("SERVER", "");
        try {
            JSONArray jsonArray = new JSONArray(json);
            for(int poi = 0; poi < jsonArray.length(); poi++){
                JSONObject jo = jsonArray.getJSONObject(poi);
                String ip = jo.getString("host");
                String port = jo.getString("port");
                String priority = jo.getString("priority");
                set.add(ip + ":" + port + ":" + priority);
                Log.d(TAG, ip + ":" + port + ":" + priority);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }finally {
            return set;
        }
    }
}
