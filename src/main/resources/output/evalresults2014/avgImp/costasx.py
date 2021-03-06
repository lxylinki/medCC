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
    avg = avg/20

    for imp in implist:
        sq += (imp-avg)*(imp-avg)
    sq = sq/20
    res = math.sqrt(sq)
    return res


def calcuAvg(Mods, Edges, maxbudlevel):
    
    filedir = './overIndex/'
    outfilename = 'costasx.dat'
    outfilename = os.path.join('./', outfilename)
    outfile = open(outfilename, 'w')
    outfile.write('Bd    Imp(HBCS)    StdDv    Imp(ScaleStar)    StdDv\n')

    # for each bud level
    for i in range(1, maxbudlevel+1):
        avgoverhbcs = 0
        avgoverss = 0
        impoverhbcsallindex = []
        impoverssallindex = []
        
        # collect imp across each index on this level
        for j in range(0, 20):
            filename = '{}_{}_AvgOverIndex.txt'.format(Mods[j], Edges[j])
            filename = os.path.join(filedir, filename)
            imps = open(filename, 'r')
            for line in imps:
                items = line.split()
                if (items[0].isdigit()==False):
                    continue
                budlevel = int(items[0])
                if (budlevel != i):
                    continue

                # find corresponding imp
                if (budlevel == i):
                    impoverhbcs = float(items[1])
                    impoverss = float(items[2])

                    avgoverhbcs += impoverhbcs
                    avgoverss += impoverss

                    # record to list
                    impoverhbcsallindex.append(impoverhbcs)
                    impoverssallindex.append(impoverss)
            imps.close()
        avgoverhbcs = avgoverhbcs/20
        stdvhbcs = stdv(impoverhbcsallindex)
        avgoverss = avgoverss/20
        stdvss = stdv(impoverssallindex)
        writeline = '%d        %.2f        %.2f        %.2f        %.2f\n'%(i, avgoverhbcs, stdvhbcs, avgoverss, stdvss)
        outfile.write(writeline)
    outfile.close()


if __name__=='__main__':
    Mods = [5, 10, 15, 20, 25, 30, 35, 40, 45, 50,
            55, 60, 65, 70, 75, 80, 85, 90, 95, 100]
    Edges = [6, 15, 60, 80, 200, 300, 500, 500, 580, 500,
            800, 900, 950, 950, 1000, 1200, 1200, 1600, 1600, 2000]
    # 20 bud levels
    calcuAvg(Mods, Edges, 20)





    
