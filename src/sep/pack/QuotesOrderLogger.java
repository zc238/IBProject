package sep.pack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import sep.pack.data.Quotes;

public class QuotesOrderLogger { 
	private ConcurrentHashMap<String, Quotes> latestNbbo = new ConcurrentHashMap<String, Quotes>();
		
	private ConcurrentHashMap<String, List<Quotes>> storedData = new ConcurrentHashMap<String, List<Quotes>>();
	private ConcurrentHashMap<String, Integer> currentPositions = new ConcurrentHashMap<String, Integer>();
	private ConcurrentHashMap<String, Integer> submittedPositions = new ConcurrentHashMap<String, Integer>();
	private Set<Integer> activeOrders = new HashSet<Integer>();
	public static AtomicBoolean RECORD_DATA = new AtomicBoolean(true);
	public static final int quotesLimit = 1000000;
	
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
	
	public synchronized ConcurrentHashMap<String, List<Quotes>> getStoredData() {
		return storedData;
	}
	
	public synchronized void addQuotesToRecords(String ticker, Quotes q){
		if (storedData.get(ticker) == null){
			storedData.put(ticker, Collections.synchronizedList(new ArrayList<Quotes>()));
		}
		if (storedData.get(ticker).size() > QuotesOrderLogger.quotesLimit){
			storedData.get(ticker).remove(0);
		}
//		System.out.println("ADD " + ticker + ", for mid price " + q.getMidPrice());
		storedData.get(ticker).add(q);
	}
	
	public synchronized ConcurrentHashMap<String, Quotes> getNbboMap(){
		return latestNbbo;
	}
	
	public synchronized void updateLatestNbbo(String ticker, Quotes q){
		latestNbbo.put(ticker, q);
	}
	
	public synchronized Quotes getLatestNbbo(String ticker){
		if (!latestNbbo.containsKey(ticker)){
			latestNbbo.put(ticker, new Quotes());
		}
		return latestNbbo.get(ticker);
	}
}
