package sep.pack.strategy;

import java.util.List;

import sep.pack.OrderContractContainer;
import sep.pack.QuotesOrderController;

public class TradeStrategyTask implements Runnable{
	private TradeStrategy strategy;
	private QuotesOrderController controller;
	
	public TradeStrategyTask(TradeStrategy strat, QuotesOrderController ct){
		strategy = strat;
		controller = ct;
		System.out.println(strat.toString());
	}
	
	@Override
	public void run() {
		while(true){
			try {
				List<OrderContractContainer> orders = strategy.getOrdersFromHistQuotes();
				controller.reqPositions(false);
				if (orders.size() == 0){ // Nothing to submit, wait 5 seconds. 
					Thread.sleep(5000); 
				}else{
					for (OrderContractContainer o : orders){
						controller.sendOrder(o);
					}
					Thread.sleep(5000); // Rest for 5 seconds
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
