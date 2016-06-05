

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class P2PServerThread extends Thread {
	private MainActivity mActivity;
	private ServerSocket mServerSocket;
	private ContentRoutingDualTable contentDualTable;

	public P2PServerThread(MainActivity mActivity, ContentRoutingDualTable crdt) {
		super();
		this.mActivity = mActivity;
		this.contentDualTable = crdt;
	}

	@Override
	public void run() {
		super.run();
		try {
			mServerSocket = new ServerSocket();
			InetSocketAddress localaddr = new InetSocketAddress(
					"192.168.49.1", MainActivity.SERVER_PORT);
			mServerSocket.bind(localaddr);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	
		while (!mActivity.isStopP2PServer()) {
			try {
				Socket clientSocket = mServerSocket.accept();
				File path = mActivity.getExternalFilesDir(null);
				File file = new File(path, "PeerSysInfo.txt");
				if (!path.exists())
					path.mkdirs();
				file.createNewFile();

				Log.d("WifiTwo", "server: copying files " + file.toString());
				InputStream inputStream = clientSocket.getInputStream();
				// Write the information of the client into a string
				copyInfo(inputStream, clientSocket.getInetAddress());				
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		try {
			mServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean copyInfo(InputStream inputStream, InetAddress p2pIP)
	{
		byte buf[] = new byte[1024];
		//StringBuilder sbl = new StringBuilder();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(800);
		int len;
		try {
			while ((len = inputStream.read(buf)) != -1) {
				baos.write(buf, 0, len);
			}
			inputStream.close();
			// Convert the ByteArrayOutputStream to a string
			String clientInfo = baos.toString();
			// Extract info from json string
			try {
				JSONObject json = new JSONObject(clientInfo);
				JSONArray interfacesArray = json.getJSONArray("interfaces");
				// wifi
				JSONObject jsonWifi = interfacesArray.getJSONObject(2);
				String wifiMAC = jsonWifi.getString("MAC");
				// p2p
				JSONObject jsonP2P = interfacesArray.getJSONObject(3);
				String p2pMAC = jsonP2P.getString("MAC");
				// add the client to the client list in the main activity
				this.contentDualTable.putClient(new WifiP2pClient(p2pIP, p2pMAC, wifiMAC, true));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			Log.d("WifiTwo", e.toString());
			return false;
		}
		return true;
	}

}
