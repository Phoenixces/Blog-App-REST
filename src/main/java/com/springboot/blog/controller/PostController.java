package com.springboot.blog.controller;

import com.springboot.blog.payload.PostDto;
import com.springboot.blog.payload.PostResponse;
import com.springboot.blog.payload.PostResponseWrapper;
import com.springboot.blog.service.PostService;
import com.springboot.blog.exception.RateLimitExceededException;
import com.springboot.blog.utils.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import jakarta.validation.Valid;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.io.IOException;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Post")
public class PostController {

        private PostService postService;
        private final StringRedisTemplate redisTemplate;

        public PostController(PostService postService, StringRedisTemplate redisTemplate) {
                this.postService = postService;
                this.redisTemplate = redisTemplate;
        }

        private boolean isRateLimited(String userId) {
                String key = "rate_limit:" + userId;
                ValueOperations<String, String> ops = redisTemplate.opsForValue();

                String count = ops.get(key);
                if (count == null) {
                        // Set a limit of 5 requests per minute
                        ops.set(key, "1", Duration.ofMinutes(1));
                        return false;
                } else if (Integer.parseInt(count) < 5) {
                        ops.increment(key);
                        return false;
                }
                return true; // Rate limit exceeded
        }

        @Operation(summary = "Create Post REST API", description = "Create Post REST API is used to save post into database")
        @ApiResponse(responseCode = "201", description = "Http Status 201 CREATED")
        @SecurityRequirement(name = "Bear Authentication")
        // create blog post rest api
        @PreAuthorize("hasRole('ADMIN')")
        @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }, produces = {
                        MediaType.APPLICATION_JSON_VALUE })
        public ResponseEntity<PostResponseWrapper> createPost(@Valid @ModelAttribute PostDto postDto)
                        throws IOException {
                return new ResponseEntity<>(postService.createPost(postDto), HttpStatus.CREATED);
        }

        @Operation(summary = "Get All Posts REST API", description = "Get All Posts REST API is used to fetch all the posts from the database")
        @ApiResponse(responseCode = "200", description = "Http Status 200 SUCCESS")
        // get all posts rest api
        @GetMapping
        public PostResponse getAllPosts(
                        @RequestParam(value = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
                        @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
                        @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
                        @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
                String userId = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                                .getRequest()
                                .getRemoteAddr();
                if (isRateLimited(userId)) {
                        throw new RateLimitExceededException("Rate limit exceeded. Try again later.");
                }
                return postService.getAllPosts(pageNo, pageSize, sortBy, sortDir);
        }

        @Operation(summary = "Get Post By Id REST API", description = "Get Post By Id REST API is used to get single post from the database")
        @ApiResponse(responseCode = "200", description = "Http Status 200 SUCCESS")
        // get post by id
        @GetMapping("/{id}")
        public ResponseEntity<PostDto> getPostById(@PathVariable(name = "id") long id) {
                return ResponseEntity.ok(postService.getPostById(id));
        }

        @Operation(summary = "update Post REST API", description = "Update Post REST API is used to update a particular post in the database")
        @ApiResponse(responseCode = "200", description = "Http Status 200 SUCCESS")
        @SecurityRequirement(name = "Bear Authentication")
        // update post by id rest api
        @PreAuthorize("hasRole('ADMIN')")
        @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<PostDto> updatePost(@Valid @ModelAttribute PostDto postDto,
                        @PathVariable(name = "id") long id) throws IOException {

                PostDto postResponse = postService.updatePost(postDto, id);

                return new ResponseEntity<>(postResponse, HttpStatus.OK);
        }

        @Operation(summary = "Delete Post REST API", description = "Delete Post REST API is used to delete a particular post from the database")
        @ApiResponse(responseCode = "200", description = "Http Status 200 SUCCESS")
        @SecurityRequirement(name = "Bear Authentication")
        // delete post rest api
        @PreAuthorize("hasRole('ADMIN')")
        @DeleteMapping("/{id}")
        public ResponseEntity<String> deletePost(@PathVariable(name = "id") long id) {

                postService.deletePostById(id);

                return new ResponseEntity<>("Post entity deleted successfully.", HttpStatus.OK);
        }

        // Build Get Posts by Category REST API
        // http://localhost:8080/api/posts/category/3
        @GetMapping("/category/{id}")
        public ResponseEntity<List<PostDto>> getPostsByCategory(@PathVariable("id") Long categoryId) {
                List<PostDto> postDtos = postService.getPostsByCategory(categoryId);
                return ResponseEntity.ok(postDtos);
        }
}
