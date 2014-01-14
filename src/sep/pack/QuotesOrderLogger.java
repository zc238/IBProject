package sep.pack;

import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class QuotesOrderLogger { 
	private ConcurrentHashMap<String, Quotes> latestNbbo = new ConcurrentHashMap<String, Quotes>();
	private HashMap<String, Vector<Quotes>> storedData = new HashMap<String, Vector<Quotes>>();
	private HashMap<String, Integer> currentPositions = new HashMap<String, Integer>();
	
	public void addToCurrentPositions(String ticker, int amount){
		if (currentPositions.get(ticker)!=null){
			currentPositions.put(ticker, currentPositions.get(ticker) + amount);
		}else{
			currentPositions.put(ticker, amount);
		}
	}
	
	public int getPosition(String ticker){
		if (currentPositions.get(ticker)!=null){
			return currentPositions.get(ticker);
		}else{
			return 0;
		}
	}
	
	public HashMap<String, Vector<Quotes>> getStoredData() {
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
