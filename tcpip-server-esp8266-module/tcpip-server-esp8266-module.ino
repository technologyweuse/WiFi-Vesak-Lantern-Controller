/****************************************************************************
 * Copyright (C) 2018 by Maduranga Jayasinghe                               *
 *                                                                          *
 *  Wireless Relay Control System for Vesak Lantern                         *
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
 * @file    tcpip-server-esp8266-module.ino
 * @author  Maduranga Jayasinghe
 * @date    2018-04-26
 * @brief   This  program  is  work  as  TCP/IP  server  and  set GPIO pin
 *          to  HIGH/LOW  accourding  to  the  instruction from the client
 *          application  with  arduino-ide  core  and  on NodeMCU. Assumes
 *          that only one remote socket would try to connect to the server.
 */

#include <ESP8266WiFi.h>
#include <ESP8266WiFiAP.h>
#include <ESP8266WiFiGeneric.h>
#include <ESP8266WiFiMulti.h>
#include <ESP8266WiFiScan.h>
#include <ESP8266WiFiSTA.h>
#include <ESP8266WiFiType.h>
#include <WiFiClient.h>
#include <WiFiClientSecure.h>
#include <WiFiServer.h>
#include <WiFiServerSecure.h>
#include <WiFiUdp.h>

/**
 * @brief Macro definitions
 * 
 * @internal
 *      History:
 *      2018.04.26	Initialise
 * @note
 *      Ref: http://www.electronicwings.com/nodemcu/nodemcu-gpio-with-arduino-ide
 */
 
/*-----------------------------------------------
  |                DATA PACKET			|
  -----------------------------------------------
  |   BYTE 1	|      |	PKT_ST (0x33)   |
  -----------------------------------------------
  |   BYTE 2	| BIT7 |	RES		|
  |         	| BIT6 |	RES		|
  |         	| BIT5 |	RES		|
  |         	| BIT4 |	RES		|
  |         	| BIT3 |	RELAY 1		|
  |         	| BIT2 |	RELAY 2		|
  |         	| BIT1 |	RELAY 3		|
  |         	| BIT0 |	RELAY 4		|
  -----------------------------------------------
  |   BYTE 3	| BIT7 |	RES		|
  |         	| BIT6 |	RES		|
  |         	| BIT5 |	RES		|
  |         	| BIT4 |	RES		|
  |         	| BIT3 |	RELAY 5		|
  |         	| BIT2 |	RELAY 6		|
  |         	| BIT1 |	RELAY 7		|
  |         	| BIT0 |	RELAY 8		|
  -----------------------------------------------
  |   BYTE 4	| BIT7 |	RES		|
  |         	| BIT6 |	RES		|
  |         	| BIT5 |	RES		|
  |         	| BIT4 |	RES		|
  |         	| BIT3 |	RELAY L		|
  |         	| BIT2 |	RELAY M		|
  |         	| BIT1 |	RES		|
  |         	| BIT0 |	RES		|
  -----------------------------------------------
  |   BYTE 5	| BIT7 |	RES		|
  |         	| BIT6 |	RES		|
  |         	| BIT5 |	RES		|
  |         	| BIT4 |	RES		|
  |         	| BIT3 |	RES		|
  |         	| BIT2 |	RES		|
  |         	| BIT1 |	RES		|
  |         	| BIT0 |	RES		|
  -----------------------------------------------
  |   BYTE 6	|      |	PKT_END (0x3C)	|
  -----------------------------------------------
  */

#define WIFI_BUFFER_SIZE	6  
#define PKT_ST			0x33
#define PKT_END			0x3C
#define PKT_ST_POS		0x00
#define PKT_DATA1_POS_H		0x01
#define PKT_DATA1_POS_L		0x02
#define PKT_DATA2_POS_H		0x03
#define PKT_DATA2_POS_L		0x04
#define PKT_END_POS		0x05

/* GPIO PORTS */
#define GPIO_PIN_0		D0	 /* Relay 1 */
#define GPIO_PIN_1		D1	 /* Relay 2 */
#define GPIO_PIN_2		D2	 /* Relay 3 */
#define GPIO_PIN_3		D3	 /* Relay 4 */
#define GPIO_PIN_4		D4	 /* Relay 5 */
#define GPIO_PIN_5		D5	 /* Relay 6 */
#define GPIO_PIN_6		D6	 /* Relay 7 */
#define GPIO_PIN_7		D7	 /* Relay 8 */
#define GPIO_PIN_8		D9	 /* Relay L */
#define GPIO_PIN_9		D10	 /* Relay M */

//#define DEBUG_MODE_ENABLE		/* Open serial port for debugging */
#define USE_WIFI_AP_MODE		/* This module works as access point */
//#define USE_STATIC_IP			/* This module use static ip address */
#define PORT			4444

#ifdef DEBUG_MODE_ENABLE
  #define LOG(...) Serial.print(__VA_ARGS__)
  #define LOG_LN(...) Serial.println(__VA_ARGS__)
#else
	#define LOG(...)
	#define LOG_LN(...)
#endif

#ifdef USE_WIFI_AP_MODE	
	const char* ssid     = "VESAK-LANTERN";
	const char* password = "yourpassword";
#else
	const char* ssid     = "YOUR-ACCESS-POINT-NAME";
	const char* password = "yourpassword";	
#endif

/**
 * @brief Global variable definitions
 * 
 * @internal
 *      History:
 *      2018.04.26      Initialise
 */
WiFiServer 	server(PORT);
WiFiClient 	client;
int 		status 			= WL_IDLE_STATUS;
boolean		ClientConnected 	= false;
boolean		LastClientConnected	= false;
uint8_t		wifiBuffer[WIFI_BUFFER_SIZE];

/**
 * @brief Initial setup
 * 
 * @internal
 *      History:
 *      2018.04.26      Initialise
 */
void setup()
{
	pinMode(GPIO_PIN_0, OUTPUT); /* Connected to Relay 1 */
	pinMode(GPIO_PIN_1, OUTPUT); /* Connected to Relay 2 */
	pinMode(GPIO_PIN_2, OUTPUT); /* Connected to Relay 3 */
	pinMode(GPIO_PIN_3, OUTPUT); /* Connected to Relay 4 */
	pinMode(GPIO_PIN_4, OUTPUT); /* Connected to Relay 5 */
	pinMode(GPIO_PIN_5, OUTPUT); /* Connected to Relay 6 */
	pinMode(GPIO_PIN_6, OUTPUT); /* Connected to Relay 7 */
	pinMode(GPIO_PIN_7, OUTPUT); /* Connected to Relay 8 */
	pinMode(GPIO_PIN_8, OUTPUT); /* Connected to Relay L (Main Lantern) */
	pinMode(GPIO_PIN_9, OUTPUT); /* Connected to Relay M (Motor) */

	/* Set child lantern design */
	digitalWrite(GPIO_PIN_0, HIGH);
	digitalWrite(GPIO_PIN_1, HIGH);
	digitalWrite(GPIO_PIN_2, HIGH);
	digitalWrite(GPIO_PIN_3, HIGH);
	digitalWrite(GPIO_PIN_4, HIGH);
	digitalWrite(GPIO_PIN_5, HIGH);
	digitalWrite(GPIO_PIN_6, HIGH);
	digitalWrite(GPIO_PIN_7, HIGH);
	/* Set main lanten bulb On/Off */
	digitalWrite(GPIO_PIN_8, HIGH);
	/* Set motor rotation On/Off */
	digitalWrite(GPIO_PIN_9, HIGH);
	
#ifdef DEBUG_MODE_ENABLE
	/* Open serial port for debugging only. */
	Serial.begin(115200);
	delay(100);
#endif
	
#ifdef USE_WIFI_AP_MODE	
	IPAddress ip(20, 0, 0, 20);
	IPAddress gateway(20, 0, 0, 1);
	IPAddress subnet(255, 255, 255, 0);
	
	/* WiFi Access Point(AP) Mode*/
	WiFi.softAP(ssid, password);
	WiFi.softAPConfig(ip, gateway, subnet);
	
	delay(100);
#else
	#ifdef USE_STATIC_IP
		/* Set the static IP info here if you use static IP */
		IPAddress ip(10, 0, 0, 150);
		IPAddress gateway(10, 0, 0, 1);
		IPAddress subnet(255, 255, 255, 0);
		WiFi.config(ip, gateway, subnet);
	#else
		/*Use dynamic IP address assigned by WiFi Access Point
		  - Do nothing here
		 */
	#endif
	/* Connect to WiFi LAN. */
	WiFi.begin(ssid, password);
	
	while (WiFi.status() != WL_CONNECTED) {
		delay(250);
	}	
#endif
    
	LOG("WiFi Connected at ");
	LOG_LN(WiFi.localIP());
	
	/* Start Server */
	server.begin();
	delay(1000);
	
	LOG_LN("Server Ready");
}

/**
 * @brief Application main loop
 * 
 * @internal
 *      History:
 *      2018.04.26      Initialise
 */
void loop()
{
	/* 
	 * Check whether or not a client is
	 * connected once each loop
	 */
	SetClientConnected(CheckClientConnection());

	if (ClientConnected && GetClientData())
	{
		ProcessClientData();
	}
	//delay(10);
}

/**
 * @brief Only process this routine when the ClientConnected
 *	  state has actually changed. Otherwise,
 *	  return immediately. 
 * 
 * @internal
 *	  History:
 *        2018.04.26      Initialise
 */
void SetClientConnected(boolean flag)
{
    if (flag != LastClientConnected)
    {
        ClientConnected = flag;
        LastClientConnected = ClientConnected;
        if (ClientConnected)
        {
            LOG_LN("Client Connected");
        }
        else
        {
            LOG_LN("Client Disconnected");
        }
    }
}

/**
 * @brief Check whether or not a client is connected once each loop
 * 
 * @internal
 *      History:
 *      2018.04.26      Initialise
 */
boolean CheckClientConnection()
{
	/* 
	* If we have a running WiFiClient and there is a remote connection,
  	* just confirm the connection
	*/
	if (client && client.connected())
	{
		return true;
	}

	/*
	 * If we have a running WiFiClient but the remote has disconnected,
	 * disable WiFiClient and report no connection
	 */
	if (client && !client.connected())
	{
		client.stop();
		return false;
	}

	/*
	 * At this point we are ready for a new remote connection.
	 * Create the WiFiClient and confirn the connection
	 */
	if (server.hasClient())
	{
		if ((!client) || (!client.connected()))
		{
			if (client) client.stop();
			client = server.available();
      client.setNoDelay(1);
			return true;
		}
	}
}

/**
 * @brief Check data availability
 * 
 * If the remote connection has sent data, read the data and put it
 * into  the  WiFiBuffer. Tell the main loop that data is available 
 * for  processing.  Otherwise,  return immediately and report that
 * no data is available.
 *
 * @internal
 *      History:
 *      2018.04.26      Initialise
 */
boolean GetClientData()
{    
	if (client.available())
	{
		client.read(wifiBuffer, WIFI_BUFFER_SIZE);
		client.flush();
		return true;
	}
	else
	{
		return false;
	}
}

/**
 * @brief Process client data
 * 
 * This is a trivial routine to process data from remote socket.
 *
 * @internal
 *      History:
 *      2018.04.26      Initialise
 */
void ProcessClientData(void)
{
    if (client)
    {
        if(((byte)wifiBuffer[PKT_ST_POS] & 0xFF)== PKT_ST && 
	   ((byte)wifiBuffer[PKT_END_POS] & 0xFF) == PKT_END)
        {
		uint8_t data1_h, data2_h;
		uint8_t data1_l, data2_l;

		data1_h = wifiBuffer[PKT_DATA1_POS_H] & 0x0F;
		data1_l = wifiBuffer[PKT_DATA1_POS_L] & 0x0F;
		data2_h = wifiBuffer[PKT_DATA2_POS_H] & 0x0F;
		data2_l = wifiBuffer[PKT_DATA2_POS_L] & 0x0F;

		//LOG("Client Data Received: ");
		//LOG(wifiBuffer[PKT_ST_POS] & 0xFF, HEX);
		//LOG("-");
		//LOG((data1_h << 4 & 0xF0 | data1_l), BIN);
		//LOG("-");
		//LOG((data2_h << 4 & 0xF0 | data2_l), BIN);
		//LOG("-");
		//LOG(wifiBuffer[PKT_END_POS] & 0xFF, HEX);
		//LOG_LN("");

		/* Set child lantern design */
		digitalWrite(GPIO_PIN_0, bitRead(data1_h, 3) == 1 ? LOW : HIGH );
		digitalWrite(GPIO_PIN_1, bitRead(data1_h, 2) == 1 ? LOW : HIGH );
		digitalWrite(GPIO_PIN_2, bitRead(data1_h, 1) == 1 ? LOW : HIGH );
		digitalWrite(GPIO_PIN_3, bitRead(data1_h, 0) == 1 ? LOW : HIGH );
		digitalWrite(GPIO_PIN_4, bitRead(data1_l, 3) == 1 ? LOW : HIGH );
		digitalWrite(GPIO_PIN_5, bitRead(data1_l, 2) == 1 ? LOW : HIGH );
		digitalWrite(GPIO_PIN_6, bitRead(data1_l, 1) == 1 ? LOW : HIGH );
		digitalWrite(GPIO_PIN_7, bitRead(data1_l, 0) == 1 ? LOW : HIGH );

		/* Set main lanten bulb On/Off */
		digitalWrite(GPIO_PIN_8, bitRead(data2_h, 3) == 1 ? LOW : HIGH );
		/* Set motor rotation On/Off */
		digitalWrite(GPIO_PIN_9, bitRead(data2_h, 2) == 1 ? LOW : HIGH );
        }
    }
    else{
	LOG_LN("ProcessClientData:NG");
    }
}
//EOF
