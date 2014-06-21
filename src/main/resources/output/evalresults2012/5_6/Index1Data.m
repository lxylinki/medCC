%function [ output_args ] = Untitled1( input_args )
%UNTITLED1 Summary of this function goes here
%  Detailed explanation goes here
numfiles = 10;
mydata1 = cell(1, numfiles);
for i = 0:numfiles-1
  myfilename = sprintf('workflow_5_6_%d_ImpPercent.txt', i);
  mydata1{i+1} = load(myfilename);
end
