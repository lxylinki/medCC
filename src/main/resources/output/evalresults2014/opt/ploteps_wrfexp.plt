#!/usr/bin/gnuplot
set linetype 1 lc rgb '#FF6600' lt 1 lw 2   # --- orange
set linetype 2 lc rgb '#0099FF' lt 1 lw 2   # --- cyan
set linetype 3 lc rgb '#3299D9' lt 1 lw 2   # --- lightblue
set linetype 4 lc rgb '#9AD322' lt 1 lw 2   # --- lightgreen
set linetype 5 lc rgb '#C21602' lt 1 lw 2   # --- darkred
set linetype 6 lc rgb '#3C3C3C' lt 1 lw 2   # --- grey
set terminal postscript eps enhanced color font ',18'
set size 1,1
set output 'wrfexp.eps'
set style data histogram
set xtics nomirror scale 0 font ',18'
set xlabel 'Budget Level'
set ylabel 'MED'
set grid y
set auto x
set auto y
set style histogram cluster gap 2
set style fill solid noborder
plot 'wrfexp_8_10.dat' u 2:xtic(1) ti col lc rgb '#9AD322', '' u 3 ti col lc rgb '#C21602', '' u 4 ti col lc rgb '#3299D9', '' u 5 ti col w lines lt 6

