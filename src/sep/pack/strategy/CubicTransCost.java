package sep.pack.strategy;

import java.util.HashMap;
import java.util.Map;

import sep.pack.data.Quadralet;

public class CubicTransCost {
	
	private Map<String, Quadralet> transCostMap = new HashMap<String, Quadralet>();
	
	public void insertCost(String ticker, Quadralet coeffs){
		transCostMap.put(ticker, coeffs);
	}
	
	public double getTransCost(String ticker, double imbalance){
		Quadralet q = transCostMap.get(ticker);

		return (Math.pow(imbalance, 3)*q.getA()
				+ Math.pow(imbalance, 2)*q.getB()
				+ Math.pow(imbalance, 1)*q.getC()
				+ q.getD());
	}
}
