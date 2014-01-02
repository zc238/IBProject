package sep.pack;

import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiController;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.NewOrderState;
import com.ib.controller.OrderStatus;

public class OrderProcessor extends ApiController {


	public OrderProcessor(IConnectionHandler handler, ILogger inLogger,
			ILogger outLogger) {
		super(handler, inLogger, outLogger);
	}

	public void openOrder(NewContract contract, NewOrder order, NewOrderState orderState) {

	}

	protected boolean shouldAdd(NewContract contract, NewOrder order, NewOrderState orderState) {
		return true;
	}


	public void openOrderEnd() {
	}
	
	public void orderStatus(int orderId, OrderStatus status, int filled, int remaining, double avgFillPrice, long permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {

	}
	
	public void handle(int orderId, int errorCode, String errorMsg) {
		
	}
}
