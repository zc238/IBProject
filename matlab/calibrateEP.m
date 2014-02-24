function EPParams=calibrateEP(ticker1,ticker2,slope)
data1=readData(ticker1);
data2=readData(ticker2);
timeStamp1=data1(:,1);
timeStamp2=data2(:,1);
midPrice1=(data1(:,2)+data1(:,3))/2;
midPrice2=(data2(:,2)+data2(:,3))/2;

timeStamp=unique([timeStamp1;timeStamp2])';
n=numel(timeStamp);

m=4;
cleanData=zeros(n,5+m);
cleanData(:,1)=timeStamp;

%merge data set
index=zeros(n,2);
for i=1:n
    index1=find(data1(:,1)<=timeStamp(i),1,'last');
    index2=find(data2(:,1)<=timeStamp(i),1,'last');
    index(i,:)=[index1,index2];
end
cleanData(:,2)=midPrice1(index(:,1));
cleanData(:,3)=midPrice2(index(:,2));

B=zeros(n,1);
f=mean(cleanData(:,3))/mean(cleanData(:,2));
windowSize=100;
%cut off the beginning of the data, which has no valid B and residual
%values
startIndex=find(cleanData(:,1)>windowSize,1,'first');

for i=startIndex:n
    localTime=cleanData(i,1);
    index=find(cleanData(:,1)>(localTime-windowSize),1,'first');
    pastMidPrice1=cleanData(index:i,2);
    pastMidPrice2=cleanData(index:i,3);
    B(i)=mean(pastMidPrice2-slope*f*pastMidPrice1);
end

residual=cleanData(:,3)-B-slope*f*cleanData(:,2);
cleanData(:,4)=B;
cleanData(:,5)=residual;

portfolio=cleanData(:,3)-slope*f*cleanData(:,2);
for i=1:m
    timeStampLag=timeStamp+10^(i+1);
    index=zeros(n,1);
    for j=1:n
        index(j)=j-1+find(cleanData(j:end,1)<=timeStampLag(j),1,'last');
    end
    cleanData(:,5+i)=portfolio(index)-portfolio;
end

numBucket=10;
residualBucket=-0.01:0.002:0.01;
PnLBucketAve=zeros(numBucket,m);
for i=1:m
    PnL=cleanData(:,5+i);
    %cut off the bottom of the data, which has not valid PnL change
    index=find(cleanData(:,1)<=cleanData(end,1)-10^(i+1),1,'last');
    PnL=PnL(startIndex:index);
    for j=1:numel(residualBucket)-1
        PnLBucketAve(j,i)=mean(PnL(and(residual(startIndex:index)>=residualBucket(j),residual(startIndex:index)<residualBucket(j+1))));
        if (j==1) || (j==5) || (j==10)
            %figure();
            %hist(PnL(and(residual(startIndex:index)>=residualBucket(j),residual(startIndex:index)<residualBucket(j+1))),20);
        end
    end
end
residualBucketAvg=(residualBucket(1:end-1)+residualBucket(2:end))/2;
hist(residual(startIndex:end));
figure();
plot(residualBucketAvg,PnLBucketAve);
title([strcat(ticker1,'-',ticker2) 'Expected Profit vs. Residual Value']);
xlabel('Residual Value');
ylabel('Expected Profit');
legend('100ms','1s','10s','100s','Location','NorthEast');
%EPParams=regress(profitBucketAvg',[ones(size(bucketAvg')),bucketAvg']);