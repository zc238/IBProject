function TCParams=calibrateTC()
ticker='SDS';
if strcmp(ticker,'SPY')
    data=csvread('SPY.csv');
elseif strcmp(ticker,'SH')
    data=csvread('SH.csv');
elseif strcmp(ticker,'SSO')
    data=csvread('SSO.csv');
elseif strcmp(ticker,'SDS')
    data=csvread('SDS.csv');
elseif strcmp(ticker,'UPRO')
    data=csvread('UPRO.csv');
elseif strcmp(ticker,'SPXU')
    data=csvread('SPXU.csv');
end

data=data(data(:,1)>=0,:);
data=data(data(:,1)<=23400000,:);

timeStamp=data(:,1);
n=numel(timeStamp);
bid=data(:,2);
ask=data(:,3);
bidSize=data(:,4);
askSize=data(:,5);
imb=bidSize./(bidSize+askSize);
midPrice=(bid+ask)/2;

cleanData=zeros(n,7);
cleanData(:,1)=timeStamp;
cleanData(:,2)=imb;
cleanData(:,3)=midPrice;

for i=1:4
    i
    timeStampLag=timeStamp+10^(i-1);
    index=zeros(n,1);
    for j=1:n
        index(j)=find(data(:,1)<=timeStampLag(j),1,'last');
    end
    cleanData(:,3+i)=midPrice(index);
end

numBucket=10;
imbBucket=0:1/numBucket:1;
midPriceChangeBucketAve=zeros(numBucket,4);
for i=1:4
    midPriceChange=cleanData(:,3+i)-cleanData(:,3);
    for j=1:numel(imbBucket)-1
        midPriceChangeBucketAvg(j,i)=mean(midPriceChange(and(imb>=imbBucket(j),imb<imbBucket(j+1))));
    end
end
bucketAvg=(imbBucket(1:end-1)+imbBucket(2:end))/2;

plot(bucketAvg,midPriceChangeBucketAvg);
%TCParams=regress(midPriceChangeBucketAvg',[ones(size(bucketAvg')),bucketAvg']);