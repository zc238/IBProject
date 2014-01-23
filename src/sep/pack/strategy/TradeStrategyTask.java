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
	}
	
	@Override
	public void run() {
		while(true){
			try {
				List<OrderContractContainer> orders = strategy.getOrdersFromHistQuotes();
				if (orders.size() == 0){ //Nothing to submit, wait 1 second. 
					Thread.sleep(1000); 
				}else{
					for (OrderContractContainer o : orders){
						controller.sendOrder(o);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
