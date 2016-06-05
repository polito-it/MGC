
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

//Internal Disk Size, Availability of Blue Tooth,
//Instantiated in OnCreate() of the main activity.

import java.io.File;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

public class SysInfoClass {
	private long InternalDiskSize;
	private long availableDiskSize;
	private boolean BtAvailable;

	public SysInfoClass(Context context) {
		// ---Get the size of the internal storage
		File appDir = context.getFilesDir();
		appDir.mkdirs();
		InternalDiskSize = appDir.getTotalSpace();
		availableDiskSize = appDir.getUsableSpace();

		// ---Get the availability of Blue Tooth
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (mBluetoothAdapter == null || (!mBluetoothAdapter.isEnabled())) {
			BtAvailable = false;
		} else
			BtAvailable = true;
	}

	public long getInternalDiskSize() {
		return InternalDiskSize;
	}

	public boolean getBtAvailability() {
		return BtAvailable;
	}

	public long getAvailableDiskSize() {
		return availableDiskSize;
	}

}
