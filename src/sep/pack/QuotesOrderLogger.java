package sep.pack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class QuotesOrderLogger { 
	private ConcurrentHashMap<String, Quotes> latestNbbo = new ConcurrentHashMap<String, Quotes>();
	private HashMap<String, Vector<Quotes>> storedData = new HashMap<String, Vector<Quotes>>();
	private HashMap<String, Integer> currentPositions = new HashMap<String, Integer>();
	private HashMap<String, Integer> submittedPositions = new HashMap<String, Integer>();
	private Set<Integer> activeOrders = new HashSet<Integer>();
	
	public void addActiveOrder(int orderId){
		activeOrders.add(orderId);
	}
	
	public void removeActiveOrder(int orderId){
		activeOrders.remove(orderId);
	}
	
	public boolean hasOrder(int orderId){
		return activeOrders.contains(orderId);
	}
	
	public void addToCurrentPositions(String ticker, int amount){
		if (currentPositions.get(ticker)!=null){
			currentPositions.put(ticker, currentPositions.get(ticker) + amount);
		}else{
			currentPositions.put(ticker, amount);
		}
	}
	
	public void addToSubmittedPositions(String ticker, int amount){
		if (submittedPositions.get(ticker)!=null){
			submittedPositions.put(ticker, submittedPositions.get(ticker) + amount);
		}else{
			submittedPositions.put(ticker, amount);
		}
	}
	
	public int getUnfilledPosition(String ticker){
		if (submittedPositions.get(ticker)!=null){
			return submittedPositions.get(ticker);
		}else{
			return 0;
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
	
	public void addQuotesToRecords(String ticker, Quotes q){
		if (storedData.get(ticker) == null){
			storedData.put(ticker, new Vector<Quotes>());
		}
		storedData.get(ticker).add(q);
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
