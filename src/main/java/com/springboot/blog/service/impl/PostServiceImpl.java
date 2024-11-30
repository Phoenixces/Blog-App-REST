package com.springboot.blog.service.impl;

import com.springboot.blog.entity.Category;
import com.springboot.blog.entity.Post;
import com.springboot.blog.exception.InvalidImageException;
import com.springboot.blog.exception.ResourceNotFoundException;
import com.springboot.blog.payload.*;
import com.springboot.blog.repository.CategoryRepository;
import com.springboot.blog.repository.PostRepository;
import com.springboot.blog.service.PostService;
import org.modelmapper.ModelMapper;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private PostRepository postRepository;

    private ModelMapper mapper;

    private CategoryRepository categoryRepository;

    public PostServiceImpl(PostRepository postRepository, ModelMapper mapper,
                           CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.mapper = mapper;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public PostDto createPost(PostDto postDto) throws IOException {
        MultipartFile imageFile = postDto.getImage();
        ImageValidationResponse validationResponse = null;

        Category category = categoryRepository.findById(postDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", postDto.getCategoryId()));

        // convert DTO to entity
        Post post = mapToEntity(postDto);
        post.setCategory(category);
        if (imageFile != null && !imageFile.isEmpty()){
            validationResponse = validateImageWithAPI(imageFile, post.getCategory().getName()); //Send Image to External API
                if (Objects.equals(validationResponse.getResult(), "No")) {
                    throw new InvalidImageException("Image does not matches with the Category, Reason: "+validationResponse.getReasoning(),
                    "Invalid image");
        } 
        }
            Post newPost = postRepository.save(post);
            // convert entity to DTO
            PostDto postResponse = mapToDTO(newPost);
            return postResponse;
    }

    @Override
    @Cacheable(value = "posts", key = "#pageNo + '-' + #pageSize + '-' + #sortBy + '-' + #sortDir")
    public PostResponse getAllPosts(int pageNo, int pageSize, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Post> posts = postRepository.findAll(pageable);

        // get content for page object
        List<Post> listOfPosts = posts.getContent();

        List<PostDto> content= listOfPosts.stream().map(post -> mapToDTO(post)).collect(Collectors.toList());

        PostResponse postResponse = new PostResponse();
        postResponse.setContent(content);
        postResponse.setPageNo(posts.getNumber());
        postResponse.setPageSize(posts.getSize());
        postResponse.setTotalElements(posts.getTotalElements());
        postResponse.setTotalPages(posts.getTotalPages());
        postResponse.setLast(posts.isLast());

        return postResponse;
    }

    @Override
    @Cacheable(value = "postById", key = "#id")
    public PostDto getPostById(long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        return mapToDTO(post);
    }

    @Override

    @CacheEvict(value = "posts", key = "#postDto.categoryId")
    public PostDto updatePost(PostDto postDto, long id) throws IOException {
        // get post by id from the database
        MultipartFile imageFile = postDto.getImage();
        ImageValidationResponse validationResponse = null;
        Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        Category category = categoryRepository.findById(postDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", postDto.getCategoryId()));

        post.setTitle(postDto.getTitle());
        post.setDescription(postDto.getDescription());
        post.setContent(postDto.getContent());
        post.setCategory(category);
        if (imageFile != null && !imageFile.isEmpty()){
            validationResponse = validateImageWithAPI(imageFile, post.getCategory().getName());//Send Image to External API
        if (Objects.equals(validationResponse.getResult(), "No")) {
            throw new InvalidImageException("Image does not matches with the Category-Reason: "+validationResponse.getReasoning(),"Invalid image");
        }}

            Post updatedPost = postRepository.save(post);
            return mapToDTO(updatedPost);
        
    }

    @Override
    @CacheEvict(value = "postById", key = "#id")
    public void deletePostById(long id) {
        // get post by id from the database
        Post post = postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        postRepository.delete(post);
    }

    @Override
    @Cacheable(value = "postsByCategory", key = "#categoryId")
    public List<PostDto> getPostsByCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        List<Post> posts = postRepository.findByCategoryId(categoryId);

        return posts.stream().map((post) -> mapToDTO(post))
                .collect(Collectors.toList());
    }

    // convert Entity into DTO
    private PostDto mapToDTO(Post post){

        PostDto postDto = new PostDto();
        postDto.setId(post.getId());
        postDto.setTitle(post.getTitle());
        postDto.setDescription(post.getDescription());
        postDto.setContent(post.getContent());
        postDto.setCategoryId(post.getCategory().getId());
        if (post.getImage() != null) {
            postDto.setImageUrl(Base64.getEncoder().encodeToString(post.getImage()));
        }
        postDto.setComments(post.getComments().stream().map(comment -> {
            CommentDto commentDto = new CommentDto();
            commentDto.setId(comment.getId());
            commentDto.setName(comment.getName());
            commentDto.setEmail(comment.getEmail());
            commentDto.setBody(comment.getBody());
            return commentDto;
        }).collect(Collectors.toSet()));

        return postDto;
    }

    // convert DTO to entity
    private Post mapToEntity(PostDto postDto) throws IOException {

        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setDescription(postDto.getDescription());
        post.setContent(postDto.getContent());

        if (postDto.getImage() != null) {
            post.setImage(postDto.getImage().getBytes());
        }
        return post;
    }
    private ImageValidationResponse validateImageWithAPI(MultipartFile image, String category) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Prepare body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("category", category);
        body.add("uploaded_file", new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        });

        // Send request to external API
        String validationApiUrl = "https://api-products-validator.cfapps.us10-001.hana.ondemand.com/validate"; // Replace with actual API URL
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    validationApiUrl, requestEntity, String.class
            );

            // Return validation response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String rawResponse = response.getBody();
                return parseRawResponse(rawResponse);
            } else {
                throw new RuntimeException("Validation API returned error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error during image validation: " + e.getMessage());
        }
    }
    private ImageValidationResponse parseRawResponse(String rawResponse) {
        ImageValidationResponse response = new ImageValidationResponse();
        rawResponse = rawResponse.replace("\\n", "\n");
        String[] lines = rawResponse.split("\n");
        for (String line : lines) {
            if (line.startsWith("\"Result:")) {
                response.setResult(line.substring(8).trim());
            } else if (line.startsWith("Confidence:")) {
                response.setConfidence(line.substring(11).trim());
            } else if (line.startsWith("Reasoning:")) {
                response.setReasoning(line.substring(10).trim());
            }
        }
        return response;
    }


}
