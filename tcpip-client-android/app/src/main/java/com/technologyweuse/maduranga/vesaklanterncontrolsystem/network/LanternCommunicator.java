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
 *   <http://www.technologyweuse.com/>                                      *
 *                                                                          *
 ****************************************************************************/

/**
 * @file    LanternCommunicator.java
 * @author  Maduranga Jayasinghe
 * @date    2018-04-26
 * @brief   This class is responsible for controlling the design data packet communication.
 *
 */

package com.technologyweuse.maduranga.vesaklanterncontrolsystem.network;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import com.technologyweuse.maduranga.vesaklanterncontrolsystem.values.Defines;
import com.technologyweuse.maduranga.vesaklanterncontrolsystem.activity.MainActivity;

/**
 * @brief   This class is responsible for controlling the design data packet communication.
 * @internal
 *      History:
 *      2018.04.26      Initialise
 */
public class LanternCommunicator extends Defines {

    private String      mIP;
    private int         mPort;
    public boolean      mIsConnected;
    public Context      mContext;
    private TCPIPClient mTcpClient;

    /**
     *  Constructor
     */

    public LanternCommunicator(Context context)
    {
        mIsConnected    = false;
        mContext        = context;
    }

    /**
     *  Methods (public)
     */

    public void setIPAndPort(String ip, int port)
    {
        mIP     = ip;
        mPort   = port;
    }

    public boolean getIsConnected()
    {
        return mIsConnected;
    }

    public boolean connect()
    {
        if(!mIsConnected)
        {
            try
            {
                new TCPIPClientConnectTask().execute();
                return true;
            }
            catch(Exception e)
            {
                mIsConnected = false;
                callbackErrMsg(MainActivity.CallbackReceiver.ACTION_ERR_MSG,"getIsConnected", e.getMessage());
                return false;
            }
        }
        else{
            return false;
        }
    }

    public boolean disConnect()
    {
        if(mIsConnected)
        {
            try
            {
                new TCPIPClientDisconnectTask().execute();
                return true;
            }
            catch(Exception e)
            {
                callbackErrMsg(MainActivity.CallbackReceiver.ACTION_ERR_MSG,"disConnect", e.getMessage());
                return false;
            }
        }
        else{
            return true;
        }
    }


    public void callbackMsg(String action, String msg)
    {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("msg", msg);
        mContext.sendBroadcast(intent);
    }

    public void callbackErrMsg(String action, String func, String msg)
    {
        callbackMsg(action, "Func\t: " + func + "\nErr\t\t\t: " + msg);
    }

    public String sendDataPacket(String twoByteBitString, boolean motorRotate, boolean mainLanternLightOn) {
        byte data[] = new byte[Defines.WIFI_DATA_PKT_BUFFER_SIZE];
        int intData1, intData2;

        //  twoByteBitString
        // --data2--|--data1--|
        // 0000 0000 0000 0000

        if(mainLanternLightOn) {
            twoByteBitString = twoByteBitString.substring(0, BIN_STR_MAIN_LANTERN_LIGHT_POS) + '1' + twoByteBitString.substring(BIN_STR_MAIN_LANTERN_LIGHT_POS + 1);
        }
        if(motorRotate) {
            twoByteBitString = twoByteBitString.substring(0, BIN_STR_MOTOR_ROTATE_BIT_POS) + '1' + twoByteBitString.substring(BIN_STR_MOTOR_ROTATE_BIT_POS + 1);
        }

        intData1 = Integer.parseInt(twoByteBitString.substring(0, 8).toString(), 2);
        intData2 = Integer.parseInt(twoByteBitString.substring(8), 2);

        data[Defines.PKT_ST_POS] = (byte)Defines.PKT_ST;
        data[Defines.PKT_DATA1_POS_H] = (byte)((intData1 >>> 4) & 0x0F);
        data[Defines.PKT_DATA1_POS_L] = (byte)(intData1 & 0x0F);
        data[Defines.PKT_DATA2_POS_H] = (byte)((intData2 >>> 4) & 0x0F);
        data[Defines.PKT_DATA2_POS_L] = (byte)(intData2 & 0x0F);
        data[Defines.PKT_END_POS] = (byte)Defines.PKT_END;

        new TCPIPClientSendMessageTask().execute(data);

        return twoByteBitString;
    }

    /**
     *  Methods (private)
     */
    private class TCPIPClientSendMessageTask extends AsyncTask<byte[], Void, Void> {
        @Override
        protected Void doInBackground(byte[]... params) {
            mTcpClient.sendMessage(params[0]);
            return null;
        }

    }

    private class TCPIPClientDisconnectTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if(mTcpClient.stopClient() == 0){
                mIsConnected = false;
            }
            mTcpClient = null;
            return null;
        }
    }

    private class TCPIPClientConnectTask extends AsyncTask<Void, Void, TCPIPClient> {
        @Override
        protected TCPIPClient doInBackground(Void... message) {
            mTcpClient = new TCPIPClient(mIP, mPort);
            if(mTcpClient.run() == 0){
                mIsConnected = true;
            }
            else{
                mIsConnected = false;
            }
            return null;
        }
    }
}
