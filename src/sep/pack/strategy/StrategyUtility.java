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
	
	public static double getLatestResidual(List<Quotes> xs, List<Quotes> ys, 
										double slope, double oldBeta, boolean computeAgain){
		if(computeAgain){//If we decide to recompute the residual using a new beta
			DoubleArrayList avgTickXQ = convertQuoteToDList(xs);
			DoubleArrayList avgTickYQ = convertQuoteToDList(ys);
			
			double meanY = Descriptive.mean(avgTickYQ);
			double meanX = Descriptive.mean(avgTickXQ);
			
			double scaling = meanY / meanX;
			DoubleArrayList resYs = new DoubleArrayList();
//			System.out.println("QX Size: " + avgTickXQ.size());
//			System.out.println("QY Size: " + avgTickYQ.size());
			
			for (int i=avgTickXQ.size()-1, j=avgTickYQ.size()-1; i>=0 && j>=0; --i,--j){
				double y = avgTickYQ.get(j) - slope*avgTickXQ.get(i)*scaling;
				resYs.add(y);
			}
//			System.out.println("SizeRes: " + resYs.size());
			return resYs.get(resYs.size()-1) - Descriptive.mean(resYs);
		}else{//else we use the old beta already computed from old quotes
			return ys.get(ys.size()-1).getMidPrice() - oldBeta*xs.get(xs.size()-1).getMidPrice();
		}
	}
}
