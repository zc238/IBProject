package sep.pack.strategy;

import java.util.HashMap;
import java.util.Map;

import sep.pack.data.Pair;

public class ExpectedProfit {
	
	Map<Pair<String>, Pair<Double>> regressionCoeffs = new HashMap<Pair<String>, Pair<Double>>();
	
	public void insertPair(Pair<String> tickerPair, double beta, double intercept){
		regressionCoeffs.put(tickerPair, new Pair<Double>(beta, intercept));
	}
	
	public double getExpectedProf(Pair<String> tickerPair, double residual){
		Pair<Double> coeffs = regressionCoeffs.get(tickerPair);
		return coeffs.getA() * residual + coeffs.getB();
	}
}
