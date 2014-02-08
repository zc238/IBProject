function cleanData=cleanData(ticker)
if strcmp(ticker,'SPY')
    dirtyData=csvread('SPY.csv');
elseif strcmp(ticker,'SH')
    dirtyData=csvread('SH.csv');
elseif strcmp(ticker,'SSO')
    dirtyData=csvread('SSO.csv');
elseif strcmp(ticker,'SDS')
    dirtyData=csvread('SDS.csv');
elseif strcmp(ticker,'UPRO')
    dirtyData=csvread('UPRO.csv');
elseif strcmp(ticker,'SPXU')
    dirtyData=csvread('SPXU.csv');
end
dirtyData=dirtyData(dirtyData(:,1)>=0,:);
n=23400;
index=zeros(n,1);
ts=1:23400;
for i=1:23400
    index(i)=find(dirtyData(:,1)<=i,1,'last');
end
cleanData=[ts' dirtyData(index,2:5)];