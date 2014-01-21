function TCParams=calibrateTC(ticker,timeLag)

%data=cleanData(ticker);
data=csvread('SDS_SPX_03-Dec-2012.csv');

bid=data(:,3);
ask=data(:,4);
bidSize=data(:,5);
askSize=data(:,6);
midPrice=(bid+ask)/2;
midPriceChange=midPrice(1+timeLag:timeLag:end)-midPrice(1:timeLag:end-timeLag);
imb=bidSize./(bidSize+askSize);

% calibrate transaction cost
TCParams=polyfit(imb(1+timeLag:timeLag:end),midPriceChange,3);