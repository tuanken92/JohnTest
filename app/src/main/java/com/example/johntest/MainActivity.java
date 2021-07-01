package com.example.johntest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.application.Application;
import com.example.common.CustomProgressDialog;
import com.example.common.hextoascii;
import com.example.rfid.RfidListeners;
import com.zebra.rfid.api3.ACCESS_OPERATION_CODE;
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;


import com.example.rfid.RFIDController;
import com.example.common.Constants;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.TagData;


import static android.content.Intent.ACTION_SCREEN_OFF;
import static android.content.Intent.ACTION_SCREEN_ON;
import static com.example.rfid.RFIDController.readers;

import static com.example.rfid.RFIDController.AUTO_DETECT_READERS;
import static com.example.rfid.RFIDController.AUTO_RECONNECT_READERS;
import static com.example.rfid.RFIDController.BatteryData;
import static com.example.rfid.RFIDController.EXPORT_DATA;
import static com.example.rfid.RFIDController.LAST_CONNECTED_READER;
import static com.example.rfid.RFIDController.NON_MATCHING;
import static com.example.rfid.RFIDController.NOTIFY_BATTERY_STATUS;
import static com.example.rfid.RFIDController.NOTIFY_READER_AVAILABLE;
import static com.example.rfid.RFIDController.NOTIFY_READER_CONNECTION;
import static com.example.rfid.RFIDController.SHOW_CSV_TAG_NAMES;
import static com.example.rfid.RFIDController.TagProximityPercent;
import static com.example.rfid.RFIDController.asciiMode;
import static com.example.rfid.RFIDController.bFound;
import static com.example.rfid.RFIDController.beeperVolume;
import static com.example.rfid.RFIDController.brandidcheckenabled;
import static com.example.rfid.RFIDController.channelIndex;
import static com.example.rfid.RFIDController.currentFragment;
import static com.example.rfid.RFIDController.dynamicPowerSettings;
import static com.example.rfid.RFIDController.getInstance;
import static com.example.rfid.RFIDController.inventoryMode;
import static com.example.rfid.RFIDController.isAccessCriteriaRead;
import static com.example.rfid.RFIDController.isBatchModeInventoryRunning;
import static com.example.rfid.RFIDController.isGettingTags;
import static com.example.rfid.RFIDController.isInventoryAborted;
import static com.example.rfid.RFIDController.isLocatingTag;
import static com.example.rfid.RFIDController.isLocationingAborted;
import static com.example.rfid.RFIDController.isTriggerRepeat;
import static com.example.rfid.RFIDController.is_connection_requested;
import static com.example.rfid.RFIDController.is_disconnection_requested;
import static com.example.rfid.RFIDController.ledState;
import static com.example.rfid.RFIDController.mConnectedDevice;
import static com.example.rfid.RFIDController.mConnectedReader;
import static com.example.rfid.RFIDController.mInventoryStartPending;
import static com.example.rfid.RFIDController.mIsInventoryRunning;
import static com.example.rfid.RFIDController.mRRStartedTime;
import static com.example.rfid.RFIDController.mReaderDisappeared;
import static com.example.rfid.RFIDController.pc;
import static com.example.rfid.RFIDController.phase;
import static com.example.rfid.RFIDController.readers;
import static com.example.rfid.RFIDController.regionNotSet;
import static com.example.rfid.RFIDController.reset;
import static com.example.rfid.RFIDController.rssi;
import static com.example.rfid.RFIDController.settings_startTrigger;
import static com.example.rfid.RFIDController.settings_stopTrigger;
import static com.example.rfid.RFIDController.tagListMatchAutoStop;
import static com.example.rfid.RFIDController.tagListMatchNotice;
import static com.example.rfid.RFIDController.toneGenerator;

public class MainActivity extends AppCompatActivity implements  View.OnClickListener{

    String TAG = "john.main";
    //GUI
    Button btnAdd;
    Button btnClear;
    ListView listview_data_tag;
    TextView tv_status;

    List<String> list_tag; //show UI
    ArrayAdapter<String> arrayAdapter;  //for display listview


    public static MainActivity.EventHandler eventHandler;

    //common Result Intent broadcasted by DataWedge
    private static final String DW_APIRESULT_ACTION = "com.symbol.datawedge.api.RESULT_ACTION";
    private static final String scanner_status = "com.symbol.datawedge.scanner_status";


    void first_Init()
    {
        eventHandler = new EventHandler();

    }



    public static boolean isBluetoothEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    private void loadReaders(final MainActivity context) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                InvalidUsageException invalidUsageException = null;
                try {
                    ArrayList<ReaderDevice> readersListArray = readers.GetAvailableRFIDReaderList();
                } catch (InvalidUsageException e) {
                    e.printStackTrace();
                    invalidUsageException = e;
                }
                if (invalidUsageException != null) {
                    readers.Dispose();
                    readers = null;
                    if (!isBluetoothEnabled()) {
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivity(enableIntent);
                    }

                    readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
                    readers.attach((Readers.RFIDReaderEventHandler) context);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (AUTO_RECONNECT_READERS && mConnectedDevice == null) {
                    Log.d(TAG,"Need reconnect here");
                }
            }
        }.execute();
    }



    void GUI_UI()
    {
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnClear = (Button) findViewById(R.id.btnClearData);
        listview_data_tag = (ListView) findViewById(R.id.list_receive_data);
        tv_status = (TextView) findViewById(R.id.tv_status);


        list_tag = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list_tag);
        listview_data_tag.setAdapter(arrayAdapter);
        btnAdd.setOnClickListener(this);
        btnClear.setOnClickListener(this);
    }

    void initializeConnectionSettings()
    {
        SharedPreferences settings = getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        AUTO_DETECT_READERS = settings.getBoolean(Constants.AUTO_DETECT_READERS, true);
        AUTO_RECONNECT_READERS = settings.getBoolean(Constants.AUTO_RECONNECT_READERS, true);
        NOTIFY_READER_AVAILABLE = settings.getBoolean(Constants.NOTIFY_READER_AVAILABLE, false);
        NOTIFY_READER_CONNECTION = settings.getBoolean(Constants.NOTIFY_READER_CONNECTION, false);
        if (Build.MODEL.contains("MC33"))
            NOTIFY_BATTERY_STATUS = settings.getBoolean(Constants.NOTIFY_BATTERY_STATUS, false);
        else
            NOTIFY_BATTERY_STATUS = settings.getBoolean(Constants.NOTIFY_BATTERY_STATUS, true);
        EXPORT_DATA = settings.getBoolean(Constants.EXPORT_DATA, false);
        //TAG_LIST_MATCH_MODE = settings.getBoolean(Constants.TAG_LIST_MATCH_MODE, false);
        SHOW_CSV_TAG_NAMES = settings.getBoolean(Constants.SHOW_CSV_TAG_NAMES, false);
        asciiMode = settings.getBoolean(Constants.ASCII_MODE, false);
        NON_MATCHING = settings.getBoolean(Constants.NON_MATCHING, false);
        LAST_CONNECTED_READER = settings.getString(Constants.LAST_READER, "");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GUI_UI();

        if (savedInstanceState == null) {
            Log.i(TAG, "savedInstanceState == null");
            //Toast.makeText(getApplicationContext(), "savedInstanceState == null", Toast.LENGTH_SHORT).show();
            eventHandler = new EventHandler();
            initializeConnectionSettings();
        } else {
            Log.i(TAG, "savedInstanceState != null");
            //Toast.makeText(getApplicationContext(), "savedInstanceState != null", Toast.LENGTH_SHORT).show();
            try {
                if (mConnectedReader != null) {
                    mConnectedReader.Events.removeEventsListener(eventHandler);
                    eventHandler = new EventHandler();
                    mConnectedReader.Events.addEventsListener(eventHandler);
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        }

        if (readers == null) {
            Log.i(TAG, "readers == null 1");
            readers = new Readers(this, ENUM_TRANSPORT.SERVICE_SERIAL);
            Log.i(TAG, "readers == null 2");
        }
        Log.i(TAG, "readers == null 11");
        readers.attach((Readers.RFIDReaderEventHandler) this);
        Log.i(TAG, "readers == null 22");

// Create a filter for the broadcast intent
        IntentFilter filter = new IntentFilter();
        // filter.addAction(scanner_status);
        filter.addAction(ACTION_SCREEN_OFF);
        filter.addAction(ACTION_SCREEN_ON);
        filter.addAction(DW_APIRESULT_ACTION);
        filter.addCategory("android.intent.category.DEFAULT");
        registerReceiver(BroadcastReceiver, filter);
        if (savedInstanceState == null) {
            loadReaders(this);
            // creates DW profile for Demo application
            getInstance().clearAllInventoryData();

        } else if (AUTO_RECONNECT_READERS) {
            AutoConnectDevice();

        }
        IntentFilter bluetoothFilter = new IntentFilter();
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(mReceiver, bluetoothFilter);


    }


    public static InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!isAllowed(source.charAt(i)))
                    return "";
            }
            return null;
        }

        String allowed = "0123456789ABCDEFabcdef";

        private boolean isAllowed(char c) {
            if (asciiMode == false) {
                for (char ch : allowed.toCharArray()) {
                    if (ch == c)
                        return true;
                }
                return false;
            }
            return true;
        }
    };

    private boolean m_ScreenOn = true;
    // Broadcast receiver to receive the scanner_status, and disable the scanner
    public BroadcastReceiver BroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_SCREEN_OFF:
                    //Log.d(TAG, "BroadcastReceiver: " + action + " " + RFIDController.mConnectedReader.getHostName());
                    if (!mIsInventoryRunning)
                        m_ScreenOn = false;
                    break;
                case ACTION_SCREEN_ON:
                    //Log.d(TAG, "BroadcastReceiver: " + action + " " + RFIDController.mConnectedReader.getHostName());
                    m_ScreenOn = true;
                    break;
                case scanner_status:
                    //Log.d(TAG, intent.getExtras().getString("STATUS"));
                    break;
                case DW_APIRESULT_ACTION: {
                    String command = intent.getStringExtra("COMMAND");
                    String commandidentifier = intent.getStringExtra("COMMAND_IDENTIFIER");
                    String result = intent.getStringExtra("RESULT");
                    if (command != null && command.equals("com.symbol.datawedge.api.SET_CONFIG")) {
                        if (commandidentifier.equals(Application.RFID_DATAWEDGE_PROFILE_CREATION)) {
                            Bundle bundle = new Bundle();
                            String resultInfo = "";
                            if (intent.hasExtra("RESULT_INFO")) {
                                bundle = intent.getBundleExtra("RESULT_INFO");
                                resultInfo = bundle.getString("RESULT_CODE");
                            }
                            if (result.equals("SUCCESS")) {
                                disableScanner();

                            } else {
                                //   Log.d(TAG, "Failed to Disable scanner " + resultInfo);
                            }
                            Set<String> keys = bundle.keySet();
                            resultInfo = "";
                            for (String key : keys) {
                                resultInfo += key + ": " + bundle.getString(key) + "\n";
                            }
                            Log.d(TAG, "Disable scanner " + resultInfo);
                        }
                    }
                }
                break;
            }
        }
    };

    public void disableScanner() {
        Intent i = new Intent();
        i.setAction("com.symbol.datawedge.api.ACTION");
        i.putExtra("com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN", "DISABLE_PLUGIN");
        i.putExtra("SEND_RESULT", "false");
        i.putExtra("COMMAND_IDENTIFIER", Application.RFID_DATAWEDGE_DISABLE_SCANNER);  //Unique identifier
        sendBroadcast(i);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        // setButtonText("Bluetooth off");

                        Log.i(TAG, "Bluetooth off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:

                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.i(TAG, "Bluetooth on");
                        //Toast.makeText(context, "Bluetooth on", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        // setButtonText("Turning Bluetooth on...");
                        break;
                }

            }

            final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
            if (bondState == BluetoothDevice.BOND_NONE) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mConnectedReader != null && mConnectedReader.getHostName() != null && mConnectedReader.getHostName().equals(device.getName())) {

                    if (mConnectedReader.isConnected()) {
                        try {
                            mConnectedReader.disconnect();
                        } catch (InvalidUsageException e) {
                            e.printStackTrace();
                        } catch (OperationFailureException e) {
                            e.printStackTrace();
                        }

                        clearConnectedReader();
                        //BluetoothHandler.pair(device.getAddress());
                    } else {
                        clearConnectedReader();
                    }

                } else if (LAST_CONNECTED_READER.equals(device.getName())) {
                    clearConnectedReader();

                }
            }
        }
    };

    void clearConnectedReader() {
        SharedPreferences settings = getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.LAST_READER, "");
        editor.commit();
        LAST_CONNECTED_READER = "";
        RFIDController.mConnectedDevice = null;
    }

    void addListViewData(String data)
    {
        String currentDateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        list_tag.add(0, currentDateTime + ": " + data);
        arrayAdapter.notifyDataSetChanged();
        toask_message("added " + data);
    }

    void clearListViewData()
    {
        list_tag.clear();
        arrayAdapter.notifyDataSetChanged();
        toask_message("Clear data done!");
    }

    public void toask_message(String message)
    {
        Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = 10;
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(128));
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAdd:
                addListViewData(random());
                break;
            case R.id.btnClearData:
                clearListViewData();
                break;
            default:
                break;
        }
    }

    private String getReaderPassword(String address) {
        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.READER_PASSWORDS, 0);
        return sharedPreferences.getString(address, null);
    }

    public void ReaderDeviceConnected(ReaderDevice device) {


    }
    // Autoconnect reader on detatch and attach or reader reboot.
    public synchronized void AutoConnectDevice() {

        getInstance().AutoConnectDevice(getReaderPassword(LAST_CONNECTED_READER), eventHandler, new RfidListeners() {
                    @Override
                    public void onSuccess(Object object) {

                        //StoreConnectedReader();
                        if (mConnectedDevice != null) {
                            Log.i(TAG,"onSuccess to " + mConnectedDevice.getName());
                            ReaderDeviceConnected(mConnectedDevice);
                        }

                        if (regionNotSet) {

                        }

                    }

                    @Override
                    public void onFailure(Exception exception) {
                        Log.i(TAG,"onFailure to " + exception.getMessage());

                        if (exception != null && ((OperationFailureException) exception).getResults() ==
                                RFIDResults.RFID_READER_REGION_NOT_CONFIGURED) {
                            ReaderDeviceConnected(mConnectedDevice);



                        } else if (exception != null && ((OperationFailureException) exception).getResults() == RFIDResults.RFID_BATCHMODE_IN_PROGRESS) {
                            if (NOTIFY_READER_CONNECTION)
                                Log.i(TAG,"Connected to " + mConnectedDevice.getName());
                        }

                        if (exception != null && exception.getMessage() != null)
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String message) {
                        Log.i(TAG,"onFailure " + message);
                        //  Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                    }


                }, message -> runOnUiThread(() -> {

                    Log.i(TAG,"update UI here....");
                }));


    }

    public class EventHandler implements RfidEventsListener {

        @Override
        public void eventReadNotify(RfidReadEvents e) {
            if (mConnectedReader != null) {
                if(!mConnectedReader.Actions.MultiTagLocate.isMultiTagLocatePerforming()) {
                    final TagData[] myTags = mConnectedReader.Actions.getReadTags(100);
                    if (myTags != null) {
                        Log.d(TAG,"l: "+myTags.length);

                        for (int index = 0; index < myTags.length; index++) {
                            if (myTags[index].getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                                    myTags[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) {

                                String tagID = myTags[index].getTagID();
                                Log.d(TAG,"tagID [hex]: "+ tagID);
                                String asciiTag = hextoascii.convert(tagID);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        addListViewData(asciiTag);
                                    }
                                });
                            }
                        }
                    }
                } else { ////multi-tal locationing results
                    final TagData[] myTags = mConnectedReader.Actions.getMultiTagLocateTagInfo(100);
                    if (myTags != null) {

                        for (int index = 0; index < myTags.length; index++) {
                            TagData tagData = myTags[index];
                            if (tagData.isContainsMultiTagLocateInfo()) {
                                String asciiTag = hextoascii.convert(tagData.getTagID());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        addListViewData(asciiTag);
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());
            //notificationFromGenericReader(rfidStatusEvents);
        }
    }


    private void disconnectReaderConnections() {
        //disconnect from reader
        if (mConnectedReader != null) {

            try {
                if (mConnectedReader.isConnected()) {
                    mConnectedReader.Events.removeEventsListener(eventHandler);
                }
                mConnectedReader.disconnect();
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        }
        mConnectedReader = null;
        // update dpo icon in settings list
        getInstance().clearSettings();
        mConnectedDevice = null;
        if (readers != null) {
            readers.deattach((Readers.RFIDReaderEventHandler) this);
            readers.Dispose();
            readers = null;
        }
        reset();
    }

    @Override
    protected void onDestroy() {
        disconnectReaderConnections();
        super.onDestroy();
    }
}