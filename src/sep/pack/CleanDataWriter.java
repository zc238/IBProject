package sep.pack;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CleanDataWriter implements Runnable{

	private QuotesOrderProcessor processor;
	private long sleepTime;
	
	public CleanDataWriter(QuotesOrderProcessor p, long st){
		processor = p;
		sleepTime = st;
	}
	
	@Override
	public void run() {
		while(true){
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			List<String> tickers = new LinkedList<String>(){
				private static final long serialVersionUID = 1L;
	
			{add("SPY"); add("SH"); add("SSO"); add("SDS"); add("SPXU"); add("UPRO");}};
			for (int i=0; i<tickers.size(); ++i){
				for (int j=i; j<tickers.size(); ++j){
					if (i==j){ continue; }
					else{
						try {
							processor.writeToCleanData(tickers.get(i), tickers.get(j));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	

}
