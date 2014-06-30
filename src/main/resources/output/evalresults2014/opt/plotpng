set terminal png enhanced font ',16'
set output 'optstats.png'
set style data histogram
set xtics nomirror rotate by -45 scale 0 font ',16'
set xlabel 'Problem Index'
set ylabel 'Number of Optimal Results'
set grid y
set auto x
set auto y
set style histogram cluster gap 2
set style fill solid noborder
plot "optstats.dat" u 1:xtic(4) ti col lc rgb '#0099FF', '' u 2 ti col lc rgb '#3C3C3C', '' u 3 ti col lc rgb '#171717'

