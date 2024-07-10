package com.alura.conversor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class ConversorHandler implements HttpHandler {

    private final ConversorService conversorService;

    public ConversorHandler() {
        this.conversorService = new ConversorService();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String from = params.get("from");
            String to = params.get("to");
            double amount = Double.parseDouble(params.get("amount"));

            try {
                double result = conversorService.convertirMoneda(from, to, amount);
                String response = Double.toString(result);
                exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes(StandardCharsets.UTF_8));
                os.close();
            } catch (Exception e) {
                String response = "Error: " + e.getMessage();
                exchange.sendResponseHeaders(500, response.getBytes(StandardCharsets.UTF_8).length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes(StandardCharsets.UTF_8));
                os.close();
            }
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        return query != null ? 
            Arrays.stream(query.split("&"))
                  .map(param -> param.split("="))
                  .collect(Collectors.toMap(pair -> pair[0], pair -> pair[1]))
            : Collections.emptyMap();
    }
}
