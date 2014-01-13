package sep.pack;

import com.ib.controller.NewContract;
import com.ib.controller.NewOrder;

public class OrderContractContainer {
	private NewContract contract;
	private NewOrder order;
	public OrderContractContainer(NewContract c, NewOrder o){
		contract = c;
		order = o;
	}
	
	public NewContract getContract() {
		return contract;
	}
	public NewOrder getOrder() {
		return order;
	}	
}
