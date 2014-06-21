x = [1:20];
y1 = load('workflow_10_15_6_GAIN_output.txt');
y2 = load('workflow_10_15_6_CG_output.txt');
plot(x, y1, ':or', x, y2, '-^b', 'LineWidth', 2.0);

xlabel('Budget Increment');
ylabel('Minimal End-to-end Delay (hours)');