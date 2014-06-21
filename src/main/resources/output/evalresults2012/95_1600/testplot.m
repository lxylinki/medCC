x = [1:20];
y1 = load('workflow_95_1600_3_GAIN_output.txt');
y2 = load('workflow_95_1600_3_CG_output.txt');
plot(x, y1, ':or', x, y2, '-^b', 'LineWidth', 2.0);

xlabel('Index of budget');
ylabel('End-to-end Delay (hours)');