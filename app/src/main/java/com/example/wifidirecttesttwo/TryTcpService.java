
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

import java.io.IOException;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class TryTcpService extends IntentService {
	public static final String EXTRA_TRYTCP_WIFI_IP = "com.example.android.wifitwo.TRYTCP_WIFI_IP";
	private static final int SOCKET_TIMEOUT = 5000;
	public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.TRYTCP_SEND_FILE";
	
	public TryTcpService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public TryTcpService() {
		super("TryTcpService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (!intent.getAction().equals(ACTION_SEND_FILE)) {
			return;
		}
		InetSocketAddress localSocketAddress = new InetSocketAddress(intent
				.getExtras().getString(EXTRA_TRYTCP_WIFI_IP),
				MainActivity.CLIENT_PORT);

		String host = "192.168.49.134";
		Socket socket = new Socket();
		try {
			socket.setReuseAddress(true);
		} catch (SocketException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		try {
			socket.bind(localSocketAddress);
			Log.d("WifiTwo", "Opening client socket - ");

			socket.connect((new InetSocketAddress(host, 9000)), SOCKET_TIMEOUT);

			Log.d("WifiTwo", "Client socket - " + socket.isConnected());

		} catch (IOException e) {
			Log.e("WifiTwo", e.getMessage());
		} finally {
			if (socket != null) {
				if (socket.isConnected()) {
					try {
						socket.close();
					} catch (IOException e) {
						// Give up
						e.printStackTrace();
					}
				}
			}
		}

	}

}
