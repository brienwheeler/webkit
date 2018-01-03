/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2018 Brien L. Wheeler (brienwheeler@yahoo.com)
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
package com.brienwheeler.svc.email.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.URLName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.mail.smtp.SMTPTransport;

/*
 * To mock out the send, we only need to change the behavior of protocolConnect, sendMessage, and close.
 * Isn't that nice.  :)
 */
public class MockSMTPTransport extends SMTPTransport
{
	private static final Log log = LogFactory.getLog(MockSMTPTransport.class);
	private static final boolean doMock = true;
	
	public MockSMTPTransport(Session session, URLName urlname)
	{
		super(session, urlname);
	}

	public MockSMTPTransport(Session session, URLName urlname, String name, boolean isSSL)
	{
		super(session, urlname, name, isSSL);
	}

	@Override
	protected void checkConnected() {
		super.checkConnected();
		log.debug("checkConnected");
	}

	@Override
	public synchronized void close() throws MessagingException {
		if (!doMock)
			super.close();
		log.debug("close");
	}

	@Override
	public synchronized void connect(Socket socket) throws MessagingException {
		super.connect(socket);
		log.debug("connect");
	}

	@Override
	protected OutputStream data() throws MessagingException {
		OutputStream ret = super.data();
		log.debug("data " + ret);
		return ret;
	}

	@Override
	protected boolean ehlo(String arg0) throws MessagingException {
		boolean ret = super.ehlo(arg0);
		log.debug("ehlo " + arg0 + " " + ret);
		return ret;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		log.debug("finalize");
	}

	@Override
	protected void finishData() throws IOException, MessagingException {
		super.finishData();
		log.debug("finishData");
	}

	@Override
	public synchronized String getAuthorizationId() {
		String ret = super.getAuthorizationId();
		log.debug("getAuthorizationId " + ret);
		return ret;
	}

	@Override
	public String getExtensionParameter(String ext) {
		String ret = super.getExtensionParameter(ext);
		log.debug("getExtensionParameter " + ret);
		return ret;
	}

	@Override
	public synchronized int getLastReturnCode() {
		int ret = super.getLastReturnCode();
		log.debug("getLastReturnCode " + ret);
		return ret;
	}

	@Override
	public synchronized String getLastServerResponse() {
		String ret = super.getLastServerResponse();
		log.debug("getLastServerResponse " + ret);
		return ret;
	}

	@Override
	public synchronized String getLocalHost() {
		String ret = super.getLocalHost();
		log.debug("getLocalHost " + ret);
		return ret;
	}

	@Override
	public synchronized String getNTLMDomain() {
		String ret = super.getNTLMDomain();
		log.debug("getNTLMDomain " + ret);
		return ret;
	}

	@Override
	public synchronized boolean getNoopStrict() {
		boolean ret = super.getNoopStrict();
		log.debug("getNoopStrict " + ret);
		return ret;
	}

	@Override
	public synchronized boolean getReportSuccess() {
		boolean ret = super.getReportSuccess();
		log.debug("getReportSuccess " + ret);
		return ret;
	}

	@Override
	public synchronized boolean getRequireStartTLS() {
		boolean ret = super.getRequireStartTLS();
		log.debug("getRequireStartTLS " + ret);
		return ret;
	}

	@Override
	public synchronized boolean getSASLEnabled() {
		boolean ret = super.getSASLEnabled();
		log.debug("getSASLEnabled " + ret);
		return ret;
	}

	@Override
	public synchronized String[] getSASLMechanisms() {
		String[] ret = super.getSASLMechanisms();
		if (log.isDebugEnabled()) {
			String debugMsg = "getSASLMechanisms";
			for (String value : ret)
				debugMsg += " " + value;
			log.debug(debugMsg);
		}
		return ret;
	}

	@Override
	public synchronized String getSASLRealm() {
		String ret = super.getSASLRealm();
		log.debug("getSASLRealm " + ret);
		return ret;
	}

	@Override
	public synchronized boolean getStartTLS() {
		boolean ret = super.getStartTLS();
		log.debug("getStartTLS " + ret);
		return ret;
	}

	@Override
	public synchronized boolean getUseRset() {
		boolean ret = super.getUseRset();
		log.debug("getUseRset " + ret);
		return ret;
	}

	@Override
	protected void helo(String domain) throws MessagingException {
		super.helo(domain);
		log.debug("helo");
	}

	@Override
	public synchronized boolean isConnected() {
		boolean ret = super.isConnected();
		log.debug("isConnected " + ret);
		return ret;
	}

	@Override
	public boolean isSSL() {
		boolean ret = super.isSSL();
		log.debug("isSSL " + ret);
		return ret;
	}

	@Override
	public synchronized void issueCommand(String cmd, int expect)
			throws MessagingException {
		super.issueCommand(cmd, expect);
		log.debug("issueCommand " + cmd + " " + expect);
	}

	@Override
	protected void mailFrom() throws MessagingException {
		super.mailFrom();
		log.debug("mailFrom");
	}

	@Override
	protected void notifyTransportListeners(int type, Address[] validSent,
			Address[] validUnsent, Address[] invalid, Message msg) {
		super.notifyTransportListeners(type, validSent, validUnsent, invalid, msg);
		log.debug("notifyTransportListeners");
	}

	@Override
	protected synchronized boolean protocolConnect(String arg0, int arg1,
			String arg2, String arg3) throws MessagingException {
		boolean ret;
		if (doMock)
			ret = true;
		else
			ret = super.protocolConnect(arg0, arg1, arg2, arg3);
		
		log.debug("protocolConnect " + ret);
		return ret;
	}

	@Override
	protected void rcptTo() throws MessagingException {
		super.rcptTo();
		log.debug("rcptTo");
	}

	@Override
	protected int readServerResponse() throws MessagingException {
		int ret = super.readServerResponse();
		log.debug("readServerResponse " + ret);
		return ret;
	}

	@Override
	public boolean sasllogin(String[] arg0, String arg1, String arg2,
			String arg3, String arg4) throws MessagingException {
		boolean ret = super.sasllogin(arg0, arg1, arg2, arg3, arg4);
		log.debug("sasllogin " + ret);
		return ret;
	}

	@Override
	protected void sendCommand(String cmd) throws MessagingException {
		super.sendCommand(cmd);
		log.debug("sendCommand " + cmd);
	}

	@Override
	public synchronized void sendMessage(Message message, Address[] addresses)
			throws MessagingException, SendFailedException {
		if (doMock) {
			String infoMsg = "MockSMTPTransport.sendMessage";
			for (Address address : addresses)
				infoMsg += " " + address;
			log.info(infoMsg);
		}
		else
			super.sendMessage(message, addresses);
		log.debug("sendMessage");
	}

	@Override
	public synchronized void setAuthorizationID(String authzid) {
		super.setAuthorizationID(authzid);
		log.debug("setAuthorizationID " + authzid);
	}

	@Override
	public synchronized void setLocalHost(String localhost) {
		super.setLocalHost(localhost);
		log.debug("setLocalHost " + localhost);
	}

	@Override
	public synchronized void setNTLMDomain(String ntlmDomain) {
		super.setNTLMDomain(ntlmDomain);
		log.debug("setNTLMDomain " + ntlmDomain);
	}

	@Override
	public synchronized void setNoopStrict(boolean noopStrict) {
		super.setNoopStrict(noopStrict);
		log.debug("setNoopStrict " + noopStrict);
	}

	@Override
	public synchronized void setReportSuccess(boolean reportSuccess) {
		super.setReportSuccess(reportSuccess);
		log.debug("setReportSuccess " + reportSuccess);
	}

	@Override
	public synchronized void setRequireStartTLS(boolean requireStartTLS) {
		super.setRequireStartTLS(requireStartTLS);
		log.debug("setRequireStartTLS " + requireStartTLS);
	}

	@Override
	public synchronized void setSASLEnabled(boolean enableSASL) {
		super.setSASLEnabled(enableSASL);
		log.debug("setSASLEnabled " + enableSASL);
	}

	@Override
	public synchronized void setSASLMechanisms(String[] mechanisms) {
		super.setSASLMechanisms(mechanisms);
		if (log.isDebugEnabled()) {
			String debugMsg = "setSASLMechanisms";
			for (String value : mechanisms)
				debugMsg += " " + value;
			log.debug(debugMsg);
		}
	}

	@Override
	public synchronized void setSASLRealm(String saslRealm) {
		super.setSASLRealm(saslRealm);
		log.debug("setSASLRealm " + saslRealm);
	}

	@Override
	public synchronized void setStartTLS(boolean useStartTLS) {
		super.setStartTLS(useStartTLS);
		log.debug("setStartTLS " + useStartTLS);
	}

	@Override
	public synchronized void setUseRset(boolean useRset) {
		super.setUseRset(useRset);
		log.debug("setUseRset " + useRset);
	}

	@Override
	protected int simpleCommand(byte[] cmd) throws MessagingException {
		int ret = super.simpleCommand(cmd);
		log.debug("simpleCommand " + new String(cmd) + " " + ret);
		return ret;
	}

	@Override
	public synchronized int simpleCommand(String cmd) throws MessagingException {
		int ret = super.simpleCommand(cmd);
		log.debug("simpleCommand " + cmd + " " + ret);
		return ret;
	}

	@Override
	protected void startTLS() throws MessagingException {
		super.startTLS();
		log.debug("startTLS");
	}

	@Override
	protected boolean supportsAuthentication(String arg0) {
		boolean ret = super.supportsAuthentication(arg0);
		log.debug("supportsAuthentication " + arg0 + " " + ret);
		return ret;
	}

	@Override
	public boolean supportsExtension(String ext) {
		boolean ret = super.supportsExtension(ext);
		log.debug("supportsExtension " + ext + " " + ret);
		return ret;
	}

}
