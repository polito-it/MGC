
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
import java.io.OutputStream;

import android.view.View;
import android.widget.Button;

public class WriteFile {
	private MainActivity activity;
	private String strWrite = "test+++";

	public WriteFile(MainActivity activity) {
		super();
		this.activity = activity;
	}

	public WriteFile(MainActivity activity, String strWrite) {
		super();
		this.activity = activity;
		this.strWrite = strWrite;
	}

	public String WriteFileToSD() {
		File path = activity.getExternalFilesDir(null);
		File file = new File(path, "info.txt");
		path.mkdirs();
		strWrite = new SystemInfoJson(activity).getJsonString()
				+ "***This is a test!";

		OutputStream os;
		try {
			os = new FileOutputStream(file);
			os.write(strWrite.getBytes());
			os.close();
			//Toast.makeText(activity, "Written", Toast.LENGTH_SHORT).show();
			/* --- Not this version
			Button btn = (Button) activity.findViewById(R.id.btn_openTextFile);
			btn.setVisibility(View.VISIBLE);
			*/
			activity.jsonFile = file;
			return file.getAbsolutePath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
