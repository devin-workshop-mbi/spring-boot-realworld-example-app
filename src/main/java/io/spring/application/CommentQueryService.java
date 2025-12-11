package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.r2dbc.R2dbcCommentRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class CommentQueryService {
  private R2dbcCommentRepository commentRepository;
  private UserRepository userRepository;

  public Mono<CommentData> findById(String id, User user) {
    return commentRepository.findById(id).flatMap(comment -> toCommentData(comment, user));
  }

  public Flux<CommentData> findByArticleId(String articleId, User user) {
    return commentRepository
        .findByArticleId(articleId)
        .flatMap(comment -> toCommentData(comment, user));
  }

  public Mono<CursorPager<CommentData>> findByArticleIdWithCursor(
      String articleId, User user, CursorPageParameter<DateTime> page) {
    return commentRepository
        .findByArticleId(articleId)
        .take(page.getLimit() + 1)
        .flatMap(comment -> toCommentData(comment, user))
        .collectList()
        .map(
            comments -> {
              boolean hasExtra = comments.size() > page.getLimit();
              if (hasExtra) {
                comments = new ArrayList<>(comments.subList(0, page.getLimit()));
              }
              return new CursorPager<>(comments, page.getDirection(), hasExtra);
            });
  }

  private Mono<CommentData> toCommentData(Comment comment, User currentUser) {
    return userRepository
        .findById(comment.getUserId())
        .flatMap(
            author -> {
              Mono<Boolean> isFollowingMono =
                  currentUser != null
                      ? userRepository
                          .findRelation(currentUser.getId(), author.getId())
                          .map(rel -> true)
                          .defaultIfEmpty(false)
                      : Mono.just(false);

              return isFollowingMono.map(
                  isFollowing -> {
                    ProfileData profileData =
                        new ProfileData(
                            author.getId(),
                            author.getUsername(),
                            author.getBio(),
                            author.getImage(),
                            isFollowing);

                    CommentData commentData = new CommentData();
                    commentData.setId(comment.getId());
                    commentData.setBody(comment.getBody());
                    commentData.setArticleId(comment.getArticleId());
                    commentData.setCreatedAt(comment.getCreatedAt());
                    commentData.setUpdatedAt(comment.getCreatedAt());
                    commentData.setProfileData(profileData);

                    return commentData;
                  });
            });
  }
}
