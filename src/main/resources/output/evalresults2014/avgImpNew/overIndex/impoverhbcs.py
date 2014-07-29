#!/usr/bin/python3
# generate a 20x20 matrix
import os

def collectimps(Mods, Edges, maxbudlevel):
    filedir = './'
    outfilename = 'impoverhbcs.dat'
    outfilename = os.path.join('./', outfilename)
    outfile = open(outfilename, 'w')
    
    # for each bud level
    for x in range(1, maxbudlevel+1):
        impoverhbcsallindex = []
        
        # collect imp across each index on this level
        for y in range(0,20):
            filename = '{}_{}_AvgOverIndex.txt'.format(Mods[y], Edges[y])
            filename = os.path.join(filedir, filename)
            imps = open(filename, 'r')
            for line in imps:
                items = line.split()
                if (items[0].isdigit()==False):
                    continue
                budlevel = int(items[0])
                if (budlevel != x):
                    continue

                # find corresponding imp
                if (budlevel == x):
                    impoverhbcs = float(items[1])
                    # record to list
                    impoverhbcsallindex.append(impoverhbcs)
            imps.close()
        for y in range(0,len(impoverhbcsallindex)):
            outfile.write('%d\t%d\t%.2f\n'% (x, y, impoverhbcsallindex[y]))
        outfile.write('\n')
    outfile.close()


if __name__=='__main__':
    Mods = [5, 10, 15, 20, 25, 30, 35, 40, 45, 50,
            55, 60, 65, 70, 75, 80, 85, 90, 95, 100]
    Edges = [6, 15, 60, 80, 200, 300, 500, 500, 580, 500,
            800, 900, 950, 950, 1000, 1200, 1200, 1600, 1600, 2000]
    # 20 bud levels
    collectimps(Mods, Edges, 20)






    
