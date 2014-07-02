set terminal postscript eps enhanced color font ',18'
set output 'impoverhbcs.eps'
unset key
set style data pm3d
set pointsize 0.1
set style line 1 lc rgb '#3C3C3C' pt 7   # circle
set pm3d at s
set palette model HSV
set palette rgb 3,2,2
set xlabel 'Budget Level' 
set ylabel 'Problem Index'
set title 'Imp(HBCS)'
set auto x
set auto y
set zrange [-5:60]
set grid
splot 'impoverhbcs.dat' w points ls 1
