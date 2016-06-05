
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

package com.example.wifidirecttesttwo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.app.IntentService;
import android.content.Intent;

public class UDPClientService extends IntentService {
	
	public static final String ACTION_SEND_UDP_MSG = "com.example.android.wifitwo.SEND_UDP_MSG";
	public static final String ACTION_SEND_GR_MSG = "com.example.android.wifitwo.SEND_GR_MSG";
	
	public static final String EXTRA_UDP_IP = "com.example.android.wifitwo.UDP_SENDTO_IP";
	public static final String EXTRA_UDP_PACKET_NO = "com.example.android.wifitwo.UDP_PACKNO";
	public static final String EXTRA_UDP_DEST_NAME = "com.example.android.wifitwo.UDP_DEST_NAME";
	public static final String EXTRA_UDP_DEST_IP = "com.example.android.wifitwo.UDP_DEST_IP";
	public static final String EXTRA_UDP_SRC_NAME = "com.example.android.wifitwo.UDP_SRC_NAME";
	public static final String EXTRA_UDP_WIFI_IP = "com.example.android.wifitwo.UDP_WIFI_IP";

	public static final String EXTRA_UDP_PAYLOAD = "com.example.android.wifitwo.UDP_PAYLOAD";
	public static final String EXTRA_UDP_RELAY = "com.example.android.wifitwo.UDP_RELAY";
	public static final String EXTRA_UDP_DEST_PORT = "com.example.android.wifitwo.UDP_DEST_PORT";
	public static final String EXTRA_UDP_SOCKET = "com.example.android.wifitwo.UDP_SOCKET";
	
	private DatagramSocket socket;
	

	public UDPClientService(String name) {
		super(name);
	}

	public UDPClientService() {
		super("UDPClientService");
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		socket.close();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		if (intent.getAction().equals(ACTION_SEND_GR_MSG)){
			
			int port = intent.getIntExtra(EXTRA_UDP_DEST_PORT, 9000);
			byte[] payload = intent.getByteArrayExtra(EXTRA_UDP_PAYLOAD);
			//boolean relayNeeded = intent.getBooleanExtra(EXTRA_UDP_RELAY, false);
			Serializable s = intent.getSerializableExtra(EXTRA_UDP_DEST_IP);
			InetAddress dest = null;
			if (s instanceof InetAddress){
				dest = (InetAddress)s;
			}
			
			DatagramPacket pack = new DatagramPacket(payload, payload.length, dest, port);
			
			try {
				socket.send(pack);
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return;
		}
		
		
		else if (intent.getAction().equals(ACTION_SEND_UDP_MSG)) {
			
			int port = UDPServer.UDP_SERVER_PORT;
			String src = intent.getStringExtra(EXTRA_UDP_SRC_NAME);
			String dst = intent.getExtras().getString(EXTRA_UDP_DEST_NAME);
			String destIP = intent.getExtras().getString(EXTRA_UDP_DEST_IP);
			int packn = intent.getIntExtra(EXTRA_UDP_PACKET_NO, 65531);

			ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
			DataOutputStream dos = new DataOutputStream(baos);
			
			try {
				dos.writeByte(0);
				dos.write(src.length());
				dos.write(dst.length());
				dos.writeInt(packn);
				dos.writeBytes(src);
				dos.writeBytes(dst);
				dos.close();
				baos.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			byte[] data = baos.toByteArray();
			// for now, it is the dest mac
			try {
				InetAddress address = InetAddress.getByName(destIP);
				DatagramPacket packet = new DatagramPacket(data, data.length,
						address, port);
				socket.send(packet);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
