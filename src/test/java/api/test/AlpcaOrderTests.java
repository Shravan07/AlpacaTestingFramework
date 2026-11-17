package api.test;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.github.javafaker.Faker;

import api.endpoints.AlpacaEndPoints;
import api.payload.OrderRequest;
import io.restassured.response.Response;

public class AlpcaOrderTests {
	
	Faker faker;
	OrderRequest orderRequestPayload;
	String clientOrderId;
	String orderId;
	
	@BeforeClass
	public void setupData() {
		faker = new Faker();
		orderRequestPayload = new OrderRequest();
		String symbol = "AAPL";
		clientOrderId = "QA-" + symbol + "-" + faker.random().hex(8);
		String orderType = "market";
		String tif = "day";
		String side = "buy";
		String limitPrice = faker.commerce().price(100.0, 500.0);
		
		orderRequestPayload.setQty("1");
		orderRequestPayload.setSide(side);
		orderRequestPayload.setType(orderType);
		orderRequestPayload.setTimeInForce(tif);
		orderRequestPayload.setClientOrderId(clientOrderId);
		orderRequestPayload.setSymbol(symbol);
		
		if ("limit".equals(orderType)) {
			orderRequestPayload.setLimitPrice(limitPrice);
		}
	}
	
	@Test(priority = 1)
	public void testSubmitOrder() {
		Response response = AlpacaEndPoints.submitOrder(orderRequestPayload);
		response.then().log().all();
		Assert.assertEquals(response.getStatusCode(), 200, "Order submission failed");
		orderId = response.jsonPath().getString("id");
		Assert.assertNotNull(orderId, "Order id should not be null");
		clientOrderId = response.jsonPath().getString("client_order_id");
	}
	
	@Test(priority = 2)
	public void testGetOrderByID() {
		Response response = AlpacaEndPoints.getOrderByID(orderId);
		response.then().log().all();
		Assert.assertEquals(response.getStatusCode(), 200, "Get order by ID failed");
		
		String currentStatus = response.jsonPath().getString("status");
        List<String> validOmsStates = Arrays.asList("new", "accepted", "pending_new", "partially_filled", "filled");
        Assert.assertTrue(validOmsStates.contains(currentStatus), "Unexpected OMS order status: " + currentStatus);

	}
	
	@Test(priority = 3, dependsOnMethods = "testSubmitOrder")
	public void testReplaceOrder() {
		OrderRequest replacementPayload = new OrderRequest();
		replacementPayload.setQty("2");
		replacementPayload.setSide("buy");
		replacementPayload.setType("market");
		replacementPayload.setTimeInForce("day");
		replacementPayload.setClientOrderId("QA-AAPL-REPLACE-" + System.currentTimeMillis());
		replacementPayload.setSymbol("AAPL");
		
		Response response = AlpacaEndPoints.replaceOrder(orderId, replacementPayload);
		response.then().log().all();
		Assert.assertEquals(response.getStatusCode(), 200, "Order replacement failed");
		orderId = response.jsonPath().getString("id");
		Assert.assertNotNull(orderId, "Order id should not be null");
		clientOrderId = response.jsonPath().getString("client_order_id");
	}
	
	
	
}