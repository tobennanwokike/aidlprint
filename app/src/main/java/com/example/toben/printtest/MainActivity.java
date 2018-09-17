package com.example.toben.printtest;

import android.app.Activity;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.toben.printtest.common.MessageType;
import com.smartdevice.aidl.IZKCService;

import org.w3c.dom.Text;

public class MainActivity extends BaseActivityN implements View.OnClickListener {

    private Button btnPrint;
    private TextView printest;

    private boolean runFlag = true;
    private boolean detectFlag = false;
    private float PINTER_LINK_TIMEOUT_MAX = 30*1000L;

    DetectPrinterThread mDetectPrinterThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();

        mDetectPrinterThread = new DetectPrinterThread();
        mDetectPrinterThread.start();

    }

    class DetectPrinterThread extends Thread{
        @Override
        public void run() {
            super.run();
            while(runFlag){
                float start_time = SystemClock.currentThreadTimeMillis();
                float end_time = 0;
                float time_lapse = 0;
                if(detectFlag){
                    //检测打印是否正常 detect if printer is normal
                    try {
                        if(mIzkcService!=null){
                            String printerSoftVersion = mIzkcService.getFirmwareVersion1();
                            if(TextUtils.isEmpty(printerSoftVersion)){
                                mIzkcService.setModuleFlag(module_flag);
                                end_time = SystemClock.currentThreadTimeMillis();
                                time_lapse = end_time - start_time;
                                if(time_lapse>PINTER_LINK_TIMEOUT_MAX){
                                    detectFlag = false;
                                    //打印机连接超时 printer link timeout
                                    sendEmptyMessage(MessageType.BaiscMessage.PRINTER_LINK_TIMEOUT);
                                }
                            }else{
                                //打印机连接成功 printer link success
                                sendMessage(MessageType.BaiscMessage.DETECT_PRINTER_SUCCESS, printerSoftVersion);
                                detectFlag = false;
                            }
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
                SystemClock.sleep(1);
            }
        }
    }


    private void initView() {
        btnPrint = (Button) findViewById(R.id.printing);
        printest = (TextView) findViewById(R.id.printest);
    }

    private void initEvent() {

        btnPrint.setOnClickListener(this);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.printing:
                printGBKText();
                break;
        }
    }

    private void printGBKText() {
        String text= printest.getText().toString();
        try {
            mIzkcService.printerInit();
            mIzkcService.printGBKText(text);
            Log.i("","Print fam");
        } catch (RemoteException e) {
            Log.e("", "远程服务未连接...");
            e.printStackTrace();
        }
    }


}
