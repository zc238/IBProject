package sep.pack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiController;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.NewOrderState;
import com.ib.controller.OrderStatus;

public class QuotesOrderProcessor extends ApiController{
//	private Vector<String> dataHolder = new Vector<String>();
	private QuotesLogger records = new QuotesLogger();
	private AtomicInteger counter = new AtomicInteger(0);
	private QuotesOrderController controller;
	
	private void displayTimeNQuote(Quotes q){
		System.out.println(q.toString());
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
	}
	
	public QuotesOrderProcessor(IConnectionHandler handler, ILogger inLogger, ILogger outLogger, QuotesOrderController control) {
		super(handler, inLogger, outLogger);
		controller = control;
		try{
			controller.getLock().acquire();
		}catch(Exception e){
			System.out.println("Fail to Acquire Lock");
		}
	}
	
	
	@Override public void tickGeneric(int reqId, int tickType, double value) {
		System.out.println("Geneic Update for Req ID: " + reqId + "; tickType: " + tickType + "; value: " + value);
	}

	@Override public void tickSize(int reqId, int tickType, int size) {
		//0-bidS, 3-askS, 5-lastS, 8-Volume
		if (tickType != 0 && tickType != 3){ return; }
		Quotes lastNbbo = records.getLatestNbbo(QuotesOrderController.REQ_TO_TICKER.get(reqId));
		switch(tickType){
			case 0: lastNbbo.setBidSize(size);; break;
			case 3: lastNbbo.setAskSize(size); break;
		}
		records.updateLatestNbbo(QuotesOrderController.REQ_TO_TICKER.get(reqId), lastNbbo);
		records.getStoredData().add(lastNbbo);
		counter.addAndGet(1);
		try{
			writeNbboToFile();
		}catch(IOException ex){}
		displayTimeNQuote(lastNbbo);
	}

	@Override public void tickString(int reqId, int tickType, String value) {
		System.out.println("String Update for Req ID: " + reqId + "; tickType: " + tickType + "; value: " + value);
	}
	
	@Override public void tickPrice(int reqId, int tickType, double price, int canAutoExecute) {
		//1-bid, 2-ask, 4-last, 6-high, 7-low, 9-close
		if (tickType != 1 && tickType != 2){ return; }
		Quotes lastNbbo = records.getLatestNbbo(QuotesOrderController.REQ_TO_TICKER.get(reqId));
		switch(tickType){
			case 1: lastNbbo.setBid(price); break;
			case 2: lastNbbo.setAsk(price); break;
		}
		records.updateLatestNbbo(QuotesOrderController.REQ_TO_TICKER.get(reqId), lastNbbo);
		records.getStoredData().add(lastNbbo);
		//dataHolder.add(reqId + "," + tickType + "," + price + "\n");	
		counter.addAndGet(1);
		try{
			writeNbboToFile();
		}catch(IOException ex){}
//		if (counter.get() > 10000){
//			try{
//				writeQuotes();
//			}catch(IOException ex){}
//		}
		displayTimeNQuote(lastNbbo);
	}
	
	@Override public void nextValidId(int orderId) {
		OrderPlacer.orderID.set(orderId);
		controller.getLock().release();
	}
	
	@Override public void openOrder(int orderId, Contract contract, Order orderIn, OrderState orderState) {
		OrderPlacer.acct = orderIn.m_account; //update acct, a hack...
		System.out.println("Receiving Order Information for Order ID: " + orderId);
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
	}
	
	public void openOrder(NewContract contract, NewOrder order, NewOrderState orderState) {
		System.out.println("Open");
	}

	protected boolean shouldAdd(NewContract contract, NewOrder order, NewOrderState orderState) {
		return true;
	}


	public void openOrderEnd() {
		System.out.println("End of receiving Order Info");
	}
	
	public void orderStatus(int orderId, OrderStatus status, int filled, int remaining, double avgFillPrice, long permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
	}
	
	public void handle(int orderId, int errorCode, String errorMsg) {
		System.out.println("Order ID: " + orderId + ". Error Message: " + errorMsg);
	}
	
	private synchronized void writeNbboToFile() throws IOException{
		File quotes = new File("C:/cfem2013/quotes.csv");
		FileWriter writer = new FileWriter(quotes,true);
		ConcurrentHashMap<String, Quotes> nbboMap = records.getNbboMap();
		String row = new Date().toString() + ",";
		for (String ticker : nbboMap.keySet()){
			row += nbboMap.get(ticker).toStringOnlyQ();
		}
		row += "\n";
		writer.write(row);
		writer.close();
	}
	
	private synchronized void writeQuotes() throws IOException{
		File quotes = new File("C:/cfem2013/quotes.csv");
		FileWriter writer = new FileWriter(quotes,true);
		for (Quotes q : records.getStoredData()){
			writer.write(q.toString());
		}
		records.getStoredData().clear();
		writer.close();
	}
}
