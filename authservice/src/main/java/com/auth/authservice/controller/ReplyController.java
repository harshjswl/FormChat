package com.auth.authservice.controller;

import com.auth.authservice.dto.PostDto;
import com.auth.authservice.security.JwtUtil;
import com.auth.authservice.service.ReplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts/{postId}/replies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReplyController {

    private final ReplyService replyService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<PostDto.ReplyResponse> createReply(
            @PathVariable Long postId,
            @Valid @RequestBody PostDto.CreateReplyRequest request,
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractEmail(token.substring(7));
        return ResponseEntity.ok(replyService.createReply(postId, request, email));
    }

    @GetMapping
    public ResponseEntity<List<PostDto.ReplyResponse>> getReplies(
            @PathVariable Long postId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        String email = token != null ? jwtUtil.extractEmail(token.substring(7)) : null;
        return ResponseEntity.ok(replyService.getRepliesByPost(postId, email));
    }

    @DeleteMapping("/{replyId}")
    public ResponseEntity<Void> deleteReply(
            @PathVariable Long postId,
            @PathVariable Long replyId,
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractEmail(token.substring(7));
        replyService.deleteReply(replyId, email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{replyId}/like")
    public ResponseEntity<Void> toggleLike(
            @PathVariable Long postId,
            @PathVariable Long replyId,
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractEmail(token.substring(7));
        replyService.toggleLike(replyId, email);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
