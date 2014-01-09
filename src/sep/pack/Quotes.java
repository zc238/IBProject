package sep.pack;

import java.text.SimpleDateFormat;
import java.util.Date;


public class Quotes{
	private double bid;
	private double ask;
	private int bidSize;
	private int askSize;
	private String ticker;
	Date localTimeStamp;
	private int reqId;
	
	public long getBidSize() {
		return bidSize;
	}
	public long getAskSize() {
		return askSize;
	}
	public Date getLocalTimeStamp() {
		return localTimeStamp;
	}
	public int getReqId() {
		return reqId;
	}
	
	public String getTicker() {
		return ticker;
	}
	
	public Quotes(){
		localTimeStamp = new Date();
	}
	
	public Quotes(double b, double a, int bS, int aS, int id, String tick){
		bid = b;
		ask = a;
		bidSize = bS;
		askSize = aS;
		localTimeStamp = new Date();
		reqId = id;
		ticker = tick;
	}
	
	public double getBid() {
		return bid;
	}
	public void setBid(double bid) {
		this.bid = bid;
	}
	public double getAsk() {
		return ask;
	}
	public void setAsk(double ask) {
		this.ask = ask;
	}
	public void setBidSize(int bidSize) {
		this.bidSize = bidSize;
	}
	public void setAskSize(int askSize) {
		this.askSize = askSize;
	}

	public void setReqId(int reqId) {
		this.reqId = reqId;
	}
	
	public String toStringOnlyQ(){
		return bid + "," + ask + "," + bidSize  + "," + askSize + "," ;
	}
	
	public boolean hasZero(){
		if (bid == 0 || ask == 0 && bidSize == 0 && askSize == 0){
			return true;
		}
		return false;
	}
	public String toString(){
		
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(localTimeStamp) + "," + ask + "," + askSize + "," + bid + "," + bidSize + ">\n";
	}
}