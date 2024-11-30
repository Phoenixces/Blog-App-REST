package com.springboot.blog.controller;

import com.springboot.blog.exception.RateLimitExceededException;
import com.springboot.blog.payload.CommentDto;
import com.springboot.blog.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Comment-v1")

@RestController
@RequestMapping("/api/v1")
public class CommentController {

    private CommentService commentService;
    private final StringRedisTemplate redisTemplate;

    public CommentController(CommentService commentService, StringRedisTemplate redisTemplate) {
        this.commentService = commentService;
        this.redisTemplate = redisTemplate;
    }

    private boolean isRateLimited(String userId) {
        String key = "rate_limit:" + userId;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        String count = ops.get(key);
        if (count == null) {
            // Set a limit of 5 requests per minute
            ops.set(key, "1", Duration.ofMinutes(1)); // Set expiration time to 1 minute
            return false;
        } else if (Integer.parseInt(count) < 5) {
            ops.increment(key); // Increment request count
            return false;
        }

        // If more than 5 requests are made within the time window
        return true; // Rate limit exceeded
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentDto> createComment(@PathVariable(value = "postId") long postId,
            @Valid @RequestBody CommentDto commentDto) {
        return new ResponseEntity<>(commentService.createComment(postId, commentDto), HttpStatus.CREATED);
    }

    @GetMapping("/posts/{postId}/comments")
    public List<CommentDto> getCommentsByPostId(@PathVariable(value = "postId") Long postId) {
        String userId = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest()
                .getRemoteAddr();
        if (isRateLimited(userId)) {
            throw new RateLimitExceededException("Rate limit exceeded. Try again later.");
        }
        return commentService.getCommentsByPostId(postId);
    }

    @GetMapping("/posts/{postId}/comments/{id}")
    public ResponseEntity<CommentDto> getCommentById(@PathVariable(value = "postId") Long postId,
            @PathVariable(value = "id") Long commentId) {
        CommentDto commentDto = commentService.getCommentById(postId, commentId);
        return new ResponseEntity<>(commentDto, HttpStatus.OK);
    }

    @PutMapping("/posts/{postId}/comments/{id}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable(value = "postId") Long postId,
            @PathVariable(value = "id") Long commentId,
            @Valid @RequestBody CommentDto commentDto) {
        CommentDto updatedComment = commentService.updateComment(postId, commentId, commentDto);
        return new ResponseEntity<>(updatedComment, HttpStatus.OK);
    }

    @DeleteMapping("/posts/{postId}/comments/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable(value = "postId") Long postId,
            @PathVariable(value = "id") Long commentId) {
        commentService.deleteComment(postId, commentId);
        return new ResponseEntity<>("Comment deleted successfully", HttpStatus.OK);
    }
}
