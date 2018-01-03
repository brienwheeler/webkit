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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.brienwheeler.lib.monitor.work.MonitoredWork;
import com.brienwheeler.lib.svc.GracefulShutdown;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import com.brienwheeler.lib.email.EmailAddress;
import com.brienwheeler.lib.svc.ServiceOperationException;
import com.brienwheeler.lib.svc.impl.SpringStoppableServiceBase;
import com.brienwheeler.lib.util.ValidationUtils;
import com.brienwheeler.svc.email.EmailTemplateOpenException;
import com.brienwheeler.svc.email.IEmailService;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

public class EmailService extends SpringStoppableServiceBase
		implements IEmailService
{
	private static final Log log = LogFactory.getLog(EmailService.class);
	
	private static final String SUBJECT_TAG = "-subject";
	
	private Boolean authenticated = Boolean.FALSE;
	private Boolean useStartTLS = Boolean.FALSE;
	private Configuration freemarkerConfig;
	private int port = 25;
	private EmailAddress fromAddress;
	private File baseDirectory;
	private Session mailSession;
	private String mailHost;
	private String username;
	private String password;
	
	public void setAuthenticated(Boolean authenticated)
	{
		this.authenticated = authenticated;
	}

	public void setUseStartTLS(Boolean useStartTLS)
	{
		this.useStartTLS = useStartTLS;
	}

	public void setPort(int port)
	{
		ValidationUtils.assertTrue(port > 0 && port < 65536, "port must be greater than 0 and less than 65536");
		this.port = port;
	}

	@Required
	public void setFromAddress(EmailAddress fromAddress)
	{
		this.fromAddress = fromAddress;
	}

	@Required
	public void setBaseDirectory(File baseDirectory)
	{
		this.baseDirectory = baseDirectory;
	}
	
	@Required
	public void setMailHost(String mailHost)
	{
		this.mailHost = mailHost;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	@Override
	protected void onStart() throws InterruptedException
	{
		freemarkerConfig = new Configuration();
		freemarkerConfig.setTemplateLoader(new SmartTemplateLoader(baseDirectory));
		freemarkerConfig.setDefaultEncoding("UTF-8");
		freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		freemarkerConfig.setIncompatibleImprovements(new Version(2, 3, 20));
		
		Properties properties = new Properties();
		properties.setProperty("mail.smtp.host", mailHost);
		properties.setProperty("mail.smtp.port", Integer.toString(port));
		properties.setProperty("mail.smtp.auth", authenticated.toString());
		properties.setProperty("mail.smtp.starttls.enable", useStartTLS.toString());
		
		if (authenticated) {
			mailSession = Session.getInstance(properties, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
		}
		else {
			mailSession = Session.getInstance(properties);
		}
	}
	
	@Override
	@MonitoredWork
    @GracefulShutdown
	public void sendEmail(EmailAddress recipient, String subject, String body)
	{
		doSendEmail(recipient, subject, body);
	}
	
	@Override
	@MonitoredWork
    @GracefulShutdown
	public void sendEmail(EmailAddress recipient, String templateName, Map<String, Object> templateModel)
	{
		// use empty string for default subject
		doSendEmailFromTemplate(recipient, "", templateName, templateModel);
	}
	
	@Override
	@MonitoredWork
    @GracefulShutdown
	public void sendEmail(EmailAddress recipient, String defaultSubject,
			String templateName, Map<String, Object> templateModel)
	{
		doSendEmailFromTemplate(recipient, defaultSubject, templateName, templateModel);
	}
	
	private void doSendEmailFromTemplate(EmailAddress recipient, String defaultSubject,
			String templateName, Map<String, Object> templateModel)
	{
		ValidationUtils.assertNotNull(recipient, "recipient cannot be null");
		templateName = ValidationUtils.assertNotEmpty(templateName, "templateName cannot be null");
		ValidationUtils.assertNotNull(templateModel, "templateModel cannot be null");
		
		// first make sure we have a template for the message body
		Template template;
		try {
			template = freemarkerConfig.getTemplate(templateName);
		}
		catch (IOException e) {
			throw new EmailTemplateOpenException(e);
		}
		
		// attempt to locate a template for the subject line
		String basename = templateName;
		int slashF = basename.lastIndexOf('/');
		int slashB = basename.lastIndexOf('\\');
		int slash = Math.max(slashF,  slashB);
		int dot = basename.lastIndexOf('.');
		String suffix = "";
		if (dot > slash) {
			suffix = basename.substring(dot);
			basename = basename.substring(0, dot);
		}
		String subjectTemplateName = basename + SUBJECT_TAG + suffix;
		
		try {
			// evaluate subject line template
			Template subjectTemplate = freemarkerConfig.getTemplate(subjectTemplateName);
			StringWriter subjectWriter = new StringWriter();
			subjectTemplate.process(templateModel, subjectWriter);
			defaultSubject = subjectWriter.toString();
			// take first line only
			if (defaultSubject.indexOf('\n') != -1)
				defaultSubject = defaultSubject.substring(0, defaultSubject.indexOf('\n'));
			if (defaultSubject.indexOf('\r') != -1)
				defaultSubject = defaultSubject.substring(0, defaultSubject.indexOf('\r'));
		}
		catch (Exception e) {
			// error here is ok -- just use default subject
		}

		try {
			StringWriter writer = new StringWriter();
			template.process(templateModel, writer);
			doSendEmail(recipient, defaultSubject, writer.toString());
		}
		catch (Exception e) {
			throw new ServiceOperationException(e);
		}
	}
	
	/**
	 * This is a private method (rather than having the public template method
	 * call the public non-template method) to prevent inaccurate MonitoredWork
	 * operation counts.
	 * 
	 * @param recipient
	 * @param subject
	 * @param body
	 */
	private void doSendEmail(EmailAddress recipient, String subject, String body)
	{
		ValidationUtils.assertNotNull(recipient, "recipient cannot be null");
		subject = ValidationUtils.assertNotEmpty(subject, "subject cannot be empty");
		body = ValidationUtils.assertNotEmpty(body, "body cannot be empty");
		
		try {
			MimeMessage message = new MimeMessage(mailSession);
			message.setFrom(new InternetAddress(fromAddress.getAddress()));
			message.setRecipients(Message.RecipientType.TO, recipient.getAddress());
			message.setSubject(subject);
			message.setSentDate(new Date());
			message.setText(body);
			Transport.send(message);
			log.info("sent email to " + recipient.getAddress() + " (" + subject + ")");
		}
		catch (MessagingException e) {
			throw new ServiceOperationException(e);
		}
	}
}
