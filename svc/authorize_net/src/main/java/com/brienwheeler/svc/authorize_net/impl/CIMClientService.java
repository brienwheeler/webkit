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
 package com.brienwheeler.svc.authorize_net.impl;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.brienwheeler.lib.monitor.work.MonitoredWork;
import com.brienwheeler.lib.svc.GracefulShutdown;
import net.authorize.Environment;
import net.authorize.Merchant;
import net.authorize.ResponseCode;
import net.authorize.ResponseField;
import net.authorize.ResponseReasonCode;
import net.authorize.cim.Result;
import net.authorize.cim.Transaction;
import net.authorize.cim.TransactionType;
import net.authorize.cim.ValidationModeType;
import net.authorize.data.Order;
import net.authorize.data.cim.CustomerProfile;
import net.authorize.data.cim.DirectResponse;
import net.authorize.data.cim.PaymentProfile;
import net.authorize.data.cim.PaymentTransaction;
import net.authorize.data.xml.Payment;
import net.authorize.util.BasicXmlDocument;
import net.authorize.xml.Message;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.brienwheeler.lib.db.DbValidationUtils;
import com.brienwheeler.lib.db.TransactionWrapper;
import com.brienwheeler.lib.db.domain.DbId;
import com.brienwheeler.lib.util.OperationDisallowedException;
import com.brienwheeler.svc.authorize_net.AmountTooLargeException;
import com.brienwheeler.svc.authorize_net.AuthorizeNetException;
import com.brienwheeler.svc.authorize_net.CardExpiredException;
import com.brienwheeler.svc.authorize_net.ICIMClientService;
import com.brienwheeler.svc.authorize_net.InvalidCardAddressException;
import com.brienwheeler.svc.authorize_net.InvalidCardCodeException;
import com.brienwheeler.svc.authorize_net.InvalidCardNumberException;
import com.brienwheeler.svc.authorize_net.PaymentDeclinedException;
import com.brienwheeler.svc.authorize_net.PaymentMethod;
import com.brienwheeler.svc.users.IUserAttributeService;
import com.brienwheeler.svc.users.IUserService;
import com.brienwheeler.svc.users.domain.User;

public class CIMClientService extends AuthorizeNetClientBase implements ICIMClientService
{
	private static final String PRODUCTION_URL = "https://api.authorize.net/xml/v1/request.api";
	private static final String TEST_URL = "https://apitest.authorize.net/xml/v1/request.api";
	
	private static final String ELEMENT_TOKEN_OPEN = "<token>";
	private static final String ELEMENT_TOKEN_CLOSE = "</token>";
	
	private static final Map<ResponseReasonCode,Class<? extends AuthorizeNetException>> exceptionMap =
			new HashMap<ResponseReasonCode,Class<? extends AuthorizeNetException>>();

	private IUserAttributeService userAttributeService;
	private IUserService userService;
	private TransactionWrapper transactionWrapper;
	
	private Environment environment;
	private Merchant merchant;
	private String apiLoginID;
	private String transactionKey;
	private ValidationModeType validationMode = ValidationModeType.NONE;

	static {
		exceptionMap.put(ResponseReasonCode.RRC_2_27, InvalidCardAddressException.class);
		exceptionMap.put(ResponseReasonCode.RRC_2_37, InvalidCardNumberException.class);
		exceptionMap.put(ResponseReasonCode.RRC_2_44, InvalidCardCodeException.class);
		exceptionMap.put(ResponseReasonCode.RRC_2_65, InvalidCardCodeException.class);
		exceptionMap.put(ResponseReasonCode.RRC_2_127, InvalidCardAddressException.class);
		exceptionMap.put(ResponseReasonCode.RRC_2_315, InvalidCardNumberException.class);
		exceptionMap.put(ResponseReasonCode.RRC_2_317, CardExpiredException.class);
		exceptionMap.put(ResponseReasonCode.RRC_3_6, InvalidCardNumberException.class);
		exceptionMap.put(ResponseReasonCode.RRC_3_8, CardExpiredException.class);
		exceptionMap.put(ResponseReasonCode.RRC_3_49, AmountTooLargeException.class);
		exceptionMap.put(ResponseReasonCode.RRC_3_78, InvalidCardCodeException.class);
	}
	
	@Override
	protected void onStart() throws InterruptedException
	{
		super.onStart();
		merchant = Merchant.createMerchant(environment, apiLoginID, transactionKey);
	}

	@Override
	public boolean isProduction()
	{
		return ! merchant.isSandboxEnvironment();
	}
	
	@Override
	@MonitoredWork
    @GracefulShutdown
	// the write logic inside this function is in its own new transaction, so the interceptor can
	// treat this as a readOnly transaction
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	public String createCustomerProfile(DbId<User> userId)
	{
		String existingCustomerProfileId = userAttributeService.getAttribute(userId, ATTR_PROFILE_ID);
		if (existingCustomerProfileId != null)
			return existingCustomerProfileId;
		
		final User user = userService.findById(userId);
		DbValidationUtils.assertPersisted(user);
		
		CustomerProfile customerProfile = CustomerProfile.createCustomerProfile();
		customerProfile.setMerchantCustomerId(Long.toString(userId.getId()));

		Transaction transaction = createTransaction(TransactionType.CREATE_CUSTOMER_PROFILE);
		transaction.setCustomerProfile(customerProfile);

		Result<Transaction> result = executeTransaction("create profile", userId, transaction);
		final String createdCustomerProfileId = result.getCustomerProfileId();
		log.info("created Authorize.Net customer profile " + createdCustomerProfileId + " for " + user);
		
		// we want to commit our own transaction to prevent the record from being created at Authorize.Net
		// and then an exception in a calling function that might have a transaction open preventing
		// the save of the UserAttribute recording the customer profile ID
		return transactionWrapper.doInNewWriteTransaction(new Callable<String>() {
			@Override
			public String call() throws Exception
			{
				try {
					userAttributeService.setAttribute(user, ATTR_PROFILE_ID, createdCustomerProfileId);
				}
				catch (RuntimeException e) {
					cleanupProfileId(createdCustomerProfileId);
					throw e;
				}
				catch (Error e) {
					cleanupProfileId(createdCustomerProfileId);
					throw e;
				}
				return createdCustomerProfileId;
			}
		});
	}
	
	@Override
	@MonitoredWork
    @GracefulShutdown
	@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
	public List<PaymentMethod> getPaymentMethods(DbId<User> userId)
	{
		String customerProfileId = userAttributeService.getAttribute(userId, ATTR_PROFILE_ID);
		if (customerProfileId == null)
			return new ArrayList<PaymentMethod>();
		return getPaymentMethods(userId, customerProfileId);
	}
	
	private List<PaymentMethod> getPaymentMethods(DbId<User> userId, String customerProfileId)
	{
		Transaction transaction = createTransaction(TransactionType.GET_CUSTOMER_PROFILE);
		transaction.setCustomerProfileId(customerProfileId);
		
		Result<Transaction> result = executeTransaction("get payment methods", userId, transaction);
		
		List<PaymentMethod> paymentMethods = new ArrayList<PaymentMethod>();
		for (PaymentProfile paymentProfile : result.getCustomerPaymentProfileList()) {
			for (Payment payment : paymentProfile.getPaymentList()) {
				if (payment.getCreditCard() != null) {
					paymentMethods.add(new PaymentMethod(paymentProfile.getCustomerPaymentProfileId(),
							payment.getCreditCard().getCreditCardNumber()));
				}
			}
		}
		
		return paymentMethods;
	}
	
	@Override
	@MonitoredWork
    @GracefulShutdown
	@Transactional//(readOnly=true, propagation=Propagation.SUPPORTS)
	public String getHostedProfilePageToken(DbId<User> userId, String returnUrl)
	{
		// More than two years later this still isn't in their Java SDK.  Oh well, let's just do it
		// the stupid way...
		
		String customerProfileId = userAttributeService.getAttribute(userId, ATTR_PROFILE_ID);
		if (customerProfileId == null)
			customerProfileId = createCustomerProfile(userId);

		StringBuffer buffer = new StringBuffer(4096);
		buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		buffer.append("<getHostedProfilePageRequest xmlns=\"AnetApi/xml/v1/schema/AnetApiSchema.xsd\">\n");
		buffer.append("  <merchantAuthentication>\n");
		buffer.append("    <name>" + apiLoginID + "</name>");
		buffer.append("    <transactionKey>" + transactionKey + "</transactionKey>\n");
		buffer.append("  </merchantAuthentication>\n");
		buffer.append("  <customerProfileId>" + customerProfileId + "</customerProfileId> \n");
		buffer.append("  <hostedProfileSettings>\n");
		buffer.append("    <setting>\n");
		buffer.append("      <settingName>hostedProfileReturnUrl</settingName>\n");
		buffer.append("      <settingValue>" + returnUrl + "</settingValue>\n");
		buffer.append("    </setting>\n");
		buffer.append("  </hostedProfileSettings>\n");
		buffer.append("</getHostedProfilePageRequest>\n");
	
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(merchant.isSandboxEnvironment() ? TEST_URL : PRODUCTION_URL);
		EntityBuilder entityBuilder = EntityBuilder.create();
		entityBuilder.setContentType(ContentType.TEXT_XML);
		entityBuilder.setContentEncoding("utf-8");
		entityBuilder.setText(buffer.toString());
		httpPost.setEntity(entityBuilder.build());
		
		try {
			CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
			String response = EntityUtils.toString(httpResponse.getEntity());
			int start  = response.indexOf(ELEMENT_TOKEN_OPEN);
			if (start == -1)
				throw new AuthorizeNetException("error fetching hosted profile page token for " + userId + ", response: " + response);
			int end  = response.indexOf(ELEMENT_TOKEN_CLOSE);
			if (end == -1)
				throw new AuthorizeNetException("error fetching hosted profile page token for " + userId + ", response: " + response);
			return response.substring(start + ELEMENT_TOKEN_OPEN.length(), end);
				
		}
		catch (ClientProtocolException e) {
			throw new AuthorizeNetException(e.getMessage(), e);
		}
		catch (IOException e) {
			throw new AuthorizeNetException(e.getMessage(), e);
		}
	}
	
	@Override
	public String authorizePayment(DbId<User> userId, String paymentProfileId, BigDecimal amountToAuthorize)
	{
		return authorizePayment(userId, paymentProfileId, null, amountToAuthorize);
	}
	
	@Override
	@MonitoredWork
    @GracefulShutdown
	public String authorizePayment(DbId<User> userId, String paymentProfileId, String cardCode, BigDecimal amountToAuthorize)
	{
		String customerProfileId = userAttributeService.getAttribute(userId, ATTR_PROFILE_ID);
		if (customerProfileId == null)
			throw new OperationDisallowedException(userId + " has no Authorize.Net customer profile");
		
		List<PaymentMethod> paymentMethods = getPaymentMethods(userId, customerProfileId);
		PaymentMethod paymentMethodToUse = null; 
		for (PaymentMethod paymentMethod : paymentMethods)
			if (paymentMethod.getPaymentProfileId().equals(paymentProfileId)) {
				paymentMethodToUse = paymentMethod;
				break;
			}
		if (paymentMethodToUse == null)
			throw new OperationDisallowedException(userId + " does not have paymentProfileId " + paymentProfileId);

		Order order = Order.createOrder();
		order.setTotalAmount(amountToAuthorize);

		PaymentTransaction paymentTransaction = PaymentTransaction.createPaymentTransaction();
		paymentTransaction.setTransactionType(net.authorize.TransactionType.AUTH_ONLY);
		paymentTransaction.setCustomerPaymentProfileId(paymentProfileId);
		paymentTransaction.setOrder(order);
		if (cardCode != null)
			paymentTransaction.setCardCode(cardCode);
		
		Transaction transaction = createTransaction(TransactionType.CREATE_CUSTOMER_PROFILE_TRANSACTION);
		transaction.setPaymentTransaction(paymentTransaction);
		transaction.setCustomerProfileId(customerProfileId);
		
		Result<Transaction> result = executeTransaction("authorize", userId, amountToAuthorize, transaction);

		Map<ResponseField,String> responseMap = result.getDirectResponseList().get(0).getDirectResponseMap();
		ResponseReasonCode responseReasonCode = ResponseReasonCode.findByReasonCode(responseMap.get(ResponseField.RESPONSE_REASON_CODE));
		switch (responseReasonCode) {
			case RRC_1_1:
				log.info("successfully authorized payment of " + amountToAuthorize + " for " + userId + ": " + responseReasonCode);
				return responseMap.get(ResponseField.TRANSACTION_ID);
			case RRC_4_253:
				log.info("successfully authorized (but held for review) payment of " + amountToAuthorize + " for " + userId + ": " + responseReasonCode);
				return responseMap.get(ResponseField.TRANSACTION_ID);
			default :
				log.info("authorization failed in amount of " + amountToAuthorize + " for " + userId + ": " + responseReasonCode);
				throw new AuthorizeNetException(responseReasonCode.getReasonText());
		}
	}
	
	@Override
	@MonitoredWork
    @GracefulShutdown
	public String settlePayment(DbId<User> userId, String transactionId, BigDecimal amountToSettle)
	{
		String customerProfileId = userAttributeService.getAttribute(userId, ATTR_PROFILE_ID);
		if (customerProfileId == null)
			throw new OperationDisallowedException(userId + " has no Authorize.Net customer profile");
		
		Order order = Order.createOrder();
		order.setTotalAmount(amountToSettle);

		PaymentTransaction paymentTransaction = PaymentTransaction.createPaymentTransaction();
		paymentTransaction.setTransactionType(net.authorize.TransactionType.PRIOR_AUTH_CAPTURE);
		paymentTransaction.setTransactionId(transactionId);
		paymentTransaction.setOrder(order);

		Transaction transaction = createTransaction(TransactionType.CREATE_CUSTOMER_PROFILE_TRANSACTION);
		transaction.setPaymentTransaction(paymentTransaction);
		transaction.setCustomerProfileId(customerProfileId);
		
		Result<Transaction> result = executeTransaction("settle", userId, amountToSettle,transaction);

		Map<ResponseField,String> responseMap = result.getDirectResponseList().get(0).getDirectResponseMap();
		ResponseReasonCode responseReasonCode = ResponseReasonCode.findByReasonCode(responseMap.get(ResponseField.RESPONSE_REASON_CODE));
		if (responseReasonCode == ResponseReasonCode.RRC_1_1) {
			log.info("successfully settled payment of " + amountToSettle + " for " + userId);
			return responseMap.get(ResponseField.TRANSACTION_ID);
		}
		else
			throw new AuthorizeNetException(responseReasonCode.getReasonText());
	}
	
	private void cleanupProfileId(String customerProfileId)
	{
		Transaction transaction = createTransaction(TransactionType.DELETE_CUSTOMER_PROFILE);
		transaction.setCustomerProfileId(customerProfileId);
		BasicXmlDocument response = net.authorize.util.HttpClient.executeXML(environment, transaction);
		Result<Transaction> result = Result.createResult(transaction, response);
		if (!result.isOk()) {
			recordInterventionRequest("failed to clean up Authorize.Net customer profile id " + customerProfileId + " " + 
					createErrorMessage(result));
		}
	}
	
	private Transaction createTransaction(TransactionType transactionType)
	{
		Transaction transaction = merchant.createCIMTransaction(transactionType);
		transaction.setValidationMode(validationMode);
		return transaction;
	}
	
	private Result<Transaction> executeTransaction(String logOperation, DbId<User> userId, Transaction transaction)
	{
		return executeTransaction(logOperation, userId, new BigDecimal(0), transaction);
	}
	
	private Result<Transaction> executeTransaction(String logOperation, DbId<User> userId, BigDecimal amount, Transaction transaction)
	{
		BasicXmlDocument response = net.authorize.util.HttpClient.executeXML(environment, transaction);

		if (log.isInfoEnabled()) {
			BasicXmlDocument request = transaction.getCurrentRequest();
			if (request != null) {
				log.info(request.dump());
				log.info(response.dump());
			}
		}
		
		Result<Transaction> result = Result.createResult(transaction, response);
		
		List<DirectResponse> directResponses = result.getDirectResponseList();
		// check ResponseReasonCode to see if it means we should throw an exception
		if (directResponses != null && directResponses.size() > 0) {
			Map<ResponseField,String> directResponseMap = directResponses.get(0).getDirectResponseMap();
			ResponseCode responseCode = ResponseCode.findByResponseCode(directResponseMap.get(ResponseField.RESPONSE_CODE));
			ResponseReasonCode responseReasonCode = ResponseReasonCode.findByReasonCode(directResponseMap.get(ResponseField.RESPONSE_REASON_CODE));

			// check exceptionMap to see if we should throw specific exception
			Class<? extends AuthorizeNetException> exceptionClass = exceptionMap.get(responseReasonCode);
			if (exceptionClass != null) {
				log.info(logOperation + " failed in amount of " + amount + " for " + userId + ": " + responseReasonCode);
				try {
					Constructor<? extends AuthorizeNetException> constructor = exceptionClass.getConstructor(String.class);
					throw constructor.newInstance(responseReasonCode.getReasonText());
				}
				catch (NoSuchMethodException e) { /* fall through */ }
				catch (InvocationTargetException e) { /* fall through */ }
				catch (IllegalAccessException e) { /* fall through */ }
				catch (InstantiationException e) { /* fall through */ }
				log.warn("Exception class " + exceptionClass.getSimpleName() + " failed reflection instantiation");
				// we know we want an exception but failed to instantiate it.  Throw ANE as default
				throw new AuthorizeNetException(responseReasonCode.getReasonText());
			}

			// umbrella processing for DECLINED
			if (responseCode == ResponseCode.DECLINED) {
				log.info(logOperation + " failed in amount of " + amount + " for " + userId + ": " + responseReasonCode);
				throw new PaymentDeclinedException(responseReasonCode.getReasonText());
			}
		}
		
		// map all other failures (where no DirectResponseMap or RRC may not be in exceptionMap) into ANE
		if (!result.isOk()) {
			String message = createErrorMessage(result);
			log.warn(logOperation + " failed for " + userId + ": " + message);
			throw new AuthorizeNetException(message);
		}
		
		return result;
	}
	
	private String createErrorMessage(Result<Transaction> result)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(result.getResultCode());
		for (Message message : result.getMessages()) {
			buffer.append(" (");
			buffer.append(message.getCode());
			buffer.append(":");
			buffer.append(message.getText());
			buffer.append(")");
		}
		return buffer.toString();
	}
	
	@Required
	public void setUserService(IUserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setUserAttributeService(IUserAttributeService userAttributeService)
	{
		this.userAttributeService = userAttributeService;
	}

	@Required
	public void setEnvironment(String environment)
	{
		this.environment = Environment.valueOf(environment);
	}

	@Required
	public void setApiLoginID(String apiLoginID)
	{
		this.apiLoginID = apiLoginID;
	}

	@Required
	public void setTransactionKey(String transactionKey)
	{
		this.transactionKey = transactionKey;
	}

	public void setValidationMode(String validationMode)
	{
		this.validationMode = ValidationModeType.valueOf(validationMode);
	}

	@Required
	public void setTransactionWrapper(TransactionWrapper transactionWrapper)
	{
		this.transactionWrapper = transactionWrapper;
	}
	
}
