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
package com.brienwheeler.svc.authorize_net.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import com.brienwheeler.lib.util.EnvironmentUtils;
import com.brienwheeler.svc.authorize_net.AuthorizeNetException;
import com.brienwheeler.svc.authorize_net.InvalidCardCodeException;
import com.brienwheeler.svc.authorize_net.PaymentDeclinedException;
import com.brienwheeler.svc.authorize_net.PaymentMethod;
import com.brienwheeler.svc.users.domain.User;
import com.brienwheeler.svc.users.impl.AbstractSvcUsersTest;

public class CIMClientServiceTest extends AbstractSvcAuthorizeNetTest
{
	private static int testNumber = 0;
	
	@Test
	public void testCreateCustomerProfile()
	{
		if (!EnvironmentUtils.getBooleanProperty(ENABLE_NETWORKED_TESTS))
			return;
		
		User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
		String customerProfileId = makeTestCustomerProfile(user.getDbId());
		Assert.assertNotNull(customerProfileId);
		Assert.assertTrue(customerProfileId.trim().length() > 0);

		String customerProfileIdCheck = userAttributeService.getAttribute(user.getDbId(), CIMClientService.ATTR_PROFILE_ID);
		Assert.assertNotNull(customerProfileIdCheck);
		Assert.assertEquals(customerProfileId, customerProfileIdCheck);
	}

	@Test
	public void testCreateCustomerProfileAlwaysCommits() throws Exception
	{
		if (!EnvironmentUtils.getBooleanProperty(ENABLE_NETWORKED_TESTS))
			return;
		
		final User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
		final AtomicReference<String> customerProfileId = new AtomicReference<String>();
		
		try {
			transactionWrapper.doInWriteTransaction(new Callable<String>() {
				@Override
				public String call() throws Exception {
					customerProfileId.set(makeTestCustomerProfile(user.getDbId()));
					Assert.assertNotNull(customerProfileId.get());
					Assert.assertTrue(customerProfileId.get().trim().length() > 0);
					// to prove that an exception thrown within an enclosing transaction doesn't
					// prevent the save of the UserAttribute holding the Authorize.Net customer profile ID
					throw new RuntimeException();
				}
			});
		}
		catch (RuntimeException e) {
			// expected
		}
		
		String customerProfileIdCheck = userAttributeService.getAttribute(user.getDbId(), CIMClientService.ATTR_PROFILE_ID);
		Assert.assertNotNull(customerProfileIdCheck);
		Assert.assertEquals(customerProfileId.get(), customerProfileIdCheck);
	}

	@Test
	public void testGetPaymentMethods() throws Exception
	{
		if (!EnvironmentUtils.getBooleanProperty(ENABLE_NETWORKED_TESTS))
			return;
		
		User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
		String customerProfileId = makeTestCustomerProfile(user.getDbId());
		createPaymentProfile(customerProfileId, "4007000000027", ZIP_CODE_OK);
		createPaymentProfile(customerProfileId, "4012888818888", ZIP_CODE_OK);

		List<PaymentMethod> paymentMethods = cimClientService.getPaymentMethods(user.getDbId());
		Assert.assertEquals(2, paymentMethods.size());
	}

	@Test
	public void getHostedProfilePageToken() throws Exception
	{
		if (!EnvironmentUtils.getBooleanProperty(ENABLE_NETWORKED_TESTS))
			return;
		
		User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
		makeTestCustomerProfile(user.getDbId());
		String token = cimClientService.getHostedProfilePageToken(user.getDbId(), "http://localhost");
		Assert.assertNotNull(token);
		Assert.assertTrue(token.trim().length() > 0);
	}
	
	@Test
	public void testAuthorize() throws Exception
	{
		if (!EnvironmentUtils.getBooleanProperty(ENABLE_NETWORKED_TESTS))
			return;
		
		User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
		String customerProfileId = makeTestCustomerProfile(user.getDbId());
		String paymentProfileId = createPaymentProfile(customerProfileId, "4007000000027", ZIP_CODE_OK);
		
		BigDecimal amount = getTestChargeAmount();
		String transactionId = cimClientService.authorizePayment(user.getDbId(), paymentProfileId, amount);
		Assert.assertNotNull(transactionId);
		Assert.assertTrue(transactionId.trim().length() > 0);
	}

	@Test(expected=PaymentDeclinedException.class)
	public void testAuthorizeDeclined() throws Exception
	{
		if (!EnvironmentUtils.getBooleanProperty(ENABLE_NETWORKED_TESTS))
			throw new PaymentDeclinedException("");
		
		User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
		String customerProfileId = makeTestCustomerProfile(user.getDbId());
		String paymentProfileId = createPaymentProfile(customerProfileId, "4007000000027", ZIP_CODE_DECLINE);
		
		BigDecimal amount = getTestChargeAmount();
		cimClientService.authorizePayment(user.getDbId(), paymentProfileId, amount);
		Assert.fail();
	}

	@Test
	public void testAuthorizeCvvOk() throws Exception
	{
		if (!EnvironmentUtils.getBooleanProperty(ENABLE_NETWORKED_TESTS))
			return;
		
		User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
		String customerProfileId = makeTestCustomerProfile(user.getDbId());
		String paymentProfileId = createPaymentProfile(customerProfileId, "4007000000027", ZIP_CODE_OK);
		
		BigDecimal amount = getTestChargeAmount();
		String transactionId = cimClientService.authorizePayment(user.getDbId(), paymentProfileId, CVV_OK, amount);
		Assert.assertNotNull(transactionId);
		Assert.assertTrue(transactionId.trim().length() > 0);
	}

	@Test(expected=InvalidCardCodeException.class)
	public void testAuthorizeCvvMismatch() throws Exception
	{
		if (!EnvironmentUtils.getBooleanProperty(ENABLE_NETWORKED_TESTS))
			throw new InvalidCardCodeException("");
		
		User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
		String customerProfileId = makeTestCustomerProfile(user.getDbId());
		String paymentProfileId = createPaymentProfile(customerProfileId, "4007000000027", ZIP_CODE_OK);
		
		BigDecimal amount = getTestChargeAmount();
		String transactionId = cimClientService.authorizePayment(user.getDbId(), paymentProfileId, CVV_MISMATCH, amount);
		Assert.assertNotNull(transactionId);
		Assert.assertTrue(transactionId.trim().length() > 0);
	}

	@Test
	public void testAuthorizeCvvNotProcessed() throws Exception
	{
		if (!EnvironmentUtils.getBooleanProperty(ENABLE_NETWORKED_TESTS))
			return;
		
		boolean shouldFail = EnvironmentUtils.getBooleanProperty(CVV_NOT_PROCESSED_SHOULD_FAIL);
		
		User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
		String customerProfileId = makeTestCustomerProfile(user.getDbId());
		String paymentProfileId = createPaymentProfile(customerProfileId, "4007000000027", ZIP_CODE_OK);
		
		BigDecimal amount = getTestChargeAmount();
		try {
			String transactionId = cimClientService.authorizePayment(user.getDbId(), paymentProfileId, CVV_NOT_PROCESSED, amount);
			if (shouldFail)
				Assert.fail();
			Assert.assertNotNull(transactionId);
			Assert.assertTrue(transactionId.trim().length() > 0);
		}
		catch (InvalidCardCodeException e) {
			if (!shouldFail)
				throw e;
		}
	}

	@Test
	public void testAuthorizeCvvNotIndicated() throws Exception
	{
		if (!EnvironmentUtils.getBooleanProperty(ENABLE_NETWORKED_TESTS))
			return;
		
		boolean shouldFail = EnvironmentUtils.getBooleanProperty(CVV_NOT_INDICATED_SHOULD_FAIL);
		
		User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
		String customerProfileId = makeTestCustomerProfile(user.getDbId());
		String paymentProfileId = createPaymentProfile(customerProfileId, "4007000000027", ZIP_CODE_OK);
		
		BigDecimal amount = getTestChargeAmount();
		try {
			String transactionId = cimClientService.authorizePayment(user.getDbId(), paymentProfileId, CVV_NOT_INDICATED, amount);
			if (shouldFail)
				Assert.fail();
			Assert.assertNotNull(transactionId);
			Assert.assertTrue(transactionId.trim().length() > 0);
		}
		catch (InvalidCardCodeException e) {
			if (!shouldFail)
				throw e;
		}
	}

	@Test
	public void testAuthorizeCvvNotCertified() throws Exception
	{
		if (!EnvironmentUtils.getBooleanProperty(ENABLE_NETWORKED_TESTS))
			return;
		
		boolean shouldFail = EnvironmentUtils.getBooleanProperty(CVV_NOT_CERTIFIED_SHOULD_FAIL);
		
		User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
		String customerProfileId = makeTestCustomerProfile(user.getDbId());
		String paymentProfileId = createPaymentProfile(customerProfileId, "4007000000027", ZIP_CODE_OK);
		
		BigDecimal amount = getTestChargeAmount();
		try {
			String transactionId = cimClientService.authorizePayment(user.getDbId(), paymentProfileId, CVV_NOT_CERTIFIED, amount);
			if (shouldFail)
				Assert.fail();
			Assert.assertNotNull(transactionId);
			Assert.assertTrue(transactionId.trim().length() > 0);
		}
		catch (InvalidCardCodeException e) {
			if (!shouldFail)
				throw e;
		}
	}

	@Test
	public void testSettle() throws Exception
	{
		if (!EnvironmentUtils.getBooleanProperty(ENABLE_NETWORKED_TESTS))
			return;
		
		User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
		String customerProfileId = makeTestCustomerProfile(user.getDbId());
		String paymentProfileId = createPaymentProfile(customerProfileId, "4007000000027", ZIP_CODE_OK);
		
		BigDecimal amount = getTestChargeAmount();
		String transactionId = cimClientService.authorizePayment(user.getDbId(), paymentProfileId, amount);
		cimClientService.settlePayment(user.getDbId(), transactionId, amount);
	}

	@Test(expected=AuthorizeNetException.class)
	public void testMultipleSettle() throws Exception
	{
		if (!EnvironmentUtils.getBooleanProperty(ENABLE_NETWORKED_TESTS))
			throw new AuthorizeNetException("");
		
		User user = AbstractSvcUsersTest.makePersistedUser(applicationContext);
		String customerProfileId = makeTestCustomerProfile(user.getDbId());
		String paymentProfileId = createPaymentProfile(customerProfileId, "4007000000027", ZIP_CODE_OK);

		BigDecimal amount = getTestChargeAmount();
		String transactionId = cimClientService.authorizePayment(user.getDbId(), paymentProfileId, amount);
		cimClientService.settlePayment(user.getDbId(), transactionId, amount);
		cimClientService.settlePayment(user.getDbId(), transactionId, amount);
		Assert.fail();
	}

	private BigDecimal getTestChargeAmount()
	{
		DateTime now = new DateTime();
		return new BigDecimal((++testNumber * 10000) + (now.getMinuteOfHour() * 100) + now.getSecondOfMinute()).divide(new BigDecimal(100));
	}
}
