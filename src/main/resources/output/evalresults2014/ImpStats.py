import concurrent.futures
import re

def imp(filename):
    file = open(filename, 'r')
    for line in file:
        if(!line.split()[0].isdigit):
            continue
        items = line.split()
    file.close

if __name__=='__main__':
    with concurrent.futures.ProcessPoolExecutor(max_worker=2) as executor:
        for result in executor.map(imp, [filename1, filename2]):
            #process the file

