package sep.pack;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class QuotesLogger { 
	private ConcurrentHashMap<String, Quotes> latestNbbo = new ConcurrentHashMap<String, Quotes>();
	private Vector<Quotes> storedData = new Vector<Quotes>();
	
	public Vector<Quotes> getStoredData() {
		return storedData;
	}
	
	public ConcurrentHashMap<String, Quotes> getNbboMap(){
		return latestNbbo;
	}
	
	public void updateLatestNbbo(String ticker, Quotes q){
		latestNbbo.put(ticker, q);
	}
	
	public Quotes getLatestNbbo(String ticker){
		if (!latestNbbo.containsKey(ticker)){
			latestNbbo.put(ticker, new Quotes());
		}
		return latestNbbo.get(ticker);
	}
}
