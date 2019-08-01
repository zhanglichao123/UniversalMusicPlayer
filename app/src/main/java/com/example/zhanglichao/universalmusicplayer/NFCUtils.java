package com.example.zhanglichao.universalmusicplayer;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.sunrise.icardreader.helper.ConsantHelper;
import com.sunrise.icardreader.model.IdentityCardZ;
import com.sunrise.reader.IDecodeIDServerListener;

import sunrise.otg.SRotgCardReader;

public class NFCUtils {
    private volatile static NFCUtils nfaUtils;
    private final Context mContext;
    private static final int SERVER_TV = 5555;
    Handler uiHandler;
    private final SRotgCardReader mOTGReaderHelper;
    String TAG = "身份证";
    private boolean b;
    private OnNfcLisener onNfcLisener;
    //NFC开始读取
    private static final int NFC_START = 52;
    private NfcAdapter.ReaderCallback nfcCallBack;
    private boolean stopNfc = true;

    //绑定NFC回调函数, SDK需要Lv.19（android 4.4）以上
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initReaderCallback() {
        try {

            //  isNFC = true;
            nfcCallBack = new NfcAdapter.ReaderCallback() {
                @Override
                public void onTagDiscovered(final Tag tag) {
                    //  isNFC = true;
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public NFCUtils(Context context) {
        mContext = context;
        uiHandler = new MyHandler();
        mOTGReaderHelper = new SRotgCardReader(uiHandler, mContext, "test03", "12315aA..1", "FE870B0163113409C134283661490AEF");
        initReaderCallback();
        mOTGReaderHelper.setDecodeServerListener(new IDecodeIDServerListener() {
            @Override
            public void getThisServer(String ip, int port) {
                uiHandler.obtainMessage(SERVER_TV, ip + "-" + port).sendToTarget();
            }
        });

    }

    /**
     * 开启nfc
     *
     * @param context
     * @return
     */
    public static NFCUtils initNFAtils(Context context) {
        if (nfaUtils == null) {
            synchronized (NFCUtils.class) {
                if (null == nfaUtils) {
                    nfaUtils = new NFCUtils(context);
                }
            }
        }
        return nfaUtils;
    }

    /**
     * 开启身份证读取
     */
    public void startNfa() {
        stopNfc = false;
        b = mOTGReaderHelper.registerOTGCard();
        if (b) {
            mOTGReaderHelper.readCard(30);
        } else {
            Log.e(TAG, "授权失败");
        }
    }

    /**
     * \
     * 关闭nfc
     */
    public void stopNfc() {
        stopNfc = true;
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
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
                    //  mOTGReaderHelper.readCard(30);
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
                    break;
            }
        }
    }


    /**
     * 返回成功信息后的处理
     *
     * @param message
     */
    private void handleReturnSuccessMsg(Message message) {
        // 身份证读取成功，返回所有信息
        IdentityCardZ identityCardZ = (IdentityCardZ) message.obj;
        onNfcLisener.onSueeccd(identityCardZ);
        Log.i(TAG, "READ_CARD_SUCCESS:" + ((IdentityCardZ) message.obj).name);
    }


    /**
     * 返回错误信息后的处理
     *
     * @param message
     */
    private void handleReturnErrorMsg(Message message) {
        Object err = message.obj;
        Log.e(TAG, "(错误代码: " + message.what + ")");
        String s = err.toString();
        onNfcLisener.onError(message.what + "===" + s == null ? "" : s);
        //根据阅读次数重复读取
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                repeatRead();
            }
        }, 500);
    }


    /**
     * 根据阅读次数重复读取
     */
    private void repeatRead() {
        if (stopNfc) return;
        if (mOTGReaderHelper != null && b) {
            boolean b = mOTGReaderHelper.registerOTGCard();
            if (b){
                mOTGReaderHelper.readCard(30);
            }
        }
    }

    public void setOnNfcLisener(OnNfcLisener onNfcLisener) {
        this.onNfcLisener = onNfcLisener;
    }

    public interface OnNfcLisener {
        void onSueeccd(IdentityCardZ identityCardZ);

        void onError(String error);
    }
}
