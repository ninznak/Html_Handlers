package ru.netology;

public class Main {
    public static void main(String[] args) {

        final var server = new Server();

        // Обработчик для "/search" с query-параметрами
        server.addHandler("GET", "/search", (request, out) -> {
            String query = request.getQueryParam("q").orElse("default");
            String response = "Search results for: " + query;

            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/plain\r\n" +
                            "Content-Length: " + response.length() + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n" + response
            ).getBytes());
            out.flush();
        });

        server.listen(8008);
    }
}


