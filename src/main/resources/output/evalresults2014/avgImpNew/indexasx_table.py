#!/usr/bin/python3
# generate one file x-axis is problem indexes 1-20
import os
import math

def stdv(implist):
    totalnum = len(implist)
    avg = 0
    sq = 0
    for imp in implist:
        avg += imp
    avg = avg/totalnum

    for imp in implist:
        sq += ((imp-avg)*(imp-avg))
    sq = sq/totalnum
    res = math.sqrt(sq)
    return res

def calcuAvg(Mods, Edges, maxindex):
    outfilename = 'indexasx_table.tex'
    outfile = open(outfilename, 'w')
    outfile.write('Prb Idx  & $(m, |E_w|, n)$  & Imp(HBCS)\%  &   StdDv  &  Imp(SS)\%  &  StdDv \\\\ \n')
    outfile.write('\\hline\n')
    # prb Idx
    for i in range(0, maxindex):
        avgoverhbcs = 0
        avgoverss = 0
        impoverhbcsalllevels = []
        impoverssalllevels = []

        # instance Idx
        maxIdx = 50
        for j in range(0, maxIdx):
            filename = '{}_{}_{}_Imp.txt'.format(Mods[i], Edges[i], j)
            imps = open(filename, 'r')
        
            for line in imps:
                items = line.split()
                if (items[0].isdigit()==False):
                    continue
                #budlevel = int(items[0])
                impoverhbcs = float(items[1])
                impoverss = float(items[2])

                avgoverhbcs += impoverhbcs;
                impoverhbcsalllevels.append(impoverhbcs)
                avgoverss += impoverss;
                impoverssalllevels.append(impoverss)
            imps.close()

        avgoverhbcs = avgoverhbcs/(len(impoverhbcsalllevels))
        avgoverss = avgoverss/(len(impoverssalllevels))
        stdvhbcs = stdv(impoverhbcsalllevels)
        stdvss = stdv(impoverssalllevels)
        writeline = '%d  &    $(%d, %d, %d)$   &    $%.2f$   &     $%.2f$   &     $%.2f$    &    $%.2f$ \\\\ \n'%( i+1, Mods[i], Edges[i], i+5, avgoverhbcs, stdvhbcs, avgoverss, stdvss)
        outfile.write(writeline)
    outfile.close()

    

if __name__=='__main__':
    Mods = [5, 10, 15, 20, 25, 30, 35, 40, 45, 50,
            55, 60, 65, 70, 75, 80, 85, 90, 95, 100]
    
    Edges = [6, 15, 60, 80, 200, 300, 500, 500, 580, 500,
            800, 900, 950, 950, 1000, 1200, 1200, 1600, 1600, 2000]
    
    scales = 20

    calcuAvg(Mods, Edges, scales)
