
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Enumeration;

import android.app.ActivityManager.MemoryInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
	private WifiP2pManager mManager;
	private Channel mChannel;
	private MainActivity mActivity;
	private long countSec;
	private File path;
	private File file;
	private OutputStream os;

	private InetAddress groupOwnerAddress;
	private InetAddress myP2pAddress;
	private String myP2pMacAddress;
	private ContentRoutingDualTable contentDualTable;

	private WifiP2pGroup oldGroup = null;

	private Handler roleTextHandler;

	/*
	 * // For remove the group private Timer rmgTimer; private TimerTask
	 * rmgTimerTask; private static int rmgTimerDelay = 5000;
	 */

	public WiFiDirectBroadcastReceiver(WifiP2pManager mManager,
			Channel mChannel, MainActivity activity, ContentRoutingDualTable cdt) {
		super();
		this.contentDualTable = cdt;
		this.mManager = mManager;
		this.mChannel = mChannel;
		this.mActivity = activity;

		this.myP2pAddress = null;
		this.myP2pMacAddress = null;

		this.roleTextHandler = mActivity.getRoleTextHandler();

		path = activity.getExternalFilesDir(null);
		path.mkdirs();
		file = new File(path, "time.txt");
		try {
			os = new FileOutputStream(file, true);
		} catch (FileNotFoundException e) {
			// TODO1 Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO1 Auto-generated method stub

		String action = intent.getAction();
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			mActivity
			.setIsWifiP2pEnabled(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED);

		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			if (mManager != null) {
				mManager.requestPeers(mChannel, (PeerListListener) mActivity);
			}

		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
				.equals(action)) {

			if (mManager == null) {
				return;
			}

			NetworkInfo networkInfo = (NetworkInfo) intent
					.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
			
			WifiP2pInfo wifiP2pInfo = (WifiP2pInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);

			WifiP2pGroup wifiP2pGroup = null;
			int currentapiVersion = android.os.Build.VERSION.SDK_INT;

			if (currentapiVersion >= 18){
				wifiP2pGroup = (WifiP2pGroup)intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
			}
			

			if (networkInfo.isConnected()) {
				// retrieving own IP and MAC address
				try {
					NetworkInterface nif = NetworkInterface.getByName(wifiP2pGroup.getInterface());
					String temp = new String(nif.getHardwareAddress());
					if (myP2pMacAddress == null || !temp.equals(myP2pMacAddress))
						myP2pMacAddress = temp;
					for (Enumeration<InetAddress> enumIpAddr = nif.getInetAddresses(); 
							enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (inetAddress.getAddress().length == 4) {
							if (this.myP2pAddress == null
									|| this.myP2pAddress != inetAddress) {
								this.myP2pAddress = inetAddress;
								this.mActivity.getGroupRoutingLayer()
								.setP2pAddress(this.myP2pAddress);
                                // update ui: ip address
                                Handler mainActivityIpHandler = mActivity.getIpTextHandler();
                                Bundle data = new Bundle(1);
                                data.putString(MainActivity.IP_TEXT_VIEW_MESSAGE, this.myP2pAddress.toString().substring(1));
                                Message msg = mainActivityIpHandler.obtainMessage();
                                msg.setData(data);
                                mainActivityIpHandler.sendMessage(msg);
							}
							break;
						}
					}
				} catch (SocketException e) {
					e.printStackTrace();
				}

				processConnectionInfo(wifiP2pInfo, wifiP2pGroup);
			} else {
                // If disconnected
                Handler mainActivityRoleHandler = mActivity.getRoleTextHandler();
                Bundle data = new Bundle(1);
                data.putString(MainActivity.ROLE_TEXT_VIEW_MESSAGE, "Disconnect");
                Message msg = mainActivityRoleHandler.obtainMessage();
                msg.setData(data);
                mainActivityRoleHandler.sendMessage(msg);
            }

		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
				.equals(action)) {
		}

	}

	public void processConnectionInfo(WifiP2pInfo info, WifiP2pGroup infoGroup) {
		
		if(infoGroup==null || info==null)
			return;

		this.groupOwnerAddress = info.groupOwnerAddress;

		mActivity.setGroupFormed(true);
		mActivity.StartUDPServer(null);

		
		if (info.groupFormed && info.isGroupOwner) {

			mActivity.setIsGO(true);
			mActivity.StartP2PServer(null);
			this.processGroupInfo(infoGroup);
			mActivity.getContentRoutingLayer().startBeaconing();
			// activity.getContentRoutingLayer().advertiseService(MainActivity.SERVICE_NAME,MainActivity.SERVICE_TYPE);
			//Set the role type in the UI
			Bundle data = new Bundle(1);
			data.putString(MainActivity.ROLE_TEXT_VIEW_MESSAGE, "Group Owner");
			Message msg = this.roleTextHandler.obtainMessage();
			msg.setData(data);
			this.roleTextHandler.sendMessage(msg);

		} else if (info.groupFormed) {
			//Toast.makeText(mActivity,"I'm not the GO: "+ this.groupOwnerAddress.getHostAddress(),Toast.LENGTH_SHORT).show();
			mActivity.setIsGO(false);
			mActivity.startClient(null);

			//Set the role type in the UI
			Bundle data = new Bundle(1);
			data.putString(MainActivity.ROLE_TEXT_VIEW_MESSAGE, "Client");
			Message msg = this.roleTextHandler.obtainMessage();
			msg.setData(data);
			this.roleTextHandler.sendMessage(msg);
		}
	}


	private void processGroupInfo(WifiP2pGroup newGroup) {
		// 1. Update our client list
		Collection<WifiP2pClient> ourClientList = contentDualTable.getClients();
		boolean found;
		for (WifiP2pClient client : ourClientList) {
			found = false;
			for (WifiP2pDevice device : newGroup.getClientList()) {
				if (client.getP2pMacAddress() == device.deviceAddress) {
					found = true;
					break;
				}
			}
			if(!found){
				//TODO rimuovere client
			}
		}

		// 2. Decide whether to tell the p2p client to broadcast the service
		if (oldGroup != null && oldGroup.isGroupOwner()
				&& newGroup.isGroupOwner()) {
			boolean tell = false;
			for (WifiP2pDevice newDevice : newGroup.getClientList()) {
				switch (newDevice.deviceAddress.charAt(1)) {
				case '2':
				case '6':
				case 'a':
				case 'e':
					break;
				default:
					// This is a legacy client
					// Check if this device is already in the client list
					if (!oldGroup.getClientList().contains(newDevice))
						tell = true;
					break;
				}
				if (tell) {
                    // If there is a new joined legacy client, send advertise notification, .
                    // But why only when there is a new legacy client joining???
                    // Because if there is no legacy client, there is no meaning sending advertise notification
					mActivity.getContentRoutingLayer().sendElectingClientNotification();
					break;
				}
			}
		}
		oldGroup = new WifiP2pGroup(newGroup);
	}




	public InetAddress getP2pAddress() {
		return myP2pAddress;
	}

	public String getP2pMacAddress() {
		return myP2pMacAddress;
	}

}
