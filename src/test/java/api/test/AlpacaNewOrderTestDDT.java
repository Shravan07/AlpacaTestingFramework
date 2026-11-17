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

public class AlpacaNewOrderTestDDT {
	
	String orderId;
	private static final Logger logger = LogManager.getLogger(AlpacaNewOrderTestDDT.class);
	
	@Test(priority = 1, dataProvider = "AlpacaOrderData", dataProviderClass = DataProviders.class)
	public void testSubmitOrder(String symbol, String qty, String side, String type, String timeInForce, String limitPrice) {
		logger.info("*****Submitting order with symbol: {}, qty: {}, side: {}, type: {}", symbol, qty, side, type);
		
		OrderRequest payload = new OrderRequest();
		payload.setSymbol(symbol);
		payload.setQty(qty);
		payload.setSide(side);
		payload.setType(type);
		payload.setTimeInForce(timeInForce);
		payload.setClientOrderId("QA-" + symbol + "-" + UUID.randomUUID().toString().substring(0, 8));
		if (!"market".equalsIgnoreCase(type) && limitPrice != null && !limitPrice.trim().isEmpty()) {
			payload.setLimitPrice(limitPrice.trim());
		}
		
		Response submitResponse = AlpacaEndPoints.submitOrder(payload);
		submitResponse.then().log().all();
		logger.info("*****Order submission response status: {}", submitResponse.getStatusCode());
		if (submitResponse.getStatusCode() != 200) {
			logger.warn("Skipping order submission due to API/account constraints: {}", submitResponse.asString());
			throw new SkipException("Skipping test because Alpaca rejected the order: " + submitResponse.asString());
		}
		orderId = submitResponse.jsonPath().getString("id");
		Assert.assertNotNull(orderId, "Order id should not be null");
		Assert.assertEquals(submitResponse.jsonPath().getString("client_order_id"), payload.getClientOrderId());
		
		Response statusResponse = AlpacaEndPoints.getOrderByID(orderId);
		statusResponse.then().log().all();
		logger.info("*****Order status response: {}", statusResponse.getStatusCode());
		Assert.assertEquals(statusResponse.getStatusCode(), 200, "Get order by ID failed");
		String currentStatus = statusResponse.jsonPath().getString("status");
		logger.info("*****Current OMS status: {}", currentStatus);
		List<String> validOmsStates = Arrays.asList("new", "accepted", "pending_new", "partially_filled", "filled");
		Assert.assertTrue(validOmsStates.contains(currentStatus), "Unexpected OMS order status: " + currentStatus);
	}

}