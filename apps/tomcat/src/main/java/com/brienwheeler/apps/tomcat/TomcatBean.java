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
package com.brienwheeler.apps.tomcat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

public class TomcatBean implements InitializingBean, DisposableBean
{
	private final static Log log = LogFactory.getLog(TomcatBean.class);
	
	private int port;
	private String baseDirectory;
	private String webAppBase = null;
	private String contextRoot = null;
	private final Tomcat tomcat;
	
	public TomcatBean()
	{
		tomcat = new Tomcat();
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		tomcat.setBaseDir(baseDirectory);
		tomcat.getHost().setAppBase(baseDirectory);
		tomcat.setPort(port);
		extractWarFile();

		tomcat.start();
	}

	@Override
	public void destroy() throws Exception
	{
		tomcat.stop();
		tomcat.getServer().await();
	}

	@Required
	public void setBaseDirectory(String baseDirectory)
	{
		this.baseDirectory = baseDirectory;
	}

	@Required
	public void setWebAppBase(String webAppBase)
	{
		this.webAppBase = webAppBase;
	}

	public void setContextRoot(String contextRoot)
	{
		this.contextRoot = contextRoot;
	}
	
	@Required
	public void setPort(int port)
	{
		this.port = port;
	}
	
	private void extractWarFile()
	{
		if (webAppBase != null && webAppBase.length() > 0) {
			ProtectionDomain protectionDomain = this.getClass().getProtectionDomain();
			URL location = protectionDomain.getCodeSource().getLocation();
			log.info("detected run JAR at " + location);
			
			if (!location.toExternalForm().startsWith("file:") ||
					!location.toExternalForm().endsWith(".jar"))
				throw new IllegalArgumentException("invalid code location: " + location);
	
			try {
				ZipFile zipFile = new ZipFile(new File(location.toURI()));

				Enumeration<? extends ZipEntry> entryEnum = zipFile.entries();
				ZipEntry warEntry = null;
				while (entryEnum.hasMoreElements()) {
					ZipEntry entry = entryEnum.nextElement();
					String entryName = entry.getName();
					if (entryName.startsWith(webAppBase) && entryName.endsWith(".war")) {
						warEntry = entry;
						break;
					}
				}
				
				if (warEntry == null)
					throw new RuntimeException("can't find JAR entry for " + webAppBase + "*.war");
				
				log.info("extracting WAR file " + warEntry.getName());
				
				// extract web app WAR to current directory
				InputStream inputStream = zipFile.getInputStream(warEntry);
				OutputStream outputStream = new FileOutputStream(new File(warEntry.getName()));
				byte buf[] = new byte[1024];
				int nread;
				while ((nread = inputStream.read(buf, 0, 1024)) > 0) {
					outputStream.write(buf, 0, nread);
				}
				outputStream.close();
				inputStream.close();
				zipFile.close();

				String launchContextRoot = contextRoot != null ? contextRoot : webAppBase;
				if (!launchContextRoot.startsWith("/"))
					launchContextRoot = "/" + launchContextRoot;
				
				log.info("launching WAR file " + warEntry.getName() + " at context root " + launchContextRoot);

				// add web app to Tomcat
				Context context = tomcat.addWebapp(launchContextRoot, warEntry.getName());
				if (context instanceof StandardContext)
					((StandardContext) context).setUnpackWAR(false);
			}
			catch (Exception e) {
				throw new RuntimeException("error extracting WAR file", e);
			}
		}

	}
}
