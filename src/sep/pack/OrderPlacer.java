package sep.pack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.ib.controller.ApiConnection;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.Types.Action;


public class OrderPlacer{
	// construct a contract
	public static String acct = ""; //"DU168728"
	public static AtomicInteger orderID = new AtomicInteger(0);
	
	public void sendOrder(ApiConnection connection, String ticker, int quantity, Action action){
		System.out.println("Placing Order for Order ID: " + (OrderPlacer.orderID.get() + 1));
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
		NewContract contract = OrderUtility.createContract(ticker);
		NewOrder order = OrderUtility.createNewOrder(quantity, action);
		connection.placeOrder(contract, order);
		System.out.println("Orders Sent");
	}
	
	public void sendOrder(ApiConnection connection, OrderContractContainer container){
		System.out.println("Placing Order for Order ID: " + (OrderPlacer.orderID.get() + 1));
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
		NewContract contract = container.getContract();
		NewOrder order = container.getOrder();
		connection.placeOrder(contract, order);
		System.out.println("Orders Sent");
	}
}