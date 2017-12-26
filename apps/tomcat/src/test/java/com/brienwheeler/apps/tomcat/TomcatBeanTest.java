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
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class TomcatBeanTest {
    @Test
    public void testReadKeyFile() throws Exception {
        TomcatBean tomcatBean = new TomcatBean();
        File keyFile = new File(getClass().getClassLoader().getResource("key.pem").getFile());
        tomcatBean.setSslKeyFile(keyFile);
        RSAPrivateKey key = ReflectionTestUtils.<RSAPrivateKey>invokeMethod(tomcatBean, "readKeyFile");
        Assert.assertNotNull(key);
    }

    @Test
    public void testReadCertChainFile() throws Exception {
        TomcatBean tomcatBean = new TomcatBean();
        File certFile = new File(getClass().getClassLoader().getResource("ca-chain.cert.pem").getFile());
        tomcatBean.setSslCertFile(certFile);
        List<X509Certificate> certs = ReflectionTestUtils.<List<X509Certificate>>invokeMethod(tomcatBean, "readCertFile");
        Assert.assertNotNull(certs);
        Assert.assertEquals(3, certs.size());
    }
}
