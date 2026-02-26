package com.auth.authservice.service;

import com.auth.authservice.dto.PostDto;
import com.auth.authservice.entity.*;
import com.auth.authservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

    @Transactional
    public PostDto.ReplyResponse createReply(Long postId, PostDto.CreateReplyRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Reply parentReply = null;
        if (request.getParentReplyId() != null) {
            parentReply = replyRepository.findById(request.getParentReplyId())
                    .orElseThrow(() -> new RuntimeException("Parent reply not found"));
        }

        Reply reply = Reply.builder()
                .post(post)
                .user(user)
                .parentReply(parentReply)
                .content(request.getContent())
                .build();

        reply = replyRepository.save(reply);
        return mapToReplyResponse(reply, user.getId());
    }

    public List<PostDto.ReplyResponse> getRepliesByPost(Long postId, String email) {
        Long userId = email != null ? userRepository.findByEmail(email).map(User::getId).orElse(null) : null;
        return replyRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(reply -> mapToReplyResponse(reply, userId))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReply(Long replyId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("Reply not found"));

        if (!reply.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        likeRepository.deleteByReplyId(replyId);
        replyRepository.delete(reply);
    }

    @Transactional
    public void toggleLike(Long replyId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("Reply not found"));

        if (likeRepository.existsByUserIdAndReplyId(user.getId(), replyId)) {
            likeRepository.deleteByUserIdAndReplyId(user.getId(), replyId);
            reply.setLikes(reply.getLikes() - 1);
        } else {
            Like like = Like.builder()
                    .user(user)
                    .reply(reply)
                    .build();
            likeRepository.save(like);
            reply.setLikes(reply.getLikes() + 1);
        }
        replyRepository.save(reply);
    }

    private PostDto.ReplyResponse mapToReplyResponse(Reply reply, Long currentUserId) {
        Boolean isLiked = currentUserId != null && likeRepository.existsByUserIdAndReplyId(currentUserId, reply.getId());

        return PostDto.ReplyResponse.builder()
                .id(reply.getId())
                .content(reply.getContent())
                .likes(reply.getLikes())
                .user(mapToUserInfo(reply.getUser()))
                .isLiked(isLiked)
                .parentReplyId(reply.getParentReply() != null ? reply.getParentReply().getId() : null)
                .createdAt(reply.getCreatedAt())
                .updatedAt(reply.getUpdatedAt())
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
