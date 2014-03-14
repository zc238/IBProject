function [pnl commissions numtrades cash inv] = run_algos_trading(data,tradesize,windowsize,slope,sym1,sym2)                                

bid1=data(:,3); ask1=data(:,4);
bs1=data(:,5); as1=data(:,6);
bid2=data(:,7); ask2=data(:,8);
bs2=data(:,9); as2=data(:,10);
tp1 = 0.5*(ask1+bid1);
tp2 = 0.5*(ask2+bid2);

[pnl commissions numtrades cash inv] = hft4_trading(bid1,bid2,ask1,ask2,bs1,bs2,as1,as2,tp1,tp2,tradesize,windowsize,slope,sym1,sym2);
%matrix = hft4Buy_residual0(bid1,bid2,ask1,ask2,bs1,bs2,as1,as2,tp1,tp2,tradesize,windowsize,slope,lengthHold,lev1,lev2);