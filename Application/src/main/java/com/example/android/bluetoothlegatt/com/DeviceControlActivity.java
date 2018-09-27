/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt.com;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.example.android.bluetoothlegatt.Common.CommonData;
import com.example.android.bluetoothlegatt.R;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity implements View.OnClickListener {
    private final static String TAG = "DCA";

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss");
    public static final int DUMP = -1;

    private TextView mDataTextView;
    private ScrollView mDataScrollView;

    //TODO: GTO code
    public static String b_length;

    public static String b_gyro1;
    public static String b_gyro2;
    public static String b_gyro3;

    public static String b_acc1;
    public static String b_acc2;
    public static String b_acc3;

    public static int b_ac_x;
    public static int b_ac_y;
    public static int b_ac_z;

    public static int b_gy_x;
    public static int b_gy_y;
    public static int b_gy_z;

    public static int count;

    public static String b_sleep_bpm;
    public static String b_discount_bpm;
    public static String b_condition;

    public static int side_count;
    public static int front_count;
    public static int up_count;
    public static int anti_count;
    public static int clock_count;
    public static int default_fail_count;
    public static int fail_count;

    public static int gesture_value;

    public static int select_radio;

    // TODO: URBAN
    private Button mManboSyncBtn;
    private Button mPPGSyncBtn;
    private Button mSleepSyncBtn;
    private Button mPPGBtn;
    private Button mBaroBtn;

    // TODO: EXERCISE
    private Button mEXStartBtn;
    private Button mEXStopBtn;
    private Button mEXSyncBtn;
    private Button mEXUpdateBtn;

    // TODO: SETTING
    private Button mConnectionBtn;
    private Button mRTCBtn;
    private Button mUserProfileBtn;
    private Button mLanguageBtn;
    private Button mUnitBtn;
    private Button mVersionBtn;
    private Button mUserPPGBtn;
    private Button mSleepTimeBtn;
    private Button mPPGIntervalBtn;
    private Button mEXDisplayItemBtn;

    // TODO: NOTI
    private Button mCallBtn;
    private Button mAcceptCallBtn;
    private Button mSMSBtn;
    private Button mGoalBtn;
    private Button mAppNotiBtn;

    private RadioGroup r_Group;

    private Button side_btn;
    private Button up_btn;
    private Button front_btn;
    private Button clock_btn;


    private RadioButton r_side;
    private RadioButton r_up;
    private RadioButton r_front;
    private RadioButton r_clock;
    private RadioButton r_anti;

    private TextView sucess_Num;
    private TextView fail_Num;
    private TextView percent_Num;

    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";


    AccSlidingCollection asc = new AccSlidingCollection();



    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {

            //byte value [] = "H_Band".getBytes();
           // mBluetoothLeService.writeRXCharacteristic(value);

            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                 String action = intent.getAction();
                 if (mDeviceName.equals("H_Band"))    {

                    final Intent mIntent = intent;
                    //*********************//
                    if (action.equals(mBluetoothLeService.ACTION_GATT_CONNECTED)) {

                        runOnUiThread(new Runnable() {
                            public void run() {
                                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                                mBluetoothLeService.mState = mBluetoothLeService.UART_PROFILE_CONNECTED;
                            }
                        });
                    }

                    //*********************//
                    if (action.equals(mBluetoothLeService.ACTION_GATT_DISCONNECTED)) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                                mBluetoothLeService.mState = mBluetoothLeService.UART_PROFILE_DISCONNECTED;
                                mBluetoothLeService.close();
                                //setUiState();

                            }
                        });
                    }


                    //*********************//
                    if (action.equals(mBluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)) {

                        mBluetoothLeService.enableTXNotification();

                    }
                    //*********************//
                    if (action.equals(mBluetoothLeService.ACTION_DATA_AVAILABLE)) {

                        final byte[] txValue = intent.getByteArrayExtra(mBluetoothLeService.EXTRA_DATA);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    String text = new String(txValue, "UTF-8");
                                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                                    //Log.d (TAG, currentDateTimeString + " " + text);
                                    Log.d (TAG, stringToHex0x(text));


                                } catch (Exception e) {
                                    Log.e(TAG, e.toString());
                                }
                            }
                        });
                    }
                    //*********************//
                    if (action.equals(mBluetoothLeService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                        Log.d(TAG, "Device doesn't support UART. Disconnecting");
                        mBluetoothLeService.disconnect();
                    }

                }



            else    {

                    if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                        mConnected = true;
                        updateConnectionState(R.string.connected);
                        invalidateOptionsMenu();
                    } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                        mConnected = false;
                        updateConnectionState(R.string.disconnected);
                        invalidateOptionsMenu();
                    } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                        // Show all the supported services and characteristics on the user interface.


                        displayGattServices(mBluetoothLeService.getSupportedGattServices());

                        final BluetoothGattCharacteristic notifyCharacteristic = getNottifyCharacteristic();
                        if (notifyCharacteristic == null) {
                            Toast.makeText(getApplication(), "gatt_services can not supported", Toast.LENGTH_SHORT).show();
                            mConnected = false;
                            return;
                        }
                        final int charaProp = notifyCharacteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mBluetoothLeService.setCharacteristicNotification(
                                    notifyCharacteristic, true);
                        }

                    } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                        byte[] packet = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                        displayData(packet);
                    }
             }
            }

    };

    public static String stringToHex(String s) {
        String result = "";

        for (int i = 0; i < s.length(); i++) {
            result += String.format("%02X ", (int) s.charAt(i));
        }

        return result;
    }


    // 헥사 접두사 "0x" 붙이는 버전
    public static String stringToHex0x(String s) {
        String result = "";

        for (int i = 0; i < s.length(); i++) {
            result += String.format("0x%02X ", (int) s.charAt(i));
        }

        return result;
    }





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataTextView = (TextView) findViewById(R.id.send_data_tv);
        mDataScrollView = (ScrollView) findViewById(R.id.sd_scroll);

        // TODO: URBAN
        mManboSyncBtn = (Button) findViewById(R.id.ManboUpdateBtn);
        mPPGSyncBtn = (Button) findViewById(R.id.PPGUpdateBtn);
        mSleepSyncBtn = (Button) findViewById(R.id.SleepUpdateBtn);
        mPPGBtn = (Button) findViewById(R.id.PPGBtn);
        mBaroBtn = (Button) findViewById(R.id.BaroBtn);
        // TODO: EXERCISE
        mEXStartBtn = (Button) findViewById(R.id.ExerciseStartBtn);
        mEXStopBtn = (Button) findViewById(R.id.ExerciseStopBtn);
        mEXSyncBtn = (Button) findViewById(R.id.ExerciseSyncBtn);
        mEXUpdateBtn = (Button) findViewById(R.id.ExerciseUpdateBtn);
        // TODO: SETTING
        mConnectionBtn = (Button) findViewById(R.id.ConnectionBtn);
        mRTCBtn = (Button) findViewById(R.id.RTCBtn);
        mUserProfileBtn = (Button) findViewById(R.id.UserProfileBtn);
        mLanguageBtn = (Button) findViewById(R.id.LanguageBtn);
        mUnitBtn = (Button) findViewById(R.id.UnitBtn);
        mVersionBtn = (Button) findViewById(R.id.VersionBtn);
        mUserPPGBtn = (Button) findViewById(R.id.UserPPGBtn);
        mSleepTimeBtn = (Button) findViewById(R.id.SleepTimeBtn);
        mPPGIntervalBtn = (Button) findViewById(R.id.PPGIntervalBtn);
        mEXDisplayItemBtn = (Button) findViewById(R.id.ExerciseDisplayBtn);
        // TODO: NOTI
        mCallBtn = (Button) findViewById(R.id.CallBtn);
        mAcceptCallBtn = (Button) findViewById(R.id.CallAcceptBtn);
        mSMSBtn = (Button) findViewById(R.id.SMSBtn);
        mGoalBtn = (Button) findViewById(R.id.GoalBtn);
        mAppNotiBtn = (Button) findViewById(R.id.AppNotiBtn);

        //TODO RadioGroup
        final TextView g_Name = (TextView) findViewById(R.id.gesture_name);
        r_Group = (RadioGroup) findViewById(R.id.radioGroup);

        //TODO Checked List Radio Button
        r_Group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mDataTextView.setText("");
                //sucess_Num.setText("");
                //fail_Num.setText("");
                //percent_Num.setText("");
                asc.gesture_count = 0;
                up_count = 0;
                side_count = 0;
                front_count = 0;
                clock_count = 0;
                anti_count = 0;

                g_Name.setText(((RadioButton)findViewById(checkedId)).getText());
            }
        });

        side_btn = (Button) findViewById(R.id.figure_layer_1);
        up_btn = (Button) findViewById(R.id.figure_layer_2);
        front_btn = (Button) findViewById(R.id.figure_layer_3);
        clock_btn = (Button) findViewById(R.id.figure_layer_4);

        final ImageView point_btn = (ImageView) findViewById(R.id.cl_line);

        side_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(
                        getApplicationContext(), R.anim.translate
                );
                point_btn.startAnimation(anim);
            }
        });

        up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(
                        getApplicationContext(), R.anim.translate_up
                );
                point_btn.startAnimation(anim);
            }
        });

        clock_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(
                        getApplicationContext(), R.anim.rotate
                );
                point_btn.startAnimation(anim);
            }
        });

        front_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(
                        getApplicationContext(), R.anim.scale_front
                );
                point_btn.startAnimation(anim);
            }
        });





        mDataTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus == true) {
                    mDataScrollView.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            mDataScrollView.smoothScrollBy(0, 800);
                        }
                    }, 100);
                }
            }
        });

        // TODO: URBAN
        mManboSyncBtn.setOnClickListener(this);
        mPPGSyncBtn.setOnClickListener(this);
        mSleepSyncBtn.setOnClickListener(this);
        mPPGBtn.setOnClickListener(this);
        mBaroBtn.setOnClickListener(this);
        // TODO: EXERCISE
        mEXStartBtn.setOnClickListener(this);
        mEXStopBtn.setOnClickListener(this);
        mEXSyncBtn.setOnClickListener(this);
        mEXUpdateBtn.setOnClickListener(this);
        // TODO: SETTING
        mConnectionBtn.setOnClickListener(this);
        mRTCBtn.setOnClickListener(this);
        mUserProfileBtn.setOnClickListener(this);
        mLanguageBtn.setOnClickListener(this);
        mUnitBtn.setOnClickListener(this);
        mVersionBtn.setOnClickListener(this);
        mUserPPGBtn.setOnClickListener(this);
        mSleepTimeBtn.setOnClickListener(this);
        mPPGIntervalBtn.setOnClickListener(this);
        mEXDisplayItemBtn.setOnClickListener(this);
        // TODO: NOTI
        mCallBtn.setOnClickListener(this);
        mAcceptCallBtn.setOnClickListener(this);
        mSMSBtn.setOnClickListener(this);
        mGoalBtn.setOnClickListener(this);
        mAppNotiBtn.setOnClickListener(this);

        //getActionBar().setTitle(mDeviceName);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mManboSyncBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_INFO_SYNC_START_REQ);
        } else if (v.equals(mPPGSyncBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_PPG_SYNC_START_REQ);
        } else if (v.equals(mSleepSyncBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_SLEEP_SYNC_START_REQ);
        } else if (v.equals(mPPGBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_PPG_START_REQ);
        } else if (v.equals(mBaroBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_BARO_START_REQ);
        } else if (v.equals(mEXStartBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.EXERCISE_AtoB_START_REQ);
        } else if (v.equals(mEXStopBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.EXERCISE_AtoB_STOP_REQ);
        } else if (v.equals(mEXSyncBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.EXERCISE_SYNC_REQ);
        } else if (v.equals(mEXUpdateBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.EXERCISE_SYNC_START_REQ);
        } else if (v.equals(mConnectionBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.CONNECTION_REQ);
        } else if (v.equals(mRTCBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.RTC_REQ);
        } else if (v.equals(mUserProfileBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.USERPROFILE_REQ);
        } else if (v.equals(mLanguageBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.LANGUAGE_REQ);
        } else if (v.equals(mUnitBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.UNIT_REQ);
        } else if (v.equals(mVersionBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.VERSION_REQ);
        } else if (v.equals(mUserPPGBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.USER_PPG_REQ);
        } else if (v.equals(mSleepTimeBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.SLEEP_TIME_REQ);
        } else if (v.equals(mPPGIntervalBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.PPG_INTERVAL_REQ);
        } else if (v.equals(mEXDisplayItemBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.EXERCISE_DISPLAY_ITEM_REQ);
        } else if (v.equals(mCallBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.CUSTOM_CONTENTS_START_REQ);
        } else if (v.equals(mAcceptCallBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.CALL_ACCEPT_REQ);
        } else if (v.equals(mSMSBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.SMS_REQ);
        } else if (v.equals(mGoalBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.GOAL_AtoB_REQ);
        } else if (v.equals(mAppNotiBtn)) {
            mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.OTA_START_REQ);
        }


    }
    private void showMessage(String msg) {
        Log.e(TAG, msg);
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private String getCurrentTime() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        return mFormat.format(date);
    }

    private void displayData(byte[] packet) {

        if (packet != null) {
            byte msgType = packet[1];

            switch(msgType) {
                // TODO: URBAN
                case CommonData.URBAN_INFO_REQ:
                    mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_INFO_CFM);
                    break;

                case CommonData.URBAN_INFO_DB_REQ:
                    mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_INFO_DB_CFM);
                    break;

                case CommonData.URBAN_PPG_REQ:
                    mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_PPG_CFM);
                    break;

                case CommonData.URBAN_PPG_DB_REQ:
                    mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_PPG_DB_REQ);
                    break;

                case CommonData.URBAN_CONDITION_REQ:
                    mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_CONDITION_CFM);
                    break;

                case CommonData.URBAN_GOAL_CFM:
                    break;

                case CommonData.URBAN_SLEEP_DB_REQ:
                    mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_SLEEP_DB_CFM);
                    break;

                case CommonData.URBAN_SLEEP_WU_REQ:
                    mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_SLEEP_WU_CFM);
                    break;

                case CommonData.URBAN_INFO_SYNC_START_CFM:
                    mBluetoothLeService.mManboTotalDataLen = ((packet[4] & 0XFF) << 8 | (packet[5] & 0XFF));
                    mBluetoothLeService.mManboPacketCnt = packet[6];
                    mBluetoothLeService.mManboRemindDataLen = mBluetoothLeService.mManboTotalDataLen % 12;
                    mBluetoothLeService.mManboSendCnt = 0;
                    Log.i(TAG, "PaketCnt = " + mBluetoothLeService.mManboPacketCnt
                            + ", TotalDataLen = " + mBluetoothLeService.mManboTotalDataLen
                            + ", RemindDataLen = " + mBluetoothLeService.mManboRemindDataLen);
                    mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_INFO_SYNC_DATA_REQ);
                    break;

                case CommonData.URBAN_INFO_SYNC_DATA_CFM:
                    mBluetoothLeService.mManboSendCnt = packet[3];
                    Log.i(TAG, "SendCnt = " + mBluetoothLeService.mManboSendCnt);
                    if(mBluetoothLeService.mManboSendCnt < mBluetoothLeService.mManboPacketCnt) {
                        mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_INFO_SYNC_DATA_REQ);
                    } else if (mBluetoothLeService.mManboSendCnt == mBluetoothLeService.mManboPacketCnt) {
                        Log.i(TAG, "MANBO SYNC SUCCESS");
                    }
                    break;

                case CommonData.URBAN_PPG_SYNC_START_CFM:
                    mBluetoothLeService.mPPGTotalDataLen = ((packet[4] & 0XFF) << 8 | (packet[5] & 0XFF));
                    mBluetoothLeService.mPPGPacketCnt = packet[6];
                    mBluetoothLeService.mPPGRemindDataLen = mBluetoothLeService.mPPGTotalDataLen % 12;
                    mBluetoothLeService.mPPGSendCnt = 0;
                    Log.i(TAG, "PaketCnt = " + mBluetoothLeService.mPPGPacketCnt
                            + ", TotalDataLen = " + mBluetoothLeService.mPPGTotalDataLen
                            + ", RemindDataLen = " + mBluetoothLeService.mPPGRemindDataLen);
                    mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_PPG_SYNC_DATA_REQ);
                    break;

                case CommonData.URBAN_PPG_SYNC_DATA_CFM:
                    mBluetoothLeService.mPPGSendCnt = packet[3];
                    Log.i(TAG, "SendCnt = " + mBluetoothLeService.mPPGSendCnt);
                    if(mBluetoothLeService.mPPGSendCnt < mBluetoothLeService.mPPGPacketCnt) {
                        mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_PPG_SYNC_DATA_REQ);
                    } else if (mBluetoothLeService.mPPGSendCnt == mBluetoothLeService.mPPGPacketCnt) {
                        Log.i(TAG, "PPG SYNC SUCCESS");
                    }
                    break;

                case CommonData.URBAN_SLEEP_SYNC_START_CFM:
                    if(packet[3] != 0xFF) {
                        mBluetoothLeService.mSleepTotalDataLen = packet[3];
                        mBluetoothLeService.mSleepPacketCnt = packet[4];
                        mBluetoothLeService.mSleepRemindDataLen = mBluetoothLeService.mSleepTotalDataLen % 12;
                        mBluetoothLeService.mSleepSendCnt = 0;
                        Log.i(TAG, "PaketCnt = " + mBluetoothLeService.mSleepPacketCnt
                                + ", TotalDataLen = " + mBluetoothLeService.mSleepTotalDataLen
                                + ", RemindDataLen = " + mBluetoothLeService.mSleepRemindDataLen);
                        mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_SLEEP_SYNC_DATA_REQ);
                    }
                    break;

                case CommonData.URBAN_SLEEP_SYNC_DATA_CFM:
                    mBluetoothLeService.mSleepSendCnt = packet[3];
                    Log.i(TAG, "SendCnt = " + mBluetoothLeService.mSleepSendCnt);
                    if(mBluetoothLeService.mSleepSendCnt < mBluetoothLeService.mSleepPacketCnt) {
                        mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.URBAN_SLEEP_SYNC_DATA_REQ);
                    } else if (mBluetoothLeService.mSleepSendCnt == mBluetoothLeService.mSleepPacketCnt) {
                        Log.i(TAG, "SLEEP SYNC SUCCESS");
                    }
                    break;

                case CommonData.URBAN_PPG_START_CFM:
                    Log.i(TAG, "HRM = " + packet[4] + "STRESS = " + packet[5]);
                    break;

                case CommonData.URBAN_BARO_START_CFM:
                    Log.i(TAG, "ALTITUDE = " + ((packet[4] & 0XFF) << 8 | (packet[5] & 0XFF)));
                    break;

                // TODO: EXERCISE
                case CommonData.EXERCISE_BtoA_START_REQ:
                    mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.EXERCISE_BtoA_START_CFM);
                    break;

                case CommonData.EXERCISE_BtoA_STOP_REQ:
                    mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.EXERCISE_BtoA_STOP_CFM);
                    break;

                case CommonData.EXERCISE_AtoB_START_CFM:
                    break;

                case CommonData.EXERCISE_AtoB_STOP_CFM:
                    Log.i(TAG, "Step = " + ((packet[4] & 0XFF) << 8 | (packet[5] & 0XFF))
                            + ", Distance = " + (((packet[6] & 0XFF) << 8 | (packet[7] & 0XFF)) / 100)
                            + ", Calories = " + ((packet[8] & 0XFF) << 8 | (packet[9] & 0XFF))
                            + ", Speed = " + (((packet[10] & 0XFF) << 8 | (packet[11] & 0XFF)) / 100)
                            + ", Time = " + ((packet[12] & 0XFF) << 8 | (packet[13] & 0XFF))
                            + ", Altitude = " + ((packet[14] & 0XFF) << 8 | (packet[15] & 0XFF)));
                    break;

                case CommonData.EXERCISE_INFO_REQ:
                    Log.i(TAG, "Step = " + ((packet[3] & 0XFF) << 8 | (packet[4] & 0XFF))
                            + ", Distance = " + (((packet[5] & 0XFF) << 8 | (packet[6] & 0XFF)) / 100)
                            + ", Calories = " + ((packet[7] & 0XFF) << 8 | (packet[8] & 0XFF))
                            + ", Speed = " + (((packet[9] & 0XFF) << 8 | (packet[10] & 0XFF)) / 100)
                            + ", Time = " + ((packet[11] & 0XFF) << 8 | (packet[12] & 0XFF))
                            + ", Altitude = " + ((packet[13] & 0XFF) << 8 | (packet[14] & 0XFF)));
                    mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.EXERCISE_INFO_CFM);
                    break;

                case CommonData.EXERCISE_PPG_REQ:
                    Log.i(TAG, "HRM = " + packet[3]);
                    mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.EXERCISE_PPG_CFM);
                    break;

                case CommonData.EXERCISE_SYNC_CFM:
                    Log.i(TAG, "Step = " + ((packet[4] & 0XFF) << 8 | (packet[5] & 0XFF))
                            + ", Distance = " + (((packet[6] & 0XFF) << 8 | (packet[7] & 0XFF)) / 100)
                            + ", Calories = " + ((packet[8] & 0XFF) << 8 | (packet[9] & 0XFF))
                            + ", Speed = " + (((packet[10] & 0XFF) << 8 | (packet[11] & 0XFF)) / 100)
                            + ", Time = " + ((packet[12] & 0XFF) << 8 | (packet[13] & 0XFF))
                            + ", Altitude = " + ((packet[14] & 0XFF) << 8 | (packet[15] & 0XFF)));
                    break;

                case CommonData.EXERCISE_SYNC_START_CFM:
                    if(packet[3] == 0x00) {
                        mBluetoothLeService.mExerciseInterval = packet[4];
                        mBluetoothLeService.mExerciseHRMTotalDataLen = ((packet[5] & 0XFF) << 8 | (packet[6] & 0XFF));
                        mBluetoothLeService.mExerciseHRMPacketCnt = packet[7];
                        mBluetoothLeService.mExerciseHRMRemindDataLen = mBluetoothLeService.mExerciseHRMTotalDataLen % 12;
                        mBluetoothLeService.mExerciseHRMSendCnt = 0;

                        mBluetoothLeService.mExerciseAltitudeTotalDataLen = ((packet[8] & 0XFF) << 8 | (packet[9] & 0XFF));
                        mBluetoothLeService.mExerciseAltitudePacketCnt = packet[10];
                        mBluetoothLeService.mExerciseAltitudeRemindDataLen = mBluetoothLeService.mExerciseAltitudeTotalDataLen % 12;
                        mBluetoothLeService.mExerciseAltitudeSendCnt = 0;

                        Log.i(TAG, "Interval = " + mBluetoothLeService.mExerciseInterval);

                        Log.i(TAG, "HRMPaketCnt = " + mBluetoothLeService.mExerciseHRMPacketCnt
                                + ", HRMTotalDataLen = " + mBluetoothLeService.mExerciseHRMTotalDataLen
                                + ", HRMRemindDataLen = " + mBluetoothLeService.mExerciseHRMRemindDataLen);

                        Log.i(TAG, "AltitudePaketCnt = " + mBluetoothLeService.mExerciseAltitudePacketCnt
                                + ", AltitudeTotalDataLen = " + mBluetoothLeService.mExerciseAltitudeTotalDataLen
                                + ", AltitudeRemindDataLen = " + mBluetoothLeService.mExerciseAltitudeRemindDataLen);

                        mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.EXERCISE_SYNC_DATA_INFO_REQ);
                    } else {
                        Log.d(TAG, "EXERCISE_SYNC_START_CFM::Already SYNC");
                    }
                    break;

                case CommonData.EXERCISE_SYNC_DATA_CFM:
                    mBluetoothLeService.mExerciseHRMSendCnt = packet[3];
                    Log.i(TAG, "SendCnt = " + mBluetoothLeService.mExerciseHRMSendCnt);
                    if(mBluetoothLeService.mExerciseHRMSendCnt < mBluetoothLeService.mExerciseHRMPacketCnt) {
                        mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.EXERCISE_SYNC_DATA_REQ);
                    } else if (mBluetoothLeService.mExerciseHRMSendCnt == mBluetoothLeService.mExerciseHRMPacketCnt) {
                        Log.i(TAG, "EXERCISE SYNC SUCCESS");
                    }
                    break;

                case CommonData.EXERCISE_SYNC_DATA_INFO_CFM:
                    Log.d(TAG, "EXERCISE_SYNC_DATA_INFO_CFM::Status = "+packet[3]);
                    mBluetoothLeService.mWalkingStep = ((packet[4] & 0XFF) << 8 | (packet[5] & 0XFF));
                    mBluetoothLeService.mWalkingDistance = (((packet[6] & 0XFF) << 8 | (packet[7] & 0XFF)) / 100);
                    mBluetoothLeService.mWalkingCalories = ((packet[8] & 0XFF) << 8 | (packet[9] & 0XFF));
                    mBluetoothLeService.mWalkingSpeed = (((packet[10] & 0XFF) << 8 | (packet[11] & 0XFF)) / 100);
                    mBluetoothLeService.mWalkingTime = ((packet[12] & 0XFF) << 8 | (packet[13] & 0XFF));
                    mBluetoothLeService.mWalkingAltitude = ((packet[14] & 0XFF) << 8 | (packet[15] & 0XFF));

                    Log.i(TAG, "Step = " + mBluetoothLeService.mWalkingStep
                            + ", Distance = " + mBluetoothLeService.mWalkingDistance
                            + ", Calories = " + mBluetoothLeService.mWalkingCalories
                            + ", Speed = " + mBluetoothLeService.mWalkingSpeed
                            + ", Time = " + mBluetoothLeService.mWalkingTime
                            + ", Altitude = " + mBluetoothLeService.mWalkingAltitude);

                    mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.EXERCISE_SYNC_DATA_HRM_REQ);
                    break;

                case CommonData.EXERCISE_SYNC_DATA_HRM_CFM:
                    mBluetoothLeService.mExerciseHRMSendCnt = packet[3];
                    Log.i(TAG, "SendCnt = " + mBluetoothLeService.mExerciseHRMSendCnt);
                    if(mBluetoothLeService.mExerciseHRMSendCnt < mBluetoothLeService.mExerciseHRMPacketCnt) {
                        mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.EXERCISE_SYNC_DATA_HRM_REQ);
                    } else if (mBluetoothLeService.mExerciseHRMSendCnt == mBluetoothLeService.mExerciseHRMPacketCnt) {
                        Log.i(TAG, "EXERCISE HRM SYNC SUCCESS");
                        mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.EXERCISE_SYNC_DATA_ALTITUDE_REQ);
                    }
                    break;

                case CommonData.EXERCISE_SYNC_DATA_ALTITUDE_CFM:
                    mBluetoothLeService.mExerciseAltitudeSendCnt = packet[3];
                    Log.i(TAG, "SendCnt = " + mBluetoothLeService.mExerciseAltitudeSendCnt);
                    if(mBluetoothLeService.mExerciseAltitudeSendCnt < mBluetoothLeService.mExerciseAltitudePacketCnt) {
                        mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.EXERCISE_SYNC_DATA_ALTITUDE_REQ);
                    } else if (mBluetoothLeService.mExerciseAltitudeSendCnt == mBluetoothLeService.mExerciseAltitudePacketCnt) {
                        Log.i(TAG, "EXERCISE ALTITUDE SYNC SUCCESS");
                    }
                    break;

                case (byte)CommonData.ACC:

                    break;

                case CommonData.CONNECTION_CFM:
                    break;

                case CommonData.RTC_CFM:
                    break;

                case CommonData.USERPROFILE_CFM:
                    break;

                case CommonData.LANGUAGE_CFM:
                    break;

                case CommonData.UNIT_CFM:
                    break;

                case CommonData.VERSION_CFM:
                    break;

                case CommonData.USER_PPG_CFM:
                    break;

                case CommonData.SLEEP_TIME_CFM:
                    break;

                case CommonData.PPG_INTERVAL_CFM:
                    break;

                case CommonData.EXERCISE_DISPLAY_ITEM_CFM:
                    break;

                case CommonData.CALL_CFM:
                    break;

                case CommonData.CALL_ACCEPT_CFM:
                    break;

                case CommonData.SMS_CFM:
                    break;

                case CommonData.GOAL_BtoA_REQ:
                    mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.GOAL_BtoA_CFM);
                    break;

                case CommonData.GOAL_AtoB_CFM:
                    break;

                case CommonData.APP_NOTI_CFM:
                    break;

                case CommonData.CUSTOM_CONTENTS_START_CFM:
                    Log.d(TAG, "CUSTOM_CONTENTS_START_CFM : " + mBluetoothLeService.mCustomSendCnt);
                    mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.CUSTOM_CONTENTS_DATA_REQ);
                    break;

                case CommonData.CUSTOM_CONTENTS_DATA_CFM:
                    mBluetoothLeService.mCustomSendCnt = packet[3];
                    Log.d(TAG, "CUSTOM_CONTENTS_DATA_CFM : " + mBluetoothLeService.mCustomSendCnt);
                    if(mBluetoothLeService.mCustomSendCnt < 2) {
                        mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.CUSTOM_CONTENTS_DATA_REQ);
                    } else {
                        Log.i(TAG, "CUSTOM_CONTENTS_DATA SUCCESS!!!");
                    }
                    break;

                case CommonData.OTA_START_CFM:
                    mBluetoothLeService.mOTASendCnt = 1;
                    mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.OTA_DATA_REQ);
                    break;

                case CommonData.OTA_DATA_CFM:
                    mBluetoothLeService.mOTASendCnt = 1;
                    mBluetoothLeService.mOTASendPageCnt = packet[3];
                    Log.i(TAG, "SendPageCnt = " + mBluetoothLeService.mOTASendPageCnt);
                    if(mBluetoothLeService.mOTASendPageCnt < 105) {
                        mBluetoothLeService.writeGattCharacteristic(getWriteGattCharacteristic(), CommonData.OTA_DATA_REQ);
                    } else if (mBluetoothLeService.mOTASendPageCnt == 105) {
                        Log.i(TAG, "OTA SUCCESS");
                    }
                    break;




                default:
                    break;

            }

            autoScrollView(getStringPacket(packet));
            getStringPacket(packet);
        }
    }

    private void autoScrollView(String text) {
        if (!text.isEmpty())
            mDataTextView.append(text);
        mDataScrollView.post(new Runnable() {
            @Override
            public void run() {
                mDataScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private String getStringPacket(byte[] packet) {

        String hexString = "";

        for (byte b : packet) {  						//readBuf -> Hex
            hexString += Integer.toString((b & 0xF0) >> 4, 16);
            hexString += Integer.toString(b & 0x0F, 16);
        }

        int [] recv_sensors = new int [6];


        String b_head;
        // TODO : GYRO X Y Z HEX
        b_head = hexString.substring(1, 6);

        b_gyro1 = hexString.substring(6, 10);
        b_gyro2 = hexString.substring(10, 14);
        b_gyro3 = hexString.substring(14, 18);

        // TODO : ACC X Y Z HEX
        b_acc1 = hexString.substring(18, 22);
        b_acc2 = hexString.substring(22, 26);
        b_acc3 = hexString.substring(26, 30);

        //TODO : Band BPM information
        b_sleep_bpm = hexString.substring(32, 34);
        b_discount_bpm = hexString.substring(34, 36);
        b_condition = hexString.substring(36, 38);
        String b_condition_0;
        b_condition_0 = hexString.substring(30, 32);


        // TODO : ACC X Y Z Decimal
        b_ac_x = (short) Integer.parseInt(b_acc1, 16);
        b_ac_y = (short) Integer.parseInt(b_acc2, 16);
        b_ac_z = (short) Integer.parseInt(b_acc3, 16);

        // TODO : GYRO X Y Z Decimal
        b_gy_x = (short) Integer.parseInt(b_gyro1, 16);
        b_gy_y = (short) Integer.parseInt(b_gyro2, 16);
        b_gy_z = (short) Integer.parseInt(b_gyro3, 16);

        int r_bpm;
        int f_bpm;
        int b_con;
        int b_con_0;

        r_bpm = (short) Integer.parseInt(b_sleep_bpm, 16);
        f_bpm = (short) Integer.parseInt(b_discount_bpm, 16);
        b_con = (short) Integer.parseInt(b_condition, 16);
        b_con_0 = (short) Integer.parseInt(b_condition_0, 16);

        //System.out.println("fixBPM, realBPM -> "+ r_bpm + " " +  " " + f_bpm  + " ");
        //System.out.println("condition data -> " + " " + b_condition_0 + " " + b_con + " ");
        //System.out.println("Data length is -> "+ hexString.length() + " ");

        System.out.println(b_head + " " + b_ac_x + " " + b_ac_y + " " + b_ac_z + " " + b_gy_x + " " + b_gy_y + " " + b_gy_z + " " + r_bpm + " " + f_bpm + " "+ b_con_0 + " " + b_con);

        //System.out.println(b_ac_x + " " + b_ac_y + " " + b_ac_z + " " + b_gy_x + " " + b_gy_y + " " + b_gy_z + " " );           // Integer
        //System.out.println(b_acc1 + " " + b_acc2 + " " + b_acc3 + " " + b_gyro1 + " " + b_gyro2 + " " + b_gyro3 + " " );         // Hex
        //sb.append(" " + b_ac_x + " " + b_ac_y + " " + b_ac_z + " " + b_gy_x + " " + b_gy_y + " " + b_gy_z);

        recv_sensors [0] = b_ac_x;
        recv_sensors [1] = b_ac_y;
        recv_sensors [2] = b_ac_z;

        recv_sensors [3] = b_gy_x;
        recv_sensors [4] = b_gy_x;
        recv_sensors [5] = b_gy_x;

        StringBuilder sb = new StringBuilder(packet.length * 2);

        //sb.append ("###" + " Real BPM : " + "b_sleep_bpm"+ " " + " Discount BPM : " + "b_discount_bpm" + "###\n");

        int result_ = asc.SlidingCollectionInterface (recv_sensors);
        if (result_ != DUMP) {
            Log.d (TAG, "In IF branch");

            asc.gesture_count ++;
            sb.append(String.valueOf(asc.gesture_count));
            sb.append(" \t ");

            switch (result_)	{

                //TODO : FRONT Action
                case LEFT:
                    sb.append(" |||| FRONT ||| \n");
                    gesture_value = 1;
                    ++front_count;
                    front_btn.performClick();
                    break;

                case RIGHT:
                    sb.append (" ^^^^ UP ^^^^ \n");
                    gesture_value = 5;
                    ++up_count;
                    up_btn.performClick();
                    break;

                //TODO : CLOCK Action
                case FRONT:
                    sb.append (" ->->-> CLOCK ^^^^ \n");
                    gesture_value = 2;
                    ++clock_count;
                    clock_btn.performClick();
                    break;

                //TODO : SIDE Action
                case UP:
                    if(select_radio == r_up.getId()) {
                        sb.append (" ^^^^ UP ^^^^ \n");
                        ++up_count;
                        up_btn.performClick();
                    }
                    else {
                        sb.append("<<<< SIDE >>>> \n");
                        gesture_value = 3;
                        ++side_count;
                        side_btn.performClick();
                    }
                    break;

                case CLOCK:
                    //sb.append(" ??? Can't Detect ??? \n");
                    gesture_value = 4;
                    break;

                //TODO : UP Action
                case ANTI_CLOCK:
                    sb.append (" ^^^^ UP ^^^^ \n");
                    gesture_value = 5;
                    ++up_count;
                    up_btn.performClick();
                    break;

                default:
                    sb.append(" ??? Can't Detect ??? \n");
                    ++default_fail_count;
                    break;

            }	//switch

            sb.append("\n");

        }

        TextView per_side = (TextView) findViewById(R.id.textView7);
        TextView per_up = (TextView) findViewById(R.id.textView11);
        TextView per_front = (TextView) findViewById(R.id.textView8);
        TextView per_clock = (TextView) findViewById(R.id.textView6);

        int per_side_num = 0;
        int per_up_num = 0;
        int per_front_num = 0;
        int per_clock_num = 0;

        r_side = (RadioButton) findViewById(R.id.radioButton_side);
        r_up = (RadioButton) findViewById(R.id.radioButton_up);
        r_front = (RadioButton) findViewById(R.id.radioButton_front);
        r_clock = (RadioButton) findViewById(R.id.radioButton_clock);

        r_Group = (RadioGroup) findViewById(R.id.radioGroup);

        sucess_Num = (TextView) findViewById(R.id.Sucess_num) ;
        fail_Num = (TextView) findViewById(R.id.fail_num);
        percent_Num = (TextView) findViewById(R.id.percent_num);

        select_radio = r_Group.getCheckedRadioButtonId();

        if (select_radio == r_side.getId()) {
                int fail_sum = (up_count+front_count+clock_count+anti_count+default_fail_count);
                if (gesture_value == 3)
                {
                    sucess_Num.setText(Integer.toString(side_count));             // SIDE Gesture
                }
                else {
                    fail_Num.setText(Integer.toString(fail_sum));
                }

            int p_num = (int)(((double)side_count/((double)side_count+fail_sum))*100.0);

                percent_Num.setText(Integer.toString(p_num));
                per_side_num = p_num;
                per_side.setText(Integer.toString(per_side_num));
        }

        else if (select_radio == r_up.getId()) {
            int fail_sum = (clock_count+anti_count+front_count+default_fail_count);
            if (gesture_value == 5)
            {
                sucess_Num.setText(Integer.toString(up_count));             // UP Gesture
            }
            else {
                fail_Num.setText(Integer.toString(fail_sum));
            }

            int p_num = (int)(((double)up_count/((double)up_count+fail_sum))*100.0);

            percent_Num.setText(Integer.toString(p_num));
            per_up_num = p_num;
            per_up.setText(Integer.toString(per_up_num));
        }

        else if (select_radio == r_front.getId()) {
            int fail_sum = (side_count+clock_count+anti_count+up_count+default_fail_count);
            if (gesture_value == 1)
            {
                sucess_Num.setText(Integer.toString(front_count));             // FRONT Gesture
            }
            else {
                fail_Num.setText(Integer.toString(fail_sum));
            }

            int p_num = (int)(((double)front_count/((double)front_count+fail_sum))*100.0);

            percent_Num.setText(Integer.toString(p_num));
            per_front_num = p_num;
            per_front.setText(Integer.toString(per_front_num));
        }

        else if (select_radio == r_clock.getId()) {
            int fail_sum = (side_count+front_count+anti_count+up_count+default_fail_count);
            if (gesture_value == 2)
            {
                sucess_Num.setText(Integer.toString(clock_count));             // CLOCK Gesture
            }
            else {
                fail_Num.setText(Integer.toString(fail_sum));
            }

            int p_num = (int)(((double)clock_count/((double)clock_count+fail_sum))*100.0);

            percent_Num.setText(Integer.toString(p_num));
            per_clock_num = p_num;
            per_clock.setText(Integer.toString(per_clock_num));
        }


        return sb.toString();
    }

    private BluetoothGattCharacteristic getNottifyCharacteristic(){

        BluetoothGattCharacteristic notifyCharacteristic = null;
        if(mGattCharacteristics == null || mGattCharacteristics.size() == 0){
            return null;
        }
        for (int i = 0; i < mGattCharacteristics.size() ; i++) {
            for (int j = 0; j < mGattCharacteristics.get(i).size() ; j++) {
                notifyCharacteristic =  mGattCharacteristics.get(i).get(j);
                if(notifyCharacteristic.getUuid().equals(BluetoothLeService.FFF4_RATE_MEASUREMENT)){
                    return notifyCharacteristic;
                }
            }
        }
        return null;
    }

    private BluetoothGattCharacteristic getWriteGattCharacteristic(){

        BluetoothGattCharacteristic writeGattCharacteristic = null;
        if(mGattCharacteristics == null || mGattCharacteristics.size() == 0){
            return null;
        }

        for (int i = 0; i < mGattCharacteristics.size() ; i++) {
            for (int j = 0; j < mGattCharacteristics.get(i).size() ; j++) {
                writeGattCharacteristic =  mGattCharacteristics.get(i).get(j);
                if(writeGattCharacteristic. getUuid().equals(BluetoothLeService.FFF3_RATE_MEASUREMENT)){
                    return writeGattCharacteristic;
                }
            }
        }
        return null;
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {

        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_SEND_PACKET);
        return intentFilter;
    }







    public static final int FRONT = 0;
    public static final int BACK = FRONT + 1;	//1
    public static final int RIGHT = BACK + 1;   //2
    public static final int LEFT = RIGHT + 1;	//3
    public static final int UP = LEFT + 1;	//4
    public static final int DOWN = UP + 1;	//5
    public static final int	CLOCK = DOWN + 1;	//6
    public static final int ANTI_CLOCK = CLOCK + 1;	//7
    public static final int LOW_CLOCK = ANTI_CLOCK + 1;	//8
    public static final int LOW_ANTI = LOW_CLOCK + 1;	//9
    public static final int UNKNOWN_ = 99;

    public static final int GESTURE_NUM = LOW_ANTI + 1;	//10






}
