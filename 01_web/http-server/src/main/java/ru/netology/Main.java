package ru.netology;

public class Main {
    public static void main(String[] args) {

        final var server = new Server();

        server.addHandler("GET", "/messages", (request, responseStream) -> {
        });
        server.addHandler("POST", "/messages", (request, responseStream) -> {
        });
        server.listen(8008);
    }
}


