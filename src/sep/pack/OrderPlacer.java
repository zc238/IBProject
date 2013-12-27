package sep.pack;

import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import apidemo.ApiDemo;
import apidemo.TicketDlg;

import com.ib.client.ComboLeg;
import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiController.IConnectionHandler;
import com.ib.controller.ApiController.IOrderHandler;
import com.ib.controller.ApiController;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.NewOrderState;

public class OrderPlacer extends ApiController{
	private IConnectionHandler handler;
	private ILogger inLogger;
	private ILogger outLogger;
	
	public OrderPlacer(IConnectionHandler handler, ILogger inLogger,ILogger outLogger) {
		super(handler, inLogger, outLogger);
		this.inLogger = inLogger;
		this.outLogger = outLogger;
		this.handler = handler;
	}
	
	// construct a contract
	private NewContract createContract(String ticker){
		Vector<ComboLeg> cblg = new Vector<ComboLeg>();
		Contract contract = new Contract(0, ticker, "STK", "", 0.0, "", "",
                "SMART", "USD", "", "", cblg, null, false, "", "");
		return new NewContract(contract);
	}
	
	// construct a order
	private NewOrder createOrder(){
		Order order = new Order(); // requires too many parameters (see NewOrder.java)
		return new NewOrder(order);
	}
	
	// send order (see placeOrModifyOrder in TickDlg.java)
	// requires three parameters: 1.contract; 2.order; 3.IOrderHandler
	public void sendOrder(){
		// construct ApiController in order to call placeOrModifyOrder
		ApiController controller = new ApiController(handler, inLogger, outLogger);
		
		// construct IOrderHandler
		public void IOrderHandler(){
			@Override public void orderState(NewOrderState orderState) {
				controller().removeOrderHandler( this);
				SwingUtilities.invokeLater( new Runnable() {
					@Override public void run() {
						dispose();
					}
				});
			}
			@Override public void handle(int errorCode, final String errorMsg) {
				order.orderId( 0);
				SwingUtilities.invokeLater( new Runnable() {
					@Override public void run() {
						JOptionPane.showMessageDialog( TicketDlg.this, errorMsg);
					}
				});
			}
		}
		IOrderHandler orderhandler = new IOrderHandler();
		
		// call placeOrModifyOrder
		controller.placeOrModifyOrder(
				createContract("SPY").getContract(),
				createOrder("SPY").getOrder(), // function getOrder is not defined in NewOrder.java
				orderhandler);

		System.out.println("Orders Sent");
	}
}