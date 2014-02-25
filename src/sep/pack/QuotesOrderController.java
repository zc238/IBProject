package sep.pack;

import java.util.HashMap;
import java.util.List;

import sep.pack.support.OrderUtility;
import sep.pack.support.UserInfo;

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

	public void disconnect(){
		connection.eDisconnect();
	}
	
	public void reqIDs(){
		connection.reqIds(1);
	}
		
	public void makeconnection(){
		connection.eConnect("127.0.0.1",7496,0);
	}
	
	// This method should be used for only delay measurement
	public void reqMktData(String ticker, boolean useSnapShot){
		if (!connection.isConnected()){
			makeconnection();
		}
		QuotesOrderLogger.RECORD_DATA.set(false);
		QuotesOrderController.REQ_TO_TICKER.put(0, ticker);
		connection.reqMktData( 0, OrderUtility.createContract(ticker).getContract(), "", useSnapShot);
	}
	
	public void reqMktData(List<String> tickers, boolean writeTofile){
		if (!connection.isConnected()){
			makeconnection();
		}

		QuotesOrderLogger.RECORD_DATA.set(writeTofile);
		for (int i=0; i<tickers.size(); ++i){
			String ticker = tickers.get(i);
			QuotesOrderController.REQ_TO_TICKER.put(i, ticker);
			connection.reqMktData( i, OrderUtility.createContract(ticker).getContract(), "", false);
		}
		
		System.out.println("Quotes Request Sent");
		OrderUtility.displayTime();
	}
	
	public void sendOrder(String ticker, int quantity, Action action, double limitPrice) throws InterruptedException{
		if (!connection.isConnected()){
			makeconnection();
		}
		while (UserInfo.getOrderID().get() == -1){
			Thread.sleep(1000);
		}
		
		OrderUtility.displayTime();
		NewContract contract = OrderUtility.createContract(ticker);
		NewOrder order = OrderUtility.createNewOrder(quantity, action, false, limitPrice);
		connection.placeOrder(contract, order);
		logger.addActiveOrder(UserInfo.getOrderID().get());
		System.out.println("Orders Sent");
	}
	
	public void sendOrder(String ticker, int quantity, Action action) throws InterruptedException{
		if (!connection.isConnected()){
			makeconnection();
		}
		while (UserInfo.getOrderID().get() == -1){
			Thread.sleep(1000);
		}
		
		OrderUtility.displayTime();
		NewContract contract = OrderUtility.createContract(ticker);
		NewOrder order = OrderUtility.createNewOrder(quantity, action);
		connection.placeOrder(contract, order);
		logger.addActiveOrder(UserInfo.getOrderID().get());
		System.out.println("Orders Sent");
	}
	
    public void reqPositions(boolean subscribe) {
    	connection.reqPositions();
//    	connection.reqAccountUpdates(subscribe, UserInfo.acct);
    }
    
	public void sendOrder(OrderContractContainer container) throws InterruptedException{
		if (!connection.isConnected()){
			makeconnection();
		}
		while (UserInfo.getOrderID().get() == -1){
			Thread.sleep(1000);
		}
		OrderUtility.displayTime();
		NewContract contract = container.getContract();
		NewOrder order = container.getOrder();
		connection.placeOrder(contract, order);
		System.out.println("Orders Sent");
	}
}
