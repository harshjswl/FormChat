package com.auth.authservice.service;

import com.auth.authservice.dto.PostDto;
import com.auth.authservice.entity.*;
import com.auth.authservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final ReplyRepository replyRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    @Transactional
    public PostDto.PostResponse createPost(PostDto.CreatePostRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .tags(request.getTags())
                .user(user)
                .build();

        post = postRepository.save(post);
        return mapToPostResponse(post, user.getId());
    }

    @Transactional
    public PostDto.PostResponse updatePost(Long postId, PostDto.UpdatePostRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(request.getCategory());
        post.setTags(request.getTags());

        post = postRepository.save(post);
        return mapToPostResponse(post, user.getId());
    }

    @Transactional
    public void deletePost(Long postId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        likeRepository.deleteByPostId(postId);
        replyRepository.deleteByPostId(postId);
        postRepository.delete(post);
    }

    @Transactional
    public PostDto.PostResponse getPost(Long postId, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        post.setViews(post.getViews() + 1);
        postRepository.save(post);

        Long userId = email != null ? userRepository.findByEmail(email).map(User::getId).orElse(null) : null;
        return mapToPostResponse(post, userId);
    }

    public Page<PostDto.PostResponse> getAllPosts(int page, int size, String email) {
        Pageable pageable = PageRequest.of(page, size);
        Long userId = email != null ? userRepository.findByEmail(email).map(User::getId).orElse(null) : null;
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(post -> mapToPostResponse(post, userId));
    }

    public Page<PostDto.PostResponse> searchPosts(String query, int page, int size, String email) {
        Pageable pageable = PageRequest.of(page, size);
        Long userId = email != null ? userRepository.findByEmail(email).map(User::getId).orElse(null) : null;
        return postRepository.searchPosts(query, pageable)
                .map(post -> mapToPostResponse(post, userId));
    }

    @Transactional
    public void toggleLike(Long postId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (likeRepository.existsByUserIdAndPostId(user.getId(), postId)) {
            likeRepository.deleteByUserIdAndPostId(user.getId(), postId);
            post.setLikes(post.getLikes() - 1);
        } else {
            Like like = Like.builder()
                    .user(user)
                    .post(post)
                    .build();
            likeRepository.save(like);
            post.setLikes(post.getLikes() + 1);
        }
        postRepository.save(post);
    }

    private PostDto.PostResponse mapToPostResponse(Post post, Long currentUserId) {
        Long replyCount = replyRepository.countByPostId(post.getId());
        Boolean isLiked = currentUserId != null && likeRepository.existsByUserIdAndPostId(currentUserId, post.getId());

        return PostDto.PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .tags(post.getTags())
                .views(post.getViews())
                .likes(post.getLikes())
                .replyCount(replyCount)
                .user(mapToUserInfo(post.getUser()))
                .isLiked(isLiked)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private PostDto.UserInfo mapToUserInfo(User user) {
        return PostDto.UserInfo.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .reputation(user.getReputation())
                .build();
    }
}
