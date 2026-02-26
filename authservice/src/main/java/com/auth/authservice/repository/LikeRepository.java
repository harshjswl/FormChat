package com.auth.authservice.repository;

import com.auth.authservice.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);
    Optional<Like> findByUserIdAndReplyId(Long userId, Long replyId);
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    boolean existsByUserIdAndReplyId(Long userId, Long replyId);
    void deleteByUserIdAndPostId(Long userId, Long postId);
    void deleteByUserIdAndReplyId(Long userId, Long replyId);
    void deleteByPostId(Long postId);
    void deleteByReplyId(Long replyId);
}
