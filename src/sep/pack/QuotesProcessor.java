package sep.pack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiController;

public class QuotesProcessor extends ApiController{
//	private Vector<String> dataHolder = new Vector<String>();
	private QuotesLogger records = new QuotesLogger();
	private AtomicInteger counter = new AtomicInteger(0);
	
	public QuotesProcessor(IConnectionHandler handler, ILogger inLogger,
			ILogger outLogger) {
		super(handler, inLogger, outLogger);
	}
	
	
	@Override public void tickGeneric(int reqId, int tickType, double value) {
		System.out.println("Geneic Update for Req ID: " + reqId + "; tickType: " + tickType + "; value: " + value);
	}

	@Override public void tickSize(int reqId, int tickType, int size) {
		//0-bidS, 3-askS, 5-lastS, 8-Volume
		if (tickType != 0 && tickType != 3){ return; }
		Quotes lastNbbo = records.getLatestNbbo(reqId);
		switch(tickType){
			case 0: lastNbbo.setBidSize(size);; break;
			case 3: lastNbbo.setAskSize(size); break;
		}
		records.updateLatestNbbo(reqId, lastNbbo);
		records.getStoredData().add(lastNbbo);
		counter.addAndGet(1);
	}

	@Override public void tickString(int reqId, int tickType, String value) {
		System.out.println("String Update for Req ID: " + reqId + "; tickType: " + tickType + "; value: " + value);
	}
	
	@Override public void tickPrice(int reqId, int tickType, double price, int canAutoExecute) {
		//1-bid, 2-ask, 4-last, 6-high, 7-low, 9-close
		if (tickType != 1 && tickType != 2){ return; }
		Quotes lastNbbo = records.getLatestNbbo(reqId);
		switch(tickType){
			case 1: lastNbbo.setBid(price); break;
			case 2: lastNbbo.setAsk(price); break;
		}
		records.updateLatestNbbo(reqId, lastNbbo);
		records.getStoredData().add(lastNbbo);
		//dataHolder.add(reqId + "," + tickType + "," + price + "\n");	
		counter.addAndGet(1);
		if (counter.get() > 10000){
			try{
				writeQuotes();
			}catch(IOException ex){}
		}
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
