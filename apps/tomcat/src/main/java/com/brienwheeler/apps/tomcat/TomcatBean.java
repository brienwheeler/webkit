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

import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

public class TomcatBean implements InitializingBean, DisposableBean
{
	private static final Log log = LogFactory.getLog(TomcatBean.class);

  private static final Pattern KEY_PATTERN = Pattern.compile("-+BEGIN (.*)PRIVATE KEY-+([^-]*)-+END .*PRIVATE KEY-+");
  private static final Pattern CERT_PATTERN = Pattern.compile("-+BEGIN CERTIFICATE-+([^-]*)-+END CERTIFICATE-+");

  private static final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	private int port;
  private int sslPort;
  private int sessionTimeout;
  private File sslKeyFile;
  private File sslCertFile;
	private String baseDirectory;
	private String webAppBase = null;
	private String contextRoot = null;
	private String sslProperties = null;
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
        configureNetwork();
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

  public void setSessionTimeout(int sessionTimeout) {
    this.sessionTimeout = sessionTimeout;
  }

  @Required
  public void setSslProperties(String sslProperties) {
    this.sslProperties = sslProperties;
  }

  @Required
    public void setSslPort(int sslPort)
    {
        this.sslPort = sslPort;
    }

    @Required
    public void setSslKeyFile(File sslKeyFile)
    {
        this.sslKeyFile = sslKeyFile;
    }

    @Required
    public void setSslCertFile(File sslCertFile)
    {
        this.sslCertFile = sslCertFile;
    }

    private void configureNetwork() throws Exception
    {
        if (port > 0) {
            tomcat.setPort(port);
        }
        else {
            tomcat.getService().removeConnector(tomcat.getConnector());
        }

        if (sslPort > 0) {
            StringBuilder randomPass = new StringBuilder();
            for (int i=0; i<10; i++)
                randomPass.append(characters.charAt((int) (characters.length() * Math.random())));
            String keystorePass = randomPass.toString();

            RSAPrivateKey privateKey = readKeyFile();
            log.info("successfully read SSL private key from " + sslKeyFile.getAbsolutePath());
            X509Certificate certificate = readCertFile();
            log.info("successfully read SSL certificate from " + sslCertFile.getAbsolutePath());

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null);
            keyStore.setCertificateEntry("cert-alias", certificate);
            keyStore.setKeyEntry("key-alias", privateKey, keystorePass.toCharArray(), new Certificate[]{certificate});
            File keyStoreFile = new File("tcks");
            keyStore.store(new FileOutputStream(keyStoreFile), keystorePass.toCharArray());

            Connector sslConnector = new Connector();
            sslConnector.setPort(sslPort);
            sslConnector.setSecure(true);
            sslConnector.setScheme("https");
            sslConnector.setAttribute("keystoreFile", keyStoreFile.getAbsolutePath());
            sslConnector.setAttribute("keystorePass", keystorePass);
            sslConnector.setAttribute("clientAuth", "false");
            sslConnector.setAttribute("SSLEnabled", true);

            if (sslProperties != null && sslProperties.trim().length() > 0) {
                String sslPropDefs[] = sslProperties.trim().split(";");
                for (String sslPropDef : sslPropDefs) {
                    String parts[] = sslPropDef.split("=");
                    if ((parts.length != 2) || (parts[0].trim().length() == 0) || (parts[1].trim().length() == 0)) {
                        log.warn("invalid SSL property definition -- ignoring: " + sslPropDef);
                        continue;
                    }
                    String name = parts[0].trim();
                    String value = parts[1].trim();
                    log.info("setting Tomcat SSL connector property: " + name + "=" + value);
                    sslConnector.setAttribute(name, value);
                }
            }
            sslConnector.setAttribute("sslProtocol", "TLS");
            tomcat.getService().addConnector(sslConnector);
        }
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

        // replace standard DefaultWebXmlListener with our subclass that sets the session
        // timeout after calling its super method (which sets session timeout to 30)
        for (LifecycleListener listener : context.findLifecycleListeners()) {
          if (listener instanceof Tomcat.DefaultWebXmlListener) {
            context.removeLifecycleListener(listener);
            break;
          }
        }
        context.addLifecycleListener(new MyDefaultWebXmlListener());
			}
			catch (Exception e) {
				throw new RuntimeException("error extracting WAR file", e);
			}
		}
	}

    private RSAPrivateKey readKeyFile() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String parse[] = readPEMFile(sslKeyFile, KEY_PATTERN, 2);
        if (parse == null)
            throw new IllegalArgumentException("invalid key file contents");

        if (parse[0].length() == 0) { // BEGIN PRIVATE KEY
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(parse[1])));
        }

        if (parse[0].contains("RSA")) { // BEGIN RSA PRIVATE KEY
            Security.addProvider(new BouncyCastleProvider());

            PEMParser pemParser = new PEMParser(new FileReader(sslKeyFile));
            Object parsedObject = pemParser.readObject();
            if (!(parsedObject instanceof PEMKeyPair))
                throw new IllegalArgumentException("invalid key file contents");

            PEMKeyPair keyPair = (PEMKeyPair) parsedObject;
            RSAPrivateKey privateKey = (RSAPrivateKey) BouncyCastleProvider.getPrivateKey(keyPair.getPrivateKeyInfo());
            if (privateKey == null)
                throw new IllegalArgumentException("invalid key file contents");
            return privateKey;
        }

        throw new IllegalArgumentException("invalid key file contents");
    }

    private X509Certificate readCertFile() throws IOException, CertificateException {
        String parse[] = readPEMFile(sslCertFile, CERT_PATTERN, 1);
        if (parse == null)
            throw new IllegalArgumentException("invalid certificate file contents");
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(Base64.decode(parse[0])));
    }

    private String[] readPEMFile(File inFile, Pattern pattern, int groups) throws IOException {
        StringBuilder buffer = new StringBuilder();

        BufferedReader reader = new BufferedReader(new FileReader(inFile));
        for (String line=reader.readLine(); line!=null; line=reader.readLine())
            buffer.append(line);
        reader.close();

        Matcher matcher = pattern.matcher(buffer.toString());
        if (!matcher.matches())
            return null;

        String[] ret = new String[groups];
        for (int i=0; i<groups; i++)
            ret[i] = matcher.group(i + 1);
        return ret;
    }

  private class MyDefaultWebXmlListener extends Tomcat.DefaultWebXmlListener {
    @Override
    public void lifecycleEvent(LifecycleEvent event) {
      super.lifecycleEvent(event);
      if (Lifecycle.BEFORE_START_EVENT.equals(event.getType())) {
        if (event.getLifecycle() instanceof StandardContext && sessionTimeout > 0) {
          ((StandardContext) event.getLifecycle()).setSessionTimeout(sessionTimeout);
        }
      }
    }
  }
}
