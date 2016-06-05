
/*
 * Copyright 2013 Yufeng Duan, Politecnico di Torino, Turin, Italy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.wifidirecttesttwo; //new version

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.example.wifidirecttesttwo.ContentRoutingLayer.NoPitEntryException;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Messenger;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

public class UDPFileService extends IntentService {

    private final static String TAG = "UDPFileService";

	public static final String ACTION_SEND_UDP_MSG = "com.dyf.android.udptest.SEND_UDP_MSG";
	public static final String ACTION_SEND_FILE = "com.dyf.android.udptest.SEND_FILE";
	public static final String EXTRA_UDP_BIND_IP = "com.dyf.android.udptest.EXTRA_UDP_BIND_IP";
	public static final String EXTRA_UDP_REMOTE_IP = "com.dyf.android.udptest.EXTRA_UDP_REMOTE_IP";
	public static final String EXTRA_UDP_REMOTE_PORT = "com.dyf.android.udptest.EXTRA_UDP_REMOTE_PORT";
	public static final String EXTRA_UDP_SEQUENCE_NUMBER = "com.dyf.android.udptest.EXTRA_UDP_SEQUENCE_NUMBER";
	public static final String EXTRA_UDP_FILE_DATA = "com.dyf.android.udptest.EXTRA_UDP_FILE_DATA";
	public static final String EXTRA_NONCE = "com.dyf.android.udptest.NONCE";
	public static final String EXTRA_FILE_NAME = "com.dyf.android.udptest.EXTRA_FILE_NAME";
    public static final String EXTRA_SERVICE_NAME = "com.dyf.android.udptest.EXTRA_SERVICE_NAME";
    public static final String EXTRA_STATUS_MESSENGER = "com.dyf.android.udptest.EXTRA_STATUS_MESSENGER";
    public static final String BROADCAST_ACTION_SENT_PACKETS = "com.dyf.android.udptest.BROADCAST_ACTION_SENT_PACKETS";
    public static final String BROADCAST_ACTION_SENT_RESULTS = "com.dyf.android.udptest.BROADCAST_ACTION_SENT_RESULTS";
    public static final String EXTRA_SENT_PACKETS = "com.dyf.android.udptest.EXTRA_SENT_PACKETS";
    public static final String EXTRA_SENT_THROUGHPUT = "com.dyf.android.udptest.EXTRA_SENT_THROUGHPUT";
    public static final int MSG_SENT_PACKETS = 1045;

    private final Handler handler = new Handler();
    private int fileSize;

	private static final int UDP_CLIENT_PORT = 54321;

	public UDPFileService(String name) {
		super(name);
	}

	public UDPFileService() {
		super("UDPFileService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (intent.getAction() == ACTION_SEND_FILE){
			sendUdpFile(intent);
		}
	}

    private void sendUdpFile(Intent intent){
        //messenger = (Messenger) intent.getExtras().get(
        //		MainActivity.UDP_CLIENT_MESSENGER);
        String bindIP = intent.getStringExtra(EXTRA_UDP_BIND_IP);
        DatagramSocket socket;

        long startTime = 0;
        int i = 0;

        try {
            // Construct a socket
            if (bindIP != null) {
                socket = new DatagramSocket(UDP_CLIENT_PORT,
                        InetAddress.getByName(bindIP));
            } else
                socket = new DatagramSocket();

            // Construct a packet
            // Get the remote IP and port
            String remoteIP = intent.getExtras().getString(EXTRA_UDP_REMOTE_IP);
            InetAddress address = InetAddress.getByName(remoteIP);
            int remotePort = intent.getIntExtra(EXTRA_UDP_REMOTE_PORT, UDPServer.UDP_SERVER_PORT);
            int nonce = intent.getIntExtra(EXTRA_NONCE, 0);

            //socket.setSendBufferSize(Integer.MAX_VALUE);

            //File file = new File(this.getExternalFilesDir(null), intent.getStringExtra(EXTRA_FILE_NAME));
            //FileInputStream fileTosend = new FileInputStream(file);
            byte[] buf = new byte[1400];
            byte[] packetBuffer;
            //long fileSize = file.length();

            int srvlen;
            String service = intent.getExtras().getString(EXTRA_SERVICE_NAME, "file");
            srvlen = service.length();

            int headerLength = 1 + 4 + 4 + 1 + service.length() + 1;
            int dataLength = 1400 - headerLength;
            byte[] data = new byte[dataLength];



            // long numberOfPackets = fileSize/dataLength + 1;
            /* --- Not used in Demo
			long[] timestamps = new long[(int)numberOfPackets];
			*/

            //datalen = (data == null) ? 0 : data.length;

            ByteArrayOutputStream baos = new ByteArrayOutputStream(headerLength);
            DataOutputStream dos = new DataOutputStream(baos);

            try {
                dos.write(ContentRoutingLayer.SERVICE_APPLICATION_REPLY);
                dos.write(address.getAddress());
                dos.writeInt(nonce);
                dos.write(srvlen);
                dos.write(service.getBytes());
                dos.write(dataLength);

            } catch (IOException e) {
                e.printStackTrace();
            }

            packetBuffer = baos.toByteArray();
            System.arraycopy(packetBuffer, 0, buf, 0, headerLength);
            (new Random()).nextBytes(data);
            System.arraycopy(data, 0, buf, headerLength, dataLength);

            fileSize = 18000;
            startTime = System.currentTimeMillis();
            while (i<fileSize) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, remotePort);
                //SystemClock.sleep(2);
                //for(int y=0; y<1400000; y++);

                socket.send(packet);
                //long timeSend = System.currentTimeMillis();
                //timestamps[i] = timeSend; //--- Not used in Demo
                // TODO:
                if(i%400 == 0){
                    Intent sentPacketsIntent = new Intent(BROADCAST_ACTION_SENT_PACKETS);
                    sentPacketsIntent.putExtra(EXTRA_SENT_PACKETS, i);
                    sendBroadcast(sentPacketsIntent);
                }

                i++;

/*                try{
                    Thread.sleep(1);
                }
                catch (InterruptedException e){
                    Log.d(TAG, e.toString());
                }*/
                try{
                    TimeUnit.MICROSECONDS.sleep(800);
                }
                catch (InterruptedException e){
                    Log.d(TAG, e.toString());
                }

            }
            long endTime = System.currentTimeMillis();
            double throughput = i * 1400 * 8 / 1000 / (endTime-startTime);
            Intent resultPacketsIntent = new Intent(BROADCAST_ACTION_SENT_RESULTS);
            resultPacketsIntent.putExtra(EXTRA_SENT_PACKETS, i);
            resultPacketsIntent.putExtra(EXTRA_SENT_THROUGHPUT, throughput);
            sendBroadcast(resultPacketsIntent);

            Toast.makeText(getApplicationContext(), "Sending finished: " + i, Toast.LENGTH_LONG).show();
            //fileTosend.close();
            socket.close();

            //writeLogFile(timestamps); //--- Not used in Demo
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            long endTime = System.currentTimeMillis();
            double throughput = i * 1400 * 8 / 1000 / (endTime-startTime);
            Intent resultPacketsIntent = new Intent(BROADCAST_ACTION_SENT_RESULTS);
            resultPacketsIntent.putExtra(EXTRA_SENT_PACKETS, i);
            resultPacketsIntent.putExtra(EXTRA_SENT_THROUGHPUT, throughput);
            sendBroadcast(resultPacketsIntent);
        }
    }

    /*
	private void sendUdpFile(Intent intent){
		//messenger = (Messenger) intent.getExtras().get(
		//		MainActivity.UDP_CLIENT_MESSENGER);
		String bindIP = intent.getStringExtra(EXTRA_UDP_BIND_IP);
		DatagramSocket socket;

		try {
			// Construct a socket
			if (bindIP != null) {
				socket = new DatagramSocket(UDP_CLIENT_PORT,
						InetAddress.getByName(bindIP));
			} else
				socket = new DatagramSocket();

			// Construct a packet
			// Get the remote IP and port
			String remoteIP = intent.getExtras().getString(EXTRA_UDP_REMOTE_IP);
			InetAddress address = InetAddress.getByName(remoteIP);
			int remotePort = intent.getIntExtra(EXTRA_UDP_REMOTE_PORT, UDPServer.UDP_SERVER_PORT);
			int nonce = intent.getIntExtra(EXTRA_NONCE, 0);

            this.messenger = intent.getParcelableExtra(EXTRA_STATUS_MESSENGER);

			//socket.setSendBufferSize(Integer.MAX_VALUE);

			File file = new File(this.getExternalFilesDir(null), intent.getStringExtra(EXTRA_FILE_NAME));
			FileInputStream fileTosend = new FileInputStream(file);
			byte[] buf = new byte[1400];
			byte[] packetBuffer;
			long fileSize = file.length();
			int i = 0;
			
			
			int srvlen;
			String service = intent.getExtras().getString(EXTRA_SERVICE_NAME, "file");
			srvlen = service.length();
			
			int headerLength = 1 + 1 + 4 + 4 + 4 + 1;
			int dataLength = 1400 - headerLength;
			

			long numberOfPackets = fileSize/dataLength + 1;
            // --- Not used in Demo
			//long[] timestamps = new long[(int)numberOfPackets];

			
			//datalen = (data == null) ? 0 : data.length;

			ByteArrayOutputStream baos = new ByteArrayOutputStream(1400);
			DataOutputStream dos = new DataOutputStream(baos);

			try {
				dos.write(ContentRoutingLayer.SERVICE_APPLICATION_REPLY);
				dos.write(address.getAddress());
				dos.writeInt(nonce);
				dos.write(srvlen);
				dos.write(service.getBytes());
				dos.write(dataLength);

			} catch (IOException e) {
				e.printStackTrace();
			}
			
			packetBuffer = baos.toByteArray();
			System.arraycopy(packetBuffer, 0, buf, 0, headerLength);
			
			while ((fileTosend.read(buf, headerLength, dataLength)) > 0) {
				DatagramPacket packet = new DatagramPacket(buf, buf.length, address, remotePort);
				//SystemClock.sleep(2);
				//for(int y=0; y<1400000; y++);
				
				socket.send(packet);
				long timeSend = System.currentTimeMillis();
				// timestamps[i] = timeSend; //--- Not used in Demo
				i++;
			}

            Toast.makeText(getApplicationContext(), "Sending finished", Toast.LENGTH_LONG).show();
			fileTosend.close();
			socket.close();

			//writeLogFile(timestamps); //--- Not used in Demo
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
    */
	
	
	

	private void writeLogFile(long[] timestamps){
		File logFile = new File(getExternalFilesDir(null),"SENT_121.txt");

		if (!logFile.exists()){
			try{
				logFile.createNewFile();
			} 
			catch (IOException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try{
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
			for (long l : timestamps) {
				buf.append(String.valueOf(l));
				buf.newLine();
			}
			buf.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	

}
