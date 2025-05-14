package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final List<String> VALID_PATHS = List.of(
            "/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js", "favicon.ico"
    );
    private static final int THREAD_POOL_SIZE = 64;
    private final int port;
    private final ExecutorService threadPool;

    public Server(int port) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public void start() {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    final var socket = serverSocket.accept();
                    threadPool.submit(() -> connection(socket));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            threadPool.shutdown();
        }
    }

    private void connection(Socket socket) {
        try (
                socket;
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            final var requestLine = in.readLine();
            if (requestLine == null) return;

            final var parts = requestLine.split(" ");
            if (parts.length != 3) {
                return;
            }

            final var path = parts[1];
            if (!VALID_PATHS.contains(path)) {
                sendNotFound(out);
            }

            final var filePath = Path.of("01_web/http-server/", "public", path);
            if (path.equals("/classic.html")) {
                classicHtml(filePath, out);
            } else {
                sendFile(filePath, out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void classicHtml(Path filePath, BufferedOutputStream out) throws IOException {
        final var mimeType = Files.probeContentType(filePath);
        final var template = Files.readString(filePath);
        final var content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();

        sendResponse(out, "200 OK", mimeType, content.length);
        out.write(content);
        out.flush();
    }

    private void sendFile(Path filePath, BufferedOutputStream out) throws IOException {
        final var mimeType = Files.probeContentType(filePath);
        final var length = Files.size(filePath);

        sendResponse(out, "200 OK", mimeType, length);
        Files.copy(filePath, out);
        out.flush();
    }

    private void sendNotFound(BufferedOutputStream out) throws IOException {
        sendResponse(out, "404 Not Found", null, 0);
    }

    private void sendResponse(BufferedOutputStream out, String status, String mimeType, long contentLength) throws IOException {
        var response = new StringBuilder()
                .append("HTTP/1.1 ").append(status).append("\r\n");

        if (mimeType != null) {
            response.append("Content-Type: ").append(mimeType).append("\r\n");
        }

        response.append("Content-Length: ").append(contentLength).append("\r\n")
                .append("Connection: close\r\n")
                .append("\r\n");

        out.write(response.toString().getBytes());
    }
}
