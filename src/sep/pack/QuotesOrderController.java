package sep.pack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.ib.controller.ApiConnection;
import com.ib.controller.ApiController.IConnectionHandler;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.Types.Action;

public class QuotesOrderController{

	private ApiConnection connection;
	private QuotesOrderLogger logger;
	public static HashMap<Integer, String> REQ_TO_TICKER = new HashMap<Integer, String>();

	public QuotesOrderController(IConnectionHandler handler, QuotesOrderProcessor p, QuotesOrderLogger log) {
		connection = new ApiConnection(p, new MyLogger(), new MyLogger());
		logger = log;
	}

	public void reqIDs(){
		connection.reqIds(1);
	}
		
	public void makeconnection(){
		connection.eConnect("127.0.0.1",7496,0);
	}
	
	public void reqMktData(List<String> tickers){
		if (!connection.isConnected()){
			makeconnection();
		}
		for (int i=0; i<tickers.size(); ++i){
			String ticker = tickers.get(i);
			QuotesOrderController.REQ_TO_TICKER.put(i, ticker);
			connection.reqMktData( i, OrderUtility.createContract(ticker).getContract(), "", false);
		}
		
		System.out.println("Quotes Request Sent");
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
	}
	
	public void sendOrder(String ticker, int quantity, Action action, double limitPrice) throws InterruptedException{
		if (!connection.isConnected()){
			makeconnection();
		}
		while (UserInfo.orderID.get() == -1){
			Thread.sleep(1000);
		}
		
		System.out.println("Placing Order for Order ID: " + (UserInfo.orderID.get() + 1));
		OrderUtility.displayTime();
		NewContract contract = OrderUtility.createContract(ticker);
		NewOrder order = OrderUtility.createNewOrder(quantity, action, false, limitPrice);
		connection.placeOrder(contract, order);
		logger.addActiveOrder(UserInfo.orderID.get());
		System.out.println("Orders Sent");
	}
	
	public void sendOrder(String ticker, int quantity, Action action) throws InterruptedException{
		if (!connection.isConnected()){
			makeconnection();
		}
		while (UserInfo.orderID.get() == -1){
			Thread.sleep(1000);
		}
		
		System.out.println("Placing Order for Order ID: " + (UserInfo.orderID.get() + 1));
		OrderUtility.displayTime();
		NewContract contract = OrderUtility.createContract(ticker);
		NewOrder order = OrderUtility.createNewOrder(quantity, action);
		connection.placeOrder(contract, order);
		logger.addActiveOrder(UserInfo.orderID.get());
		System.out.println("Orders Sent");
	}
	
	public void sendOrder(OrderContractContainer container) throws InterruptedException{
		if (!connection.isConnected()){
			makeconnection();
		}
		while (UserInfo.orderID.get() == -1){
			Thread.sleep(1000);
		}
		System.out.println("Placing Order for Order ID: " + (UserInfo.orderID.get() + 1));
		OrderUtility.displayTime();
		NewContract contract = container.getContract();
		NewOrder order = container.getOrder();
		connection.placeOrder(contract, order);
		System.out.println("Orders Sent");
	}
}
