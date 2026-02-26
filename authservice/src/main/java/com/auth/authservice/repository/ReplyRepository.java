package com.auth.authservice.repository;

import com.auth.authservice.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByPostIdOrderByCreatedAtAsc(Long postId);
    List<Reply> findByParentReplyIdOrderByCreatedAtAsc(Long parentReplyId);
    Long countByPostId(Long postId);
    void deleteByPostId(Long postId);
}
