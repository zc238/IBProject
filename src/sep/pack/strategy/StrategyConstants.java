package sep.pack.strategy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import sep.pack.data.TICKER;

public class StrategyConstants {
	public static final Map<String, Integer> tickerLeverage;
    static {
    	Map<String, Integer> tL = new HashMap<String, Integer>();
		tL.put(TICKER.SPY, 1);
		tL.put(TICKER.SH, -1);
		tL.put(TICKER.SDS, -2);
		tL.put(TICKER.SPX, -3);
		tL.put(TICKER.SSO, 2);
		tL.put(TICKER.UPR, 3);
		tickerLeverage = Collections.unmodifiableMap(tL);
    }
	
	public static final double IB_TRANS_COST = 0.005;
}
