package ru.netology.config;

import ru.netology.controller.PostController;
import ru.netology.repository.PostRepository;
import ru.netology.service.PostService;

public class AppConfig {

    public PostRepository postRepository() {
        return new PostRepository();
    }

    public PostService postService() {
        return new PostService(postRepository());
    }

    public PostController postController() {
        return new PostController(postService());
    }
}