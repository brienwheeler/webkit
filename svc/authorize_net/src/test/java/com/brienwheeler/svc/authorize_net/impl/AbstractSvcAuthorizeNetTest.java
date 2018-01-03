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

import java.lang.reflect.Method;
import java.util.HashSet;

import net.authorize.Environment;
import net.authorize.cim.Result;
import net.authorize.cim.Transaction;
import net.authorize.cim.TransactionType;
import net.authorize.data.cim.PaymentProfile;
import net.authorize.data.creditcard.CreditCard;
import net.authorize.data.xml.Address;
import net.authorize.data.xml.CustomerType;
import net.authorize.data.xml.Payment;
import net.authorize.util.BasicXmlDocument;
import net.authorize.util.HttpClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.util.ReflectionTestUtils;

import com.brienwheeler.lib.db.TransactionWrapper;
import com.brienwheeler.lib.db.domain.DbId;
import com.brienwheeler.lib.spring.beans.PropertyPlaceholderConfigurer;
import com.brienwheeler.lib.test.spring.aop.AopTestUtils;
import com.brienwheeler.svc.authorize_net.ICIMClientService;
import com.brienwheeler.svc.users.IUserAttributeService;
import com.brienwheeler.svc.users.domain.User;

@ContextConfiguration({
    "classpath:com/brienwheeler/lib/spring/beans/contextBeanDumper.xml",
	"classpath:com/brienwheeler/svc/authorize_net/cimClientService.xml" })
public abstract class AbstractSvcAuthorizeNetTest extends AbstractJUnit4SpringContextTests
{
	protected static final String ENABLE_NETWORKED_TESTS = "com.brienwheeler.svc.authorize_net.enableNetworkedTests";
	protected static final String CVV_NOT_PROCESSED_SHOULD_FAIL = "com.brienwheeler.svc.authorize_net.cvvNotProcessedShouldFail";
	protected static final String CVV_NOT_INDICATED_SHOULD_FAIL = "com.brienwheeler.svc.authorize_net.cvvNotIndicatedShouldFail";
	protected static final String CVV_NOT_CERTIFIED_SHOULD_FAIL = "com.brienwheeler.svc.authorize_net.cvvNotCertifiedShouldFail";
	
	protected static final String ZIP_CODE_OK = "11111";
	protected static final String ZIP_CODE_DECLINE = "46282";
	protected static final String ZIP_CODE_AVS_NA = "46206";
	
	protected static final String CVV_OK = "900";
	protected static final String CVV_MISMATCH = "901";
	protected static final String CVV_NOT_PROCESSED = "904";
	protected static final String CVV_NOT_INDICATED = "902";
	protected static final String CVV_NOT_CERTIFIED = "903";
	
	protected final Log log = LogFactory.getLog(getClass());
	
	protected final HashSet<String> profileIdsToClean = new HashSet<String>();
	protected ICIMClientService cimClientService;
	protected IUserAttributeService userAttributeService;
	protected TransactionWrapper transactionWrapper;
	
    @BeforeClass
    public static void oneTimeSetUp()
    {
        PropertyPlaceholderConfigurer.processLocation("classpath:com/brienwheeler/svc/authorize_net/test.properties");
        PropertyPlaceholderConfigurer.processLocation("classpath:com/brienwheeler/svc/users/test.properties");
        PropertyPlaceholderConfigurer.processLocation("classpath:com/brienwheeler/lib/db/test.properties");
    }

    @Before
	public void setUp() throws Exception
	{
		cimClientService = applicationContext.getBean("com.brienwheeler.svc.authorize_net.cimClientService",
				ICIMClientService.class);
		userAttributeService = applicationContext.getBean("com.brienwheeler.svc.users.userAttributeService",
				IUserAttributeService.class);
		transactionWrapper = applicationContext.getBean("com.brienwheeler.lib.db.transactionWrapper",
				TransactionWrapper.class);
	}

	@After
	public void cleanup() throws Exception
	{
		for (String customerProfileId : profileIdsToClean) {
			deleteProfileId(customerProfileId);
		}
	}
	
	protected String makeTestCustomerProfile(DbId<User> userId)
	{
		String customerProfileId = cimClientService.createCustomerProfile(userId);
		profileIdsToClean.add(customerProfileId);
		return customerProfileId;
	}
	
	protected void deleteProfileId(String customerProfileId) throws Exception
	{
		Transaction transaction = createTransaction(TransactionType.DELETE_CUSTOMER_PROFILE);
		transaction.setCustomerProfileId(customerProfileId);
		Result<Transaction> result = executeXML(transaction);
		if (result.isOk())
			log.info("cleaned up customer profile id " + customerProfileId);
		else
			log.warn("failed to clean up customer profile id " + customerProfileId + ": " + createErrorMessage(result));
	}
	
	protected String createPaymentProfile(String customerProfileId, String creditCardNumber, String zipCode) throws Exception
	{
		CreditCard creditCard = CreditCard.createCreditCard();
		creditCard.setCreditCardNumber(creditCardNumber);
		DateTime now = new DateTime();
		creditCard.setExpirationDate(String.format("%04d-%02d", now.getYear(), now.getMonthOfYear()));
		
		Payment payment = Payment.createPayment(creditCard);

		Address address = Address.createAddress();
		address.setFirstName("FirstName");
		address.setLastName("LastName");
		address.setAddress("1 Main Street");
		address.setCity("City");
		address.setState("State");
		address.setZipPostalCode(zipCode);
		
		PaymentProfile paymentProfile = PaymentProfile.createPaymentProfile();
		paymentProfile.addPayment(payment);
		paymentProfile.setBillTo(address);
		paymentProfile.setCustomerType(CustomerType.INDIVIDUAL); // doc says optional, but NPE without
		
		Transaction transaction = createTransaction(TransactionType.CREATE_CUSTOMER_PAYMENT_PROFILE);
		transaction.setCustomerProfileId(customerProfileId);
		transaction.addPaymentProfile(paymentProfile);

		Result<Transaction> result = executeXML(transaction);
		if (!result.isOk())
			throw new RuntimeException(createErrorMessage(result));
		
		return result.getCustomerPaymentProfileIdList().get(0);
	}
	
	protected Transaction createTransaction(TransactionType transactionType) throws Exception
	{
		CIMClientService target = AopTestUtils.getTarget(cimClientService);
		Method createTransactionMethod = CIMClientService.class.getDeclaredMethod("createTransaction", TransactionType.class);
		createTransactionMethod.setAccessible(true);
		return (Transaction) createTransactionMethod.invoke(target, transactionType);
	}
	
	protected String createErrorMessage(Result<Transaction> result) throws Exception
	{
		CIMClientService target = AopTestUtils.getTarget(cimClientService);
		Method createErrorMessageMethod = CIMClientService.class.getDeclaredMethod("createErrorMessage", Result.class);
		createErrorMessageMethod.setAccessible(true);
		return (String) createErrorMessageMethod.invoke(target, result);
	}
	
	protected Result<Transaction> executeXML(Transaction transaction)
	{
		CIMClientService target = AopTestUtils.getTarget(cimClientService);
		Environment environment = (Environment) ReflectionTestUtils.getField(target, "environment");
		BasicXmlDocument response = HttpClient.executeXML(environment, transaction);
		if (log.isDebugEnabled()) {
			log.debug(transaction.getCurrentRequest().dump());
			log.debug(response.dump());
		}
		return Result.createResult(transaction, response);
	}
}
