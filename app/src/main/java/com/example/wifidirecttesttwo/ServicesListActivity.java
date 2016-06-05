
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

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class ServicesListActivity extends ListActivity {

    public static final String EXTRA_SERVICES_NAME = "com.mgc.serviceslist.EXTRA_SERVICES_NAME";
    public static final String EXTRA_SERVICES_IP = "com.mgc.serviceslist.EXTRA_SERVICES_IP";
    public static final String EXTRA_REQUESTED_SERVICE_NAME = "com.mgc.serviceslist.EXTRA_REQUESTED_SERVICE_NAME";

    private ArrayList<String> serviceNames;
    private ArrayList<String> serviceIps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services_list);

        Intent intent = getIntent();
        this.serviceNames = intent.getStringArrayListExtra(EXTRA_SERVICES_NAME);
        this.serviceIps = intent.getStringArrayListExtra(EXTRA_SERVICES_IP);

        ServicesListAdapter adapter = new ServicesListAdapter(this, R.layout.row_services, this.serviceNames, this.serviceIps);
        this.setListAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent intent = new Intent();
        String serviceName = this.serviceNames.get(position);
        if(serviceName == null || serviceName.equals("")) setResult(RESULT_CANCELED);
        intent.putExtra(EXTRA_REQUESTED_SERVICE_NAME, this.serviceNames.get(position));
        setResult(RESULT_OK, intent);
        finish();
    }

    private class ServicesListAdapter extends ArrayAdapter {

        private ArrayList<String> names;
        private ArrayList<String> ips;
        private Context context;

        /**
         * @param context
         * @param textViewResourceId
         * @param
         */
        public ServicesListAdapter(Context context, int textViewResourceId,
                                   ArrayList<String> names, ArrayList<String> ips) {
            super(context, textViewResourceId, names);
            this.context = context;
            this.names = names;
            this.ips = ips;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) LayoutInflater
                        .from(context);
                v = vi.inflate(R.layout.row_services, null);
            }
            try{
                String name = names.get(position);
                String ip = ips.get(position);

                //exception not controlled on get(position)-->add
                if (name != null) {
                    TextView top = (TextView) v.findViewById(R.id.service_name);
                    TextView bottom = (TextView) v.findViewById(R.id.service_ip);
                    if (top != null) {
                        top.setText(name);
                    }
                    if (bottom != null) {
                        bottom.setText(ip);
                    }
                }
            }catch(IndexOutOfBoundsException e){
                e.printStackTrace();
            }

            return v;
        }


    }

    /* --- Useless
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_services_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
}
