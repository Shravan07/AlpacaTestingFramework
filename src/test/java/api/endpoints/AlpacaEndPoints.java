package api.endpoints;

import static io.restassured.RestAssured.given;

import java.util.ResourceBundle;

import api.payload.OrderRequest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class AlpacaEndPoints {
	
	//Reads the properties file and returns the ResourceBundle object
	static ResourceBundle getProps() {
		 ResourceBundle routes = ResourceBundle.getBundle("routes"); //file name without extension
		 return routes;
	}
	
	private static String getApiKeyId() {
		return System.getProperty("APCA_API_KEY_ID", getProps().getString("apca_key_id"));
	}
	
	private static String getApiSecretKey() {
		return System.getProperty("APCA_API_SECRET_KEY", getProps().getString("apca_secret_key"));
	}
	
	public static Response submitOrder(OrderRequest payload) {
		String createOrder_url = getProps().getString("base_url") + getProps().getString("orders_url");
		
		return given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.header("APCA-API-KEY-ID", getApiKeyId())
			.header("APCA-API-SECRET-KEY", getApiSecretKey())
			.body(payload)
		.when()
			.post(createOrder_url);
	}
	
	
	public static Response getOrderByID(String orderId) {
		String getOrderByID_url = getProps().getString("base_url") + getProps().getString("order_by_id_url");
		
		return given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.header("APCA-API-KEY-ID", getApiKeyId())
			.header("APCA-API-SECRET-KEY", getApiSecretKey())
			.pathParam("order_id", orderId)
		.when()
			.get(getOrderByID_url);
	}
	
	public static Response replaceOrder(String orderId, OrderRequest payload) {
		String replaceOrderByID_url = getProps().getString("base_url") + getProps().getString("order_by_id_url");
		
		return given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.header("APCA-API-KEY-ID", getApiKeyId())
			.header("APCA-API-SECRET-KEY", getApiSecretKey())
			.body(payload)
			.pathParam("order_id", orderId)
		.when()
			.patch(replaceOrderByID_url);
	}
	
	public static Response deleteOrderByID(String orderId) {
		String deleteOrderByID_url = getProps().getString("base_url") + getProps().getString("order_by_id_url");
		
		return given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.header("APCA-API-KEY-ID", getApiKeyId())
			.header("APCA-API-SECRET-KEY", getApiSecretKey())
			.pathParam("order_id", orderId)
		.when()
			.delete(deleteOrderByID_url);
	}

}