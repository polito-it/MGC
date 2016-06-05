
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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.TextView;

public class DisplayGroupInfoActivity extends Activity {
	
	public final static String EXTRA_MESSAGE_GROUPPWD = "com.example.wifitwo.MESSAGE_GROUPPWD";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_group_info);
		
		Intent intent = getIntent();
		//Get interface name and isGO from the intent
		String interfaceName = intent.getStringExtra(MainActivity.EXTRA_MESSAGE_GROUPINTERFACE);
		String strGO = "Group Owner: ";
		strGO += intent.getBooleanExtra(MainActivity.EXTRA_MESSAGE_GROUPOWNER, false)?"Yes":"No";
		TextView tvInterfaceName = (TextView) findViewById(R.id.group_interface_name);
		tvInterfaceName.setText(interfaceName);
		TextView tvGO = (TextView) findViewById(R.id.group_owner);
		tvGO.setText(strGO);
		//Get interface ip
		try {
			String myIP = "unknown";
			NetworkInterface nif = NetworkInterface.getByName(interfaceName);
			for (Enumeration<InetAddress> enumIpAddr = nif.getInetAddresses(); enumIpAddr
					.hasMoreElements();) {
				InetAddress inetAddress = enumIpAddr.nextElement();
				if (inetAddress.getAddress().length == 4) {
					myIP = inetAddress.getHostAddress().toString();
				}
			}
			TextView tvMyIP = (TextView) findViewById(R.id.my_ip);
			tvMyIP.setText(myIP);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Get the SSID
		String ssid = intent.getStringExtra(MainActivity.EXTRA_MESSAGE_GROUPSSID);
		TextView tvSSID = (TextView) findViewById(R.id.group_ssid);
		tvSSID.setText(ssid);
		//Get the passphrase
		String pwd = intent.getStringExtra(EXTRA_MESSAGE_GROUPPWD);
		TextView tvPwd = (TextView) findViewById(R.id.group_pwd);
		tvPwd.setText(pwd);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_group_info, menu);
		return true;
	}

}
