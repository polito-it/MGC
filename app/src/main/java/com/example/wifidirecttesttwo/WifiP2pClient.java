
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
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WifiP2pClient{
	private InetAddress p2pIPAddress;
	private String p2pMacAddress;
	private String wifiMacAddress;
	private List<String> providedServices;
	private boolean isSameGroup;


	public WifiP2pClient(InetAddress p2pIPAddress, String p2pMacAddress, String wifiMacAddress, boolean isSameGroup) {
		super();
		this.p2pIPAddress = p2pIPAddress;
		this.p2pMacAddress = p2pMacAddress;
		this.wifiMacAddress = wifiMacAddress;
		this.isSameGroup = isSameGroup;
		this.providedServices = Collections.synchronizedList(new ArrayList<String>());
	}

	public WifiP2pClient(String strP2pIpAddress)
	{
		try {
			this.p2pIPAddress = InetAddress.getByName(strP2pIpAddress);
		} catch (UnknownHostException e) {
			this.p2pIPAddress = null;
			e.printStackTrace();
		}
		this.p2pMacAddress = null;
		this.wifiMacAddress = null;
		this.isSameGroup = true;
		this.providedServices = Collections.synchronizedList(new ArrayList<String>());
	}

	public InetAddress getP2pIPAddress() {
		return p2pIPAddress;
	}

	public String getP2pMacAddress() {
		return p2pMacAddress;
	}


	public String getWifiMacAddress() {
		return wifiMacAddress;
	}

	public boolean addService(String serv){
		if(!this.providedServices.contains(serv)){
			this.providedServices.add(serv);
			return true;
		}
		return false;
	}

	public boolean isSameGroup()
	{
		return this.isSameGroup;
	}

	public List<String> getProvidedServices(){
		return this.providedServices;
	}

	@Override
	public boolean equals(Object o){
		if(o == null)                
			return false;
		if(!(o instanceof WifiP2pClient)) 
			return false;

		WifiP2pClient wpc = (WifiP2pClient) o;

		if(this.p2pIPAddress==null || wpc.getP2pIPAddress()==null)
			return false;

		return (this.p2pIPAddress.getHostAddress() == wpc.getP2pIPAddress().getHostAddress());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((p2pIPAddress == null) ? 0 : p2pIPAddress.hashCode());
		return result;
	}


	public void setSameGroup(boolean b){
		this.isSameGroup = b;
	}

}
