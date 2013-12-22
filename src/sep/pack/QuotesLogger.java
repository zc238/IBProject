package sep.pack;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class QuotesLogger {
	private ConcurrentHashMap<Integer, Quotes> latestNbbo = new ConcurrentHashMap<Integer, Quotes>();
	private Vector<Quotes> storedData = new Vector<Quotes>();
	public Vector<Quotes> getStoredData() {
		return storedData;
	}
	public void updateLatestNbbo(int reqId, Quotes q){
		latestNbbo.put(reqId, q);
	}
	
	public Quotes getLatestNbbo(int reqId){
		if (!latestNbbo.containsKey(reqId)){
			latestNbbo.put(reqId, new Quotes());
		}
		return latestNbbo.get(reqId);
	}
}
