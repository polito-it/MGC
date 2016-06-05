
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

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;


public class NotificationQueue implements Queue<NotificationEntry> {

	private static final int NOTIFICATION_DELAY = 1000;
	private static final int NOTIFICATION_PERIOD = 1000;
	
	private ContentRoutingLayer crLayer;

	private ConcurrentLinkedQueue<NotificationEntry> notificationQueue;
	private Timer notificationTimer;
	private TimerTask notificationTimerTask;

	private NotificationMessage currentNotification;
	private boolean currentNotificationReceived;
	private boolean currentNotificationSent;

	private int seed;
	
	private boolean started;



	public NotificationQueue(ContentRoutingLayer crLayerTemp){

		this.crLayer = crLayerTemp;

		this.notificationQueue = new ConcurrentLinkedQueue<NotificationEntry>();
		this.currentNotification = null;
		this.currentNotificationReceived = false;
		this.currentNotificationSent = false;

		Random rn = new Random();
		this.seed = rn.nextInt();
		
		this.started = false;
	}

	
	

	public void ackReceived(int n){
		
		if(this.currentNotification != null && n == this.currentNotification.getSequenceNumber()){
			this.currentNotificationReceived=true;
			this.currentNotificationSent=false;
		}
	}
	
	
	public synchronized void start(){
		
		if(this.started == true)
			return;
		
		this.started = true;
		
		this.notificationTimer = new Timer();
		this.notificationTimerTask = new TimerTask() {
			@Override
			public void run() {
				if(currentNotificationSent==true && currentNotificationReceived==false){
					crLayer.sendServiceNotificationMessage(currentNotification.toByteArray(), 
							currentNotification.getDestAddress());
				}
				else if(!notificationQueue.isEmpty()){
					currentNotification = new NotificationMessage(seed++);
					try{
						while(currentNotification.add(notificationQueue.element()))
							notificationQueue.remove();
					}catch(NoSuchElementException nse){	
					}
					if(currentNotification.getSize()>0){
						currentNotificationSent=true;
						currentNotificationReceived=false;
						crLayer.sendServiceNotificationMessage(currentNotification.toByteArray(), 
								currentNotification.getDestAddress());
					}
				}
			}
		};
		
		
		notificationTimer.schedule(this.notificationTimerTask, NotificationQueue.NOTIFICATION_DELAY, 
				NotificationQueue.NOTIFICATION_PERIOD);
	}

	
	public void stop(){
		//TODO to be implemented
	}

	//############################################################
	// OVERRIDDEN METHODS
	//############################################################


	@Override
	public boolean addAll(Collection<? extends NotificationEntry> arg0) {
		return this.notificationQueue.addAll(arg0);
	}

	@Override
	public void clear() {
		this.notificationQueue.clear();
	}

	@Override
	public boolean contains(Object arg0) {
		return this.notificationQueue.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return this.notificationQueue.containsAll(arg0);
	}

	@Override
	public boolean isEmpty() {
		return this.notificationQueue.isEmpty();
	}

	@Override
	public Iterator<NotificationEntry> iterator() {
		return this.notificationQueue.iterator();
	}

	@Override
	public boolean remove(Object arg0) {
		return this.notificationQueue.remove(arg0);
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		return this.notificationQueue.removeAll(arg0);
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		return this.notificationQueue.retainAll(arg0);
	}

	@Override
	public int size() {
		return this.notificationQueue.size();
	}

	@Override
	public Object[] toArray() {
		return this.notificationQueue.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return this.notificationQueue.toArray(arg0);
	}

	@Override
	public boolean add(NotificationEntry arg0) {
		return this.notificationQueue.add(arg0);

	}

	@Override
	public NotificationEntry element() {
		return this.notificationQueue.element();
	}

	@Override
	public boolean offer(NotificationEntry arg0) {
		return this.notificationQueue.offer(arg0);
	}

	@Override
	public NotificationEntry peek() {
		return this.notificationQueue.peek();
	}

	@Override
	public NotificationEntry poll() {
		return this.notificationQueue.poll();
	}

	@Override
	public NotificationEntry remove() {
		return this.notificationQueue.remove();
	}



}
