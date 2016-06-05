
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WifiDirectConnectionInfoListener implements ConnectionInfoListener {

	//For intent
	public final static String EXTRA_MESSAGE_IP = "com.example.wifitwo.MESSAGE_IP";
	
	private MainActivity activity;
	private InetAddress groupOwnerAddress;
	public InetAddress myP2PAddress;

	public WifiDirectConnectionInfoListener(MainActivity activity) {
		super();
		this.activity = activity;
	}

	public InetAddress getGroupOwnerAddress() throws UnknownHostException {
		if(groupOwnerAddress == null)
			return InetAddress.getByName("192.168.49.1");
		return groupOwnerAddress;
	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		// InetAddress from WifiP2pInfo struct.
		this.groupOwnerAddress = info.groupOwnerAddress;
		activity.getWifiP2pManager().requestGroupInfo(activity.getChannel(), new GroupInfoListener() {
			
			@Override
			public void onGroupInfoAvailable(WifiP2pGroup group) {
				//Get interface ip
				try {
					NetworkInterface nif = NetworkInterface.getByName(group.getInterface());
					for (Enumeration<InetAddress> enumIpAddr = nif.getInetAddresses(); enumIpAddr
							.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (inetAddress.getAddress().length == 4) {
							myP2PAddress = inetAddress;
							break;
						}
					}
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		// Run UDP server
		activity.StartUDPServer(null);
		
		// After the group negotiation, we can determine the group owner.
		if (info.groupFormed && info.isGroupOwner) {
			Toast.makeText(activity,
					"I'm GO: " + this.groupOwnerAddress.getHostAddress(),
					Toast.LENGTH_SHORT).show();
			
			// set isGO variable to true
			activity.setIsGO(true);
			
			activity.StartP2PServer(null);

		} else if (info.groupFormed) {
			// The other device acts as the client. In this case,
			// you'll want to create a client thread that connects to the group
			// owner.
			Toast.makeText(
					activity,
					"I'm not the GO: "
							+ this.groupOwnerAddress.getHostAddress(),
					Toast.LENGTH_SHORT).show();
			
			// set isGO variable to false
			activity.setIsGO(false);
			
			//((TextView) activity.findViewById(R.id.show_text))
			//		.setText(this.groupOwnerAddress.getHostAddress());
			/*---Unused
			Button btn_startClient = (Button) activity
					.findViewById(R.id.btn_start_client);
			btn_startClient.setVisibility(View.VISIBLE);*/
		}

	}

	public static class FileServerAsyncTask extends
			AsyncTask<Void, Void, String> {

		private MainActivity activity;
		private TextView statusText;
		private InetAddress clientIP;

		/**
		 * @param context
		 * @param statusText
		 */
		public FileServerAsyncTask(MainActivity activity, View statusText) {
			this.activity = activity;
			this.statusText = (TextView) statusText;
		}

		@Override
		protected String doInBackground(Void... paras) {
			try {
				ServerSocket serverSocket = new ServerSocket(MainActivity.SERVER_PORT);
				Log.d("WifiTwo", "Server: Socket opened");
				Socket client = serverSocket.accept();
				clientIP = client.getInetAddress();
				Log.d("WifiTwo", "Server: connection done");
				// final File f = new
				// File(Environment.getExternalStorageDirectory() + "/"
				// + context.getPackageName() + "/wifip2pshared-" +
				// System.currentTimeMillis()
				// + ".jpg");
				File path = activity.getExternalFilesDir(null);
				File file = new File(path, "PeerSysInfo.txt");
				if (!path.exists())
					path.mkdirs();
				file.createNewFile();

				Log.d("WifiTwo", "server: copying files " + file.toString());
				InputStream inputstream = client.getInputStream();
				copyFile(inputstream, new FileOutputStream(file));
				serverSocket.close();
				return file.getAbsolutePath();
			} catch (IOException e) {
				Log.e("WifiTwo", e.getMessage());
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			if (result != null) {

				activity.peerFileName = result;
				/* ---Not this version
				((Button) activity.findViewById(R.id.btn_open_peer_text))
						.setVisibility(View.VISIBLE);
						*/

				// Prepare to start another activity to show the peer info
				Intent intent = new Intent(activity,
						DisplayPeerJsonInfoActivity.class);
				intent.putExtra(MainActivity.EXTRA_MESSAGE_FILE, result);
				intent.putExtra(WifiDirectConnectionInfoListener.EXTRA_MESSAGE_IP, clientIP.toString());
				activity.startActivity(intent);

				/*
				 * --- !!!!!! DO NOT DELETE !!!!!! ---
				 * statusText.setText("File copied - " + result); Intent intent
				 * = new Intent();
				 * intent.setAction(android.content.Intent.ACTION_VIEW);
				 * intent.setDataAndType(Uri.parse("file://" + result),
				 * "text/plain"); activity.startActivity(intent);
				 */

			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			statusText.setText("Opening a server socket");
		}

	}

	public static boolean copyFile(InputStream inputStream, OutputStream out) {
		byte buf[] = new byte[1024];
		int len;
		try {
			while ((len = inputStream.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
			out.close();
			inputStream.close();
		} catch (IOException e) {
			Log.d("WifiTwo", e.toString());
			return false;
		}
		return true;
	}

}