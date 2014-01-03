package sep.pack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import com.ib.client.ComboLeg;
import com.ib.client.Contract;
import com.ib.controller.ApiConnection;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.OrderType;
import com.ib.controller.Types.Action;


public class OrderPlacer{
	// construct a contract
	public static String acct = ""; //"DU168728"
	public static AtomicInteger orderID = new AtomicInteger(0);
	
	private NewContract createContract(String ticker){
		Vector<ComboLeg> cblg = new Vector<ComboLeg>();
		Contract contract = new Contract(0, ticker, "STK", "", 0.0, "", "",
                "SMART", "USD", "", "", cblg, null, false, "", null);
		return new NewContract(contract);
	}
	
	private NewOrder createNewOrder(int quantity, Action buySell){
		return createNewOrder(quantity, buySell, false, 1);
	}
	
	// construct a order
	private NewOrder createNewOrder(int quantity, Action buySell, boolean isMarket, double limitPrice){
		NewOrder order = new NewOrder();
		order.account(OrderPlacer.acct);
		order.action(buySell);
		if (isMarket){
			order.orderType(OrderType.MKT);
		}else{
			order.orderType(OrderType.LMT);
		}
		order.totalQuantity(quantity);
		order.lmtPrice(limitPrice);
		order.orderId(OrderPlacer.orderID.incrementAndGet());
		return order;
	}
	
	public void sendOrder(ApiConnection connection, String ticker, int quantity, Action action){
		System.out.println("Placing Order for Order ID: " + (OrderPlacer.orderID.get() + 1));
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
		NewContract contract = createContract(ticker);
		NewOrder order = createNewOrder(quantity, action);
		connection.placeOrder(contract, order);
		System.out.println("Orders Sent");
	}
}