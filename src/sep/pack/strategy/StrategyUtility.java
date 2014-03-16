package sep.pack.strategy;

import java.util.List;

import sep.pack.data.Quotes;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

public class StrategyUtility {
	public static void removeQuotes(List<Quotes> quoteRecords, Quotes latestQuotes, double windowSizeMs, long msTs){
		if (quoteRecords.size()>0){
			long earlyMsTs = quoteRecords.get(0).getLocalTimeStamp().getTime();
			if ( (msTs - earlyMsTs) > windowSizeMs){ //outside time window
				quoteRecords.remove(0);
				removeQuotes(quoteRecords, latestQuotes, windowSizeMs, msTs);
			}
		}
	}
	
	//Must ensure quotes has at least 2*windowSize elements
	public static DoubleArrayList convertQuoteToDList(List<Quotes> quotes){
		DoubleArrayList l = new DoubleArrayList();
		for (Quotes q : quotes){
			l.add(q.getMidPrice());
		}
		return l;
	}
	
	public static double getResidual(double beta, double sl, double x, double y){
		return y - beta - sl*x;
	}
	
	public static double computeBeta(Quotes x, Quotes y, double oldB, double alpha, double slope, double scaling){
		double B = (1-alpha)*oldB + alpha*(y.getMidPrice() - slope*scaling*x.getMidPrice());
		return B;
	}
	
	public static double computeScaling(List<Quotes> xs, List<Quotes> ys){
		DoubleArrayList avgTickXQ = convertQuoteToDList(xs);
		DoubleArrayList avgTickYQ = convertQuoteToDList(ys);
		
		double meanY = Descriptive.mean(avgTickYQ);
		double meanX = Descriptive.mean(avgTickXQ);
		
		double scaling = meanY / meanX;
		return scaling;
	}
	
	public static double computeBeta(List<Quotes> xs, List<Quotes> ys, double slope, double scaling){
		DoubleArrayList avgTickXQ = convertQuoteToDList(xs);
		DoubleArrayList avgTickYQ = convertQuoteToDList(ys);
		
		double meanY = Descriptive.mean(avgTickYQ);
		double meanX = Descriptive.mean(avgTickXQ);
		
		double B0 = meanY - slope*scaling * meanX;
		return B0;
	}
}
