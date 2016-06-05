
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
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import com.example.wifidirecttesttwo.ContentRoutingLayer.ContentRoutingInterface;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity implements PeerListListener, ContentRoutingInterface {

    // For UI
    private EditText etGoIntent = null;
	// For timer
	private Timer mTimer;
	private TimerTask mTimerTask;
	private Handler mHandler;
	// private TextView tvSec;
	private static int timerDelay = 100;
	private static int timerPeriod = 100;
	private long countSec = 0;
	private String timeFileName;

	// For remove group periodically

	private Handler rmgHandler;
	// NOUSE private final static int MSG_REMOVE_GROUP = 10;
	private static final int UPDATE_TEXTVIEW = 0;

	// For intent
	public final static String EXTRA_MESSAGE_FILE = "com.example.wifitwo.MESSAGE_FILE";
	public final static String EXTRA_MESSAGE_GROUPINTERFACE = "com.example.wifitwo.MESSAGE_GROUPINTERFACE";
	public final static String EXTRA_MESSAGE_GROUPOWNER = "com.example.wifitwo.MESSAGE_GROUPOWNER";
	public final static String EXTRA_MESSAGE_GROUPSSID = "com.example.wifitwo.MESSAGE_GROUPSSID";
	public final static int SERVER_PORT = 9000;
	public final static int CLIENT_PORT = 12345;
	public final static int WIFI_SERVER_PORT = 8000;

	// For activity result
	public static final int AR_REQUESTED_SERVICE_NAME = 101;
	public final static int AR_REQUEST_CODE_SET_DEVICE_NAME = 139;

	// public final static int WIFI_SERVER_PORT = 6666;

	// For getting the system info
	public float batteryPct;
	public SysInfoClass sysInfoGether;
	public SysInfoChanged sysInfoChanged;
	public LocationManager locationManager;
	private LocationListener locationListener;
	private String providerName;
	private String jsonFileName;
	public File jsonFile;
	public String peerFileName;
	public JSONObject jsonPeer;

	// For wifi direct
	private boolean isWifiP2pEnabled = false;

	private WifiP2pManager mManager;
	private Channel mChannel;
	private WiFiDirectBroadcastReceiver mReceiver;

	private WifiDirectConnectionInfoListener wdConnInfoListener;
	private IntentFilter intentFilter = new IntentFilter();
	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
	private Map <String, WifiP2pDevice> groupClientPeers = new HashMap<String, WifiP2pDevice>();

	private boolean isGO = false;
	private boolean isElectedClient = false;
	private boolean isLegacyClient = false;

	public final static String EXTRA_MESSAGE_LOCAL_ADDRESS = "com.example.wifitwo.MESSAGE_LOCAL_ADDRESS";

	// For wifi
	private boolean stopWifiServer = false;

	private boolean stopWifiClient = false;
	private boolean stopP2PServer = false;


	private ServerThread serverThread;
	private ClientThread clientThread;
	private P2PServerThread p2pServerThread;

	// For wifi udp
	private TextView udpTextView;
	private TextView roleTextView;
    private TextView ipTextView;
	private TextView numberServicesTextView;
    private TextView statusTextView;
    private BroadcastReceiver sentPacketsReceiver;

	//initialize handlers
	private final Handler udpTextHandler = new UdpTextHandler(this);
	private final Handler roleTextHandler = new RoleTextHandler(this);
    private final Handler ipTextHandler = new IpTextHandler(this);
	private final Handler numberServicesTextHandler = new NumberServicesTextHandler(this);
    private final Handler statusTextHandler = new StatusTextHandler(this);
	
	
	public final static String PACKET_COUNT = "com.example.wifitwo.PACKET_COUNT";
	public final static String T1_TIME = "com.example.wifitwo.T1_TIME";
	public final static String T2_TIME = "com.example.wifitwo.T2_TIME";
	public final static String T3_TIME = "com.example.wifitwo.T3_TIME";

	public final static String UDP_MESSAGE = "com.example.wifitwo.UDP_MESSAGE";
	public final static String NUMBER_DISCOVERED_SERVICES = "com.example.wifitwo.NUMBER_DISCOVERED_SERVICES";
    public final static String STATUS_MESSAGE = "com.example.wifitwo.STATUS_MESSAGE";
    public final static String STATUS_IF_TOAST = "com.example.wifitwo.STATUS_IF_TOAST";
	public final static String HANDLER_UDP_MESSAGE_RECVED = "com.example.wifitwo.HANDLER_UDP_MESSAGE_RECVED";
	public final static String ROLE_TEXT_VIEW_MESSAGE = "com.example.wifitwo.ROLE_TEXT_VIEW_MESSAGE";
    public final static String IP_TEXT_VIEW_MESSAGE = "com.example.wifitwo.IP_TEXT_VIEW_MESSAGE";
	public final static int MESSAGE_UDP_SERVER = 101;
	public final static int UDP_PORT = 12222;
	private UDPServer udpServerThread;
	private int udpn = 65531;


	// For preferencs
	private TextView tvMyname;
	private String myname;

	// Service Discovering
	public final static String SERVICE_TYPE = "_presence._tcp";
	public final static String SERVICE_NAME = "_test";
	private ContentRoutingLayer crLayer;
	private GroupRoutingLayer grLayer;

	// client list

	// For service advertisement
	private WiFiBroadcastReceiver wifibReceiver;
	private WifiManager wifiManager;
	private IntentFilter wifiIntentFilter;
	private EditText etNotifIP; // ------ DYF ------ debug


	private int ipRadioGroupChoice;


	//############################################################
	// ANDROID LIFECYCLE EVENTS
	//############################################################

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Set default value
		PreferenceManager.setDefaultValues(getApplicationContext(),
				R.xml.preferences, false);


		this.udpTextView = (TextView) findViewById(R.id.udp_show_msg);
		this.roleTextView = (TextView) findViewById(R.id.roleTextView);
        this.ipTextView = (TextView) findViewById(R.id.ipTextView);

		this.numberServicesTextView = (TextView) findViewById(R.id.number_discovered_services_text);
        this.statusTextView = (TextView) findViewById(R.id.status);

        this.sentPacketsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(UDPFileService.BROADCAST_ACTION_SENT_PACKETS.equals(action)){
                    updateStatusTextView("Sent: " + intent.getIntExtra(UDPFileService.EXTRA_SENT_PACKETS, -1), false);
                } else if (UDPFileService.BROADCAST_ACTION_SENT_RESULTS.equals(action)){
                    int nPackets = intent.getIntExtra(UDPFileService.EXTRA_SENT_PACKETS, -1);
                    double throughput = intent.getDoubleExtra(UDPFileService.EXTRA_SENT_THROUGHPUT, 11.2);
                    updateStatusTextView("Total sent packets: " + nPackets + " | Offered load: " + throughput + " Mbit/s ", false);
                }
            }
        };

		jsonFileName = null;
		/*---Unused
		Button btn = (Button) findViewById(R.id.btn_openTextFile);
		btn.setVisibility(View.INVISIBLE);
		((Button) findViewById(R.id.btn_open_peer_text))
		.setVisibility(View.GONE);
		*/
		batteryPct = 0;
		// ---For getting the location
		this.locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setSpeedRequired(true);
		criteria.setBearingRequired(true);
		criteria.setCostAllowed(false);
		providerName = this.locationManager.getBestProvider(criteria, true);
		locationListener = new LocationListener() {
			@SuppressLint("NewApi")
			// !!!
			@Override
			public void onLocationChanged(Location location) {
				sysInfoChanged.SetLocation(location);
			}

			@Override
			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
		};

		this.sysInfoGether = new SysInfoClass(this);

		mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(this, getMainLooper(), null);

		//this.crLayer = new ContentRoutingLayer(this, mChannel, mManager);
        this.crLayer = new ContentRoutingLayer(this);
		this.grLayer = new GroupRoutingLayer(this, this.crLayer);
		this.crLayer.setGroupRoutingLayer(this.grLayer);

		mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this, this.crLayer.getContentRoutingDualTable());

		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter
		.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter
		.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		// ---Set the list adapter
		this.setListAdapter(new WiFiPeerListAdapter(getApplicationContext(),
				R.layout.row_devices, peers));
		// ---Set the connection info listener
		wdConnInfoListener = new WifiDirectConnectionInfoListener(this);

		// WifiBroadcastReceiver
		wifiManager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		wifibReceiver = new WiFiBroadcastReceiver(this, wifiManager);
		wifiIntentFilter = new IntentFilter();
		wifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		wifiIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

		// ------ DYF ------ debug

	}


	@Override
	protected void onStart() {
		//
		super.onStart();

		// Update myname
		UpdateMyNameTextview();
		// --- WifiP2P receiver
		registerReceiver(mReceiver, intentFilter);
		registerReceiver(wifibReceiver, wifiIntentFilter);


		// --- Get battery level
		Intent batteryIntent = registerReceiver(null, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		int level = batteryIntent.getIntExtra("level", 0);
		int scale = batteryIntent.getIntExtra("scale", 100);
		batteryPct = level / (float) scale;

		// ---Get the location, register the location listener
		// ******
		if (providerName != null)
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 600000, 1, locationListener);

		this.sysInfoChanged = new SysInfoChanged(this);
		// ---Write json file to SD card
		// File path = getExternalFilesDir(null);
		((TextView) findViewById(R.id.p2p_mac_text)).setText("P2P MAC: " + sysInfoChanged
				.getP2pMac());
		jsonFileName = new WriteFile(this).WriteFileToSD();
		if (jsonFileName != null)
            Log.i("WifiTwo", "Write json file OK");
			//Toast.makeText(getApplicationContext(), "WriteOk",Toast.LENGTH_SHORT).show();
	}





	@Override
	protected void onResume() {
		super.onResume();
		StartUDPServer(null);
        IntentFilter sentPacketIntentFilter = new IntentFilter();
        sentPacketIntentFilter.addAction(UDPFileService.BROADCAST_ACTION_SENT_PACKETS);
        sentPacketIntentFilter.addAction(UDPFileService.BROADCAST_ACTION_SENT_RESULTS);
        registerReceiver(this.sentPacketsReceiver, sentPacketIntentFilter);
	}

	protected void onStop(){
		super.onStop();
		unregisterReceiver(mReceiver);
		unregisterReceiver(wifibReceiver);
        unregisterReceiver(this.sentPacketsReceiver);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}





	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                //startActivity(intent);
                startActivityForResult(intent, AR_REQUEST_CODE_SET_DEVICE_NAME);
                return true;
            case R.id.action_persistent:
                Intent intentPersistent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivityForResult(intentPersistent, AR_REQUESTED_SERVICE_NAME);
                return true;
            case R.id.action_services:
                this.showServicesList(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (serverThread != null) {
			stopWifiServer = true;
			serverThread.interrupt();
		}
		if (clientThread != null) {
			stopWifiClient = true;
			clientThread.interrupt();
		}
		if (p2pServerThread != null) {
			stopP2PServer = true;
			p2pServerThread.interrupt();
		}
		if (udpServerThread != null) {
			stopWifiServer = true;
			udpServerThread.interrupt();
		}
	}






	//####################################################################
	// UI EVENTS
	//####################################################################


	public void openTextFile(View view) {
		if (this.jsonFileName != null) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(this.jsonFileName)),
					"text/plain");
			PackageManager packageManager = getPackageManager();
			List<ResolveInfo> activities = packageManager
					.queryIntentActivities(intent, 0);
			if (activities.size() > 0) {
				startActivity(intent);
			}
		}
	}

	public void openPeerText(View view) {
		if (this.peerFileName != null) {
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" + peerFileName),
					"text/plain");
			startActivity(intent);
		}
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case AR_REQUEST_CODE_SET_DEVICE_NAME:
                break;
			// Request a services
			case AR_REQUESTED_SERVICE_NAME:
				if(resultCode != RESULT_OK) break;
                this.crLayer.resetFileServer();
				String serviceName = data.getStringExtra(ServicesListActivity.EXTRA_REQUESTED_SERVICE_NAME);
				EditText et = (EditText)findViewById(R.id.request_service_text);
				et.setText(serviceName);
				this.crLayer.sendApplicationRequest((this.myname == null ? "test" : this.myname), serviceName, null);
                Toast.makeText(this, "Service <" + serviceName + "> requested", Toast.LENGTH_LONG).show();
				break;
			default:
				break;
        }
    }

    public void DiscoverPeers(View view) {

		if (!this.isWifiP2pEnabled)
			Toast.makeText(MainActivity.this, R.string.p2p_off_warning,
					Toast.LENGTH_SHORT).show();
		else {
			this.mManager.discoverPeers(mChannel, new ActionListener() {
				@Override
				public void onSuccess() {
					Toast.makeText(getApplicationContext(),
							"Discovery Initiated", Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onFailure(int arg0) {
					Toast.makeText(getApplicationContext(), "Discovery failed",
							Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

    public void showServicesList(View view){
        Intent intent = new Intent(this, ServicesListActivity.class);
        ContentRoutingDualTable.ServicesSet servicesSet = this.crLayer.getContentRoutingDualTable().getServicesNames();
        intent.putExtra(ServicesListActivity.EXTRA_SERVICES_NAME, servicesSet.getNames());
        intent.putExtra(ServicesListActivity.EXTRA_SERVICES_IP, servicesSet.getIps());
        startActivityForResult(intent, AR_REQUESTED_SERVICE_NAME);
    }



	// Start client on p2p
    // Sending json file to server
	public void startClient(View view){

		String fileName = this.jsonFile.getAbsolutePath();
		//TextView statusText = (TextView) this.findViewById(R.id.show_text);
		//statusText.setText("Sending: " + fileName);
		Log.d("WifiTwo", "Intent----------- " + fileName);
		Intent serviceIntent = new Intent(this, FileTransferService.class);
		serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
		serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, fileName);
		try {
			serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
					wdConnInfoListener.getGroupOwnerAddress().getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT,
				SERVER_PORT);
		serviceIntent.putExtra(EXTRA_MESSAGE_LOCAL_ADDRESS,
				mReceiver.getP2pAddress().getHostAddress());
		this.startService(serviceIntent);
	}





	public void tryTCP(View view)
	{
		Intent serviceIntent = new Intent(this, TryTcpService.class);
		serviceIntent.setAction(TryTcpService.ACTION_SEND_FILE);
		serviceIntent.putExtra(TryTcpService.EXTRA_TRYTCP_WIFI_IP, sysInfoChanged.getWifiIpAddr());
		this.startService(serviceIntent);
	}





	// Start client using wifi connection(just the port is different from the
	// p2p one
	public void startWifiClient(View view) throws UnknownHostException {
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
		String wifiIpAddr = "";
		if (wifiInfo != null && dhcpInfo != null) {
			wifiIpAddr = SysInfoChanged.intToIpAddr(wifiInfo.getIpAddress());
		}

		String serverAddress = Inet4Address.getByName("192.168.49.1")
				.getHostAddress();
		String fileName = this.jsonFile.getAbsolutePath();
		//TextView statusText = (TextView) this.findViewById(R.id.show_text);
		//statusText.setText("Sending: " + fileName);
		Log.d("WifiTwo", "Intent----------- " + fileName);
		Intent serviceIntent = new Intent(this, FileTransferService.class);
		serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
		serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, fileName);
		serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
				serverAddress);
		serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT,
				SERVER_PORT);
		serviceIntent.putExtra(EXTRA_MESSAGE_LOCAL_ADDRESS, wifiIpAddr);
		this.startService(serviceIntent);
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList deviceList) {
		peers.clear();
		peers.addAll(deviceList.getDeviceList());
		WiFiPeerListAdapter adapter = ((WiFiPeerListAdapter) getListAdapter());
		adapter.notifyDataSetChanged();

		//		ListView mListView = (ListView) getListView();
		//		mListView.setOnItemClickListener()
		//		LayoutParams lp = (LayoutParams) mListView.getLayoutParams();
		//	    lp.height = 20*peers.size();
		//	    mListView.setLayoutParams(lp);

		if (peers.size() == 0) {
			Toast.makeText(getApplicationContext(), "No peers discovered",
					Toast.LENGTH_SHORT).show();
			return;
		}

	}

	/**
	 * Array adapter for ListFragment that maintains WifiP2pDevice list.
	 */
	private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

		private List<WifiP2pDevice> items;
		private Context context;

		/**
		 * @param context
		 * @param textViewResourceId
		 * @param objects
		 */
		public WiFiPeerListAdapter(Context context, int textViewResourceId,
				List<WifiP2pDevice> objects) {
			super(context, textViewResourceId, objects);
			items = objects;
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) LayoutInflater
						.from(context);
				v = vi.inflate(R.layout.row_devices, null);
			}
			try{
				WifiP2pDevice device = items.get(position);

				//exception not controlled on get(position)-->add
				if (device != null) {
					TextView top = (TextView) v.findViewById(R.id.device_name);
					TextView bottom = (TextView) v
							.findViewById(R.id.device_details);
					TextView tvAddr = (TextView) v.findViewById(R.id.device_mac);
					if (top != null) {
						top.setText(device.deviceName);
					}
					if (bottom != null) {
						bottom.setText(getDeviceStatus(device.status));
					}
					if (tvAddr != null) {
						tvAddr.setText(device.deviceAddress);
					}
				}
			}catch(IndexOutOfBoundsException e){
				e.printStackTrace();
			}

			return v;

		}
	}

	// ---Get the device status
	private static String getDeviceStatus(int deviceStatus) {
		switch (deviceStatus) {
		case WifiP2pDevice.AVAILABLE:
			return "Available";
		case WifiP2pDevice.INVITED:
			return "Invited";
		case WifiP2pDevice.CONNECTED:
			return "Connected";
		case WifiP2pDevice.FAILED:
			return "Failed";
		case WifiP2pDevice.UNAVAILABLE:
			return "Unavailable";
		default:
			return "Unknown";

		}
	}




	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// ---Get the device
		WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(
				position);
		if (device.status == WifiP2pDevice.INVITED)
			mManager.cancelConnect(mChannel, new ActionListener() {
				@Override
				public void onSuccess() {
				}

				@Override
				public void onFailure(int arg0) {
				}
			});
		else if (device.status == WifiP2pDevice.AVAILABLE) {
			// Connect to the peer device
			WifiP2pConfig config = new WifiP2pConfig();
			// Get the GO intention
			EditText editText = (EditText) findViewById(R.id.go_intent);
			String strGoIntent = editText.getText().toString();
			int goIntent;
			try {
				goIntent = Integer.parseInt(strGoIntent);
			} catch (NumberFormatException e) {
				goIntent = -1;
			}
			// Set the GO intention
			if (goIntent >= 0)
				config.groupOwnerIntent = goIntent;
			config.deviceAddress = device.deviceAddress;
			config.wps.setup = WpsInfo.PBC;
			// Connect
			mManager.connect(mChannel, config, new ActionListener() {
				@Override
				public void onFailure(int reason) {
					Toast.makeText(MainActivity.this, "Connection failed.",
							Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onSuccess() {
					Toast.makeText(MainActivity.this, "Connect initiated.",
							Toast.LENGTH_SHORT).show();
				}
			});
		} else if (device.status == WifiP2pDevice.CONNECTED){
			mManager.removeGroup(mChannel, new ActionListener() {
				@Override
				public void onFailure(int reason) {
					Toast.makeText(MainActivity.this, "removeGroup() failed.",
							Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onSuccess() {
					Toast.makeText(MainActivity.this, "removeGroup() initiated.",
							Toast.LENGTH_SHORT).show();
				}
			});
		}

	}

    // --- AREA: Buttons ---

    public void Disconnect(View view) {
        mManager.removeGroup(mChannel, new ActionListener() {
            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "removeGroup() failed.",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "removeGroup() initiated.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }



	public void ShowGroupInfo(View view) {
		mManager.requestGroupInfo(mChannel, new GroupInfoListener() {
			@Override
			public void onGroupInfoAvailable(WifiP2pGroup group) {
				Toast.makeText(MainActivity.this, "Please wait...",
						Toast.LENGTH_SHORT).show();
				if (group == null)
					return;
				Intent intent = new Intent(MainActivity.this,
						DisplayGroupInfoActivity.class);
				intent.putExtra(MainActivity.EXTRA_MESSAGE_GROUPINTERFACE,
						group.getInterface());
				intent.putExtra(MainActivity.EXTRA_MESSAGE_GROUPOWNER,
						group.isGroupOwner());
				intent.putExtra(MainActivity.EXTRA_MESSAGE_GROUPSSID,
						group.getNetworkName());
				intent.putExtra(
						DisplayGroupInfoActivity.EXTRA_MESSAGE_GROUPPWD,
						group.getPassphrase());
				startActivity(intent);
			}
		});
	}


    // NOUSE
	// Connect to a peer without having found it
    // --- Not used in v2
	public void ConnectWithoutDiscovery(View view) {
		WifiP2pConfig config = new WifiP2pConfig();
		String LgMAC = "42:b0:fa:c9:db:2c";
		String NexusMAC = "32:85:a9:5f:af:5d";
		String peerMAC = (this.sysInfoChanged.getP2pMac().equals(LgMAC)) ? NexusMAC
				: LgMAC;
		config.deviceAddress = peerMAC;
		// Connect
		mManager.connect(mChannel, config, new ActionListener() {
			@Override
			public void onFailure(int reason) {
				Toast.makeText(MainActivity.this, "Connection failed.",
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onSuccess() {
				Toast.makeText(MainActivity.this, "Connect initiated.",
						Toast.LENGTH_SHORT).show();
			}
		});
		// Start the Timer
		if (mTimer == null)
			mTimer = new Timer();
		if (mTimerTask == null)
			mTimerTask = new TimerTask() {
			@Override
			public void run() {
				countSec++;
				Message message = new Message();
				message.what = UPDATE_TEXTVIEW;
				mHandler.sendMessage(message);
			}
		};
		if (mTimer != null && mTimerTask != null)
			mTimer.schedule(mTimerTask, timerDelay, timerPeriod);
	}




    // NOUSE
	// Stop the timer
	public void stopTimer(View view) {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
		countSec = 0;
	}




	// Create a group
	public void myCreateGroup(View view) {
		mManager.createGroup(mChannel, new ActionListener() {

			@Override
			public void onSuccess() {
				Toast.makeText(MainActivity.this, "Create initiated.",
						Toast.LENGTH_SHORT).show();

			}

			@Override
			public void onFailure(int reason) {
				Toast.makeText(MainActivity.this, "Create failed.",
						Toast.LENGTH_SHORT).show();

			}
		});
	}



	// Get counter
	public long getCountSec() {
		return countSec;
	}



	// Open the file which records the connection time
	public void openTimeFile(View view) {
		if (this.timeFileName != null) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(this.timeFileName)),
					"text/plain");
			PackageManager packageManager = getPackageManager();
			List<ResolveInfo> activities = packageManager
					.queryIntentActivities(intent, 0);
			if (activities.size() > 0) {
				startActivity(intent);
			}
		}
	}



	public void RequestButtonClick(View view){
		if (this.crLayer==null)
			return;
		crLayer.sendApplicationRequest("io", MainActivity.SERVICE_NAME + "." + MainActivity.SERVICE_TYPE, "prova");
	}



	// Start the server using Wifi
	public void StartWifiServer(View view) {
		// For wifi
		if (serverThread != null) {
			serverThread = new ServerThread(this);
			serverThread.start();
		}
	}




	public void StartAPClient(View view) {
		if (clientThread == null) {
			clientThread = new ClientThread(this);
			clientThread.start();
		}
	}




	public void StartP2PServer(View view) {
		if (p2pServerThread == null) {
			p2pServerThread = new P2PServerThread(this, this.crLayer.getContentRoutingDualTable()); // TCP Server
			p2pServerThread.start();
		}
	}




	public void StartUDPServer(View view) {
		if (udpServerThread == null) {
			udpServerThread = new UDPServer(this);
			udpServerThread.start();
		}
	}





	public void StartUDPClient(View view) {
		EditText etMessage = (EditText) findViewById(R.id.udp_send_msg);
		String data = etMessage.getText().toString(); // data is the destination
		// name
		// Check where the message is destined

		String destIP;

		destIP = "192.168.49.255";

		if (data.length() > 0) {
			udpn--;
			if(udpn<0)
				udpn = 65531;
			Intent intent = new Intent(this, UDPClientService.class);
			intent.setAction(UDPClientService.ACTION_SEND_UDP_MSG);
			intent.putExtra(UDPClientService.EXTRA_UDP_SRC_NAME, myname);
			intent.putExtra(UDPClientService.EXTRA_UDP_DEST_NAME, data);
			intent.putExtra(UDPClientService.EXTRA_UDP_DEST_IP, destIP);
			intent.putExtra(UDPClientService.EXTRA_UDP_PACKET_NO, udpn);
			this.startService(intent);

			if(this.isLegacyClient){
				//send also to elected client
				destIP = getContentRoutingLayer().getElectedClientAddress().getHostAddress();
				Intent intentec = new Intent(this, UDPClientService.class);
				intentec.setAction(UDPClientService.ACTION_SEND_UDP_MSG);
				intentec.putExtra(UDPClientService.EXTRA_UDP_SRC_NAME, myname);
				intentec.putExtra(UDPClientService.EXTRA_UDP_DEST_NAME, data);
				intentec.putExtra(UDPClientService.EXTRA_UDP_DEST_IP, destIP);
				intentec.putExtra(UDPClientService.EXTRA_UDP_PACKET_NO, udpn);
				this.startService(intentec);
			}
		}

	}

	//	// ------ DYF ------ debug
	//	public void startSendAdNotification(View view)
	//	{
	//		crLayer.sendAdvertiseNotification("192.168.49." + etNotifIP.getText().toString());
	//	}

	// ------ DYF ------ debug
	public String getNotifIP()
	{
		return "192.168.49." + etNotifIP.getText().toString();
	}




	// Update myname textview: device name!
	private void UpdateMyNameTextview() {
		// Get myname
		tvMyname = (TextView) findViewById(R.id.myname);
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		if(sharedPref.getString(SettingsActivity.PREF_DEVICENAME, "").equals("")){
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString(SettingsActivity.PREF_DEVICENAME, "Device-"+String.valueOf((new Random()).nextInt(30)));
			editor.commit();
		}
		myname = sharedPref.getString(SettingsActivity.PREF_DEVICENAME, "device");
		tvMyname.setText(myname);
        // TODO update device name
        UpdateDeviceName(myname);
	}

    private boolean UpdateDeviceName(String new_name)
    {
        try {
            Method m = this.getWifiP2pManager().getClass().getMethod(
                    "setDeviceName",
                    new Class[] { WifiP2pManager.Channel.class, String.class,
                            WifiP2pManager.ActionListener.class });

            m.invoke(this.getWifiP2pManager(), this.getChannel(), new_name, new WifiP2pManager.ActionListener() {
                public void onSuccess() {
                    //Code for Success in changing name
                }

                public void onFailure(int reason) {
                    //Code to be done while name change Fails
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /* ---Unused
	public void setButtonVisible() {
		((Button) findViewById(R.id.btn_open_peer_text))
		.setVisibility(View.VISIBLE);
	}
	*/

    // Buttons
    public void onResetNumServices(View view){

    }

	//Test
	public void sendAdRequest(View view)
	{
		this.getContentRoutingLayer().sendAdvertiseListRequest();
	}

	
	public void onModifyFileName(View view){
		String fileName = ((EditText)findViewById(R.id.file_name_text)).getText().toString();
		grLayer.setFileName(fileName);
        Toast.makeText(this, "File name modified", Toast.LENGTH_SHORT).show();
	}


	public void onAddServiceClick(View view){
		String s = ((EditText)findViewById(R.id.add_service_text)).getText().toString();
		crLayer.addOwnService(s);
	}


	public void onRequestServiceClick(View view){
		String s = ((EditText)findViewById(R.id.request_service_text)).getText().toString();
		this.crLayer.sendApplicationRequest((this.myname==null ? "test" : this.myname), s, null);
	}
	
	public void onStartAdvertisementTest(View view) {
		String s = ((EditText)findViewById(R.id.sat_text)).getText().toString();
		int number = 1;
		try{
			number = Integer.parseInt(s);
		}
		catch(NumberFormatException nfe){}
		
		AdvertisementTest advTest = new AdvertisementTest(number, this.crLayer, this);
		advTest.start();
	}

	

	public void onIpRadioButtonClicked(View view){
		boolean checked = ((RadioButton) view).isChecked();
		
		if(!checked)
			return;
		
		switch(view.getId()) {
        case R.id.rg_bind_ip_p2p:
        	this.ipRadioGroupChoice = R.id.rg_bind_ip_p2p;
            break;
        case R.id.rg_bind_ip_lo:
        	this.ipRadioGroupChoice = R.id.rg_bind_ip_lo;
            break;
        case R.id.rg_bind_ip_none:
        	this.ipRadioGroupChoice = R.id.rg_bind_ip_none;
            break;
        case R.id.rg_bind_ip_wifi:
        	this.ipRadioGroupChoice = R.id.rg_bind_ip_wifi;
            break;
         
		}
	}
	
	
	public void onResetFileServerClick(View view){
		crLayer.resetFileServer();
	}



	//###########################################################
	// OTHER METHODS
	//###########################################################

	public void setGroupFormed(boolean b){
		this.crLayer.setGroupFormed(b);
	}


	public void setWiFiAddress(String ip){
		try {
			this.crLayer.setWiFiAddress(InetAddress.getByName(ip));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}



	//############################################################
	// INNER CLASSES
	//############################################################

	private static class RoleTextHandler extends Handler {
		private final WeakReference<MainActivity> mActivity;

		public RoleTextHandler(MainActivity activity) {
			mActivity = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			MainActivity activity = mActivity.get();
			if (activity != null) {
				((TextView)activity.getRoleTextView()).setText(msg.getData().getString(MainActivity.ROLE_TEXT_VIEW_MESSAGE));
			}
		}
	}

    private static class IpTextHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public IpTextHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = mActivity.get();
            if (activity != null) {
                ((TextView)activity.getIpTextView()).setText(msg.getData().getString(MainActivity.IP_TEXT_VIEW_MESSAGE));
            }
        }
    }

	private static class UdpTextHandler extends Handler {
		private final WeakReference<MainActivity> mActivity;

		public UdpTextHandler(MainActivity activity) {
			mActivity = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			MainActivity activity = mActivity.get();
			if (activity != null) {
				int count = msg.getData().getInt(MainActivity.PACKET_COUNT);
				long t1 = msg.getData().getLong(MainActivity.T1_TIME);
				long t2 = msg.getData().getLong(MainActivity.T2_TIME);
				long t3 = msg.getData().getLong(MainActivity.T3_TIME);
				String text = "Number of packets received: " + count + "\n" +
						"T1 time: " + t1 + "\n" +
						"T2 time: " + t2 + "\n" +
						"T3 time: " + t3 + "\n" ;
				((TextView)activity.getUdpTextView()).setText(text);
			}
		}

	}
	
	

	private static class NumberServicesTextHandler extends Handler {
		private final WeakReference<MainActivity> mActivity;

		public NumberServicesTextHandler(MainActivity activity) {
			mActivity = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int counter = msg.getData().getInt(MainActivity.NUMBER_DISCOVERED_SERVICES);
			MainActivity activity = mActivity.get();
			if (activity != null) {
				String serviceMessage = activity.getResources().getString(R.string.number_discovered_services_text);
				((TextView)activity.getNumberServicesTextView()).setText(serviceMessage + " " + counter);
			}
		}
	}

    private static class StatusTextHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        private StatusTextHandler(MainActivity activity) {
            this.mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = mActivity.get();
            if(activity != null){
                String status = msg.getData().getString(MainActivity.STATUS_MESSAGE);
                ((TextView) activity.getStatusTextView()).setText(status);
                if(msg.getData().getBoolean(STATUS_IF_TOAST, false))
                    Toast.makeText(activity, status, Toast.LENGTH_LONG).show();
            }
        }
    }


	//###############################################
	// GET & SET METHODS
	//###############################################



	public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
		this.isWifiP2pEnabled = isWifiP2pEnabled;
	}

	public void setTimeFileName(String filename) {
		this.timeFileName = filename;
	}

	public String getMyName() {
		return myname;
	}

	public boolean isGO(){
		return this.isGO;
	}

	public void setIsGO(boolean b){
		this.isGO=b;
	}

	public boolean isElectedClient(){
		return this.isElectedClient;
	}

	public void setIsElectedClient(boolean b){
		this.isElectedClient = b;
	}

	public boolean isLegacyClient() {
		return isLegacyClient;
	}

	public void setIsLegacyClient(boolean isLegacyClient) {
		this.isLegacyClient = isLegacyClient;
	}

	public Channel getChannel(){
		return mChannel;
	}

	public WifiP2pManager getWifiP2pManager(){
		return mManager;
	}

	public String getWlanAddress(){
		// method that retrieve the IP address of the wlan interface
		WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiinfo = wifiManager.getConnectionInfo();

		int ipIntAddress = wifiinfo.getIpAddress();
		String ipStringAddress = SysInfoChanged.intToIpAddr(ipIntAddress);

		return ipStringAddress;
	}

	public ContentRoutingLayer getContentRoutingLayer(){
		return this.crLayer;
	}

	public GroupRoutingLayer getGroupRoutingLayer(){
		return this.grLayer;
	}

	public boolean isStopP2PServer() {
		return stopP2PServer;
	}

	public boolean isStopWifiServer() {
		return stopWifiServer;
	}


	public Handler getUdpTextHandler() {
		return udpTextHandler;
	}

	public Handler getRoleTextHandler(){
		return roleTextHandler;
	}

    public Handler getIpTextHandler(){
        return ipTextHandler;
    }

	public Handler getNumberServicesTextHandler(){
		return numberServicesTextHandler;
	}

    public Handler getStatusTextHandler() {return statusTextHandler; }

	public View getRoleTextView(){
		return roleTextView;
	}

    public View getIpTextView() { return ipTextView; }

	public View getUdpTextView(){
		return udpTextView;
	}

	public View getNumberServicesTextView(){
		return numberServicesTextView;
	}

    public View getStatusTextView() { return statusTextView; }
	
	public int getIpRadioGroupChoice(){
		return this.ipRadioGroupChoice;
	}

	
	@Override
	public void onPacketArrived(byte type, int nonce, byte[]service, byte[] data) {
		if(type==ContentRoutingLayer.SERVICE_APPLICATION_REQUEST){
			try {
				crLayer.sendApplicationReply(this.myname==null ? "io".getBytes() : this.myname.getBytes(), 
						nonce, service, data);
			} catch (ContentRoutingLayer.NoPitEntryException npe) {
				npe.printStackTrace();
			}
		}
	}

    private void updateStatusTextView(String text, boolean toast){
        Handler handler = this.getStatusTextHandler();
        Bundle data = new Bundle(1);
        data.putString(MainActivity.STATUS_MESSAGE, text);
        data.putBoolean(MainActivity.STATUS_IF_TOAST, toast);
        Message msg = handler.obtainMessage();
        msg.setData(data);
        handler.sendMessage(msg);
    }


}


