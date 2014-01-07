package sep.pack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import com.ib.client.ComboLeg;
import com.ib.client.Contract;
import com.ib.controller.ApiConnection;
import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiController;
import com.ib.controller.NewContract;
import com.ib.controller.Types.Action;

//TODO extends ApiController for convinience, will need to redo this part
public class QuotesOrderController extends ApiController{

	private ApiConnection connection;
	private OrderPlacer placer = new OrderPlacer();
	private Semaphore lock = new Semaphore(1);
	public static HashMap<Integer, String> REQ_TO_TICKER = new HashMap<Integer, String>();
	
	public Semaphore getLock() {
		return lock;
	}

	public QuotesOrderController(IConnectionHandler handler, ILogger inLogger, ILogger outLogger) {
		super(handler, inLogger, outLogger);
		QuotesOrderProcessor wrapper = new QuotesOrderProcessor(handler, inLogger, outLogger, this);
		connection = new ApiConnection(wrapper, inLogger, outLogger);
	}

	public void reqIDs(){
		connection.reqIds(1);
	}
	private NewContract createContract(String ticker){
		Vector<ComboLeg> cblg = new Vector<ComboLeg>();
		Contract c = new Contract(0, ticker, "STK", "", 0.0, "", "",
                "SMART", "USD", "", "", cblg, null, false, "", "");
		return new NewContract(c);
	}
		
	public void makeconnection(){
		connection.eConnect("127.0.0.1",7496,0);
	}
	
	public void reqMktData(){
		if (!connection.isConnected()){
			makeconnection();
		}
		QuotesOrderController.REQ_TO_TICKER.put(1, "SPY");
		QuotesOrderController.REQ_TO_TICKER.put(2, "SH");
		QuotesOrderController.REQ_TO_TICKER.put(3, "SSO");
		QuotesOrderController.REQ_TO_TICKER.put(4, "SDS");
		QuotesOrderController.REQ_TO_TICKER.put(5, "SPX");
		QuotesOrderController.REQ_TO_TICKER.put(6, "UPR");
		
		connection.reqMktData( 1, createContract("SPY").getContract(), "", false);
		connection.reqMktData( 2, createContract("SH").getContract(), "", false);
		connection.reqMktData( 3, createContract("SSO").getContract(), "", false);
		connection.reqMktData( 4, createContract("SDS").getContract(), "", false);
		connection.reqMktData( 5, createContract("SPX").getContract(), "", false);
		connection.reqMktData( 6, createContract("UPR").getContract(), "", false);
		
		System.out.println("Quotes Request Sent");
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
	}
	
	public void sendOrder(String ticker, int quantity, Action action){
		if (!connection.isConnected()){
			makeconnection();
		}
		try {
			lock.acquire();
			placer.sendOrder(this.connection, ticker, quantity, action);
		} catch (InterruptedException e) {
			System.out.println("Fail to acquire lock...");
			e.printStackTrace();
		}		
	}
}
