clear;
ticker={'SPY','SH','SSO','SDS','UPRO','SPXU'};
leverage=[1,-1,2,-2,3,-3];
TCParams=[];
EPParams=[];

for i=1:2
    figure();
    TC=calibrateTC(ticker(i));
    TCParams=[TCParams,TC(2)];
end
TCParams

for i=1:1
    for j=i+1:2
        slope=leverage(j)/leverage(i);
        figure();
        EP=calibrateEP(ticker(i),ticker(j),slope);
        EPParams=[EPParams,EP(2)];
    end
end
EPParams