package com.springboot.blog.controller;

import com.springboot.blog.entity.Category;
import com.springboot.blog.exception.RateLimitExceededException;
import com.springboot.blog.payload.CategoryDto;
import com.springboot.blog.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Category-v1")

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private CategoryService categoryService;
    private final StringRedisTemplate redisTemplate;

    public CategoryController(CategoryService categoryService, StringRedisTemplate redisTemplate) {
        this.categoryService = categoryService;
        this.redisTemplate = redisTemplate;
    }

    private boolean isRateLimited(String userId) {
        String key = "rate_limit:" + userId;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        String count = ops.get(key);
        if (count == null) {
            ops.set(key, "1", Duration.ofMinutes(1));
            return false;
        } else if (Integer.parseInt(count) < 5) {
            ops.increment(key);
            return false;
        }

        return true;
    }

    // Build Add Category REST API
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto> addCategory(@RequestBody CategoryDto categoryDto) {
        CategoryDto savedCategory = categoryService.addCategory(categoryDto);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

    // Build Get Category REST API
    @GetMapping("{id}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable("id") Long categoryId) {
        CategoryDto categoryDto = categoryService.getCategory(categoryId);
        return ResponseEntity.ok(categoryDto);
    }

    // Build Get All Categories REST API
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories() {
        String userId = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest()
                .getRemoteAddr();

        if (isRateLimited(userId)) {
            throw new RateLimitExceededException("Rate limit exceeded. Try again later.");
        }

        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // Build Update Category REST API
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("{id}")
    public ResponseEntity<CategoryDto> updateCategory(@RequestBody CategoryDto categoryDto,
            @PathVariable("id") Long categoryId) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryDto, categoryId));
    }

    // Build Delete Category REST API
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable("id") Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok("Category deleted successfully!.");
    }
}
