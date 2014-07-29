#!/usr/bin/python3
# generate one file x-asix is budget level 1-20
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


def calcuAvg(Mods, Edges, maxbudlevel):
    outfilename = 'costasx_table.tex'
    outfile = open(outfilename, 'w')
    outfile.write('Budget Level &   Imp(HBCS) \% &   StdDv &   Imp(SS) \%  &  StdDv \\\\ \n')

    # for each bud level 1-20
    for l in range(1, maxbudlevel+1):
        avgoverhbcs = 0
        avgoverss = 0
        impoverhbcsallindex = []
        impoverssallindex = []
        
        # collect imp across each index on this level
        scales = 20
        maxIdx = 50
        for i in range(0, scales):
            for j in range(0, maxIdx):
                filename = '{}_{}_{}_Imp.txt'.format(Mods[i], Edges[i], j)
                imps = open(filename, 'r')
                linecount = 0
                for line in imps:
                    items = line.split()
                    if (items[0].isdigit() == False):
                        continue
                    linecount += 1
                    budlevel = int(items[0])
                    if (budlevel != l):
                        continue
                    if (budlevel == l):
                        impoverhbcs = float(items[1])
                        impoverss = float(items[2])

                        avgoverhbcs += impoverhbcs
                        avgoverss += impoverss

                        impoverhbcsallindex.append(impoverhbcs)
                        impoverssallindex.append(impoverss)
                imps.close()
        avgoverhbcs = avgoverhbcs/(len(impoverhbcsallindex))
        stdvhbcs = stdv(impoverhbcsallindex)
        avgoverss = avgoverss/(len(impoverssallindex))
        stdvss = stdv(impoverssallindex)
        writeline = '%d    &    $%.2f$    &    $%.2f$   &     $%.2f$   &     $%.2f$ \\\\ \n'%(l, avgoverhbcs, stdvhbcs, avgoverss, stdvss)
        outfile.write(writeline)
    outfile.close()

if __name__=='__main__':
    Mods = [5, 10, 15, 20, 25, 30, 35, 40, 45, 50,
            55, 60, 65, 70, 75, 80, 85, 90, 95, 100]

    Edges = [6, 15, 60, 80, 200, 300, 500, 500, 580, 500,
            800, 900, 950, 950, 1000, 1200, 1200, 1600, 1600, 2000]
    
    # 20 bud levels
    budlevels = 20

    calcuAvg(Mods, Edges, budlevels)





    
