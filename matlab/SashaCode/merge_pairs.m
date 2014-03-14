function out=merge_pairs(data1,data2)
% outputs data as a matrix, chronologically ordered, with no 2 rows identical
% column 2 is the timestamp, last 8 columns correspond to data1 and data2
% gives the state of the order book for each unique timestamp (so on 2
% following rows data1 or data2 can be identical, just not both)

% get all the values of the different timestamps, with no repetition and ordered
unique_ts=unique([data1(:,1);data2(:,1)]);
%sorts by time stamp; why is it necessary to remove repeats of a time stamp

% only keeps the timestamps greater than the first timestamp in the file
unique_ts=unique_ts(unique_ts>=max(data1(1,1),data2(1,1)));

% orders all the data in chronological order going through the timestamps
n = numel(unique_ts);

% % index1 = ones(n+1,1);
% % index2 = ones(n+1,1);
% % 
% % for i=1:numel(unique_ts)
% % index1(i+1) = find(data1(index1(i):index1(i)+1,1) <= unique_ts(i),1,'last');
% % index2(i+1) = find(data2(index2(i):index2(i)+1,1) <= unique_ts(i),1,'last');
% % end
% % 
% % index1(1:500)
% % 
% % out = [ones(n,1) unique_ts(:) data1(index1(2:end),2:5) data2(index2(2:end),2:5)];

index1 = zeros(n,1);
index2 = zeros(n,1);

for i=1:numel(unique_ts)
index1(i) = find(data1(:,1) <= unique_ts(i),1,'last');
index2(i) = find(data2(:,1) <= unique_ts(i),1,'last');
end

out = [ones(n,1) unique_ts(:) data1(index1,2:5) data2(index2,2:5)];


