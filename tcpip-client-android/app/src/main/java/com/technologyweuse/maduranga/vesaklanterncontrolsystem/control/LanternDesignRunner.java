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
 *   For detail tutorial about linked list, see                             *
 *   <http://www.technologyweuse.com/>                                      *
 *                                                                          *
 ****************************************************************************/

/**
 * @file    LanternDesignRunner.java
 * @author  Maduranga Jayasinghe
 * @date    2018-04-26
 * @brief   This is the design executing class and responsible for execute the
 *           design in the selected JSON file.
 */
package com.technologyweuse.maduranga.vesaklanterncontrolsystem.control;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Environment;
import com.technologyweuse.maduranga.vesaklanterncontrolsystem.activity.MainActivity;
import com.technologyweuse.maduranga.vesaklanterncontrolsystem.network.LanternCommunicator;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

/**
 * @brief   This is the design executing class and responsible for execute the
 *           design in the selected JSON file.
 * @internal
 *      History:
 *      2018.04.26      Initialise
 */
public class LanternDesignRunner extends LanternCommunicator {

    private CommStatus      mCommStatus;
    private JSONArray       mDesignDataJSONArray;
    private boolean         mMotorRotate;
    private boolean         mDesignLogEnable;
    private boolean         mMainLanternAlwaysOn;
    private int             mCurrentDesignNumber;
    private Design          mCurrentDesignData;
    private int             mCurrentDesignRunningIndex;
    private CountDownTimer  mCountDownTimer;

    /**
     *  Constructor
     */
    public LanternDesignRunner(Context context)
    {
        super(context);

        mCommStatus             = CommStatus.STOP;
        mCurrentDesignNumber    = 0;
        mMainLanternAlwaysOn    = false;
    }

    /**
     *  Super Class Methods (public)
     */
    public boolean isConnected() { return super.getIsConnected(); }

    public boolean connect() { return super.connect(); }

    public boolean disConnect() { return super.disConnect(); }

    public String sendDataPacket(String data1_str) { return super.sendDataPacket(data1_str, mMotorRotate, mMainLanternAlwaysOn); }

    public void setIPAndPort(String ip, int port)
    {
        super.setIPAndPort(ip, port);
    }

    /**
     *  Methods (public)
     */

    public void loadDesignData(String dbSelectedFile)
    {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + DB_DATA_FOLDER + dbSelectedFile;

        try {
            File file = new File(path.toString());
            FileInputStream inputStream = new FileInputStream(file);

            if ( inputStream != null ) {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    String json_data;

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }

                    inputStream.close();
                    json_data = stringBuilder.toString();

                    JSONObject obj = new JSONObject(json_data);
                    mDesignDataJSONArray = obj.getJSONArray("data");
                }
                catch (Exception e)
                {
                    callbackErrMsg(MainActivity.CallbackReceiver.ACTION_ERR_MSG, "loadDesignData", e.getMessage());
                }
            }
        }
        catch (FileNotFoundException e) {
            callbackErrMsg(MainActivity.CallbackReceiver.ACTION_ERR_MSG, "loadDesignData", e.getMessage());
        }
    }

    public CommStatus commStart()
    {
        if(mIsConnected && (mCommStatus == CommStatus.STOP || mCommStatus == CommStatus.PAUSE ) )
        {
            try
            {
                if(mCommStatus == CommStatus.STOP) {
                    mCurrentDesignNumber = 0;
                    mCurrentDesignData = new Design(mDesignDataJSONArray.getJSONObject(mCurrentDesignNumber++));
                }

                executeDesign();
                mCommStatus = CommStatus.RUNNING;
            }
            catch(Exception e)
            {
                mCommStatus = CommStatus.ERROR;
                callbackErrMsg(MainActivity.CallbackReceiver.ACTION_ERR_MSG, "commStart", e.getMessage());
            }
        }
        return mCommStatus;
    }

    public CommStatus commPause()
    {
        if(mIsConnected && mCommStatus == CommStatus.RUNNING)
        {
            try
            {
                mCommStatus = CommStatus.PAUSE;
                mCountDownTimer.cancel();
            }
            catch(Exception e)
            {
                mCommStatus = CommStatus.ERROR;
                callbackErrMsg(MainActivity.CallbackReceiver.ACTION_ERR_MSG, "commPause", e.getMessage());
            }
        }
        return mCommStatus;
    }

    public CommStatus commStop()
    {
        if(mIsConnected && mCommStatus != CommStatus.STOP)
        {
            try
            {
                mCurrentDesignNumber = 0;
                mCommStatus = CommStatus.STOP;

                mCountDownTimer.cancel();
            }
            catch(Exception e)
            {
                mCommStatus = CommStatus.ERROR;
                callbackErrMsg(MainActivity.CallbackReceiver.ACTION_ERR_MSG, "commStop", e.getMessage());
            }
        }
        return mCommStatus;
    }

    public void motorRotationEnable(boolean state) { mMotorRotate = state; }

    public void mainLanternAlwaysOn(boolean state) { mMainLanternAlwaysOn = state; }

    public boolean getMotorRotationEnable() { return mMotorRotate; }

    public void designLogEnable(boolean state) { mDesignLogEnable = state; }

    public boolean getDesignLogEnable() { return mDesignLogEnable; }

    public CommStatus getStatus() { return mCommStatus; }

    /**
     *  Local Class (private)
     */
    private class Design {
        public int designNumber;
        public int loopCount;
        public int delay;
        public String[] data;
        public int dataLength;

        Design(JSONObject designData)
        {
            try {
                designNumber = designData.getInt("design");
                loopCount = designData.getInt("loop");
                delay = designData.getInt("delay");
                JSONArray jsonArray = designData.getJSONArray("data");
                data = new String[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    data[i] =jsonArray.getString(i);
                }
                dataLength = data.length;
            }
            catch (Exception e)
            {
                callbackErrMsg(MainActivity.CallbackReceiver.ACTION_ERR_MSG, "Design", e.getMessage());
            }
        }
    }

    /**
     *  Local Methods (private)
     */
    private void executeDesign()
    {
        mCurrentDesignRunningIndex = 0;
        long totalLoopTime = (mCurrentDesignData.delay * mCurrentDesignData.loopCount * mCurrentDesignData.dataLength) + mCurrentDesignData.delay;
        long intervals = mCurrentDesignData.delay;
        mCountDownTimer = new CountDownTimer(totalLoopTime, intervals) {

            public void onTick(long millisUntilFinished) {
                String data = mCurrentDesignData.data[mCurrentDesignRunningIndex++];

                callbackMsg(MainActivity.CallbackReceiver.ACTION_UPDATE_BITS, data);

                if (mCurrentDesignRunningIndex == mCurrentDesignData.dataLength) {
                    mCurrentDesignRunningIndex = 0;
                }
            }

            public void onFinish() {
                try {
                    if (mCurrentDesignNumber == mDesignDataJSONArray.length()) {
                        mCurrentDesignNumber = 0;
                    }
                    mCurrentDesignData = new Design(mDesignDataJSONArray.getJSONObject(mCurrentDesignNumber++));
                    executeDesign();
                }
                catch(Exception e)
                {
                    callbackErrMsg(MainActivity.CallbackReceiver.ACTION_ERR_MSG,"executeDesign", e.getMessage());
                }
            }
        }.start();
    }
}
