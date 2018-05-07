/****************************************************************************
 * Copyright (C) 2018 by Maduranga Jayasinghe                               *
 *                                                                          *
 *  Wireless Relay Control System                                           *
 *                                                                          *
 *   This program is a free software : you can redistribute it and / or     *
 *   modify it under the terms of the GNU Lesser General Public  License    *
 *   as published by the Free Software Foundation, either version 3 of      *
 *   the License, or ( at your option ) any later version.                  *
 *                                                                          *
 *   This program is distributed in the hope that it will be useful,        *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the           *
 *   GNU Lesser General Public License for more details.                    *
 *                                                                          *
 *   You should have received a copy of the GNU Lesser General Public       *
 *   License along with Vesak Lantern Control System.                       *
 *   If not, see <http://www.gnu.org/licenses/>.                            *
 *                                                                          *
 *   For detail tutorial about this project, see                            *
 *   <http://www.technologyweuse.com/wifi-vesak-lantern-controller>         *
 *                                                                          *
 ****************************************************************************/

/**
 * @file    MainActivity.java
 * @author  Maduranga Jayasinghe
 * @date    2018-04-26
 * @brief   This  program  is  work  as  TCP/IP  Client and reads the selected
 *           JSON design data file and send TCP data packet to the server.
 */

package com.technologyweuse.maduranga.vesaklanterncontrolsystem.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import com.technologyweuse.maduranga.vesaklanterncontrolsystem.values.Defines;
import com.technologyweuse.maduranga.vesaklanterncontrolsystem.R;
import com.technologyweuse.maduranga.vesaklanterncontrolsystem.control.LanternDesignRunner;
import java.io.File;

/**
 * @brief   This is the main activity which handle the communication between the
 *           UI and lantern controller
 * @internal
 *      History:
 *      2018.04.26      Initialise
 */
public class MainActivity extends AppCompatActivity {

    private static final int mRequestCode = 0;
    private TextView mActivityTxtServerIP;
    private TextView mActivityTxtPort;
    private TextView mActivityTxtDbFile;
    private TextView mActivityTxtStatus;
    private TextView txtViewerDesignLog;
    private Switch  mActivitySwDesignLog;
    private Switch  mActivitySwMotor;
    private Switch  mActivitySwMainLanternLightAlwaysOn;
    private TextView mActivityTxtErr;
    private ImageView mActivityImgWiFi;
    private ImageView mActivityImgMotor;
    private ImageView[] mImgData = new ImageView[10];
    private Button mActivityBtnRun;
    private Button mActivityBtnConnect;
    private MenuItem mMenuItemSettings;
    private LanternDesignRunner mLanternController;
    private static String mIp;
    private static int mPort;
    private static String mDbSelectedFile;
    private static String [] mDbFileList;
    public CallbackReceiver mErrCallbackReceiver;
    private MainActivity mMainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mErrCallbackReceiver = new CallbackReceiver();
        // Register the filter for listening broadcast.
        IntentFilter filter = new IntentFilter();
        filter.addAction (CallbackReceiver.ACTION_ERR_MSG);
        filter.addAction (CallbackReceiver.ACTION_UPDATE_BITS);

        this.registerReceiver(mErrCallbackReceiver, filter);

        mMainActivity = this;

        mActivityTxtServerIP = findViewById(R.id.txtViewerServerIP);
        mActivityTxtPort = findViewById(R.id.txtViewerPort);
        mActivityTxtDbFile = findViewById(R.id.txtViewerDb);
        mActivityTxtStatus  = findViewById(R.id.txtStatus);
        mActivityImgWiFi = findViewById(R.id.imgWiFi);
        mActivityImgMotor = findViewById(R.id.imgMotor);
        txtViewerDesignLog = findViewById(R.id.txtViewerDesignLog);
        mActivitySwDesignLog = findViewById(R.id.swDesignLog);
        mActivitySwMotor = findViewById(R.id.swMotor);
        mActivitySwMainLanternLightAlwaysOn = findViewById(R.id.swMainLanternAlwaysOn);
        mActivityTxtErr = findViewById(R.id.txtErr);
        mImgData[0] = findViewById(R.id.imgBit0);
        mImgData[1] = findViewById(R.id.imgBit1);
        mImgData[2] = findViewById(R.id.imgBit2);
        mImgData[3] = findViewById(R.id.imgBit3);
        mImgData[4] = findViewById(R.id.imgBit4);
        mImgData[5] = findViewById(R.id.imgBit5);
        mImgData[6] = findViewById(R.id.imgBit6);
        mImgData[7] = findViewById(R.id.imgBit7);
        mImgData[8] = findViewById(R.id.imgBit8);
        mImgData[9] = findViewById(R.id.imgBit9);
        mActivityBtnConnect = findViewById(R.id.btnConnect);
        mActivityBtnRun = findViewById(R.id.btnRun);

        initUI();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ){
            //if you dont have required permissions ask for it (only required for API 23+)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, mRequestCode);
        }
        else {
            initSystem();
        }

        mActivityBtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mLanternController.getIsConnected())
                {
                    mActivityImgWiFi.setImageResource(R.drawable.disconnected);
                    mActivityBtnConnect.setText(R.string.btn_connecting);

                    if(mLanternController.connect())
                    {
                        int timeout = 10;

                        while(!mLanternController.getIsConnected() && timeout > 0){
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                mLanternController.callbackErrMsg(CallbackReceiver.ACTION_ERR_LOG, "mActivityBtnConnect.setOnClickListener", e.getMessage());
                            }
                            timeout--;
                        }
                        if(mLanternController.getIsConnected()) {
                            setUIWiFiIcon(Defines.ON);
                            mActivityBtnRun.setEnabled(true);
                            mActivityBtnConnect.setText(R.string.btn_disconnect);
                            mMenuItemSettings.setEnabled(false);
                        }
                        else{
                            mActivityBtnConnect.setText(R.string.btn_connect);
                            initUI();
                            initSystem();
                            mMenuItemSettings.setEnabled(true);
                            mActivityImgWiFi.setImageResource(R.drawable.motor_off);
                            mLanternController.commStop();
                        }
                    }
                }
                else
                {
                    if(mLanternController.getStatus() != Defines.CommStatus.STOP){
                        mLanternController.commStop();
                    }

                    if(mLanternController.disConnect()) {
                        mActivityBtnConnect.setText(R.string.btn_connect);
                        initUI();
                        initSystem();
                        mMenuItemSettings.setEnabled(true);
                    }
                    setUIStatusMessage();
                }
            }
        });

        mActivityBtnRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mLanternController.getIsConnected())
                {
                    if(mLanternController.getStatus() == Defines.CommStatus.PAUSE || mLanternController.getStatus() == Defines.CommStatus.STOP )
                    {
                        mActivityBtnRun.setText(R.string.btn_pause);
                        mLanternController.commStart();
                        setUIStatusMessage();
                    }
                    else if(mLanternController.getStatus() == Defines.CommStatus.RUNNING)
                    {
                        mActivityBtnRun.setText(R.string.btn_run);
                        mLanternController.commPause();
                        setUIStatusMessage();
                    }
                }
                else
                {
                    setUIStatusMessage();
                }
            }
        });

        mActivitySwDesignLog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mLanternController.designLogEnable(isChecked);
                SharedPreferences sharedPref = getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
                sharedPref.edit().putBoolean(Defines.ApplicationSetings.DESIGN_LOG_VIEW.toString(), isChecked).apply();
            }
        });

        mActivitySwMotor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mLanternController.motorRotationEnable(isChecked);
                SharedPreferences sharedPref = getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
                sharedPref.edit().putBoolean(Defines.ApplicationSetings.MOTOR_STATE.toString(), isChecked).apply();

                mActivityImgMotor.setImageResource(isChecked ? R.drawable.motor_on : R.drawable.motor_off);
            }
        });

        mActivitySwMainLanternLightAlwaysOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mLanternController.mainLanternAlwaysOn(isChecked);
                SharedPreferences sharedPref = getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
                sharedPref.edit().putBoolean(Defines.ApplicationSetings.MAIN_LANTERN_LIGHT.toString(), isChecked).apply();
            }
        });
    }

    public class CallbackReceiver extends BroadcastReceiver {
        public static final String ACTION_ERR_MSG =  "ACTION_ERR_MSG";
        public static final String ACTION_ERR_LOG =  "ACTION_ERR_LOG";
        public static final String ACTION_UPDATE_BITS =  "ACTION_UPDATE_BITS";

        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction())
            {
                case ACTION_ERR_MSG:
                    mMainActivity.mActivityTxtErr.setText(intent.getStringExtra("msg"));
                    AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(mMainActivity);
                    dlgAlert.setMessage(intent.getStringExtra("msg"));
                    dlgAlert.setTitle("Error");
                    dlgAlert.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mLanternController.commStop();
                                }
                            });
                    dlgAlert.create().show();
                    break;
                case ACTION_ERR_LOG:
                    mMainActivity.mActivityTxtErr.setText(intent.getStringExtra("msg"));
                    break;
                case ACTION_UPDATE_BITS:
                    String data1_str = intent.getStringExtra("msg");
                    String twoByteBitString;
                    twoByteBitString = mLanternController.sendDataPacket(data1_str);

                    if(mLanternController.getDesignLogEnable()) {
                        writeUIDesignDataTerminal(data1_str);
                    }

                    for(int i = 0 ; i < Defines.MAX_BITS;  i++) {
                        mMainActivity.setUIImgData(i, twoByteBitString.charAt(i) == '1' ? Defines.ON : Defines.OFF);
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        mMenuItemSettings = menu.findItem(R.id.action_settings);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showSettingsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.ic_launcher_background);
        builderSingle.setTitle(R.string.dialog_settings);
        LayoutInflater inflater = LayoutInflater.from(this);
        View mView = inflater.inflate(R.layout.settings, null);
        builderSingle.setView(mView);

        final EditText dialogTxtServerIP = mView.findViewById(R.id.txtServerIP);
        final EditText dialogTxtPort = mView.findViewById(R.id.txtPort);
        final Spinner DialogSpinnerFileList = mView.findViewById(R.id.sp_file_list);

        KeyListener txtServerIP_keyListener = DigitsKeyListener.getInstance("0123456789.");
        dialogTxtServerIP.setKeyListener(txtServerIP_keyListener);
        InputFilter[] dialogTxtServerIP_filters = new InputFilter[1];
        dialogTxtServerIP_filters[0] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start,
                                       int end, Spanned dest, int dstart, int dend) {
                if (end > start) {
                    String destTxt = dest.toString();
                    String resultingTxt = destTxt.substring(0, dstart) +
                            source.subSequence(start, end) +
                            destTxt.substring(dend);
                    if (!resultingTxt.matches ("^\\d{1,3}(\\." +
                            "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                        return "";
                    } else {
                        String[] splits = resultingTxt.split("\\.");
                        for (int i=0; i<splits.length; i++) {
                            if (Integer.valueOf(splits[i]) > 255) {
                                return "";
                            }
                        }
                    }
                }
                return null;
            }
        };
        dialogTxtServerIP.setFilters(dialogTxtServerIP_filters);

        KeyListener txtPort_keyListener = DigitsKeyListener.getInstance("0123456789");
        dialogTxtPort.setKeyListener(txtPort_keyListener);

        dialogTxtServerIP.setText(mIp);
        dialogTxtServerIP.selectAll();
        dialogTxtPort.setText(String.valueOf(mPort));

        if(mDbFileList != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mView.getContext(), android.R.layout.simple_spinner_item, mDbFileList);
            DialogSpinnerFileList.setAdapter(adapter);
            if (mDbSelectedFile != null && mDbSelectedFile.trim() != "") {
                int spinnerPosition = adapter.getPosition(mDbSelectedFile);
                DialogSpinnerFileList.setSelection(spinnerPosition);
            }
        }

        builderSingle.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        mIp = dialogTxtServerIP.getText().toString();
                        mPort = Integer.parseInt(dialogTxtPort.getText().toString());
                        mDbSelectedFile = DialogSpinnerFileList.getSelectedItem().toString();
                        saveApplicationSettings();
                        updateUIData();
                        mLanternController = new LanternDesignRunner(mMainActivity);
                        mLanternController.setIPAndPort(mIp, mPort);
                        mLanternController.loadDesignData(mDbSelectedFile);
                    }
                });

        builderSingle.setNegativeButton("Close",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builderSingle.show();
    }

    private void saveApplicationSettings(){
        SharedPreferences sharedPref = this.getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);

        sharedPref.edit().putString(Defines.ApplicationSetings.SERVER_IP.toString(), mIp).apply();
        sharedPref.edit().putString(Defines.ApplicationSetings.PORT.toString(), String.valueOf(mPort)).apply();
        sharedPref.edit().putString(Defines.ApplicationSetings.DB_SELECTED_FILE.toString(), mDbSelectedFile).apply();
    }

    public void readApplicationSettings(){
        SharedPreferences sharedPref = this.getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);

        mIp = sharedPref.getString(Defines.ApplicationSetings.SERVER_IP.toString(), Defines.DEFAULT_IP);
        mPort = Integer.parseInt(sharedPref.getString(Defines.ApplicationSetings.PORT.toString(), Defines.DEFAULT_PORT));
        mDbSelectedFile = sharedPref.getString(Defines.ApplicationSetings.DB_SELECTED_FILE.toString(), Defines.DEFAULT_DESIGN_FILE);

        mLanternController.designLogEnable(sharedPref.getBoolean(Defines.ApplicationSetings.DESIGN_LOG_VIEW.toString(), Defines.ON));
        mLanternController.motorRotationEnable(sharedPref.getBoolean(Defines.ApplicationSetings.MOTOR_STATE.toString(), Defines.ON));
        mLanternController.mainLanternAlwaysOn(sharedPref.getBoolean(Defines.ApplicationSetings.MAIN_LANTERN_LIGHT.toString(), Defines.ON));
    }

    public void loadDbFileList()
    {
        mDbFileList = null;

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + Defines.DB_DATA_FOLDER;
        File directory = new File(path);
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files.length > 0) {
                mDbFileList = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    mDbFileList[i] = files[i].getName();
                }
            }
        }
    }

    private void updateUIData()
    {
        mActivityTxtServerIP.setText(mIp);
        mActivityTxtPort.setText(String.valueOf(mPort));
        mActivityTxtDbFile.setText(mDbSelectedFile);
        txtViewerDesignLog.setText("");
    }

    private void setUIImgData(int bit, boolean value)
    {
        if(bit == 8){
            mImgData[bit].setImageResource(value == true ? R.drawable.green_on : R.drawable.green_off);
        }
        else if(bit == 9){
            mImgData[bit].setImageResource(value == true ? R.drawable.indigo_on : R.drawable.indigo_off);
            mActivityImgMotor.setImageResource(value == true ? R.drawable.motor_on : R.drawable.motor_off);
        }
        else {
            mImgData[bit].setImageResource(value == true ? R.drawable.yellow_on : R.drawable.yellow_off);
        }
    }

    private void setUIImgDataAll(boolean value)
    {
        for(int i = 0; i < 8; i++)
        {
            mImgData[i].setImageResource(value == true ? R.drawable.yellow_on : R.drawable.yellow_off);
        }
        mImgData[8].setImageResource(value == true ? R.drawable.green_on : R.drawable.green_off);
        mImgData[9].setImageResource(value == true ? R.drawable.indigo_on : R.drawable.indigo_off);
        mActivityImgMotor.setImageResource(value == true ? R.drawable.motor_on : R.drawable.motor_off);
    }

    private void setUIWiFiIcon(boolean status)
    {
        mActivityImgWiFi.setImageResource(status == true ? R.drawable.wifi_on_orange : R.drawable.wifi_off_orange);
    }

    private void setUIStatusMessage()
    {
        if(mLanternController.getStatus() == Defines.CommStatus.RUNNING)
        {
            mActivityTxtStatus.setText(R.string.msg_running);
        }
        else if(mLanternController.getStatus() == Defines.CommStatus.STOP)
        {
            mActivityTxtStatus.setText(R.string.msg_stop);
        }
        else if(mLanternController.getStatus() == Defines.CommStatus.PAUSE)
        {
            mActivityTxtStatus.setText(R.string.msg_pause);
        }
        else if(mLanternController.getStatus() == Defines.CommStatus.ERROR)
        {
            mActivityTxtStatus.setText(R.string.msg_error);
        }

    }

    public void writeUIDesignDataTerminal(String data) {
        data += "\n";
        txtViewerDesignLog.append(data);
        // Erase excessive lines
        int excessLineNumber = txtViewerDesignLog.getLineCount() - Defines.MAX_LINE;
        if (excessLineNumber > 0) {
            int eolIndex = -1;
            CharSequence charSequence = txtViewerDesignLog.getText();
            for(int i=0; i<excessLineNumber; i++) {
                do {
                    eolIndex++;
                } while(eolIndex < charSequence.length() && charSequence.charAt(eolIndex) != '\n');
            }
            if (eolIndex < charSequence.length()) {
                txtViewerDesignLog.getEditableText().delete(0, eolIndex+1);
            }
            else {
                txtViewerDesignLog.setText("");
            }
        }
    }

    private void initUI()
    {
        mActivityTxtServerIP.setText("");
        mActivityTxtPort.setText("");
        mActivityTxtStatus.setText("");
        mActivityTxtDbFile.setText("");
        mActivityTxtErr.setText("");
        txtViewerDesignLog.setText("");
        setUIImgDataAll(Defines.OFF);
        setUIWiFiIcon(Defines.OFF);

        mActivityBtnConnect.setText(R.string.btn_connect);
        mActivityBtnRun.setText(R.string.btn_run);
        mActivityBtnRun.setEnabled(false);
        setTitle(R.string.app_name);
    }

    private void initSystem()
    {
        mLanternController = new LanternDesignRunner( this);
        mLanternController.motorRotationEnable(Defines.OFF);
        readApplicationSettings();
        mLanternController.setIPAndPort(mIp, mPort);
        loadDbFileList();
        updateUIData();
        setUIStatusMessage();
        mActivitySwDesignLog.setChecked(mLanternController.getDesignLogEnable());
        mActivitySwMotor.setChecked(mLanternController.getMotorRotationEnable());

        if(mLanternController.getMotorRotationEnable())
        {
            mActivityImgMotor.setImageResource(R.drawable.motor_on);
        }
        else
        {
            mActivityImgMotor.setImageResource(R.drawable.motor_off);
        }

        mLanternController.loadDesignData(mDbSelectedFile);
    }

}
