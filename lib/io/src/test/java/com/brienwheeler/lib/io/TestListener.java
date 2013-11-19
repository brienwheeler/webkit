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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestListener
{
	private static final Log log = LogFactory.getLog(TestListener.class);
	
	private BufferedReader reader;
	private ServerSocket listenSocket;
	private final int listenPort;
	private Socket clientSocket;
	
	public TestListener() throws IOException
	{
		openServerSocket(0);
		listenPort = listenSocket.getLocalPort();
	}
	
	public int getListenPort()
	{
		return listenPort;
	}
			
	public void accept() throws IOException
	{
		clientSocket = listenSocket.accept();
		reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	}
	
	public String readLine() throws IOException
	{
		return reader.readLine();
	}
	
	public void closeClient()
	{
		if (reader != null) {
			try {
				reader.close();
			}
			catch (Exception e) {
				log.error(e);
			}
			reader = null;
		}
		if (clientSocket != null) {
			try {
				clientSocket.close();
			}
			catch (Exception e) {
				log.error(e);
			}
			clientSocket = null;
		}
	}
	
	public void closeServer()
	{
		if (listenSocket != null) {
			try {
				listenSocket.close();
			}
			catch (Exception e) {
				log.error(e);
			}
			listenSocket = null;
		}
	}

	public void close()
	{
		closeClient();
		closeServer();
	}
	
	public void reopen() throws IOException
	{
		openServerSocket(listenPort);
	}
	
	private void openServerSocket(int port) throws IOException
	{
		listenSocket = new ServerSocket(port);
		listenSocket.setReuseAddress(true);
	}
}