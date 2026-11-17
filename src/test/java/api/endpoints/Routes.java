package api.endpoints;

public class Routes {

	//Routes class contains only URL's of API Endpoints
	
	public static String base_url = "https://paper-api.alpaca.markets/v2";
	
	//API Endpoints
	public static String createOrder_url = base_url + "/orders";
	public static String getAllOrder_url = base_url + "/orders";
	public static String getOrderByID_url = base_url + "/orders/{order_id}";
	public static String replaceOrderByID_url = base_url + "/orders/{order_id}";
	public static String deleteOrderByID_url = base_url + "/orders/{order_id}";
	
}