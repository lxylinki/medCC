# import concurrent.futures
import os

# produce an avg file over prob indx 0-9
def calcuAvg(mods, edges, maxbudlevel):
    # imp files dir
    filedir = './{}_{}/'.format(mods, edges)
    outfiledir = './avgImp/overIndex/'

    # output filename
    avgfilename = '{}_{}_AvgOverIndex.txt'.format(mods, edges)
    avgfilename = os.path.join(outfiledir, avgfilename)
    avgfile = open(avgfilename, 'w')
    avgfile.write('Bd        CG/HBCS        CG/ScaleStar\n')

    for i in range (0, maxbudlevel):
        avgimpoverhbcs = 0
        avgimpoverss = 0

        # get imp at level i from imp file k
        for k in range (0, 10):
            impfilename = '{}_{}_{}_Imp.txt'.format(mods, edges, k)
            impfilename = os.path.join(filedir, impfilename)
            imps = open(impfilename, 'r')
            for line in imps:
                if (line.split()[0].isdigit()==False):
                    continue
                items  = line.split()
                budlevel = int(items[0])
                if (budlevel != i):
                    continue
                if (budlevel == i):
                    impoverhbcs = float(items[1])
                    impoverss = float(items[2])

                    avgimpoverhbcs += impoverhbcs
                    avgimpoverss += impoverss
            imps.close()

        avgimpoverhbcs = avgimpoverhbcs/10
        avgimpoverss = avgimpoverss/10

        writeline = '%d        %.2f        %.2f\n'%(i, avgimpoverhbcs, avgimpoverss)
        avgfile.write(writeline)
    avgfile.close()


if __name__=='__main__':
    
    Mods = [5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 
            55, 60, 65, 70, 75, 80, 85, 90, 95, 100]
    
    Edges = [6, 15, 60, 80, 200, 300, 500, 500, 580, 500, 
            800, 900, 950, 950, 1000, 1200, 1200, 1600, 1600, 2000]
    
    for j in range (0,20):
        m = Mods[j]
        e = Edges[j]
        calcuAvg(m, e, 20)
