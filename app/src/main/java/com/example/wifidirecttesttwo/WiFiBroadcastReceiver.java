
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class WiFiBroadcastReceiver extends BroadcastReceiver {

	private MainActivity activity;
	private WifiManager man;
	//private ContentRoutingDualTable dualTable;
	private Handler roleTextHandler;
	

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			// Test if the wifi is used to connect to another GO to be as a
			// Legacy Client
			if (networkInfo.isConnected()) {
				
				String ip = SysInfoChanged.intToIpAddr(man.getConnectionInfo()
						.getIpAddress());
				if (ip.contains("192.168.49")){
					activity.setWiFiAddress(ip);
					activity.setIsLegacyClient(true);
					activity.getContentRoutingLayer().startNotificationQueue();
                    // update role
                    this.roleTextHandler = activity.getRoleTextHandler();
                    Bundle data = new Bundle(1);
                    data.putString(MainActivity.ROLE_TEXT_VIEW_MESSAGE, "Group Owner + Legacy");
                    Message msg = this.roleTextHandler.obtainMessage();
                    msg.setData(data);
                    this.roleTextHandler.sendMessage(msg);
				}
				else
					activity.setIsLegacyClient(false);
			}
		}

	}
	
	public WiFiBroadcastReceiver(MainActivity activity, WifiManager man) {
		super();
		this.activity = activity;
		this.man = man;
	}
}
