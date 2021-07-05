package com.demo;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.demo.HttpUtil.executeHttp;
import static com.demo.KeyUtil.getApiKey;
import static com.poiji.bind.Poiji.fromExcel;

public class EmaTrendAnalysys {
    public static final String LONG_INTERVAL = "daily";
    //    public static final String SHORT_INTERVAL = "60min";
    public static final String SHORT_INTERVAL = "daily";
    //    public static final String S_T_P = "10";
    public static final String S_T_P = "30";
    public static final String L_T_P = "200";
    public static final String T_S_D = "Time Series (Daily)";
    public static final int WIDTH = 1250;
    public static final int HEIGHT = 650;
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT_EMA = SHORT_INTERVAL.equals("daily") ? new SimpleDateFormat("yyyy-MM-dd") : new SimpleDateFormat("yyyy-MM-dd hh:mm");
    public static final String T_A_E = "Technical Analysis: EMA";
    public static String code = "AAPL";
    public static final String REPORT_PATH = "D:/projects/infrastructure-as-a-code/src/main/resources/stocks_report.xlsx";


    public static void main(String[] args) throws IOException {
//        final List<CompanyReport> companyReports = fromExcel(Paths.get(REPORT_PATH).toFile(), CompanyReport.class);
//        companyReports.stream().filter(GeneralPerformanceAnalysys::skipByDate)
//                .map(CompanyReport::getCode)
//                .forEach((code) -> {
//                    try {
//                        createChart(code);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                });

        createChart(code);
    }

    private static void createChart(String code) throws IOException {
        try {
            final EasyJson shortTrend = executeHttp("https://www.alphavantage.co/query?function=EMA&symbol=" + code + "&interval=" + SHORT_INTERVAL + "&time_period=" + S_T_P + "&series_type=close&apikey=" + getApiKey());
            final EasyJson longTrend = executeHttp("https://www.alphavantage.co/query?function=EMA&symbol=" + code + "&interval=" + LONG_INTERVAL + "&time_period=" + L_T_P + "&series_type=close&apikey=" + getApiKey());
            final EasyJson daily = executeHttp("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=" + code + "&apikey=" + getApiKey());

            JFreeChart chart = ChartFactory.createTimeSeriesChart(
                    "EMA" + L_T_P + "-EMA" + S_T_P + " " + code,
                    "Date",
                    "Price($)",
                    createDataset(daily, shortTrend, longTrend)
            );
            chartCustomization(chart);
            ChartUtils.saveChartAsPNG(new File("D:\\projects\\infrastructure-as-a-code\\src\\main\\resources\\ema" + L_T_P + "-ema" + S_T_P + "-" + code + ".png"), chart, WIDTH, HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void chartCustomization(JFreeChart chart) {
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(205, 0, 23));
        renderer.setSeriesPaint(1, new Color(0, 55, 255, 255));
        renderer.setSeriesPaint(2, new Color(34, 113, 19));
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        renderer.setSeriesShapesVisible(2, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        renderer.setDefaultStroke(new BasicStroke(2.0f));
        plot.setRenderer(0, renderer);
    }

    private static XYDataset createDataset(EasyJson daily, EasyJson shortTrend, EasyJson longTrend) {
        TimeSeries originalSeries = originalSeries(daily);
        TimeSeries shortSeries = shortSeries(shortTrend);
        TimeSeries longSeries = longSeries(longTrend);
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(originalSeries);
        dataset.addSeries(shortSeries);
        dataset.addSeries(longSeries);

        return dataset;
    }

    private static TimeSeries originalSeries(EasyJson daily) {
        TimeSeries series = new TimeSeries("Original");
        final List<String> dates = ((JSONObject) daily.get(T_S_D)).toMap().keySet().stream().sorted(Collections.reverseOrder()).collect(Collectors.toList());
        dates.forEach(date -> {
            final double price = getPrice(daily, date);
            try {
                series.add(new Day(SIMPLE_DATE_FORMAT.parse(date)), price);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
        return series;
    }

    private static TimeSeries shortSeries(EasyJson shortEMA) {
        TimeSeries series = new TimeSeries("EMA" + S_T_P);
        final List<String> dates = ((JSONObject) shortEMA.get("Technical Analysis: EMA")).toMap().keySet().stream().sorted(Collections.reverseOrder()).collect(Collectors.toList());
        dates.stream().filter(date -> date.startsWith("2021")).forEach(date -> {
            final double price = getEMAPrice(shortEMA, date);
            try {
                series.add(new Hour(SIMPLE_DATE_FORMAT_EMA.parse(date)), price);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
        return series;
    }

    private static TimeSeries longSeries(EasyJson ema200) {
        TimeSeries series = new TimeSeries("EMA" + L_T_P);
        final List<String> dates = ((JSONObject) ema200.get("Technical Analysis: EMA")).toMap().keySet().stream().sorted(Collections.reverseOrder()).collect(Collectors.toList());
        dates.stream().filter(date -> date.startsWith("2021")).forEach(date -> {
            final double price = getEMAPrice(ema200, date);
            try {
                series.add(new Hour(SIMPLE_DATE_FORMAT.parse(date)), price);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
        return series;
    }

    /**
     * Open * (adjusted close/close)
     */
    public static double getPrice(EasyJson mounthly, String date) {
        final double close = Double.parseDouble(mounthly.getT(T_S_D + "." + date + ".4\\. close"));
        final double open = Double.parseDouble(mounthly.getT(T_S_D + "." + date + ".1\\. open"));
        final double adjustedClose = Double.parseDouble(mounthly.getT(T_S_D + "." + date + ".5\\. adjusted close"));
        return open * (adjustedClose / close);
    }

    /**
     * Open * (adjusted close/close)
     */
    public static double getEMAPrice(EasyJson emaJson, String date) {
        return Double.parseDouble(emaJson.getT(T_A_E + "." + date + ".EMA"));
    }
}
