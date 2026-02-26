package com.auth.authservice.controller;

import com.auth.authservice.dto.PostDto;
import com.auth.authservice.security.JwtUtil;
import com.auth.authservice.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class PostController {

    private final PostService postService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<PostDto.PostResponse> createPost(
            @Valid @RequestBody PostDto.CreatePostRequest request,
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractEmail(token.substring(7));
        return ResponseEntity.ok(postService.createPost(request, email));
    }

    @GetMapping
    public ResponseEntity<Page<PostDto.PostResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String token) {
        String email = token != null ? jwtUtil.extractEmail(token.substring(7)) : null;
        return ResponseEntity.ok(postService.getAllPosts(page, size, email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto.PostResponse> getPost(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        String email = token != null ? jwtUtil.extractEmail(token.substring(7)) : null;
        return ResponseEntity.ok(postService.getPost(id, email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto.PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostDto.UpdatePostRequest request,
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractEmail(token.substring(7));
        return ResponseEntity.ok(postService.updatePost(id, request, email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractEmail(token.substring(7));
        postService.deletePost(id, email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> toggleLike(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractEmail(token.substring(7));
        postService.toggleLike(id, email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PostDto.PostResponse>> searchPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String token) {
        String email = token != null ? jwtUtil.extractEmail(token.substring(7)) : null;
        return ResponseEntity.ok(postService.searchPosts(query, page, size, email));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
