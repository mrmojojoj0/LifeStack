package financeManager;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;


import java.util.*;
import java.awt.BorderLayout;

public class ExpenseGraph extends JPanel {
    public ExpenseGraph() {
        super(new BorderLayout());
    }

    public static JPanel createGraphPanel(int[] days, int[] expenses, int[] earnings) {
        XYSeries expensesSeries = new XYSeries("Expenses");
        XYSeries earningsSeries = new XYSeries("Earnings");

        for (int i = 0; i < days.length; i++) {
            expensesSeries.add(days[i], expenses[i]);
            earningsSeries.add(days[i], earnings[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(expensesSeries);
        dataset.addSeries(earningsSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Monthly Expense vs Earnings",
                "Day of Month",
                "Amount ($)",
                dataset
        );

        return new ChartPanel(chart);
    }

    public void updateChart(DefaultTableModel model) {
        Map<Integer, Double> dailyExpenses = new TreeMap<>();
        Map<Integer, Double> dailyEarnings = new TreeMap<>();

        for (int i = 0; i < model.getRowCount(); i++) {
            Date d = (Date) model.getValueAt(i, 0);
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            int day = c.get(Calendar.DAY_OF_MONTH);

            double amt = (Double) model.getValueAt(i, 3);
            String type = (String) model.getValueAt(i, 2);

            if (type.equals("Expense")) {
                dailyExpenses.put(day, dailyExpenses.getOrDefault(day, 0.0) + amt);
            } else if (type.equals("Earning")) {
                dailyEarnings.put(day, dailyEarnings.getOrDefault(day, 0.0) + amt);
            }
        }

        int maxDay = 31;
        int[] days = new int[maxDay];
        int[] expenses = new int[maxDay];
        int[] earnings = new int[maxDay];

        for (int day = 1; day <= maxDay; day++) {
            days[day - 1] = day;
            expenses[day - 1] = dailyExpenses.getOrDefault(day, 0.0).intValue();
            earnings[day - 1] = dailyEarnings.getOrDefault(day, 0.0).intValue();
        }

        JPanel graphPanel = createGraphPanel(days, expenses, earnings);

        this.removeAll();
        this.add(graphPanel, BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }
}
