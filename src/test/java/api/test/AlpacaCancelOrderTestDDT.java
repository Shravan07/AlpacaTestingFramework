package api.test;

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

public class AlpacaCancelOrderTestDDT {
	
	private static final Logger logger = LogManager.getLogger(AlpacaCancelOrderTestDDT.class);
	
	@Test(priority = 1, dataProvider = "AlpacaOrderData", dataProviderClass = DataProviders.class)
	public void testCancelOrder(String symbol, String qty, String side, String type, String timeInForce, String limitPrice) {
		logger.info("*****Creating a fresh order to cancel. symbol={}, qty={}, side={}, type={}, tif={}, limitPrice={}",
				symbol, qty, side, type, timeInForce, limitPrice);
		
		OrderRequest payload = new OrderRequest();
		payload.setSymbol(symbol);
		payload.setQty(qty);
		payload.setSide(side);
		payload.setType(type);
		payload.setTimeInForce(timeInForce);
		payload.setClientOrderId("QA-" + symbol + "-CANCEL-" + UUID.randomUUID().toString().substring(0, 8));
		if (!"market".equalsIgnoreCase(type) && limitPrice != null && !limitPrice.trim().isEmpty()) {
			payload.setLimitPrice(limitPrice.trim());
		}
		
		Response submitResponse = AlpacaEndPoints.submitOrder(payload);
		submitResponse.then().log().all();
		logger.info("*****Order submit response status: {}", submitResponse.getStatusCode());
		if (submitResponse.getStatusCode() != 200) {
			logger.warn("Skipping cancel test because order creation failed: {}", submitResponse.asString());
			throw new SkipException("Skipping cancel test because order creation failed: " + submitResponse.asString());
		}
		
		String orderId = submitResponse.jsonPath().getString("id");
		Assert.assertNotNull(orderId, "Order id should not be null");
		Assert.assertEquals(submitResponse.jsonPath().getString("client_order_id"), payload.getClientOrderId());
		
		Response cancelResponse = AlpacaEndPoints.deleteOrderByID(orderId);
		cancelResponse.then().log().all();
		logger.info("*****Order cancel response status: {}", cancelResponse.getStatusCode());
		Assert.assertTrue(cancelResponse.getStatusCode() == 200 || cancelResponse.getStatusCode() == 204,
				"Order cancellation Failed. Response status: " + cancelResponse.getStatusCode());
		if (cancelResponse.getStatusCode() == 200) {
			Assert.assertEquals(cancelResponse.jsonPath().getString("id"), orderId, "Cancelled order id mismatch");
			String cancelledStatus = cancelResponse.jsonPath().getString("status");
			logger.info("*****Cancelled order status: {}", cancelledStatus);
			Assert.assertTrue("canceled".equalsIgnoreCase(cancelledStatus), "Order status after cancel should be canceled. Actual: " + cancelledStatus);
		} else {
			logger.info("*****Delete endpoint returned 204 No Content; cancellation is successful for Alpaca API.");
		}
	}

}