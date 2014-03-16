package sep.pack.data;

import java.util.LinkedList;
import java.util.List;

public class TICKER {
	public static final String SPY = "SPY";
	public static final String SPX = "SPXU";
	public static final String UPR = "UPRO";
	public static final String SDS = "SDS";
	public static final String SH = "SH";
	public static final String SSO = "SSO";
	
	public static final List<String> TICKERS = new LinkedList<String>(){
		private static final long serialVersionUID = 1L;
	{
		add(TICKER.SPY);
//		add(TICKER.SSO); 
//		add(TICKER.SDS); 
//		add(TICKER.UPR); 
//		add(TICKER.SPX); 
		add(TICKER.SH); 
	}};
}
