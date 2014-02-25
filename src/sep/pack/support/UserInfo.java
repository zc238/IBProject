package sep.pack.support;

import java.util.concurrent.atomic.AtomicInteger;

public final class UserInfo {
	public static String acct = ""; 
	private static AtomicInteger orderID = new AtomicInteger(-1);
	public static synchronized AtomicInteger getOrderID(){ return orderID; }
	public static synchronized int incrementOrderId(){
		return orderID.addAndGet(1);
	}
}
