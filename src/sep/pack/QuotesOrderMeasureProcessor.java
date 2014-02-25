package sep.pack;

import sep.pack.strategy.IBDelayCalibrator;
import sep.pack.support.OrderUtility;

import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.OrderStatus;
import com.ib.controller.Types.Action;

public class QuotesOrderMeasureProcessor extends QuotesOrderProcessor{

	private IBDelayCalibrator calibrator;
	private String ticker;
	public QuotesOrderMeasureProcessor(IConnectionHandler handler,
			ILogger inLogger, ILogger outLogger, QuotesOrderLogger r, 
			IBDelayCalibrator calib) {
		super(handler, inLogger, outLogger, r);
		calibrator = calib;
	}
	
	
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public void orderStatus(int orderId, OrderStatus status, int filled, int remaining, double avgFillPrice, long permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
		System.out.println("Receiving Order Information for Order ID: " + orderId);
		System.out.println("Average Filled Price: " + avgFillPrice);
		if (status.equals(OrderStatus.Submitted)){
			OrderUtility.displayTime();
			calibrator.getOrderIdToTargetPriceMap().get(orderId).setB(avgFillPrice);
			try{
				calibrator.getController().sendOrder(ticker, 100, Action.SELL);
			}catch(Exception e){}
		}
	}
}
