function EPParams=calibrateEP(ticker1,ticker2,timeLag)

data=csvread('SDS_SPX_03-Dec-2012.csv');

data1=data(:,2:6);
data2=data(:,6:10);

midPrice1=(data1(:,2)+data1(:,3))/2;
midPrice2=(data2(:,2)+data2(:,3))/2;

midPriceChange1=midPrice1(1+timeLag:timeLag:end)-midPrice1(1:timeLag:end-timeLag);
midPriceChange2=midPrice2(1+timeLag:timeLag:end)-midPrice2(1:timeLag:end-timeLag);

[b,bint,r]=regress(midPrice1,midPrice2);

% long 1 share ticker1 and short b share ticker2
profit=midPriceChange1-b*midPriceChange2;

% calibrate expected profit
EPParams=regress(profit,r(1+timeLag:timeLag:end));