
/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.nordicsemi.nrfUARTv2;




import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

//    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect,btnSend;
    private EditText edtMessage;

    //测试调试用的设置日期数据的按钮与输入框
    private EditText commendMsg;
    private EditText numberMsg;
    private Button  testsendMsg;
    //测试调试用的设置时效数据的按钮与输入框
    private EditText timeCommendMsg;
    private EditText timeNumberMsg;
    private Button  timeTestsendMsg;
    //设置电机转动时间
    private EditText cirlceMsg;
    private Button cirlceSendBtn;

    //设置密码
    private EditText changePasswordMsg;
    private Button changePasswordBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        btnConnectDisconnect=(Button) findViewById(R.id.btn_select);
        btnSend=(Button) findViewById(R.id.sendButton);
        edtMessage = (EditText) findViewById(R.id.sendText);

        commendMsg = (EditText) findViewById(R.id.sendcommendtext);
        numberMsg = (EditText) findViewById(R.id.sendbytetext);
        testsendMsg = (Button) findViewById(R.id.clicksend);

        timeCommendMsg = (EditText) findViewById(R.id.timesendcommendtext);
        timeNumberMsg = (EditText) findViewById(R.id.timesendbytetext);
        timeTestsendMsg = (Button) findViewById(R.id.clicktimesend);

        cirlceMsg = (EditText) findViewById(R.id.circlesendbytetext);
        cirlceSendBtn = (Button) findViewById(R.id.circleclicksend);

        changePasswordMsg = (EditText) findViewById(R.id.changepasswordtext);
        changePasswordBtn = (Button) findViewById(R.id.changepasswordsend);

        service_init();

     
       
        // Handle Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                	if (btnConnectDisconnect.getText().equals("Connect")){
                		
                		//Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                		
            			Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
            			startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
        			} else {
        				//Disconnect button pressed
        				if (mDevice!=null)
        				{
        					mService.disconnect();
        					
        				}
        			}
                }
            }
        });
        // Handle Send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	EditText editText = (EditText) findViewById(R.id.sendText);
            	String message = editText.getText().toString();
            	byte[] value;
				try {
					//send data to service
                            value = message.getBytes("UTF-8");
					mService.writeRXCharacteristic(value);
					//Update the log with time stamp
					String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
					listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
               	 	messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
               	 	edtMessage.setText("");
				} catch (UnsupportedEncodingException e) {

					// TODO Auto-generated catch block
					e.printStackTrace();
				}

            }
        });





        //设置密码
        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String string = "set keys  ";
                String message = changePasswordMsg.getText().toString();
                string = string + message;
                byte[] value;
                try {
                    //send data to service
                    value = string.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    cirlceMsg.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });

        //设置电机转动时间
        cirlceSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String string = "set moto ";
                String message = cirlceMsg.getText().toString();
                string = string + message;
                byte[] value;
                try {
                    //send data to service
                    value = string.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    cirlceMsg.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });

        //测试日期设置按钮事件
        testsendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String string = "set beep ";
//                String message = numberMsg.getText().toString();
//                string = string + message;
//                byte[] value;
//                try {
//                    //send data to service
//                    value = string.getBytes("UTF-8");
//                    mService.writeRXCharacteristic(value);
//                    //Update the log with time stamp
//                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
//                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
//                    numberMsg.setText("");
//                } catch (UnsupportedEncodingException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }

                byte[] commendMessagevalue;
                byte[] numberMessagevalue;
                byte[] value;
                String commendMessage = commendMsg.getText().toString();
                String numberMessage = numberMsg.getText().toString();
                    try {

                            commendMessagevalue = commendMessage.getBytes("UTF-8");
                            numberMessagevalue = changeStringToH(numberMessage);
//                            //合并数组
//                            value = new byte[commendMessagevalue.length+numberMessagevalue.length];
//                            System.arraycopy(commendMessagevalue,0,value,0,commendMessagevalue.length);
//                            System.arraycopy(numberMessagevalue,0,value,commendMessagevalue.length,numberMessagevalue.length);

                            mService.writeRXCharacteristic(numberMessagevalue);
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            listAdapter.add("["+currentDateTimeString+"] TX: "+ commendMessage + numberMessage);
                        commendMsg.setText("");
                        numberMsg.setText("");
                    }catch (UnsupportedEncodingException e) {

                        e.printStackTrace();
                    }
            }
        });


        //测试时效设置按钮事件
        timeTestsendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String string = "set dhld ";
                String message = timeNumberMsg.getText().toString();
                string = string + message;
                byte[] value;
                try {
                    //send data to service
                    value = string.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    timeNumberMsg.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
//                byte[] commendMessagevalue;
//                byte[] numberMessagevalue;
//                byte[] value;
//                String commendMessage = "set dhld";
//                String numberMessage = timeNumberMsg.getText().toString();
//                int count = numberMessage.length();
//                if (count % 2 == 0){
//                    try {
//                        if (numberMessage.equals("")){
//                            commendMessagevalue = commendMessage.getBytes("UTF-8");
//                            mService.writeRXCharacteristic(commendMessagevalue);
//                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                            listAdapter.add("["+currentDateTimeString+"] TX: "+ commendMessage + numberMessage);
//
//                        }else {
//                            commendMessagevalue = commendMessage.getBytes("UTF-8");
//                            numberMessagevalue = changeStringToThreeByte(numberMessage);
//                            //合并数组
//                            value = new byte[commendMessagevalue.length+numberMessagevalue.length];
//                            System.arraycopy(commendMessagevalue,0,value,0,commendMessagevalue.length);
//                            System.arraycopy(numberMessagevalue,0,value,commendMessagevalue.length,numberMessagevalue.length);
//
//                            mService.writeRXCharacteristic(value);
//                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                            listAdapter.add("["+currentDateTimeString+"] TX: "+ commendMessage + numberMessage);
//                        }
//                    }catch (UnsupportedEncodingException e) {
//
//                        e.printStackTrace();
//                    }
//
//
//                }else {
//                    Toast.makeText(MainActivity.this,"输入偶数对数字",Toast.LENGTH_LONG);
//                }


            }
        });

     
        // Set initial UI state
        
    }
    //TODO:截取数据，两个两个截取，做位运算--http://blog.csdn.net/yihui823/article/details/6754213---参考这个网站两个数字第一个向左移动四位，然后第二个跟第一个做与运算。
    //但个数字转换为byte类型数据
    private byte changeToByte(String string){
        if (string.equals("0")){
            return 0x00;
        }else if (string.equals("1")){
            return 0x01;
        }else if (string.equals("2")){
            return 0x02;
        }else if (string.equals("3")){
            return 0x03;
        }else if (string.equals("4")){
            return 0x04;
        }else if (string.equals("5")){
            return 0x05;
        }else if (string.equals("6")){
            return 0x06;
        }else if (string.equals("7")){
            return 0x07;
        }else if (string.equals("8")){
            return 0x08;
        }else if (string.equals("9")){
            return 0x09;
        }else if (string.equals("a")){
            return 0x0a;
        }else if (string.equals("A")){
            return 0x0a;
        }else if (string.equals("b")){
            return 0x0b;
        }else if (string.equals("B")){
            return 0x0b;
        }else if (string.equals("c")){
            return 0x0c;
        }else if (string.equals("C")){
            return 0x0c;
        }else if (string.equals("d")){
            return 0x0d;
        }else if (string.equals("D")){
            return 0x0d;
        }else if (string.equals("e")){
            return 0x0e;
        }else if (string.equals("E")){
            return 0x0e;
        }else if (string.equals("f")){
            return 0x0f;
        }else if (string.equals("F")){
            return 0x0f;
        }
      return 0;
    }
    //截取数据转换---固定为6位
    private byte[] changeStringToH(String string){
        //初始化byte
        byte  bb1 =0x00;
        byte  bb2 =0x00;
        byte  bb3 =0x00;
        byte  bb4 =0x00;
        byte  bb5 =0x00;
        byte  bb6 =0x00;
        byte  bb7 =0x00;
        byte  bb8 =0x00;
        byte  bb9 =0x00;
        byte  bb10=0x00;
        byte  bb11=0x00;
        byte  bb12=0x00;
        byte  bb13=0x00;
        byte  bb14=0x00;
        byte  bb15=0x00;
        byte  bb16=0x00;
        byte  bb17=0x00;
        byte  bb18=0x00;
        byte  bb19=0x00;
        byte  bb20=0x00;


        int count = string.length();
        if (count != 0) {
            int beginIndex = 0;
            int endIndex = 1;
            byte b=0x00;
            for (int i = 0;i<count;i++){
                String tempString;
                tempString = string.substring(beginIndex,endIndex);
                beginIndex = beginIndex +1;
                endIndex = endIndex +1;
                byte tempByte = changeToByte(tempString);
                if (i%2==0){
                    //取出偶数数值做位运算，并存在b变量里
                    b= (byte) (tempByte << 4);
                }
                if(i == 0) {
                    continue;
                }else {
                    //在偶数数值做完位运算后把后面那个数值与其做与运算
                    if (i==1) {
                        bb1 = (byte) (b + tempByte);
                    }else if(i==3) {
                        bb2 = (byte) (b + tempByte);
                    }else if (i==5){
                        bb3 = (byte) (b + tempByte);
                    }else if (i==7){
                        bb4 = (byte) (b + tempByte);
                    }else if (i==9){
                        bb5 = (byte) (b + tempByte);
                    }else if (i==11){
                        bb6 = (byte) (b + tempByte);
                    }else if (i==13){
                        bb7 = (byte) (b + tempByte);
                    }else if (i==15){
                        bb8 = (byte) (b + tempByte);
                    }else if (i==17){
                        bb9 = (byte) (b + tempByte);
                    }else if (i==19){
                        bb10 = (byte) (b + tempByte);
                    }else if (i==21){
                        bb11 = (byte) (b + tempByte);
                    }else if (i==23){
                        bb12 = (byte) (b + tempByte);
                    }else if (i==25){
                        bb13 = (byte) (b + tempByte);
                    }else if (i==27){
                        bb14 = (byte) (b + tempByte);
                    }else if (i==29){
                        bb15 = (byte) (b + tempByte);
                    }else if (i==31){
                        bb16 = (byte) (b + tempByte);
                    }else if (i==33){
                        bb17 = (byte) (b + tempByte);
                    }else if (i==35){
                        bb18 = (byte) (b + tempByte);
                    }else if (i==37){
                        bb19 = (byte) (b + tempByte);
                    }else if (i==39){
                        bb20 = (byte) (b + tempByte);
                    }
                }

            }

        }
        byte[] value = new byte[]{bb1,bb2,bb3,bb4,bb5,bb6,bb7,bb8,bb9,bb10,bb11,bb12,bb13,bb14,bb15,bb16,bb17,bb18,bb19,bb20};
        byte[] value1 = Arrays.copyOf(value,count/2);
        return value1;
    }

    //TODO截取2个的方法
    private byte[] changeStringToByte(String string) {

        int count = string.length()/2;
        byte[] value = new byte[]{};
        if (count != 0) {
            int beginIndex = 0;
            int endIndex = 2;
            for (int i = 0;i<count;i++){

//                bb2[0] = (byte) 0x30;
//                bb2[1] = (byte) 0x31;
//                bb2[2] = (byte) 0x32;
//                bb2[3] = (byte) 0x33;
//                bb2[3] = (byte) 0x34;

                String tempString;
                tempString = string.substring(beginIndex,endIndex);
                beginIndex = beginIndex +2;
                endIndex = endIndex +2;
                byte[] tempByte = new byte[]{(byte) Integer.parseInt(tempString)};
                if (i == 0) {
                    value = tempByte;
                }else  {
                    byte[] valueTemp = new byte[value.length+tempByte.length];
                    System.arraycopy(tempByte,0,valueTemp,0,tempByte.length);
                    System.arraycopy(value,0,valueTemp,tempByte.length,value.length);
                    value = valueTemp;
                }
            }

        }
        byte[] byteNew=new  byte[value.length];
        for(int i=0;i<value.length;i++)
        {
            byteNew[i]=value[value.length-i-1];
        }

        return byteNew;
    }
    //TODO截取3个的方法
    private byte[] changeStringToThreeByte(String string) {

        int count = string.length()/3;
        byte[] value = new byte[]{};
        if (count != 0) {
            int beginIndex = 0;
            int endIndex = 3;
            for (int i = 0;i<count;i++){

//                bb2[0] = (byte) 0x30;
//                bb2[1] = (byte) 0x31;
//                bb2[2] = (byte) 0x32;
//                bb2[3] = (byte) 0x33;
//                bb2[3] = (byte) 0x34;

                String tempString;
                tempString = string.substring(beginIndex,endIndex);
                beginIndex = beginIndex +3;
                endIndex = endIndex +3;
//                byte[] tempByte = new byte[]{(byte) Integer.parseInt(tempString)};
                byte[] tempByte = tempString.getBytes();
                if (i == 0) {

                    value = tempByte;
                }else  {
                    byte[] valueTemp = new byte[value.length+tempByte.length];
                    System.arraycopy(tempByte,0,valueTemp,0,tempByte.length);
                    System.arraycopy(value,0,valueTemp,tempByte.length,value.length);
                    value = valueTemp;
                }
            }

        }
        byte[] byteNew=new  byte[value.length];
        for(int i=0;i<value.length;i++)
        {
            byteNew[i]=value[value.length-i-1];
        }

        return byteNew;
    }

    //数组合并
    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
        		mService = ((UartService.LocalBinder) rawBinder).getService();
        		Log.d(TAG, "onServiceConnected mService= " + mService);
        		if (!mService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }

        }

        public void onServiceDisconnected(ComponentName classname) {
       ////     mService.disconnect(mDevice);
        		mService = null;
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        
        //Handler events that received from UART service 
        public void handleMessage(Message msg) {
  
        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
           //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
                         	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.d(TAG, "UART_CONNECT_MSG");
                             btnConnectDisconnect.setText("Disconnect");
                             edtMessage.setEnabled(true);
                             btnSend.setEnabled(true);
//                             ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - ready");
                             listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                        	 	messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                             mState = UART_PROFILE_CONNECTED;
                     }
            	 });
            }
           
          //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
                    	 	 String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.d(TAG, "UART_DISCONNECT_MSG");
                             btnConnectDisconnect.setText("Connect");
                             edtMessage.setEnabled(false);
                             btnSend.setEnabled(false);
//                             ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                             listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                             mState = UART_PROFILE_DISCONNECTED;
                             mService.close();
                            //setUiState();
                         
                     }
                 });
            }
            
          
          //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
             	 mService.enableTXNotification();
            }
          //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
                String stringValue = "";
                 final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                //加冒号的方法
                if (txValue.length > 1){

                    for (byte txValueHex : txValue){
                        String temp = Integer.toHexString((txValueHex & 0xff));
                        stringValue = stringValue +":"+temp;
                    }
                }
                final String finalStringValue = stringValue;
                runOnUiThread(new Runnable() {
                     public void run() {
                         try {
                             //按哪种编码格式
                         	String textUTF = new String(txValue, "UTF-8");
                             String textISO = new String(txValue,"ISO-8859-1");
                         	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());


                        //     listAdapter.add("["+currentDateTimeString+"] RX: "+"编码规则UTF-8：---"+text+"----编码规则ISO-8859-1----"+textISO+"----字符Byte:"+ finalStringValue);
                             listAdapter.add("["+currentDateTimeString+"] RX:");
                             listAdapter.add("编码规则UTF-8:"+textUTF);
                             listAdapter.add("编码规则ISO-8859-1:"+textISO);
                             listAdapter.add("字符Byte:"+ finalStringValue);

                             messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                         } catch (Exception e) {
                             Log.e(TAG, e.toString());
                         }
                     }
                 });
             }
           //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
            	showMessage("Device doesn't support UART. Disconnecting");
            	mService.disconnect();
            }
            
            
        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
  
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
    	 super.onDestroy();
        Log.d(TAG, "onDestroy()");
        
        try {
        	LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        } 
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;
       
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
 
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

        case REQUEST_SELECT_DEVICE:
        	//When the DeviceListActivity return, with the selected device address
            if (resultCode == Activity.RESULT_OK && data != null) {
                String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
               
                Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
//                ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                mService.connect(deviceAddress);

            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        default:
            Log.e(TAG, "wrong request code");
            break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
       
    }

    
    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
  
    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.popup_title)
            .setMessage(R.string.popup_message)
            .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
   	                finish();
                }
            })
            .setNegativeButton(R.string.popup_no, null)
            .show();
        }
    }
}
