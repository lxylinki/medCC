x = [1:100];

opt1a = load('Opt_5_6.txt');
cg1a = load('CG_5_6.txt');
gain1a = load('GAIN_5_6.txt');
countCG1a = 0;
countGAIN1a = 0;
for(k=1:100)
    if(cg1(k)==opt1(k))
        countCG1a = countCG1a + 1;
    end    
    if(gain1(k)==opt1(k))
        countGAIN1a = countGAIN1a + 1;
    end
end

opt1b = load('Opt2_5_6.txt');
cg1b = load('CG2_5_6.txt');
gain1b = load('GAIN2_5_6.txt');
countCG1b = 0;
countGAIN1b = 0;
for(k=1:100)
    if(cg1b(k)==opt1b(k))
        countCG1b = countCG1b + 1;
    end    
    if(gain1b(k)==opt1b(k))
        countGAIN1b = countGAIN1b + 1;
    end
end

opt2 = load('Opt_6_11.txt');
cg2 = load('CG_6_11.txt');
gain2 = load('GAIN_6_11.txt');
countCG2 = 0;
countGAIN2 = 0;
for(k=1:100)
    if(cg2(k)==opt2(k))
        countCG2 = countCG2 + 1;
    end
    
    if(gain2(k)==opt2(k))
        countGAIN2 = countGAIN2 + 1;
    end
end

opt3 = load('Opt_7_14.txt');
cg3 = load('CG_7_14.txt');
gain3 = load('GAIN_7_14.txt');
countCG3 = 0;
countGAIN3 = 0;
for(k=1:100)
    if(cg3(k)==opt3(k))
        countCG3 = countCG3 + 1;
    end
    
    if(gain3(k)==opt3(k))
        countGAIN3 = countGAIN3 + 1;
    end
end

opt4a = load('Opt2_8_18.txt');
cg4a = load('CG2_8_18.txt');
gain4a = load('GAIN2_8_18.txt');
countCG4a = 0;
countGAIN4a = 0;
for(k=1:100)
    if(cg4a(k)==opt4a(k))
        countCG4a = countCG4a + 1;
    end    
    if(gain4a(k)==opt4a(k))
        countGAIN4a = countGAIN4a + 1;
    end
end



Y1 = [countCG1a countGAIN1a, 
     countCG2 countGAIN2,
     countCG3 countGAIN3,
     countCG4a countGAIN4a];
bar(Y1)
colormap summer
grid on
%plot(x, opt, ':og', x, cg, ':ob',x, gain, ':om', 'LineWidth', 2.0);
