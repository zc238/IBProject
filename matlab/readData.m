function data=readData(ticker)
if strcmp(ticker,'SPY')
    data=csvread('SPY_03-Mar-2014.csv');
elseif strcmp(ticker,'SH')
    data=csvread('SH_03-Mar-2014.csv');
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
