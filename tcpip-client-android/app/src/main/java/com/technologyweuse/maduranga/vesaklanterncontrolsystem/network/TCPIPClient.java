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
 * @file    TCPIPClient.java
 * @author  Maduranga Jayasinghe
 * @date    2018-04-26
 * @brief   This is the TCP/IP client and responsible for make a connection with
 *           Lantern control micro controller for WiFi communication.
 *
 */

package com.technologyweuse.maduranga.vesaklanterncontrolsystem.network;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @brief   This class is responsible for controlling the design data packet communication.
 * @internal
 *      History:
 *      2018.04.26      Initialise
 */

public class TCPIPClient {

    private Socket      mSocket;
    private String      mIP;
    private int         mPort;
    private PrintWriter mBufferOut;

    /**
     *  Constructor
     */

    public  TCPIPClient(String ip, int port) {
        mIP = ip;
        mPort = port;
    }

    /**
     *  Methods (public)
     */

    public void sendMessage(byte[] data) {
        if (mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.print(new String(data));
            //mBufferOut.flush();
        }
    }

    public int stopClient() {
        try {
            if (!mSocket.isClosed()) {
                mSocket.close();
            }

            if (mBufferOut != null) {
                mBufferOut.flush();
                mBufferOut.close();
            }
            mBufferOut = null;
            return 0;
        }
        catch (Exception ex) {
            return -1;
        }
    }

    public int run() {
        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(mIP);
            //create a socket to make the connection with the server
            mSocket = new Socket(serverAddr, mPort);

            try {
                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);
            } catch (Exception e) {
                return -1;
            }

        } catch (Exception e) {
            return -2;
        }
        return 0;
    }
}
