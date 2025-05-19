package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final String body;
    private final List<NameValuePair> queryParams;

    public Request(String method, String path, String queryString,
                   Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
        this.queryParams = parseQueryParams(queryString);
    }

    private List<NameValuePair> parseQueryParams(String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return Collections.emptyList();
        }
        return URLEncodedUtils.parse(queryString, StandardCharsets.UTF_8);
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getHeaders() { return headers; }
    public String getBody() { return body; }

    public Optional<String> getQueryParam(String name) {
        return queryParams.stream()
                .filter(p -> p.getName().equals(name))
                .map(NameValuePair::getValue)
                .findFirst();
    }

    public List<String> getQueryParams(String name) {
        return queryParams.stream()
                .filter(p -> p.getName().equals(name))
                .map(NameValuePair::getValue)
                .collect(Collectors.toList());
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams.stream()
                .collect(Collectors.groupingBy(
                        NameValuePair::getName,
                        Collectors.mapping(NameValuePair::getValue, Collectors.toList())
                ));
    }
}