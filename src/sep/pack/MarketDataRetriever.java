package sep.pack;

import java.util.Vector;

import com.ib.client.ComboLeg;
import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiController;
import com.ib.controller.NewContract;

public class MarketDataRetriever extends ApiController{
	private IConnectionHandler handler;
	private ILogger inLogger;
	private ILogger outLogger;
	public MarketDataRetriever(IConnectionHandler handler, ILogger inLogger,
			ILogger outLogger) {
		super(handler, inLogger, outLogger);
		this.inLogger = inLogger;
		this.outLogger = outLogger;
		this.handler = handler;
	}

	private NewContract createContract(String ticker){
		Vector<ComboLeg> cblg = new Vector<ComboLeg>();
		Contract c = new Contract(0, ticker, "STK", "", 0.0, "", "",
                "SMART", "USD", "", "", cblg, null, false, "", "");
		return new NewContract(c);
	}
		
	public void makeconnection(){
		QuotesProcessor wrapper = new QuotesProcessor(handler, inLogger, outLogger);
		EClientSocket es = new EClientSocket(wrapper);
		es.eConnect("127.0.0.1",7496,0);

		es.reqMktData( 1, createContract("SPY").getContract(), "", false);
		es.reqMktData( 2, createContract("SH").getContract(), "", false);
		es.reqMktData( 3, createContract("SSO").getContract(), "", false);
		es.reqMktData( 4, createContract("SDS").getContract(), "", false);
		
		System.out.println("Requests Sent");
	}
}
