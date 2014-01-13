package sep.pack;

import java.util.Vector;

import com.ib.client.ComboLeg;
import com.ib.client.Contract;
import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;
import com.ib.controller.OrderType;
import com.ib.controller.Types.Action;

public class OrderUtility {
	public static NewContract createContract(String ticker){
		Vector<ComboLeg> cblg = new Vector<ComboLeg>();
		Contract contract = new Contract(0, ticker, "STK", "", 0.0, "", "",
                "SMART", "USD", "", "", cblg, null, false, "", null);
		return new NewContract(contract);
	}
	
	public static NewOrder createNewOrder(int quantity, Action buySell){
		return createNewOrder(quantity, buySell, false, 1);
	}
	
	// construct a order
	public static NewOrder createNewOrder(int quantity, Action buySell, boolean isMarket, double limitPrice){
		NewOrder order = new NewOrder();
		order.account(OrderPlacer.acct);
		order.action(buySell);
		if (isMarket){
			order.orderType(OrderType.MKT);
		}else{
			order.orderType(OrderType.LMT);
		}
		order.totalQuantity(quantity);
		order.lmtPrice(limitPrice);
		order.orderId(OrderPlacer.orderID.incrementAndGet());
		return order;
	}
}
