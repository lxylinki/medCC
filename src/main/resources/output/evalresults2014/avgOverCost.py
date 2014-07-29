#!/usr/bin/python3
# import concurrent.futures
import os

# produce an file containing imp avg on budget levels
def calcuAvg(mods, edges, maxindex):
    
    # Imp files directory
    filedir = './avgImpNew'
    outfiledir = './avgImpNew/overCost/'
    
    # output filename: one file per dir
    avgfilename = '{}_{}_AvgOverCost.txt'.format(mods, edges)
    avgfilename = os.path.join(outfiledir, avgfilename)
    avgfile = open(avgfilename, 'w')
    avgfile.write('Id        CG/HBCS        CG/ScaleStar\n')

    
    # from each imp file get one line of result
    for i in range (0, maxindex):
        impfilename = '{}_{}_{}_Imp.txt'.format(mods, edges, i)
        impfilename = os.path.join(filedir,impfilename)
        imps = open(impfilename, 'r')
        
        # avg over budget calculation 
        avgimpoverhbcs = 0
        avgimpoverss = 0
        linecount = 0
        for line in imps:
            # skip header line
            if (line.split()[0].isdigit()==False):
                continue
            linecount += 1
            items = line.split()
   
            # imp measurements
            impoverhbcs = float(items[1])
            avgimpoverhbcs += impoverhbcs

            impoverss = float(items[2])
            avgimpoverss += impoverss

        avgimpoverhbcs = avgimpoverhbcs/linecount
        avgimpoverss = avgimpoverss/linecount

        imps.close()

        avgimpline = '%d        %.2f        %.2f\n' % (i, avgimpoverhbcs, avgimpoverss)
        avgfile.write(avgimpline)
    avgfile.close()
        



if __name__=='__main__':
    
    Mods = [5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 
            55, 60, 65, 70, 75, 80, 85, 90, 95, 100]
    
    Edges = [6, 15, 60, 80, 200, 300, 500, 500, 580, 500, 
            800, 900, 950, 950, 1000, 1200, 1200, 1600, 1600, 2000]

    scales = 20
    
    maxIdx = 50
    
    for j in range (0, scales):
        m = Mods[j]
        e = Edges[j]
        calcuAvg(m, e, maxIdx)
