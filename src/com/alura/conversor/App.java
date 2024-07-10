package com.alura.conversor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class App {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Manejar la solicitud para cargar index.html
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String requestMethod = exchange.getRequestMethod();
                if (requestMethod.equalsIgnoreCase("GET")) {
                    String filePath = "src/com/alura/conversor/index.html";
                    File file = new File(filePath);

                    if (file.exists()) {
                        exchange.sendResponseHeaders(200, file.length());
                        OutputStream outputStream = exchange.getResponseBody();
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        fileInputStream.close();
                        outputStream.close();
                    } else {
                        String response = "Archivo no encontrado";
                        exchange.sendResponseHeaders(404, response.getBytes().length);
                        OutputStream outputStream = exchange.getResponseBody();
                        outputStream.write(response.getBytes());
                        outputStream.close();
                    }
                }
            }
        });

        // Manejar la solicitud para convertir moneda
        server.createContext("/convertir", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                    // Obtener los parámetros de la solicitud
                    String query = exchange.getRequestURI().getQuery();
                    Map<String, String> params = queryToMap(query);
                    String fromCurrency = params.get("from");
                    String toCurrency = params.get("to");
                    double amount = Double.parseDouble(params.get("amount"));

                    try {
                        double result = ConversorService.convertirMoneda(fromCurrency, toCurrency, amount);
                        String response = Double.toString(result);
                        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes(StandardCharsets.UTF_8));
                        os.close();
                    } catch (Exception e) {
                        String response = "Error al convertir la moneda: " + e.getMessage();
                        exchange.sendResponseHeaders(500, response.getBytes(StandardCharsets.UTF_8).length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes(StandardCharsets.UTF_8));
                        os.close();
                    }
                }
            }
        });

        server.setExecutor(null); 
        server.start();
        System.out.println("Servidor iniciado en el puerto 8080");
        System.out.println("Acceda mediante el siguiente link al conversor de moneda: http://localhost:8080/");
    }

    // Método auxiliar para convertir la cadena de consulta en un mapa de parámetros clave-valor
    private static Map<String, String> queryToMap(String query) {
        if (query == null) {
            return Collections.emptyMap();
        }

        return Arrays.stream(query.split("&"))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(pair -> pair[0], pair -> pair[1]));
    }
}
