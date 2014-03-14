%hft4: 
%opens and flattens positions without building them up
%there is no restriction on the slope and the strategy grabs its value from
%the regression
%tradesize is fixed as an input and does not adapt in any way
%entry is taken to be 0.5 cents i.e. equal to the flat trading premium per share in IB: this parameter is set in the function call in run_algos 
function [pnl commissions numtrades cash inv] = hft4_trading(bid1,bid2,ask1,ask2,bs1,bs2,as1,as2,tp1,tp2,tradesize,windowsize,slope,sym1,sym2)

threshold = 0;
imb1 = bs1./(bs1+as1);
imb2 = bs2./(bs2+as2);
scaling = mean(tp1)/mean(tp2);
scalingTradeSize = scaling*abs(slope);
tradesize1 = tradesize;
tradesize2 = tradesize*scalingTradeSize;
n=numel(bid1);
nRows = n-windowsize;
residuals = zeros(nRows,1);

alpha = 1-1/windowsize;
mean1 = mean(tp1(1:windowsize));
mean2 = mean(tp2(1:windowsize));
index = 0;

numtrades=0;
cash=zeros(n,1); inv=zeros(n,1); commissions=zeros(n,1);
if slope<0 % small residual: buy both; high res: sell both; cf graph of expected profit as a function of residual
    for i=(windowsize+1):n
        index = index+1;
        tp1_window=tp1((i-windowsize):i);
        tp2_window=tp2((i-windowsize):i);

        mean1 = alpha*mean1+(1-alpha)*tp1(i);
        mean2 = alpha*mean2+(1-alpha)*tp2(i);
        scalingfactor = mean1/mean2;
        % actual regression P1 = a + b*P2 + epsilon
        y = (tp1_window+(-slope)*tp2_window*scalingfactor);
        X = ones(numel(tp2_window),1);
        b = X\y;
        res = y-X*b;
        residuals(i)=res(end);
        if inv(i-1)>0
            % i'm long, only sell
            if (-expectedProfit(sym1,sym2,residuals(i))>threshold+tradesize1*(0.005+tp1(i)-bid1(i)+TCost(sym1,imb1(i)))+tradesize2*(0.005+tp2(i)-bid2(i)+TCost(sym2,imb2(i))))
                % sell on both bids
                cash(i)=cash(i-1)+bid1(i)*tradesize1+bid2(i)*tradesize2;
                inv(i)=inv(i-1)-1;
                numtrades=numtrades+1;
                commissions(i)=commissions(i-1)+0.005*(tradesize1+tradesize2);
            else
                cash(i)=cash(i-1);
                inv(i)=inv(i-1);
                commissions(i)=commissions(i-1);
            end

        elseif inv(i-1)<0
            % i'm short, only buy
            if (expectedProfit(sym1,sym2,residuals(i))>threshold+tradesize1*(0.005+ask1(i)-tp1(i)-TCost(sym1,imb1(i)))+tradesize2*(0.005+ask2(i)-tp2(i)-TCost(sym2,imb2(i)))) 
                % buy at both asks
                cash(i)=cash(i-1)-ask1(i)*tradesize1-ask2(i)*tradesize2;
                inv(i)=inv(i-1)+1;
                numtrades=numtrades+1;
                commissions(i)=commissions(i-1)+0.005*(tradesize1+tradesize2);
            else
                cash(i)=cash(i-1);
                inv(i)=inv(i-1);
                commissions(i)=commissions(i-1);
            end

        else %no inventory
            if (expectedProfit(sym1,sym2,residuals(i))>threshold+tradesize1*(0.005+ask1(i)-tp1(i)-TCost(sym1,imb1(i)))+tradesize2*(0.005+ask2(i)-tp2(i)-TCost(sym2,imb2(i))))
                % buy at both asks
                cash(i)=cash(i-1)-ask1(i)*tradesize1-ask2(i)*tradesize2;
                inv(i)=inv(i-1)+1;
                numtrades=numtrades+1;
                commissions(i)=commissions(i-1)+0.005*(tradesize1+tradesize2);
            elseif (-expectedProfit(sym1,sym2,residuals(i))>threshold+tradesize1*(0.005+tp1(i)-bid1(i)+TCost(sym1,imb1(i)))+tradesize2*(0.005+tp2(i)-bid2(i)+TCost(sym2,imb2(i))))
                % sell at both bids
                numtrades=numtrades+1;
                cash(i)=cash(i-1)+bid1(i)*tradesize1+bid2(i)*tradesize2;
                inv(i)=inv(i-1)-1;
                commissions(i)=commissions(i-1)+0.005*(tradesize1+tradesize2);
            else
                cash(i)=cash(i-1);
                inv(i)=inv(i-1);
                commissions(i)=commissions(i-1);
            end
        end
    end
else %slope>=0
    for i=(windowsize+1):n
        index = index+1;
        tp1_window=tp1((i-windowsize):i);
        tp2_window=tp2((i-windowsize):i);

        mean1 = alpha*mean1+(1-alpha)*tp1(i);
        mean2 = alpha*mean2+(1-alpha)*tp2(i);
        scalingfactor = mean1/mean2;
        % actual regression P1 = a + b*P2 + epsilon
        y = (tp1_window+(-slope)*tp2_window*scalingfactor);
        X = ones(numel(tp2_window),1);
        b = X\y;
        res = y-X*b;
        residuals(i)=res(end);
        
        if inv(i-1)>0
            % i'm long 1, sell 1 and buy 2
            if (-expectedProfit(sym1,sym2,residuals(i))>threshold+tradesize1*(0.005+tp1(i)-bid1(i)+TCost(sym1,imb1(i)))+tradesize2*(0.005+ask2(i)-tp2(i)-TCost(sym2,imb2(i))))
                cash(i)=cash(i-1)+bid1(i)*tradesize1-ask2(i)*tradesize2;
                inv(i)=inv(i-1)-1;
                numtrades=numtrades+1;
                commissions(i)=commissions(i-1)+0.005*(tradesize1+tradesize2);
            else
                cash(i)=cash(i-1);
                inv(i)=inv(i-1);
                commissions(i)=commissions(i-1);
            end

        elseif inv(i-1)<0
            % i'm short 1, buy 1 and sell 2
            if (expectedProfit(sym1,sym2,residuals(i))>threshold+tradesize1*(0.005+ask1(i)-tp1(i)-TCost(sym1,imb1(i)))+tradesize2*(0.005+tp2(i)-bid2(i)+TCost(sym2,imb2(i))))
                cash(i)=cash(i-1)-ask1(i)*tradesize1+bid2(i)*tradesize2;
                inv(i)=inv(i-1)+1;
                numtrades=numtrades+1;
                commissions(i)=commissions(i-1)+0.005*(tradesize1+tradesize2);
            else
                cash(i)=cash(i-1);
                inv(i)=inv(i-1);
                commissions(i)=commissions(i-1);
            end

        else %inventory equals zero
            if (expectedProfit(sym1,sym2,residuals(i))>threshold+tradesize1*(0.005+ask1(i)-tp1(i)-TCost(sym1,imb1(i)))+tradesize2*(0.005+tp2(i)-bid2(i)+TCost(sym2,imb2(i))))
                % buy 1, sell 2
                cash(i)=cash(i-1)-ask1(i)*tradesize1+bid2(i)*tradesize2;
                inv(i)=inv(i-1)+1;
                numtrades=numtrades+1;
                commissions(i)=commissions(i-1)+0.005*(tradesize1+tradesize2);
            elseif (-expectedProfit(sym1,sym2,residuals(i))>threshold+tradesize1*(0.005+tp1(i)-bid1(i)+TCost(sym1,imb1(i)))+tradesize2*(0.005+ask2(i)-tp2(i)-TCost(sym2,imb2(i))))
                % sell 1, buy 2
                numtrades=numtrades+1;
                cash(i)=cash(i-1)+bid1(i)*tradesize1-ask2(i)*tradesize2;
                inv(i)=inv(i-1)-1;
                commissions(i)=commissions(i-1)+0.005*(tradesize1+tradesize2);
            else
                cash(i)=cash(i-1);
                inv(i)=inv(i-1);
                commissions(i)=commissions(i-1);
            end
        end
    end
end

price1=(bid1+ask1)/2;
price2=(bid2+ask2)/2;
if slope<0
    pnl=cash+inv.*(price1*tradesize1+price2*tradesize2);
else
    pnl=cash+inv.*(price1*tradesize1-price2*tradesize2);
end

        
        
