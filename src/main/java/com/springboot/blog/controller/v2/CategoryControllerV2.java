package com.springboot.blog.controller.v2;

import com.springboot.blog.controller.CategoryController;
import com.springboot.blog.payload.CategoryDto;
import com.springboot.blog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Category-v2")
@RestController
@RequestMapping("/api/v2/categories")
public class CategoryControllerV2 {

    private final CategoryController categoryControllerV1;
    private final CategoryService categoryService;

    public CategoryControllerV2(CategoryController categoryControllerV1, CategoryService categoryService) {
        this.categoryControllerV1 = categoryControllerV1;
        this.categoryService = categoryService;
    }

    @Operation(summary = "Partially update a category", description = "Updates specified fields of a category.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "403", description = "User does not have the required permissions")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto> patchCategory(@RequestBody CategoryDto categoryDto,
            @PathVariable Long id) {
        CategoryDto updatedCategory = categoryService.partialUpdateCategory(categoryDto, id);
        return ResponseEntity.ok(updatedCategory);
    }
}
