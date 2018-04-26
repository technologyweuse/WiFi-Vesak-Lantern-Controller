using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace design_simulator
{
    public partial class Form1 : Form
    {
        int WIFI_BUFFER_SIZE = 6;
        int PKT_ST = 0x33;
        int PKT_END = 0x3C;
        int PKT_ST_POS = 0x00;
        int PKT_DATA1_POS_H = 0x01;
        int PKT_DATA1_POS_L = 0x02;
        int PKT_DATA2_POS_H = 0x03;
        int PKT_DATA2_POS_L = 0x04;
        int PKT_END_POS = 0x05;
        

        int PORT = 4444;
        TcpListener serverSocket;
        TcpClient clientSocket;

        public Form1()
        {
            InitializeComponent();
            pic1.BackgroundImage = Properties.Resources.btn_off;
            pic2.BackgroundImage = Properties.Resources.btn_off;
            pic3.BackgroundImage = Properties.Resources.btn_off;
            pic4.BackgroundImage = Properties.Resources.btn_off;
            pic5.BackgroundImage = Properties.Resources.btn_off;
            pic6.BackgroundImage = Properties.Resources.btn_off;
            pic7.BackgroundImage = Properties.Resources.btn_off;
            pic8.BackgroundImage = Properties.Resources.btn_off;
            pic9.BackgroundImage = Properties.Resources.btn_off_orange;
            pic10.BackgroundImage = Properties.Resources.btn_off_blue;

            picClientConnected.BackgroundImage = Properties.Resources.disconnected;

            lblPort.Text = PORT.ToString();
            lblIP.Text = GetLocalIPAddress();
        }

        private void btnServerStart_Click(object sender, EventArgs e)
        {
           
            //while (true)   //we wait for a connection
            {
                serverSocket = new TcpListener(PORT);
                clientSocket = default(TcpClient);

                serverSocket.Start();

                //while (true)
                {
                    
                    clientSocket = serverSocket.AcceptTcpClient();

                    Thread ctThread = new Thread(doChat);
                    ctThread.Start();
                }


            }
        }

        private void btnServerStop_Click(object sender, EventArgs e)
        {
            clientSocket.Close();
            serverSocket.Stop();
        }

        private void doChat()
        {
            uint data1_h, data2_h;
            uint data1_l, data2_l;

            picServerState.BackgroundImage = Properties.Resources.wifi_on_maroon;
            picClientConnected.BackgroundImage = Properties.Resources.connected;

            while ((true))
            {
                try
                {
                    NetworkStream ns = clientSocket.GetStream();

                    byte[] msg = new byte[WIFI_BUFFER_SIZE];     //the messages arrive as byte array
                    int len = 0;
                    len = ns.Read(msg, 0, msg.Length);   //the same networkstream reads the message sent by the client
                    if (len > 0)
                    {
                        if (msg[PKT_ST_POS] == PKT_ST && msg[PKT_END_POS] == PKT_END)
                        {

                            data1_h = (uint)msg[PKT_DATA1_POS_H] & 0x0F;
                            data1_l = (uint)msg[PKT_DATA1_POS_L] & 0x0F;
                            data2_h = (uint)msg[PKT_DATA2_POS_H] & 0x0F;
                            //data2_l = (uint)msg[PKT_DATA2_POS_L] & 0x0F;
                            if ((data1_h & (1 << 4)) == (1 << 4))
                            {
                                int x = 0;
                            }
                            pic1.BackgroundImage = (data1_h & 8) == 8 ? Properties.Resources.btn_on : Properties.Resources.btn_off;
                            pic2.BackgroundImage = (data1_h & 4) == 4 ? Properties.Resources.btn_on : Properties.Resources.btn_off;
                            pic3.BackgroundImage = (data1_h & 2) == 2 ? Properties.Resources.btn_on : Properties.Resources.btn_off;
                            pic4.BackgroundImage = (data1_h & 1) == 1 ? Properties.Resources.btn_on : Properties.Resources.btn_off;
                            pic5.BackgroundImage = (data1_l & 8) == 8 ? Properties.Resources.btn_on : Properties.Resources.btn_off;
                            pic6.BackgroundImage = (data1_l & 4) == 4 ? Properties.Resources.btn_on : Properties.Resources.btn_off;
                            pic7.BackgroundImage = (data1_l & 2) == 2 ? Properties.Resources.btn_on : Properties.Resources.btn_off;
                            pic8.BackgroundImage = (data1_l & 1) == 1 ? Properties.Resources.btn_on : Properties.Resources.btn_off;

                            pic9.BackgroundImage = (data2_h & 8) == 8 ? Properties.Resources.btn_on_orange : Properties.Resources.btn_off_orange;
                            pic10.BackgroundImage = (data2_h & 4) == 4 ? Properties.Resources.btn_on_blue : Properties.Resources.btn_off_blue;
                        }
                    }
                }
                catch (Exception ex)
                {
                    //
                }
            }
        }

        private string GetLocalIPAddress()
        {
            string localIP;
            using (Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, 0))
            {
                socket.Connect("8.8.8.8", 65530);
                IPEndPoint endPoint = socket.LocalEndPoint as IPEndPoint;
                localIP = endPoint.Address.ToString();
            }
            return localIP;
        }
    }
}
