import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MonitoringClient {

    private static final String MONITORING_ENDPOINT = "http://localhost:8080/monitoring";
    private static final String TARGET_ENDPOINT = "/api/hello GET";
    private static final String TABLE_SUMMARY = "http";

    public static void main(String[] args) {
        try {
            String postmanResponse = sendHttpGetRequest(MONITORING_ENDPOINT);
            Document document = Jsoup.parse(postmanResponse);

            Element httpGlobalTable = findTableBySummary(document, TABLE_SUMMARY);

            if (httpGlobalTable != null) {
                Elements rows = httpGlobalTable.select("tbody tr");
                findAndPrintHitsForEndpoint(rows, TARGET_ENDPOINT);
            } else {
                System.out.println("HTTP global table not found in the Postman response.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String sendHttpGetRequest(String endpoint) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                return in.lines().reduce(String::concat).orElse("");
            }
        } else {
            throw new IOException("HTTP GET request failed. Response Code: " + responseCode);
        }
    }

    private static Element findTableBySummary(Document document, String summary) {
        return document.select("table.sortable[summary=" + summary + "]").first();
    }

    private static void findAndPrintHitsForEndpoint(Elements rows, String targetEndpoint) {
        for (Element row : rows) {
            Element requestColumn = row.selectFirst("td.wrappedText");
            if (requestColumn != null && requestColumn.text().equals(targetEndpoint)) {
                Element hitsColumn = row.select("td").get(2);
                System.out.println("Number of hits for " + targetEndpoint + ": " + hitsColumn.text());
                return;
            }
        }
        System.out.println("Endpoint " + targetEndpoint + " not found in the table.");
    }
}
