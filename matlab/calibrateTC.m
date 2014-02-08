function TCParams=calibrateTC(ticker,timeLag,numBucket)

data=cleanData(ticker);
data=data(1:timeLag:end,:);
%data=csvread('SPY_SH.csv');

bid=data(:,2);
ask=data(:,3);
bidSize=data(:,4);
askSize=data(:,5);
midPrice=(bid+ask)/2;
midPriceChange=midPrice(2:end)-midPrice(1:end-1);
imb=bidSize./(bidSize+askSize);

imbBucket=0:1/numBucket:1;
%imbBucket=min(imb):range(imb)/numBucket:max(imb);

bucketAvg=(imbBucket(1:end-1)+imbBucket(2:end))/2;
midPriceChangeBucketAvg=[];
for i=1:(length(imbBucket)-1)
    midPriceChangeBucketAvg(i)=mean(midPriceChange(and(imb(2:end)>=imbBucket(i),imb(2:end)<imbBucket(i+1))));
end

TCParams=regress(midPriceChangeBucketAvg',[ones(size(bucketAvg')),bucketAvg']);