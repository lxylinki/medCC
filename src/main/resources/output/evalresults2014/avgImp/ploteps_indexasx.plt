set linetype 1 lc rgb '#FF6600' lt 1 lw 2   # --- orange
set linetype 2 lc rgb '#0099FF' lt 1 lw 2   # --- cyan
set linetype 3 lc rgb '#3299D9' lt 1 lw 2   # --- lightblue
set linetype 4 lc rgb '#9AD322' lt 1 lw 2   # --- lightgreen
set linetype 5 lc rgb '#C21602' lt 1 lw 2   # --- darkred
set terminal postscript eps enhanced color font ',18'
set size 1,1
set output 'indexasx.eps'
set style data histogram
set xtics nomirror scale 0 font ',18'
set xlabel 'Problem Index'
set ylabel 'Average MED Improvement Percentage'
set grid y
set auto x
set yrange [-5:35]
set boxwidth 0.9
set style histogram cluster gap 2
set style histogram errorbars gap 2 lw 2
set style fill solid
plot 'indexasx.dat' u 2:3:xtic(1) ti col(2) lt 5, ''u 4:5 ti col(4) lt 3
