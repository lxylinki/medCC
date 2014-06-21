x = [1:100];

opt1 = load('Opt_5_6.txt');
cg1 = load('CG_5_6.txt');
gain1 = load('GAIN_5_6.txt');
countCG1 = 0;
countGAIN1 = 0;
for(k=1:100)
    if(cg(k)==opt(k))
        countCG1 = countCG1 + 1;
    end    
    if(gain(k)==opt(k))
        countGAIN1 = countGAIN1 + 1;
    end
end