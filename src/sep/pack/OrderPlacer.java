package sep.pack;

import java.util.Vector;

import com.ib.client.ComboLeg;
import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.controller.ApiConnection;
import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiController;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.Types.Action;


public class OrderPlacer extends ApiController{
	private IConnectionHandler handler;
	private ILogger inLogger;
	private ILogger outLogger;
	
	public OrderPlacer(IConnectionHandler handler, ILogger inLogger,ILogger outLogger){
		super(handler, inLogger, outLogger);
		this.inLogger = inLogger;
		this.outLogger = outLogger;
		this.handler = handler;
	}
	
	// construct a contract
	private NewContract createContract(String ticker){
		Vector<ComboLeg> cblg = new Vector<ComboLeg>();
		Contract contract = new Contract(0, ticker, "STK", "", 0.0, "", "",
                "SMART", "USD", "", "", cblg, null, false, "", null);
		return new NewContract(contract);
	}
	
	// construct a order
	private NewOrder createNewOrder(int quantity, Action buySell){
		NewOrder order = new NewOrder();
		order.account("DU168741");
		order.action(buySell);
		order.totalQuantity(quantity);
		order.lmtPrice(1);
		order.orderId(2);
		return order;
	}
	
	// send order (see placeOrder in ApiConnection.java) requiring two parameters: 1.contract; 2.order
	public void sendOrder(String ticker, int amt, Action action){
		OrderProcessor wrapper = new OrderProcessor(handler, inLogger, outLogger);		
		ApiConnection connection = new ApiConnection(wrapper, inLogger, outLogger);
		connection.eConnect("127.0.0.1",7496,0);
		NewContract contract = createContract(ticker);
		NewOrder order = createNewOrder(amt, action);
		connection.placeOrder(contract, order);

		System.out.println("Orders Sent");
	}
}