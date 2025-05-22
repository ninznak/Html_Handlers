package ru.netology.repository;

import ru.netology.model.Post;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PostRepository {
  private final AtomicLong counter = new AtomicLong(1);
  private final ConcurrentHashMap<Long, Post> posts = new ConcurrentHashMap<>();

  public List<Post> all() {
    return new ArrayList<>(posts.values());
  }

  public Optional<Post> getById(long id) {
    return Optional.ofNullable(posts.get(id));
  }

  public Post save(Post post) {
    long id = counter.getAndIncrement();
    Post newPost = new Post(id, post.getContent());
    posts.put(id, newPost);
    return newPost;
  }

  public Optional<Post> update(Post post) {
    if (posts.containsKey(post.getId())) {
      posts.put(post.getId(), post);
      return Optional.of(post);
    }
    return Optional.empty();
  }

  public boolean removeById(long id) {
    return posts.remove(id) != null;
  }
}
