/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Brien L. Wheeler (brienwheeler@yahoo.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.brienwheeler.lib.io;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.brienwheeler.lib.concurrent.StoppableThread;
import com.brienwheeler.lib.util.ValidationUtils;

public class ReconnectingSocket
{
	private final AtomicReference<Socket> socket = new AtomicReference<Socket>(null);
	private final AtomicReference<ConnectThread> connectThread = new AtomicReference<ConnectThread>(null);
	private Charset charset = Charset.forName("UTF-8");
	private final Log log;
	private final String hostname;
	private final int port;
	private long reconnectPeriodicity = 60000L; // default one minute
	
	public ReconnectingSocket(String hostname, int port)
	{
		this(hostname, port, LogFactory.getLog(ReconnectingSocket.class));
	}
	
	public ReconnectingSocket(String hostname, int port, Log log)
	{
		ValidationUtils.assertNotNull(hostname, "hostname cannot be null");
		ValidationUtils.assertTrue(port > 0, "port must be greater than 0");
		ValidationUtils.assertTrue(port < 65536, "port must be less than 65536");
		ValidationUtils.assertNotNull(log, "log cannot be null");
		
		this.hostname = hostname;
		this.port = port;
		this.log = log;
	}
	
	public void start() {
		startConnectThread(0L);
	}
	
	public void setReconnectPeriodicity(long reconnectPeriodicity)
	{
		this.reconnectPeriodicity = reconnectPeriodicity;
	}

	public void write(String data)
	{
		tryToWrite(data.getBytes(charset));
	}
	
	public void stop()
	{
		ConnectThread thread = connectThread.getAndSet(null);
		if (thread != null) {
			try {
				thread.shutdown();
			}
			catch (InterruptedException e) {
				log.warn("interrupted while shutting down", e);
				Thread.currentThread().interrupt();
			}
		}
		
		Socket socket = this.socket.getAndSet(null);
		if (socket != null) {
			try {
				socket.close();
			}
			catch (IOException e) {
				log.error("error closing socket", e);
			}
		}
	}
	
	public boolean isConnected()
	{
		return socket.get() != null;
	}
	
	protected void onConnected()
	{
	}
	
	protected void onDisconnected()
	{
	}
	
	protected void onConnectFail()
	{
	}

	private void startConnectThread(long initialSleep)
	{
		if (connectThread.get() != null)
			return;
		
		ConnectThread newThread = new ConnectThread(initialSleep);
		if (connectThread.compareAndSet(null, newThread)) {
			log.debug("starting new connect thread");
			newThread.start();
		}
	}
	
	private void tryToWrite(byte data[])
	{
		Socket writeSocket = socket.get();
		if (writeSocket == null)
			return;

		try {
			OutputStream outputStream = writeSocket.getOutputStream();
			outputStream.write(data);
			outputStream.flush();
		}
		catch (IOException e) {
			if (socket.compareAndSet(writeSocket, null)) {
				log.error("error writing to " + hostname, e);
				onDisconnected();
				startConnectThread(reconnectPeriodicity);
				try {
					writeSocket.close();
				}
				catch (IOException e1) {
					// silent
				}
			}
		}
	}
	
	class ConnectThread extends StoppableThread
	{
		private final long initialSleep;
		
		public ConnectThread(long initialSleep)
		{
			super(ReconnectingSocket.class.getSimpleName() + "-" + ConnectThread.class.getSimpleName());
			this.initialSleep = initialSleep;
		}

		private boolean connect()
		{
			try {
				log.debug("attempting to connect to " + hostname);
				InetAddress address = InetAddress.getByName(hostname);
				socket.set(new Socket(address, port));
				log.info("successfully connected to " + hostname);
				connectThread.set(null);
				onConnected();
				return true;
			} catch (Exception e) {
				log.error("error connecting to " + hostname, e);
				onConnectFail();
				return false;
			}
		}
		
		@Override
		protected void onRun()
		{
			long sleepTime = initialSleep;
			
			while (!isShutdown()) {
				long now = System.currentTimeMillis();
				long wakeTime = now + sleepTime;
				sleepTime = reconnectPeriodicity;
				
				while (!isShutdown() && (wakeTime > now)) {
					try {
						log.debug("sleeping " + (wakeTime - now));
						Thread.sleep(wakeTime - now);
					}
					catch (InterruptedException e) {
						// probably isShutdown()
						Thread.currentThread().interrupt();
					}
					now = System.currentTimeMillis();
				}	
				
				if (!isShutdown() && connect()) {
					// success!
					return;
				}
			}
				
		}
		
		
	}
}
