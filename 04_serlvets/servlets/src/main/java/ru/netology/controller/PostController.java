package ru.netology.controller;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.netology.exception.NotFoundException;
import ru.netology.model.Post;
import ru.netology.service.PostService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;


@Controller
public class PostController {
    public static final String APPLICATION_JSON = "application/json";
    private final PostService service;
    private final Gson gson = new Gson();

    @Autowired
    public PostController(PostService service) {
        this.service = service;
    }

    @GetMapping
    public void all(HttpServletResponse response) throws IOException {
        response.setContentType(APPLICATION_JSON);
        final var data = service.all();
        response.getWriter().print(gson.toJson(data));
    }

    @GetMapping("/{id}")
    public void getById(@PathVariable long id, HttpServletResponse response) throws IOException {
        response.setContentType(APPLICATION_JSON);
        try {
            final var post = service.getById(id);
            response.getWriter().print(gson.toJson(post));
        } catch (NotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @PostMapping
    public void save(@RequestBody Reader body, HttpServletResponse response) throws IOException {
        response.setContentType(APPLICATION_JSON);
        final var post = gson.fromJson(body, Post.class);
        final var data = service.save(post);
        response.getWriter().print(gson.toJson(data));
    }

    @DeleteMapping("/{id}")
    public void removeById(@PathVariable long id, HttpServletResponse response) {
        try {
            service.removeById(id);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (NotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
