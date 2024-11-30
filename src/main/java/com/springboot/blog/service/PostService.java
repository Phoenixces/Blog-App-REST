package com.springboot.blog.service;

import com.springboot.blog.payload.PostDto;
import com.springboot.blog.payload.PostResponse;
import com.springboot.blog.payload.PostResponseWrapper;

import java.io.IOException;
import java.util.List;

public interface PostService {
    PostDto createPost(PostDto postDto) throws IOException;

    PostResponse getAllPosts(int pageNo, int pageSize, String sortBy, String sortDir);

    PostDto getPostById(long id);

    PostDto updatePost(PostDto postDto, long id) throws IOException;

    void deletePostById(long id);

    List<PostDto> getPostsByCategory(Long categoryId);
}
