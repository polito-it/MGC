
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientThread extends Thread {
	private MainActivity mActivity;
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private byte[] buf;

	public ClientThread(MainActivity mActivity) {
		super();
		this.mActivity = mActivity;
		this.buf = new byte[512];

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();

		while (true) {
			try {
				if (socket == null) {
					socket = new Socket();
					try {
						socket.setReuseAddress(true);
						socket.bind(null);
						socket.connect(new InetSocketAddress("192.168.49.129",
								MainActivity.WIFI_SERVER_PORT));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				os = socket.getOutputStream();
				is = socket.getInputStream();
				String test = "Hello wifi ...> <... I'm AP";
				os.write(test.getBytes());
				is.read(buf);
				// test = buf.toString();
				test = new String(buf);
				String a = test;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
