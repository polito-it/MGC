
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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UDPServer extends Thread {
	private DatagramSocket socket;
	private MainActivity mActivity;
	private Handler handler;
	private int packn; // packet number extracted from the packet
	private Integer count;
	private String src; // source name
	private String dst; // dest name
	private String text;
	private Map<String, Integer> packrecord;
	private byte[] namebuffer;

	public final static int PACKET_BUFFER_SIZE = 1400;
	public final static int UDP_SERVER_PORT = 9000;

	public UDPServer(MainActivity activity) {
		super();
		this.mActivity = activity;
		try {
			this.socket = new DatagramSocket(UDP_SERVER_PORT);
			//this.socket.setReceiveBufferSize(150000);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		namebuffer = new byte[256];
		handler = this.mActivity.getUdpTextHandler();
		packrecord = new HashMap<String, Integer>();
	}

	@Override
	public void run() {
		super.run();

		while (!mActivity.isStopWifiServer()) {

			byte[] buffer = new byte[PACKET_BUFFER_SIZE];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (packet.getLength() > 0) {
				ByteArrayInputStream bais = new ByteArrayInputStream(buffer, 0, packet.getLength());
				DataInputStream dis = new DataInputStream(bais);

				try {
					byte packetType = dis.readByte();
					if(packetType!=0){
						mActivity.getGroupRoutingLayer().arrivedPacket(packet);
						continue;
					}

					byte srcnamelen = dis.readByte(); // source name length
					byte dstnamelen = dis.readByte(); // destination name length
					packn = dis.readInt(); // packet sn
					dis.read(namebuffer, 0, srcnamelen);
					src = new String(namebuffer, 0, srcnamelen); // source name
					for (int i = 0; i < srcnamelen; i++) {
						namebuffer[i] = 0; // clear name buffer
					}
					dis.read(namebuffer, 0, dstnamelen);
					dst = new String(namebuffer, 0, dstnamelen); // destination	// name
					text = String.valueOf(packn);
				} catch (IOException e) {
					e.printStackTrace();
				}

				String myname = mActivity.getMyName();

				count = packrecord.get(src);
				if(count == null || packn < count){
					packrecord.put(src, packn);
				}
				else
					//packet still received
					continue;


				// checks if the packet is sent by me
				if(src.equals(myname)){
					// the message is sent by me
					//String wlanAddress = mActivity.getWlanAddress();
					text += " I sent it! " + src + " (" + packet.getAddress().getHostAddress() + ") -> " + dst;
					Log.d("WifiTwo", "Sent by me packet: " + String.valueOf(packn) + " " + packet.getAddress().getHostAddress() + " (" + src + "->" + dst +  ")");
				}

				// check if the message is for me
				else if (dst.equals(myname)) {

					text += " For me! " + src + " (" + packet.getAddress().getHostAddress() + ") -> " + dst;
					Log.d("WifiTwo", "For me packet: " + String.valueOf(packn) + " " + packet.getAddress().getHostAddress() + " (" + src + "->" + dst +  ")");

				} 
				else{
					// the message is not for me, and not sent by me, and to relay
					text += " Not for me! " + src + " (" + packet.getAddress().getHostAddress() + ") -> " + dst;
					count = packrecord.get(src);

					// writing log
					Log.d("WifiTwo", "Not for me packet: " + String.valueOf(packn) + " " + packet.getAddress().getHostAddress() + " (" + src + "->" + dst +  ")");



					if(mActivity.isGO()) {
						//GO case
						//is from elected client? if yes broadcast, then send to the elected client, if exists.
						if(mActivity.isLegacyClient()){
							if(packet.getAddress().getHostAddress().equals(mActivity.getContentRoutingLayer().getElectedClientAddress())){
								Intent intent = new Intent(mActivity, UDPClientService.class);
								intent.setAction(UDPClientService.ACTION_SEND_UDP_MSG);
								intent.putExtra(UDPClientService.EXTRA_UDP_SRC_NAME, src);
								intent.putExtra(UDPClientService.EXTRA_UDP_DEST_NAME, dst);
								intent.putExtra(UDPClientService.EXTRA_UDP_DEST_IP, "192.168.49.255");
								intent.putExtra(UDPClientService.EXTRA_UDP_PACKET_NO, packn);
								mActivity.startService(intent);
							}
							else{
								Intent intent = new Intent(mActivity, UDPClientService.class);

								intent.setAction(UDPClientService.ACTION_SEND_UDP_MSG);
								intent.putExtra(UDPClientService.EXTRA_UDP_SRC_NAME, src);
								intent.putExtra(UDPClientService.EXTRA_UDP_DEST_NAME, dst);
								intent.putExtra(UDPClientService.EXTRA_UDP_DEST_IP, mActivity.getContentRoutingLayer().getElectedClientAddress().getHostAddress());
								intent.putExtra(UDPClientService.EXTRA_UDP_PACKET_NO, packn);
								intent.putExtra(UDPClientService.EXTRA_UDP_WIFI_IP, mActivity.getWlanAddress());
								mActivity.startService(intent);

								Intent intentbc = new Intent(mActivity, UDPClientService.class);

								intentbc.setAction(UDPClientService.ACTION_SEND_UDP_MSG);
								intentbc.putExtra(UDPClientService.EXTRA_UDP_SRC_NAME, src);
								intentbc.putExtra(UDPClientService.EXTRA_UDP_DEST_NAME, dst);
								intentbc.putExtra(UDPClientService.EXTRA_UDP_DEST_IP, "192.168.49.255");
								intentbc.putExtra(UDPClientService.EXTRA_UDP_PACKET_NO, packn);
								intentbc.putExtra(UDPClientService.EXTRA_UDP_WIFI_IP, mActivity.getWlanAddress());
								mActivity.startService(intentbc);
							}
						}
						else{
							Intent intentbc = new Intent(mActivity, UDPClientService.class);

							intentbc.setAction(UDPClientService.ACTION_SEND_UDP_MSG);
							intentbc.putExtra(UDPClientService.EXTRA_UDP_SRC_NAME, src);
							intentbc.putExtra(UDPClientService.EXTRA_UDP_DEST_NAME, dst);
							intentbc.putExtra(UDPClientService.EXTRA_UDP_DEST_IP, "192.168.49.255");
							intentbc.putExtra(UDPClientService.EXTRA_UDP_PACKET_NO, packn);
							intentbc.putExtra(UDPClientService.EXTRA_UDP_WIFI_IP, mActivity.getWlanAddress());
							mActivity.startService(intentbc);
						}
					}

					else{
						if(mActivity.isElectedClient() && packet.getAddress().getHostAddress().equals("192.168.49.1")){
							for(InetAddress address : mActivity.getContentRoutingLayer().getLegacyClientAddresses()){
								Intent intent = new Intent(mActivity, UDPClientService.class);
								intent.setAction(UDPClientService.ACTION_SEND_UDP_MSG);
								intent.putExtra(UDPClientService.EXTRA_UDP_SRC_NAME, src);
								intent.putExtra(UDPClientService.EXTRA_UDP_DEST_NAME, dst);
								intent.putExtra(UDPClientService.EXTRA_UDP_DEST_IP, address.getHostAddress());
								intent.putExtra(UDPClientService.EXTRA_UDP_PACKET_NO, packn);
								mActivity.startService(intent);
							}
						}
						else if(mActivity.isElectedClient() && mActivity.getContentRoutingLayer().getLegacyClientAddresses().contains(packet.getAddress().getHostAddress())){
							Intent intent = new Intent(mActivity, UDPClientService.class);
							intent.setAction(UDPClientService.ACTION_SEND_UDP_MSG);
							intent.putExtra(UDPClientService.EXTRA_UDP_SRC_NAME, src);
							intent.putExtra(UDPClientService.EXTRA_UDP_DEST_NAME, dst);
							intent.putExtra(UDPClientService.EXTRA_UDP_DEST_IP, "192.168.49.255");
							intent.putExtra(UDPClientService.EXTRA_UDP_PACKET_NO, packn);
							mActivity.startService(intent);
						}
					}		


				}

				Bundle data = new Bundle(1);
				data.putString(MainActivity.UDP_MESSAGE, text);
				Message msg = handler.obtainMessage(MainActivity.MESSAGE_UDP_SERVER);
				msg.setData(data);
				handler.sendMessage(msg);
			}
		}
	}
}
