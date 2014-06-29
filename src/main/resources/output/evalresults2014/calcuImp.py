# import concurrent.futures
import os

# produce an imp file
def calcuImp(mods, edges, index):
    # med results
    filedir = './{}_{}/'.format(mods, edges)

    filename = 'workflow_{}_{}_{}.txt'.format(mods, edges, index)
    filename = os.path.join(filedir,filename)
    results = open(filename, 'r')
    
    # output filename
    impfilename = '{}_{}_{}_Imp.txt'.format(mods, edges, index)
    impfilename = os.path.join(filedir, impfilename)
    impfile = open(impfilename, 'w')
    impfile.write('        CG/HBCS        CG/ScaleStar\n')

    for line in results:
        # skip header line
        if (line.split()[0].isdigit()==False):
            continue
        items = line.split()
        budlevel = int(items[0])
   
        # med measurements
        cg = float(items[1])
        hbcs = float(items[2])
        ss = float(items[3])
        
        # print percentage
        impoverhbcs = (hbcs - cg)*100/hbcs
        impoverss = (ss-cg)*100/ss
        impline = '%d        %.2f        %.2f\n' % (budlevel, impoverhbcs, impoverss)
        impfile.write(impline)
        
    results.close()
    impfile.close()



if __name__=='__main__':
    
    Mods = [5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 
            55, 60, 65, 70, 75, 80, 85, 90, 95, 100]
    
    Edges = [6, 15, 60, 80, 200, 300, 500, 500, 580, 500, 
            800, 900, 950, 950, 1000, 1200, 1200, 1600, 1600, 2000]
    
    for i in range (0,10):
        for j in range (0,20):
            m = Mods[j]
            e = Edges[j]
            calcuImp(m, e, i)
