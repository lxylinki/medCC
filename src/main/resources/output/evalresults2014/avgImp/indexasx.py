# generate one file x-axis is problem indexes 1-20
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

def calcuAvg(Mods, Edges, maxindex):
    filedir = './overCost/'
    outfilename = 'indexasx.dat'
    outfilename = os.path.join('./', outfilename)
    outfile = open(outfilename, 'w')
    outfile.write('Idx    Imp(HBCS)    StdDv    Imp(ScaleStar)    StdDv\n')

    for i in range(0, maxindex):
        avgoverhbcs = 0
        avgoverss = 0
        impoverhbcsalllevels = []
        impoverssalllevels = []

        filename = '{}_{}_AvgOverCost.txt'.format(Mods[i], Edges[i])
        filename = os.path.join(filedir, filename)
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

        avgoverhbcs = avgoverhbcs/20
        avgoverss = avgoverss/20
        stdvhbcs = stdv(impoverhbcsalllevels)
        stdvss = stdv(impoverssalllevels)
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
