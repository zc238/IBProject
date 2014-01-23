package sep.pack;

import java.io.IOException;

import sep.pack.data.TICKER;

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
			
			for (int i=0; i<TICKER.TICKERS.size(); ++i){
				for (int j=i; j<TICKER.TICKERS.size(); ++j){
					if (i==j){ continue; }
					else{
						try {
							processor.writeToCleanData(TICKER.TICKERS.get(i), TICKER.TICKERS.get(j));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	

}
