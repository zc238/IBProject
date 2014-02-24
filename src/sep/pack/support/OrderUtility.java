package sep.pack.support;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
		return createNewOrder(quantity, buySell, true, 1);
	}
	
	// construct a order
	public static NewOrder createNewOrder(int quantity, Action buySell, boolean isMarket, double limitPrice){
		NewOrder order = new NewOrder();
		order.account(UserInfo.acct);
		order.action(buySell);
		if (isMarket){
			order.orderType(OrderType.MKT);
		}else{
			order.orderType(OrderType.LMT);
		}
		order.totalQuantity(quantity);
		order.lmtPrice(limitPrice);
		order.orderId(UserInfo.incrementOrderId());
		System.out.println("Placing Order ID: " + UserInfo.getOrderID().get());
		return order;
	}
	
	public static void displayTime(){
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
	}
	
	public static int getMiliSecondsDiffFromNine(){
		Calendar c = Calendar.getInstance(); //now
	    Calendar m = Calendar.getInstance(); //midnight
	    m.set(Calendar.HOUR_OF_DAY, 9);
	    m.set(Calendar.MINUTE, 0);
	    m.set(Calendar.SECOND, 0);
	    m.set(Calendar.MILLISECOND, 0);
	
	    int diff = (int) (c.getTimeInMillis() - m.getTimeInMillis()) ;
	    return diff;
	}
}
