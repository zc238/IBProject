package sep.pack.strategy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import sep.pack.data.Pair;
import sep.pack.data.Quotes;
import sep.pack.data.TICKER;

import com.ib.controller.Types.Action;

public class BackTestStrategy extends AbstractStrategy{

	private Map<String, List<Quotes>> histQuotes = new HashMap<String, List<Quotes>>();
	
	private Map<String, Integer> currentPositions = new HashMap<String, Integer>();
	private double pnl = 0.0;
	private int tradePairNum = 0;
	
	public BackTestStrategy(TransCost tc, ExpectedProfit profit, 
			String tX, String tY, double wS, int tS){
		transCost = tc;
		expProfit = profit;
		tickerX = tX;
		tickerY = tY;
		windowSize = wS*1000*60; //wS is in minute, windowSize in millisecond
		tradeSize = tS;
	}
	
	private long getTimeMiliSec(String currentLine){
		StringTokenizer token = new StringTokenizer(currentLine, ",");
		Calendar c = Calendar.getInstance();
		c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 9, 30, -10);
		c.setTimeInMillis(c.getTimeInMillis()+Long.parseLong(token.nextToken()));
//		System.out.println(c.getTime().toLocaleString());
		return c.getTime().getTime();
	}
	
	private Quotes getQuotesFromLine(String ticker, String sCurrentLine){
		StringTokenizer token = new StringTokenizer(sCurrentLine, ",");
		int tCnt = 0;
		double bidP = 0.0;
		double askP = 0.0;
		int bidSize = 0;
		int askSize = 0;
		Date cd = null;

		while(token.hasMoreElements()){
			switch(tCnt){
				case 0: 
					Calendar c = Calendar.getInstance();
					c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 9, 30, -10);
					c.setTimeInMillis(c.getTimeInMillis()+Long.parseLong(token.nextToken()));
//					System.out.println(c.getTime().toLocaleString());
					cd = c.getTime();
					break;
				case 1:
					bidP = Double.parseDouble(token.nextToken()); break;
				case 2:
					askP = Double.parseDouble(token.nextToken()); break;	
				case 3:
					bidSize = Integer.parseInt(token.nextToken()); break;
				case 4:
					askSize = Integer.parseInt(token.nextToken()); break;
			}
			tCnt++;
		}
		Quotes quote = new Quotes(cd, bidP, askP, bidSize, askSize, ticker);
		return quote;
	}
	
	private void insertQuotes(String ticker, Quotes quote){
		if (!histQuotes.containsKey(ticker)){
			List<Quotes> q = new ArrayList<Quotes>();
			q.add(quote);
			histQuotes.put(ticker, q);
		}else{
			histQuotes.get(ticker).add(quote);
		}
	}
	
	public void parseHistQuotesFromFile(String tickerX, String tickerY, String filePathX, String filePathY) throws IOException{
		BufferedReader brX = new BufferedReader(new FileReader(filePathX));
		BufferedReader brY = new BufferedReader(new FileReader(filePathY));
		String sCurrentLineX = brX.readLine();
		String sCurrentLineY = brY.readLine();

		Quotes qX = getQuotesFromLine(tickerX, sCurrentLineX);
		Quotes qY = getQuotesFromLine(tickerY, sCurrentLineY);
		
		Boolean exitFlag = false;
		Boolean exitX = false;
		Boolean exitY = false;
		while (!exitFlag) {			
			if (getTimeMiliSec(sCurrentLineX) < getTimeMiliSec(sCurrentLineY) || exitY==true){
				String sX = brX.readLine(); 
				if ( sX != null){
					sCurrentLineX = sX;
				}else{
					exitX = true;
				}
				qX = getQuotesFromLine(tickerX, sCurrentLineX);
			}else{
				String sY = brY.readLine();
				if ( sY != null){
					sCurrentLineY = sY;
				}else{
					exitY = true;
				}
				qY = getQuotesFromLine(tickerY, sCurrentLineY);
			}
			exitFlag = exitX && exitY;
			insertQuotes(tickerX, qX);
			insertQuotes(tickerY, qY);
		}
		brX.close();
		brY.close();
		System.out.println("Finish Parsing Historical Quotes");
	}
	
	@Override
	protected Map<String, List<Quotes>> getBurnIn(){
		Map<String, List<Quotes>> qs = new HashMap<String, List<Quotes>>();
		long startMs = histQuotes.get(tickerX).get(0).getLocalTimeStamp().getTime();
		qs.put(tickerX, new LinkedList<Quotes>());
		qs.put(tickerY, new LinkedList<Quotes>());
		for(int i=0; i< histQuotes.get(tickerX).size(); ++i){
			Quotes qX = histQuotes.get(tickerX).get(i);
			Quotes qY = histQuotes.get(tickerY).get(i);
			if ((qX.getLocalTimeStamp().getTime()-startMs) < windowSize){
				qs.get(tickerX).add(qX);
				qs.get(tickerY).add(qY);
			}
		}
		return qs;
	}
	
	private void updatePosAndPnL(String ticker, int amt, double executionPrice){
		int extAmt = currentPositions.get(ticker);
		tradePairNum++;
		this.currentPositions.put(ticker, amt+extAmt);
		pnl -= executionPrice * amt;
	}
	
	public void liquidation(Quotes quotesX, Quotes quotesY){
		int liquidSizeX = currentPositions.get(tickerX);
		int liquidSizeY = currentPositions.get(tickerY);
		
		if (liquidSizeX>0) {updatePosAndPnL(tickerX, -liquidSizeX, quotesX.getBid());}
		else if (liquidSizeX<0) {updatePosAndPnL(tickerX, liquidSizeX, quotesX.getAsk());}
		
		if (liquidSizeY>0) {updatePosAndPnL(tickerY, -liquidSizeY, quotesY.getBid());}
		else if (liquidSizeY<0) {updatePosAndPnL(tickerY, liquidSizeY, quotesY.getAsk());}
	}
	
	public void kickOff() throws IOException{
//		parseHistQuotesFromFile(TICKER.SPY, TICKER.SH, "C:/Users/Long/Dropbox/PairTradingData/data/SPY_25-Feb-2014.csv", "C:/Users/Long/Dropbox/PairTradingData/data/SH_25-Feb-2014.csv");
		parseHistQuotesFromFile(TICKER.SPY, TICKER.SH, "C:/Users/demon4000/Dropbox/data/SPY_03-Mar-2014.csv", "C:/Users/demon4000/Dropbox/data/SH_03-Mar-2014.csv");
		currentPositions.put(TICKER.SPY, 0);
		currentPositions.put(TICKER.SH, 0);
	}
		
	public void runSimulation() throws IOException{
		System.out.println("Running Simulation...");
		kickOff();

		int burninSize = computeAllFirstIntParams();
		System.out.println("Obtained Burnin Period...");

		int quotesSize = histQuotes.get(tickerX).size();
		
		System.out.println("X: " + histQuotes.get(tickerX).size());
		System.out.println("Y: " + histQuotes.get(tickerY).size());
		
		for (int i=burninSize; i<quotesSize; ++i){
			Quotes quotesX = histQuotes.get(tickerX).get(i);
			Quotes quotesY = histQuotes.get(tickerY).get(i);
		
	 		oldBeta =  StrategyUtility.computeBeta(quotesX, quotesY, oldBeta, alpha, slope, scaling);
					
			System.out.println("Position for " + tickerX + " is " + currentPositions.get(tickerX));
			System.out.println("Position for " + tickerY + " is " + currentPositions.get(tickerY));
			System.out.println("Your PNL is at: " + pnl + ", Total Trade Made: " + tradePairNum);
			System.out.println("Iteration: " + i + "/" + histQuotes.get(tickerX).size());
			
			Pair<Action> decision = strategyDecision(quotesX, quotesY);
			
			if (decision == null) {continue;}
			
			if (decision.getA() == Action.BUY){
				if ((decision.getB() == Action.BUY) 
					&& (currentPositions.get(tickerY) <= 0) 
					&& (currentPositions.get(tickerX) <= 0)){
					updatePosAndPnL(tickerX, tradeSizeX, quotesX.getAsk());
					updatePosAndPnL(tickerY, tradeSizeY, quotesY.getAsk());
				}
				else if ((decision.getB() == Action.SELL) 
					&& (currentPositions.get(tickerY) <= 0) 
					&& (currentPositions.get(tickerX) >= 0)){
					updatePosAndPnL(tickerX, tradeSizeX, quotesX.getAsk());
					updatePosAndPnL(tickerY, -tradeSizeY, quotesY.getBid());
				}
			}
			else if (decision.getA() == Action.SELL){
				if ((decision.getB() == Action.BUY)
					&& (currentPositions.get(tickerY) >= 0) 
					&& (currentPositions.get(tickerX) <= 0)){
					updatePosAndPnL(tickerX, -tradeSizeX, quotesX.getBid());
					updatePosAndPnL(tickerY, tradeSizeY, quotesY.getAsk());
				}
				else if ((decision.getB() == Action.SELL)
					&& (currentPositions.get(tickerY) <= 0) 
					&& (currentPositions.get(tickerX) <= 0)){
					updatePosAndPnL(tickerX, -tradeSizeX, quotesX.getBid());
					updatePosAndPnL(tickerY, -tradeSizeY, quotesY.getBid());
				}
			}
			
		}
		Quotes quotesX = histQuotes.get(tickerX).get(quotesSize-1);
		Quotes quotesY = histQuotes.get(tickerY).get(quotesSize-1);
		liquidation(quotesX,quotesY);

		System.out.println("Your Final PNL is at: " + pnl + ", Total Trade Made: " + tradePairNum);
	}
}
