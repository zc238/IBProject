package sep.pack;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import sep.pack.data.Pair;
import sep.pack.data.Quadralet;
import sep.pack.data.TICKER;
import sep.pack.strategy.CubicTransCost;
import sep.pack.strategy.ExpectedProfit;
import sep.pack.strategy.TradeStrategy;
import sep.pack.strategy.TradeStrategyTask;
import sep.pack.support.LazyHandler;

public class Engine {
	private String paramPath;
	
	public Engine(String pPath){
		paramPath = pPath;
	}
	
	private CubicTransCost parseTransCost(){
		String line = "";
		boolean findTransCost = false;
		CubicTransCost transCost = new CubicTransCost();
		try {
			BufferedReader br = new BufferedReader(new FileReader(paramPath));
			while ((line = br.readLine())!=null){
				if(line.startsWith("//")){
					continue;
				}
				else if(line.equals("TRANSCOST")){
					findTransCost = true;
					continue;
				}
				else if(findTransCost){
					if(line.equals("END_TRANSCOST")){
						break;
					}
					StringTokenizer tokenizer = new StringTokenizer(line,",");
					if(tokenizer.countTokens()==0){ continue; }
					if(tokenizer.countTokens() != 5 && tokenizer.countTokens()!=0){
						System.out.println("WARNING: YOUR TRANSCOST PARAMS ARE NOT SET UP CORRECTED, YOU NEED 4 COEFFICIENTS and 1 TICKER.");
						System.out.println("PROBLEM INPUT: " + line);
					}
					List<String> coefs = new LinkedList<String>();
					while(tokenizer.hasMoreElements()){
						coefs.add(tokenizer.nextToken());
					}
					transCost.insertCost(coefs.get(0), new Quadralet(Double.parseDouble(coefs.get(1)), 
																	 Double.parseDouble(coefs.get(2)), 
																	 Double.parseDouble(coefs.get(3)), 
																	 Double.parseDouble(coefs.get(4))));
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return transCost;
	}
	
	private ExpectedProfit parseExpProfit(){
		String line = "";
		boolean findprofit = false;
		ExpectedProfit profit = new ExpectedProfit();
		try {
			BufferedReader br = new BufferedReader(new FileReader(paramPath));
			while ((line = br.readLine())!=null){
				if(line.startsWith("//")){
					continue;
				}
				else if(line.equals("EXPECTEDPROFIT")){
					findprofit = true;
					continue;
				}
				else if(findprofit){
					if(line.equals("END_EXPECTEDPROFIT")){
						break;
					}
					StringTokenizer tokenizer = new StringTokenizer(line,",");
					if(tokenizer.countTokens()==0){ continue; }
					if(tokenizer.countTokens() != 4){
						System.out.println("WARNING: YOUR TRANSCOST PARAMS ARE NOT SET UP CORRECTED, YOU NEED 2 TICKERS AND 2 COEFFICIENTS");
						System.out.println("PROBLEM INPUT: " + line);
					}
					List<String> coefs = new LinkedList<String>();
					while(tokenizer.hasMoreElements()){
						coefs.add(tokenizer.nextToken());
					}					
					profit.insertPair(new Pair<String>(coefs.get(0), coefs.get(1)), Double.parseDouble(coefs.get(2)), Double.parseDouble(coefs.get(3)));
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return profit;
	}
	
	private int getWindowSize(){
		String line = "";
		boolean findWindowSize = false;
		int windowSize = 10;
		try {
			BufferedReader br = new BufferedReader(new FileReader(paramPath));
			while ((line = br.readLine())!=null){
				if(line.startsWith("//")){
					continue;
				}
				else if(line.contains("WINDOWSIZE")){
					findWindowSize = true;
					continue;
				}
				else if(findWindowSize){
					windowSize = Integer.parseInt(line);
					br.close();
					return windowSize;
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return windowSize;
	}
	
	public void startStrategy(){
		// Wirings
		MyLogger m_inLogger = new MyLogger();
		LazyHandler handler = new LazyHandler();
		QuotesOrderLogger logger = new QuotesOrderLogger();
		QuotesOrderProcessor processor = new QuotesOrderProcessor(handler, m_inLogger, m_inLogger, logger);
		
		QuotesOrderController controller = new QuotesOrderController(handler, processor, logger);

		// Obtain parameters
		CubicTransCost transCost = parseTransCost();
		ExpectedProfit expProfit = parseExpProfit();
		final int windowSize = getWindowSize();
		
		// Retrieve Market Data
		controller.makeconnection();
		controller.reqMktData(TICKER.TICKERS, false);
		
		// Start Automated Trading Strategy
	
		for (int i=0; i<TICKER.TICKERS.size(); ++i){
			for (int j=i; j<TICKER.TICKERS.size(); ++j){
				int tradeSize = 100;
				
				if(i==j){ continue; }
				//This is necessary because you want different strategy pairs to independently perform, so one strategy blocking does not stop the process
				Runnable task = new TradeStrategyTask(new TradeStrategy(logger, transCost, expProfit, 
														TICKER.TICKERS.get(i), TICKER.TICKERS.get(j), windowSize, tradeSize), controller);
				Thread t = new Thread(task);
				t.start();
			}
		}
	}
}
