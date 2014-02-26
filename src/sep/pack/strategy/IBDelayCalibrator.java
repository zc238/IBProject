package sep.pack.strategy;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sep.pack.MyLogger;
import sep.pack.QuotesOrderController;
import sep.pack.QuotesOrderLogger;
import sep.pack.QuotesOrderMeasureProcessor;
import sep.pack.data.Pair;
import sep.pack.support.LazyHandler;
import sep.pack.support.UserInfo;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

import com.ib.controller.Types.Action;

public class IBDelayCalibrator {

	private MyLogger m_inLogger = new MyLogger();
	private LazyHandler handler = new LazyHandler();
	private QuotesOrderLogger logger = new QuotesOrderLogger();
	private QuotesOrderMeasureProcessor processor = new QuotesOrderMeasureProcessor(handler, m_inLogger, m_inLogger, logger, this);
	private QuotesOrderController controller = new QuotesOrderController(handler, processor, logger);
	private Map<Integer, Pair<Double> > orderIdToTargetPriceMap = new HashMap<Integer, Pair<Double> >();
	
	public QuotesOrderController getController() {
		return controller;
	}
	
	public IBDelayCalibrator(String ticker){
		processor.setTicker(ticker);
	}

	public void measureQuotesDelay(int iterations, String ticker) throws InterruptedException{
		controller.makeconnection();
		
		double[] delays = new double[iterations];
		for (int i=0; i<iterations; ++i){
			System.out.println("Iteration: " + i);
			long milSec = new Date().getTime();
			controller.reqMktData(ticker, true);
			Thread.sleep(5000); // wait 1 second, should be long enough for quotes to get back
			int l = logger.getStoredData().get(ticker).size();
			long milSecLate = logger.getStoredData().get(ticker).get(l-1).getLocalTimeStamp().getTime();
			delays[i] = milSecLate - milSec;
			System.out.println("Delay is: " + delays[i]);
			logger.getStoredData().get(ticker).clear();
		}
		double m = Descriptive.mean(new DoubleArrayList(delays));
		double v = Descriptive.sampleVariance(new DoubleArrayList(delays), m);
		System.out.println("The mean of the quotes delay is : " + m + " miliseconds.");
		System.out.println("The sample variance of the delay is : " + v + " miliseconds.");
		controller.disconnect();
	}                                                                                                                                        
	
	public void measureOrderFillDelay(int avgSize, int iterations, String ticker) throws InterruptedException{
		
		controller.makeconnection();
		controller.reqMktData(ticker, false);
		for (int i=0; i<iterations; ++i){
			orderIdToTargetPriceMap.put(UserInfo.incrementOrderId(), new Pair<Double>(logger.getLatestNbbo(ticker).getAsk(),0.0)); // We simulate with all buy orders
			controller.sendOrder(ticker, 100, Action.BUY);
		}
	}

	public Map<Integer, Pair<Double>> getOrderIdToTargetPriceMap() {
		return orderIdToTargetPriceMap;
	}	
}
