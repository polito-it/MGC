
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

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {

	private static final int SOCKET_TIMEOUT = 5000;
	public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
	public static final String EXTRAS_FILE_PATH = "file_url";
	public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
	public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

	public FileTransferService(String name) {
		super(name);
	}

	public FileTransferService() {
		super("FileTransferService");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {

		if (intent.getAction().equals(ACTION_SEND_FILE)) {
			InetSocketAddress localSocketAddress = new InetSocketAddress(intent
					.getExtras().getString(
							MainActivity.EXTRA_MESSAGE_LOCAL_ADDRESS),
					MainActivity.CLIENT_PORT);
			String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
			File file = new File(fileUri);
			String host = intent.getExtras().getString(
					EXTRAS_GROUP_OWNER_ADDRESS);
			Socket socket = new Socket();
			int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
			try {
				socket.setReuseAddress(true);
			} catch (SocketException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			try {
				socket.bind(localSocketAddress);
				Log.d("WifiTwo", "Opening client socket - ");

				socket.connect((new InetSocketAddress(host, port)),
						SOCKET_TIMEOUT);

				Log.d("WifiTwo", "Client socket - " + socket.isConnected());
				OutputStream stream = socket.getOutputStream();
				InputStream is = null;
				try {
					is = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					Log.d("WifiTwo", e.toString());
				}
				/*
				 * //Send the file name length and the file name
				 * stream.write(8); String strFileName = "info.txt"; byte
				 * fname[] = strFileName.getBytes(); stream.write(fname);
				 */
				// Send the file
				WifiDirectConnectionInfoListener.copyFile(is, stream);
				Log.d("WifiTwo", "Client: Data written");
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
}
