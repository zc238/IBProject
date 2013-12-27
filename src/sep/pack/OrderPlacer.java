package sep.pack;

import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import apidemo.ApiDemo;
import apidemo.TicketDlg;

import com.ib.client.ComboLeg;
import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.Order;
import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiConnection;
import com.ib.controller.ApiController.IConnectionHandler;
import com.ib.controller.ApiController.IOrderHandler;
import com.ib.controller.Types;
import com.ib.controller.Types.Action;
import com.ib.controller.Types.AlgoStrategy;
import com.ib.controller.Types.HedgeType;
import com.ib.controller.Types.Method;
import com.ib.controller.Types.OcaType;
import com.ib.controller.Types.ReferencePriceType;
import com.ib.controller.Types.Rule80A;
import com.ib.controller.Types.TimeInForce;
import com.ib.controller.Types.TriggerMethod;
import com.ib.controller.Types.VolatilityType;
import com.ib.controller.ApiController;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.NewOrderState;
import com.ib.controller.OrderType;

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
	private NewOrder createNewOrder(int quantity){
		Order order = new Order(); // construct an Order
		NewOrder newOrder = new NewOrder(order); // use the order to construct the NewOrder
		
		//Update newOrder information
		newOrder.account("DU168741");
		newOrder.action(Types.Action.BUY);					// Action Type
		newOrder.algoStrategy(Types.AlgoStrategy.None);
		newOrder.allOrNone(false);
		newOrder.auxPrice(Double.MAX_VALUE);
		newOrder.blockOrder(false);
		newOrder.clientId(0);
		newOrder.continuousUpdate(false);
		newOrder.delta(Double.MAX_VALUE);
		newOrder.deltaNeutralAuxPrice(Double.MAX_VALUE);
		newOrder.deltaNeutralConId(0);
		newOrder.deltaNeutralOrderType(OrderType.None);
		newOrder.discretionaryAmt(Double.MAX_VALUE);
		newOrder.displaySize(0);
		newOrder.eTradeOnly(false);
		newOrder.faGroup("");
		newOrder.faMethod(Types.Method.None);
		newOrder.faPercentage("");
		newOrder.faProfile("");
		newOrder.firmQuoteOnly(false);
		newOrder.goodAfterTime("");
		newOrder.goodTillDate("");
		newOrder.hedgeParam("");
		newOrder.hedgeType(Types.HedgeType.None);
		newOrder.hidden(false);
		newOrder.lmtPrice(1.0);								// limit price
		newOrder.minQty(2147483647);
		newOrder.nbboPriceCap(Double.MAX_VALUE);
		newOrder.notHeld(false);
		newOrder.ocaGroup("");
		newOrder.ocaType(Types.OcaType.None);
		newOrder.optOutSmartRouting(false);
		newOrder.orderId(3);								// order ID
		newOrder.orderRef("");
		newOrder.orderType(OrderType.LMT);					// Order Type
		newOrder.outsideRth(false);
		newOrder.overridePercentageConstraints(false);
		newOrder.parentId(0);
		newOrder.percentOffset(Double.MAX_VALUE);
		newOrder.permId(0);
		newOrder.referencePriceType(Types.ReferencePriceType.None);
		newOrder.rule80A(Types.Rule80A.None);
		newOrder.scaleAutoReset(false);
		newOrder.scaleInitFillQty(2147483647);
		newOrder.scaleInitLevelSize(2147483647);
		newOrder.scaleInitPosition(2147483647);
		newOrder.scalePriceAdjustInterval(2147483647);
		newOrder.scalePriceAdjustValue(Double.MAX_VALUE);
		newOrder.scalePriceIncrement(Double.MAX_VALUE);
		newOrder.scaleProfitOffset(Double.MAX_VALUE);
		newOrder.scaleRandomPercent(false);
		newOrder.scaleSubsLevelSize(2147483647);
		newOrder.scaleTable("");
		newOrder.startingPrice(Double.MAX_VALUE);
		newOrder.stockRangeLower(Double.MAX_VALUE);
		newOrder.stockRangeUpper(Double.MAX_VALUE);
		newOrder.stockRefPrice(Double.MAX_VALUE);
		newOrder.sweepToFill(false);
		newOrder.tif(Types.TimeInForce.DAY);
		newOrder.totalQuantity(quantity);					// quantity
		newOrder.trailingPercent(Double.MAX_VALUE);
		newOrder.trailStopPrice(Double.MAX_VALUE);
		newOrder.transmit(true);
		newOrder.triggerMethod(Types.TriggerMethod.Default);
		newOrder.volatility(Double.MAX_VALUE);
		newOrder.volatilityType(Types.VolatilityType.None);
		newOrder.whatIf(false);

		return newOrder;
	}
	
	// send order (see placeOrder in ApiConnection.java) requiring two parameters: 1.contract; 2.order
	public void sendOrder(){
		// construct ApiConnection in order to call placeOrder
		QuotesProcessor wrapper = new QuotesProcessor(handler, inLogger, outLogger);
		ApiConnection connection = new ApiConnection(wrapper, inLogger, outLogger);
		
		// call placeOrder
		connection.placeOrder(createContract("GOOG"), createNewOrder(100));
		
		System.out.println("Orders Sent");
	}
}