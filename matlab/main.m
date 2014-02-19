clear;
ticker={'SPY','SH','SSO','SDS','UPRO','SPXU'};
leverage=[1,-1,2,-2,3,-3];
TCParams=[];
EPParams=[];
for i=1:6
    figure();
    calibrateTC(ticker(i));
    %TCParams=[TCParams,TC(2)];
end

for i=1:5
    for j=i+1:6
        slope=leverage(j)/leverage(i);
        figure()
        calibrateEP(ticker(i),ticker(j),slope);
        %EPParams=[EPParams,EP(2)];
    end
end