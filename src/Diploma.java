import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Diploma {

    private static List<String> dataList = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        int port = 8081;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new HtmlHandler());
        server.createContext("/data", new DataHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on port " + port);
    }

    static class HtmlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head><title>TDS ESP32</title><meta charset=\"utf-8\"></head><body>");
            htmlBuilder.append("<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>");

            htmlBuilder.append("<h1 style=\"text-align: center;\"></h1>");
            htmlBuilder.append("<ul>");
            synchronized (dataList) {
                htmlBuilder.append("<h1 style=\"text-align: center;\">TDS value: ").append(dataList.get(dataList.size() - 1)).append(" ppm</h1>");
            }
            htmlBuilder.append("</ul>");
            htmlBuilder.append("<canvas id=\"dataChart\" width=\"400\" height=\"200\"></canvas>");
            htmlBuilder.append("<script>");
            htmlBuilder.append("var ctx = document.getElementById('dataChart').getContext('2d');");
            htmlBuilder.append("var dataChart = new Chart(ctx, {");
            htmlBuilder.append("type: 'line',");
            htmlBuilder.append("data: {");
            htmlBuilder.append("labels: [");
            synchronized (dataList) {
                for (int i = 0; i < dataList.size(); i++) {
                    htmlBuilder.append("\"").append(i).append("\"");
                    if (i < dataList.size() - 1) {
                        htmlBuilder.append(",");
                    }
                }
            }
            htmlBuilder.append("],");
            htmlBuilder.append("datasets: [{");
            htmlBuilder.append("label: 'Data',");
            htmlBuilder.append("data: [");
            synchronized (dataList) {
                for (int i = 0; i < dataList.size(); i++) {
                    htmlBuilder.append(dataList.get(i));
                    if (i < dataList.size() - 1) {
                        htmlBuilder.append(",");
                    }
                }
            }
            htmlBuilder.append("],");
            htmlBuilder.append("borderColor: 'rgba(75, 192, 192, 1)',");
            htmlBuilder.append("borderWidth: 1");
            htmlBuilder.append("}]");
            htmlBuilder.append("},");
            htmlBuilder.append("options: {");
            htmlBuilder.append("scales: {");
            htmlBuilder.append("y: {");
            htmlBuilder.append("beginAtZero: true");
            htmlBuilder.append("}");
            htmlBuilder.append("}");
            htmlBuilder.append("}");
            htmlBuilder.append("});");
            htmlBuilder.append("</script>");
            htmlBuilder.append("</body></html>");

            String response = htmlBuilder.toString();
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class DataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Read the request body
                InputStream is = exchange.getRequestBody();
                String requestBody = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
                System.out.println("Received data: " + requestBody);

                // Store the received data
                synchronized (dataList) {
                    dataList.add(requestBody);
                }

                String response = "Data received";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                String response = "Invalid request method";
                exchange.sendResponseHeaders(405, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
}