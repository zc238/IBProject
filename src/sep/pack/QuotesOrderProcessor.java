package sep.pack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import sep.pack.data.Quotes;
import sep.pack.data.TICKER;
import sep.pack.support.OrderUtility;
import sep.pack.support.UserInfo;

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
	private QuotesOrderLogger records;
	private AtomicInteger counter = new AtomicInteger(0);
	private String dataPath;
	private String cleanDataPath;
	
	public QuotesOrderProcessor(IConnectionHandler handler, ILogger inLogger, ILogger outLogger, QuotesOrderLogger r) {
		super(handler, inLogger, outLogger);
		records = r;
	}
	public QuotesOrderProcessor(IConnectionHandler handler, ILogger inLogger, 
								ILogger outLogger, QuotesOrderLogger r, String dP, String cdP) {
		super(handler, inLogger, outLogger);
		dataPath = dP;
		cleanDataPath = cdP;
		records = r;
	}
	
	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}
	
	private void displayTimeNQuote(Quotes q){
		System.out.println(q.toString());
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
	}
	
	@Override public void tickGeneric(int reqId, int tickType, double value) {
		System.out.println("Geneic Update for Req ID: " + reqId + "; tickType: " + tickType + "; value: " + value);
	}

	@Override public void tickSize(int reqId, int tickType, int size) {
		//0-bidS, 3-askS, 5-lastS, 8-Volume
		if (tickType != 0 && tickType != 3){ return; }
		Quotes lastNbbo = records.getLatestNbbo(QuotesOrderController.REQ_TO_TICKER.get(reqId));
		switch(tickType){
			case 0: lastNbbo.setBidSize(size); 
					lastNbbo.updateTimeStamp();
					break;
			case 3: lastNbbo.setAskSize(size); 
					lastNbbo.updateTimeStamp();
					break;
		}
		String ticker = QuotesOrderController.REQ_TO_TICKER.get(reqId);
		records.updateLatestNbbo(ticker, lastNbbo);
		//records.addQuotesToRecords(ticker, new Quotes(lastNbbo)); //#Quote
		counter.addAndGet(1);
		if (QuotesOrderLogger.RECORD_DATA.get()){
			writeNbboToFile(ticker);
			chainWrite(ticker);
			displayTimeNQuote(lastNbbo);
		}
	}

	@Override public void tickString(int reqId, int tickType, String value) {
		//System.out.println("String Update for Req ID: " + reqId + "; tickType: " + tickType + "; value: " + value);
	}
	
	private void chainWrite(String ticker){
		for (int j=0; j<TICKER.TICKERS.size(); ++j){
			if (ticker.equals(TICKER.TICKERS.get(j))){ continue; }
			else{
				try {
					writeToCleanData(ticker, TICKER.TICKERS.get(j));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override public void tickPrice(int reqId, int tickType, double price, int canAutoExecute) {
		//1-bid, 2-ask, 4-last, 6-high, 7-low, 9-close
		if (tickType != 1 && tickType != 2){ return; }
		Quotes lastNbbo = records.getLatestNbbo(QuotesOrderController.REQ_TO_TICKER.get(reqId));
		switch(tickType){
			case 1: lastNbbo.setBid(price); 
					lastNbbo.updateTimeStamp(); break;
			case 2: lastNbbo.setAsk(price); 
					lastNbbo.updateTimeStamp();
					break;
		}
		String ticker = QuotesOrderController.REQ_TO_TICKER.get(reqId);
		records.updateLatestNbbo(ticker, lastNbbo);
		records.addQuotesToRecords(ticker, new Quotes(lastNbbo));
		counter.addAndGet(1);
		if (QuotesOrderLogger.RECORD_DATA.get()){
			writeNbboToFile(ticker);
			chainWrite(ticker);
			displayTimeNQuote(lastNbbo);
		}
	}
	
	@Override public void nextValidId(int orderId) {
		if (orderId > UserInfo.getOrderID().get()){
			UserInfo.getOrderID().set(orderId);
		}
	}
	
	@Override public void position(String account, Contract contractIn, int pos, double avgCost) {
		NewContract contract = new NewContract( contractIn);
		records.addToCurrentPositions(contract.symbol(), pos);
	}

	//TODO, refactor this code
	@Override public void openOrder(int orderId, Contract contract, Order orderIn, OrderState orderState) {
		UserInfo.acct = orderIn.m_account;
		System.out.println("Receiving Order Information for Order ID: " + orderId);
		System.out.println(orderIn.m_action + " " + contract.m_symbol + 
							". The state is: " + orderState.m_status + 
							". OrderType: " + orderIn.m_orderType + 
							". Quantity: " + orderIn.m_totalQuantity);
		if (!records.hasOrder(orderId)){ //otherwise, it is already processed
			if (orderState.m_status.toUpperCase().equals("FILLED")){
				if (orderIn.m_action.toUpperCase().equals("BUY")){
					records.addToCurrentPositions(contract.m_symbol, orderIn.m_totalQuantity);
				}else{
					records.addToCurrentPositions(contract.m_symbol, -orderIn.m_totalQuantity);
				}
				records.removeActiveOrder(orderId);
			}
		}
//		if (orderState.m_status.toUpperCase().equals("PRESUBMITTED")
//		|| orderState.m_status.toUpperCase().equals("SUBMITTED")
//		|| orderState.m_status.toUpperCase().equals("PENDINGSUBMITTED")){
//		if (orderIn.m_action.toUpperCase().equals("BUY")){
//			records.addToSubmittedPositions(contract.m_symbol, orderIn.m_totalQuantity);
//		}else{
//			records.addToSubmittedPositions(contract.m_symbol, -orderIn.m_totalQuantity);
//		}
//	}else 
		OrderUtility.displayTime();
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
		//TODO, update order status (i.e. update using QuotesOrderLogger(records))
		//Also need to get rid of submitted orders
		//This method will not get triggered for paper trading accounts
		OrderUtility.displayTime();
	}
	
	public void handle(int orderId, int errorCode, String errorMsg) {
		System.out.println("Order ID: " + orderId + ". Error Message: " + errorMsg);
	}
	
	private synchronized void writeNbboToFile(String ticker){
		try{
			String fileName = dataPath + "/"
								+ ticker + "_"
								+ new SimpleDateFormat("dd-MMM-yyyy").format(new Date())
								+ ".csv";
			File quotes = new File(fileName);
			FileWriter writer = new FileWriter(quotes, true);
			ConcurrentHashMap<String, Quotes> nbboMap = records.getNbboMap();
			String row = OrderUtility.getMiliSecondsDiffFromNine() + ","; //milisecond time stamp
			Quotes q = nbboMap.get(ticker);
			if (q.hasZero()){ writer.close(); return; }
			row += nbboMap.get(ticker).toStringOnlyQ();
			row += "\n";
			writer.write(row);
			writer.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public synchronized void writeToCleanData(String ticker1, String ticker2) throws IOException{
		String fileName = cleanDataPath + "/"
							+ ticker1 + "_" + ticker2 + "_"
							+ new SimpleDateFormat("dd-MMM-yyyy").format(new Date())
							+ ".csv";
		File quotes = new File(fileName);
		FileWriter writer = new FileWriter(quotes, true);
		ConcurrentHashMap<String, Quotes> nbboMap = records.getNbboMap();
		String row = OrderUtility.getMiliSecondsDiffFromNine() + ","; //milisecond time stamp
		Quotes q1 = records.getLatestNbbo(ticker1);
		Quotes q2 = records.getLatestNbbo(ticker2);
		
		if (q1.hasZero() || q2.hasZero()){ writer.close(); return; }
		
		row += nbboMap.get(ticker1).toStringOnlyQ();
		row += nbboMap.get(ticker2).toStringOnlyQ();
		row += "\n";
		writer.write(row);
		writer.close();
	}
}
