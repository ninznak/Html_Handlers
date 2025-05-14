package ru.netology;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();
    private final ExecutorService threadPool;
    private static final int THREAD_POOL_SIZE = 64;

    public Server() {
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new ConcurrentHashMap<>()).put(path, handler);
    }

    public void listen(int port) {
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
            var request = parseRequest(in);
            if (request == null) return;

            var methodHandlers = handlers.get(request.getMethod());
            if (methodHandlers != null) {
                var handler = methodHandlers.get(request.getPath());
                if (handler != null) {
                    handler.handle(request, out);
                    return;
                }
            }
            handleDefault(request, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Request parseRequest(BufferedReader in) throws IOException {
        final var requestLine = in.readLine();
        if (requestLine == null) return null;

        final var parts = requestLine.split(" ");
        if (parts.length != 3) return null;

        final var method = parts[0];
        final var path = parts[1];

        var headers = new HashMap<String, String>();
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            var index = line.indexOf(":");
            if (index > 0) {
                headers.put(line.substring(0, index).trim(), line.substring(index + 1).trim());
            }
        }

        var body = new StringBuilder();
        if (in.ready()) {
            while (in.ready()) {
                body.append((char) in.read());
            }
        }

        return new Request(method, path, headers, body.toString());
    }

    private void handleDefault(Request request, BufferedOutputStream out) throws IOException {
        final var filePath = Path.of("01_web/http-server/", "public", request.getPath());
        if (!Files.exists(filePath)) {
            sendResponse(out, "404 Not Found", null, 0);
            return;
        }

        final var mimeType = Files.probeContentType(filePath);
        if (request.getPath().equals("/classic.html")) {
            classicHtml(filePath, out, mimeType);
        } else {
            sendFile(filePath, out, mimeType);
        }
    }

    private void classicHtml(Path filePath, BufferedOutputStream out, String mimeType) throws IOException {
        final var template = Files.readString(filePath);
        final var content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();
        sendResponse(out, "200 OK", mimeType, content.length);
        out.write(content);
        out.flush();
    }

    private void sendFile(Path filePath, BufferedOutputStream out, String mimeType) throws IOException {
        final var length = Files.size(filePath);
        sendResponse(out, "200 OK", mimeType, length);
        Files.copy(filePath, out);
        out.flush();
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