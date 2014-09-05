#!/usr/bin/python3
import os

# statistics on optimal comparison
def optstats(mods, edges, types):
    # med files dir
    filedir = './opt/{}_{}/'.format(mods, edges)
    
    cgopt = 0
    hbcsopt = 0
    ssopt = 0
    
    # for each file: x100
    for i in range (0,100):
        resfilename = 'workflow_{}_{}_{}.txt'.format(mods, edges, i)
        resfilename = os.path.join(filedir, resfilename)
        results = open(resfilename, 'r')
        
        # for each budget level: x20
        for line in results:
            if (line.split()[0].isdigit()==False):
                continue
            items = line.split()
            
            cg = float(items[1])
            hbcs = float(items[2])
            ss = float(items[3])
            opt = float(items[4])

            if(cg == opt):
                cgopt = cgopt+1
            if(hbcs == opt):
                hbcsopt = hbcsopt+1
            if(ss == opt):
                ssopt = ssopt+1

        #writeline = ' %d        %d        %d\n'%(cgopt, hbcsopt, ssopt)
    writeline = '%d        %d        %d        (%d,%d,%d)\n'%(cgopt, hbcsopt, ssopt, mods, edges, types)
    results.close()
    return writeline


if __name__=='__main__':
    Mods = [5, 6, 7, 8]
    Edges = [6, 11, 14, 21]
    Types = [5, 6, 7, 8]
    scales = 4

    # output filename: one file per dir
    statsfilename = 'optstats.dat'
    statsfilename = os.path.join('./opt/', statsfilename)
    statsfile = open(statsfilename, 'w')
    statsfile.write('CG         HBCS       ScaleStar    PrbIndex\n')

    for j in range (0,scales):
        m = Mods[j]
        e = Edges[j]
        v = Types[j]
        writeline = optstats(m, e, Types[j])
        statsfile.write(writeline)
    statsfile.close()
