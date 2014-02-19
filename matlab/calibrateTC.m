function TCParams=calibrateTC(ticker)
data=readData(ticker);
timeStamp=data(:,1);
n=numel(timeStamp);
bid=data(:,2);
ask=data(:,3);
bidSize=data(:,4);
askSize=data(:,5);
imb=bidSize./(bidSize+askSize);
midPrice=(bid+ask)/2;

m=5;
cleanData=zeros(n,m+3);
cleanData(:,1)=timeStamp;
cleanData(:,2)=imb;
cleanData(:,3)=midPrice;

for i=1:m
    timeStampLag=timeStamp+10^(i-1);
    index=zeros(n,1);
    for j=1:n
        index(j)=find(data(:,1)<=timeStampLag(j),1,'last');
    end
    cleanData(:,3+i)=midPrice(index);
end

numBucket=10;
imbBucket=0:1/numBucket:1;
midPriceChangeBucketAve=zeros(numBucket,m);

for i=1:m
    midPriceChange=cleanData(:,3+i)-cleanData(:,3);
    %cut off the bottom of the data, which has not valid midPrice change
    index=find(data(:,1)<=data(end,1)-10^(i-1),1,'last');
    midPriceChange=midPriceChange(1:index);
    for j=1:numel(imbBucket)-1
        midPriceChangeBucketAvg(j,i)=mean(midPriceChange(and(imb(1:index)>=imbBucket(j),imb(1:index)<imbBucket(j+1))));
    end
end

bucketAvg=(imbBucket(1:end-1)+imbBucket(2:end))/2;
plot(bucketAvg,midPriceChangeBucketAvg);
title([ticker 'Transaction Cost vs. Order Imbalance']);
xlabel('Order Imbalance');
ylabel('Transaction Cost');
legend('1ms','10ms','100ms','1000ms','10000ms','Location','SouthEast');
if strcmp(ticker,'SPXU');
    legend('Location','NorthWest');
end

%TCParams=regress(midPriceChangeBucketAvg',[ones(size(bucketAvg')),bucketAvg']);