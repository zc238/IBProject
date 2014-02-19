function EPParams=calibrateEP(slope)
ticker1='SPY';
ticker2='SH';
slope=-1;
data1=readData(ticker1);
data2=readData(ticker2);
timeStamp1=data1(:,1);
timeStamp2=data2(:,1);
midPrice1=(data1(:,2)+data1(:,3))/2;
midPrice2=(data2(:,2)+data2(:,3))/2;

timeStamp=unique([timeStamp1;timeStamp2])';
n=numel(timeStamp);

cleanData=zeros(n,7);
cleanData(:,1)=timeStamp;

index=zeros(n,2);
for i=1:n
    index1=find(data1(:,1)<=timeStamp(i),1,'last');
    index2=find(data2(:,1)<=timeStamp(i),1,'last');
    index(i,:)=[index1,index2];
end
cleanData(:,2)=midPrice1(index(:,1));
cleanData(:,3)=midPrice2(index(:,2));

B=zeros(n,1);
f=mean(cleanData(:,2))/mean(cleanData(:,1));
for i=1:n
    localTime=cleanData(i,1);
    index=find(and(cleanData(:,1)<=localTime, cleanData(:,1)>localTime-100));
    pastMidPrice1=cleanData(index,2);
    pastMidPrice2=cleanData(index,3);
    B(i)=mean(pastMidPrice2-slope*f*pastMidPrice1);
end

residual=cleanData(:,3)-B-slope*f*cleanData(:,2);
cleanData(:,4)=B;
cleanData(:,5)=residual;

portfolio=cleanData(:,3)-slope*f*cleanData(:,2);
for i=1:2
    timeStampLag=timeStamp+10^(i+1);
    index=zeros(n,1);
    for j=1:n
        index(j)=find(cleanData(:,1)<=timeStampLag(j),1,'last');
    end
    cleanData(:,5+i)=portfolio(index)-portfolio;
end

numBucket=10;
residualBucket=min(residual):range(residual)/numBucket:max(residual);
PnLBucketAve=zeros(numBucket,2);
for i=1:2
    PnL=cleanData(:,5+i);
    for j=1:numel(residualBucket)-1
        PnLBucketAve(j,i)=mean(PnL(and(residual>=residualBucket(j),residual<residualBucket(j+1))));
    end
end
residualBucketAvg=(residualBucket(1:end-1)+residualBucket(2:end))/2;
plot(residualBucketAvg,PnLBucketAve);

%EPParams=regress(profitBucketAvg',[ones(size(bucketAvg')),bucketAvg']);