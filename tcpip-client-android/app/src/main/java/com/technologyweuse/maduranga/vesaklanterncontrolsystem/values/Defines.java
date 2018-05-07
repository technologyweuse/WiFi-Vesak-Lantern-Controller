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

package com.technologyweuse.maduranga.vesaklanterncontrolsystem.values;

public class Defines {
    public enum ApplicationSetings {
        SERVER_IP,
        PORT,
        DB_SELECTED_FILE,
        DESIGN_LOG_VIEW,
        MOTOR_STATE,
        MAIN_LANTERN_LIGHT
    }

    public enum RelayBoard {
        RELAY_0,
        RELAY_1,
        RELAY_2,
        RELAY_3,
        RELAY_4,
        RELAY_5,
        RELAY_6,
        RELAY_7,
        RELAY_L,
        RELAY_M,
        RELAY_ALL
    }

    public enum CommStatus {
        STOP,
        RUNNING,
        PAUSE,
        ERROR
    }

    public static final String  DB_DATA_FOLDER                  = "/FLASHER_DATA/";
    public static final int     MAX_LINE                        = 8;
    public static final int     MAX_BITS                        = 10;
    public static final boolean ON                              = true;
    public static final boolean OFF                             = false;
    public static final boolean TRUE                            = true;
    public static final boolean FALSE                           = false;
    public static final int     BIN_STR_MAIN_LANTERN_LIGHT_POS  = 8;
    public static final int     BIN_STR_MOTOR_ROTATE_BIT_POS    = 9;
    public static final String  DEFAULT_IP                      = "20.0.0.20";
    public static final String  DEFAULT_PORT                    = "4444";
    public static final String  DEFAULT_DESIGN_FILE             = "Default.json";
    public static final int     WIFI_DATA_PKT_BUFFER_SIZE       = 6;
    public static final int     PKT_ST                          = 0x33;
    public static final int     PKT_END                         = 0x3C;
    public static final int     PKT_ST_POS                      = 0x00;
    public static final int     PKT_DATA1_POS_H                 = 0x01;
    public static final int     PKT_DATA1_POS_L                 = 0x02;
    public static final int     PKT_DATA2_POS_H                 = 0x03;
    public static final int     PKT_DATA2_POS_L                 = 0x04;
    public static final int     PKT_END_POS                     = 0x05;
}
