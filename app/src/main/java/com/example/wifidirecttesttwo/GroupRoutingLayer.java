
/*
 * Copyright 2014 Yufeng Duan, Politecnico di Torino, Turin, Italy
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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.content.Intent;
import android.os.Messenger;
import android.widget.RadioGroup;

public class GroupRoutingLayer {


	ContentRoutingLayer crLayer;
	MainActivity mActivity;
	DatagramSocket socket;
	InetAddress p2pAddress;
	private String fileName;

    public static String DEFAULT_FILE_NAME = "medium.mp4";



	public GroupRoutingLayer(MainActivity ma, ContentRoutingLayer crLayer){
		this.crLayer = crLayer;
		this.mActivity = ma;
		this.fileName = DEFAULT_FILE_NAME;
		p2pAddress=null;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}




	public void forwardPacket(byte[] data, InetAddress dest, Boolean relayNeeded){

        // If I am a legacy client and the destination is NOT the External Elected Client, send it in broadcast
        // Otherwise, send it directly to the destination
		if(mActivity.isLegacyClient() && !dest.equals(crLayer.getexternalElectedClientAddress()))
			try {
				dest = InetAddress.getByName("192.168.49.255");
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			
		Intent intent = new Intent(mActivity, UDPClientService.class);
		intent.setAction(UDPClientService.ACTION_SEND_GR_MSG);
		intent.putExtra(UDPClientService.EXTRA_UDP_PAYLOAD, data);
		intent.putExtra(UDPClientService.EXTRA_UDP_DEST_IP, dest);
		intent.putExtra(UDPClientService.EXTRA_UDP_RELAY, relayNeeded);
		intent.putExtra(UDPClientService.EXTRA_UDP_DEST_PORT, UDPServer.UDP_SERVER_PORT);
		mActivity.startService(intent);

	}



	public void forwardFile(InetAddress dest, int nonce, String serviceName){
		InetAddress bindAddr;
		switch (mActivity.getIpRadioGroupChoice()) {
		case R.id.rg_bind_ip_lo:
			bindAddr = null;
			break;
		case R.id.rg_bind_ip_p2p:
			bindAddr = this.getP2pAddress();
			break;
		case R.id.rg_bind_ip_wifi:
			bindAddr = this.crLayer.getWiFiAddress();
			break;
		default:
			bindAddr = null;
			break;
		}
		
		if(mActivity.isLegacyClient() && !dest.equals(crLayer.getexternalElectedClientAddress()))
			try {
				dest = InetAddress.getByName("192.168.49.255");
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		
		String remoteIP = dest.getHostAddress();
		Intent intent = new Intent(mActivity, UDPFileService.class);
		intent.setAction(UDPFileService.ACTION_SEND_FILE);
		intent.putExtra(UDPFileService.EXTRA_UDP_REMOTE_IP, remoteIP);
		if(bindAddr != null)
			intent.putExtra(UDPFileService.EXTRA_UDP_BIND_IP, bindAddr.getHostAddress());
		intent.putExtra(UDPFileService.EXTRA_FILE_NAME, fileName);
		intent.putExtra(UDPFileService.EXTRA_NONCE, nonce);
        intent.putExtra(UDPFileService.EXTRA_SERVICE_NAME, serviceName);
		mActivity.startService(intent);
		
	}




	public void arrivedPacket(DatagramPacket packet){
		crLayer.deliverPacket(packet, true);
	}




	private void relayPacket(byte[] data, InetAddress dest){
		//DA IMPLEMENTARE!!!! COMUNICAZIONE BIDIREZIONALE
	}





	public void setP2pAddress(InetAddress ia) {
		this.p2pAddress = ia;
	}

	public InetAddress getP2pAddress(){
		return this.p2pAddress;
	}




	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


}
