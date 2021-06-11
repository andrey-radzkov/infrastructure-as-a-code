package com.demo;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.*;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.poiji.bind.Poiji.fromExcel;


public class DemoApplication {
    public static final String API_KEY1 = "WHIQGIT86LA8T5KA";
    public static final String API_KEY2 = "6O4W5HH7NQUORB2Z";
    public static final String API_KEY3 = "5ALCZYTSFW3KMSV4";
    public static final String API_KEY4 = "9VCH0XM409DI1ZF3";
    public static final String API_KEY5 = "CFD6QEHK5UEK5SVQ";
    public static final String[] API_KEYS = {API_KEY1, API_KEY2, API_KEY3, API_KEY4, API_KEY5};
    public static final Map<String, Integer> keyUsageCount = new HashMap<>();
    public static final double GROW_FACTOR = 1.2;
    public static final int PE_TRASHHOLD = 50;
    /**
     * Limit is 500 calls per day per api key
     */
    public static final int DAILY_LIMIT = 500;
    public static final int MINUTE_LIMIT = 5;
    public static final String REPORT_PATH = "D:/projects/infrastructure-as-a-code/src/main/resources/stocks_report.xlsx";

    //TODO: skip not interesting in future
    //https://github.com/shilewenuw/get_all_tickers/blob/master/get_all_tickers/tickers.csv
    //https://www.alphavantage.co/query?function=TIME_SERIES_MONTHLY_ADJUSTED&symbol=IBM&apikey=2DHC1EFVR3EOQ33Z
//https://query1.finance.yahoo.com/v7/finance/download/NFLX?period1=1022112000&period2=1589241600&interval=1d&events=history
    /*    1. stable grow
          2. down from 2020 corona
          3. high volatile (big grow and down)
          4. rate by dividents
          5. P/E  https://www.alphavantage.co/query?function=OVERVIEW&symbol=IBM&apikey=2DHC1EFVR3EOQ33Z
          6. group by Industry and Technology
          7. Compare P/E by industry
     */
    public static void main(String[] args) throws IOException {
        FileInputStream inputStream = new FileInputStream(REPORT_PATH);
        Workbook workbook = WorkbookFactory.create(inputStream);
        final List<CompanyReport> companyReports = fromExcel(Paths.get(REPORT_PATH).toFile(), CompanyReport.class);
        companyReports.stream().filter(DemoApplication::skipByDate)
                .forEach((report) -> globalIndicatorsForCode(report, workbook));

    }

    private static boolean skipByDate(CompanyReport companyReport) {
        //TODO: check by outdated
        return companyReport.getLastCheckDate() == null;
    }

    private static void globalIndicatorsForCode(CompanyReport companyReport, Workbook workbook) {
        System.out.println("evaluating " + companyReport.getCode());
        Sheet sheet = workbook.getSheetAt(0);
        final EasyJson overview = executeHttp("https://www.alphavantage.co/query?function=OVERVIEW&symbol=" + companyReport.getCode() + "&apikey=" + getApiKey());
        try {
            String companyName = overview.getT("Name");
            String sector = overview.getT("Sector");
            String industry = overview.getT("Industry");
            Map<String, String> indicatorsPE = findLowPERatio(overview);
            Map<String, String> indicatorsGrow = findStableGrow(companyReport.getCode());
            Map<String, String> indicatorsAll = new HashMap<>();
            indicatorsAll.putAll(indicatorsPE);
            indicatorsAll.putAll(indicatorsGrow);
            Row currentRow = sheet.getRow(companyReport.getRow());

            updateCell(new Date().toString(), currentRow, 0);
            updateCell(companyName, currentRow, 2);
            updateCell(sector, currentRow, 3);
            updateCell(industry, currentRow, 4);
            updateCell(indicatorsAll.get("P/E"), currentRow, 6);
            updateCell(indicatorsAll.get("5 year grow"), currentRow, 7);
            updateCell(indicatorsAll.get("High stable grow"), currentRow, 8);

            updateCell(indicatorsAll.size() == 2 ? "Good" : indicatorsAll.size() == 3 ? "Perfect" : null, currentRow, 5);
            try (FileOutputStream outputFile = new FileOutputStream(REPORT_PATH)) {
                workbook.write(outputFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void updateCell(String value, Row currentRow, int i) {
        Cell cell2Update = currentRow.getCell(i);
        if (cell2Update == null) {
            cell2Update = currentRow.createCell(i);
        }
        cell2Update.setCellValue(value);
    }

    private static Map<String, String> findStableGrow(String code) {
        final EasyJson mounthly = executeHttp("https://www.alphavantage.co/query?function=TIME_SERIES_MONTHLY_ADJUSTED&symbol=" + code + "&apikey=" + getApiKey());
        Map<String, String> indicators = new HashMap<>();
        try {
            final List<String> dates = ((JSONObject) mounthly.get("Monthly Adjusted Time Series")).toMap().keySet().stream().sorted(Collections.reverseOrder()).collect(Collectors.toList());
            if (dates.size() > 60) { //TODO: common case
                final double year5 = Double.parseDouble(mounthly.getT("Monthly Adjusted Time Series." + dates.get(60) + ".4\\. close"));
                final double year4 = Double.parseDouble(mounthly.getT("Monthly Adjusted Time Series." + dates.get(48) + ".4\\. close"));
                final double year3 = Double.parseDouble(mounthly.getT("Monthly Adjusted Time Series." + dates.get(36) + ".4\\. close"));
                final double year2 = Double.parseDouble(mounthly.getT("Monthly Adjusted Time Series." + dates.get(24) + ".4\\. close"));
                final double year1 = Double.parseDouble(mounthly.getT("Monthly Adjusted Time Series." + dates.get(12) + ".4\\. close"));
                final double year0 = Double.parseDouble(mounthly.getT("Monthly Adjusted Time Series." + dates.get(0) + ".4\\. close"));
                if (year0 > year5 * 2) {
                    indicators.put("5 year grow", String.format("%.2f", year0 / year5));
                }
                if (year4 > year5 * GROW_FACTOR
                        && year3 > year4 * GROW_FACTOR
                        && year2 > year3 * GROW_FACTOR
                        && year1 > year2 * GROW_FACTOR
                        && year0 > year1 * GROW_FACTOR
                ) {
                    indicators.put("High stable grow", ">20% per each year");
                }
            }
        } catch (Exception e) {
            //do nothing
        }
        return indicators;
    }

    private static Map<String, String> findLowPERatio(EasyJson overview) {
        final String peRatio = overview.getT("PERatio");
        Map<String, String> indicators = new HashMap<>();
        double PERatio = Double.parseDouble(peRatio != null && !peRatio.equals("None") ? peRatio : "100000000000000");
        if (PERatio < PE_TRASHHOLD) {
            indicators.put("P/E", Double.toString(PERatio));
        }
        return indicators;
    }

    private static EasyJson executeHttp(String url) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet getRequest = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(getRequest);
            String responseString = EntityUtils.toString(response.getEntity(), "utf-8");
            return new EasyJson(responseString);
        } catch (IOException e) {
            throw new RuntimeException("Error during the api call", e);
        }
    }

    private static String getApiKey() {
        //TODO: dayli limit + rotation
        try {
            Thread.sleep(60000 / MINUTE_LIMIT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int i = 0;
        int usage;
        String currentKey;
        do {
            currentKey = API_KEYS[i];
            usage = keyUsageCount.getOrDefault(currentKey, 0);
            keyUsageCount.put(currentKey, usage + 1);
            i++;
        } while (usage == DAILY_LIMIT && (i != API_KEYS.length));
        if (currentKey == null) {
            System.exit(0);
        }
        return currentKey;
    }
}
