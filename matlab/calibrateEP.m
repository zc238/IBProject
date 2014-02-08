function EPParams=calibrateEP(ticker1,ticker2,slope,timeLag,numBucket)

%data=csvread('SPY_SH.csv');
data1=cleanData(ticker1);
data2=cleanData(ticker2);

data1=data1(1:timeLag:end,:);
data2=data2(1:timeLag:end,:);

midPrice1=(data1(:,2)+data1(:,3))/2;
midPrice2=(data2(:,2)+data2(:,3))/2;

midPriceScale=mean(midPrice1)/mean(midPrice2);
residual=(1-slope)*mean(midPrice1)+slope*midPriceScale.*midPrice2-midPrice1;

midPriceChange1=midPrice1(2:end)-midPrice1(1:end-1);
midPriceChange2=midPrice2(2:end)-midPrice2(1:end-1);

profit=midPriceChange1-midPriceScale*slope*midPriceChange2;

residualBucket=min(residual):range(residual)/numBucket:max(residual);
bucketAvg=(residualBucket(1:end-1)+residualBucket(2:end))/2;

for i=1:numBucket
    profitBucketAvg(i)=mean(profit(and(residual(2:end)>=residualBucket(i),residual(2:end)<residualBucket(i+1))));
end

EPParams=regress(profitBucketAvg',[ones(size(bucketAvg')),bucketAvg']);