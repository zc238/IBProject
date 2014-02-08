clear;
ticker={'SPY','SH','SSO','SDS','UPRO','SPXU'};
leverage=[1,-1,2,-2,3,-3];
numBucket=10;
TCTimeLag=1;
EPTimeLag=60;
TCParams=[];
EPParams=[];
for i=1:6
    TC=calibrateTC(ticker(i),TCTimeLag,numBucket);
    TCParams=[TCParams,TC(2)];
end

for i=1:5
    i
    for j=i+1:6
        j
        slope=leverage(i)/leverage(j);
        EP=calibrateEP(ticker(i),ticker(j),slope,EPTimeLag,numBucket);
        EPParams=[EPParams,EP(2)];
    end
end