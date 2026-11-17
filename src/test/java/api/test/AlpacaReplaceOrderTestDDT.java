package api.test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import api.endpoints.AlpacaEndPoints;
import api.payload.OrderRequest;
import api.utilities.DataProviders;
import io.restassured.response.Response;

public class AlpacaReplaceOrderTestDDT {
	
	String orderId;
	private static final Logger logger = LogManager.getLogger(AlpacaReplaceOrderTestDDT.class);
	
	@Test(priority = 1, dataProvider = "AlpacaOrderReplaceData", dataProviderClass = DataProviders.class)
	public void testReplaceOrder(String symbol, String qty, String side, String type, String timeInForce, String limitPrice) {
		logger.info("*****Creating a fresh order for replacement. symbol={}, qty={}, side={}, type={}, tif={}, limitPrice={}",
				symbol, qty, side, type, timeInForce, limitPrice);
		
		String replaceableOrderId = null;
		String currentStatus = null;
		List<String> replaceableStates = Arrays.asList("new", "pending_new");
		
		for (int attempt = 1; attempt <= 2; attempt++) {
			OrderRequest createPayload = new OrderRequest();
			createPayload.setSymbol(symbol);
			createPayload.setQty(qty);
			createPayload.setSide(side);
			createPayload.setType(type);
			createPayload.setTimeInForce(timeInForce);
			createPayload.setClientOrderId("QA-" + symbol + "-REPLACE-CREATE-" + UUID.randomUUID().toString().substring(0, 8));
			if (!"market".equalsIgnoreCase(type) && limitPrice != null && !limitPrice.trim().isEmpty()) {
				createPayload.setLimitPrice(limitPrice.trim());
			}
			
			Response createResponse = AlpacaEndPoints.submitOrder(createPayload);
			createResponse.then().log().all();
			if (createResponse.getStatusCode() != 200) {
				logger.warn("Replacement-order creation failed with status {}. Message: {}",
						createResponse.getStatusCode(), createResponse.asString());
				throw new SkipException("Skipping replacement test because order creation failed: " + createResponse.asString());
			}
			replaceableOrderId = createResponse.jsonPath().getString("id");
			Assert.assertNotNull(replaceableOrderId, "Replaceable order id should not be null");
			
			Response statusResponse = AlpacaEndPoints.getOrderByID(replaceableOrderId);
			statusResponse.then().log().all();
			currentStatus = statusResponse.jsonPath().getString("status");
			logger.info("*****Current order status before replacement attempt {}: {}", attempt, currentStatus);
			if (replaceableStates.contains(currentStatus != null ? currentStatus.toLowerCase() : "")) {
				break;
			}
			logger.warn("Order is not replaceable yet (status={}). Retrying order creation for replacement...", currentStatus);
			try {
				Thread.sleep(2000L);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("Interrupted while waiting for replaceable order status", e);
			}
		}
		
		if (!replaceableStates.contains(currentStatus != null ? currentStatus.toLowerCase() : "")) {
			throw new SkipException("Skipping replacement test because Alpaca did not return a replaceable order status. Last status: " + currentStatus);
		}
		
		String clientOrderId = "QA-" + symbol + "-REPLACE-" + UUID.randomUUID().toString().substring(0, 8);
		OrderRequest replacePayload = new OrderRequest();
		replacePayload.setSymbol(symbol);
		replacePayload.setQty(qty);
		replacePayload.setSide(side);
		replacePayload.setType(type);
		replacePayload.setTimeInForce(timeInForce);
		replacePayload.setClientOrderId(clientOrderId);
		if (!"market".equalsIgnoreCase(type) && limitPrice != null && !limitPrice.trim().isEmpty()) {
			replacePayload.setLimitPrice(limitPrice.trim());
		}
		
		Response replaceResponse = AlpacaEndPoints.replaceOrder(replaceableOrderId, replacePayload);
		replaceResponse.then().log().all();
		logger.info("*****Order replacement response status: {}", replaceResponse.getStatusCode());
		Assert.assertEquals(replaceResponse.getStatusCode(), 200, "Order replacement Failed");
		String replacedOrderId = replaceResponse.jsonPath().getString("id");
		Assert.assertNotNull(replacedOrderId, "Order id should not be null");
		Assert.assertEquals(replaceResponse.jsonPath().getString("client_order_id"), clientOrderId);
		orderId = replacedOrderId;
	}

}
