package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import graphql.relay.DefaultConnectionCursor;
import graphql.relay.DefaultPageInfo;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.core.user.User;
import io.spring.graphql.DgsConstants.ARTICLE;
import io.spring.graphql.DgsConstants.COMMENTPAYLOAD;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentEdge;
import io.spring.graphql.types.CommentsConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.format.ISODateTimeFormat;
import reactor.core.publisher.Mono;

@DgsComponent
@AllArgsConstructor
public class CommentDatafetcher {
  private CommentQueryService commentQueryService;

  @DgsData(parentType = COMMENTPAYLOAD.TYPE_NAME, field = COMMENTPAYLOAD.Comment)
  public DataFetcherResult<Comment> getComment(DgsDataFetchingEnvironment dfe) {
    CommentData comment = dfe.getLocalContext();
    Comment commentResult = buildCommentResult(comment);
    return DataFetcherResult.<Comment>newResult()
        .data(commentResult)
        .localContext(
            new HashMap<String, Object>() {
              {
                put(comment.getId(), comment);
              }
            })
        .build();
  }

  @DgsData(parentType = ARTICLE.TYPE_NAME, field = ARTICLE.Comments)
  public CompletableFuture<DataFetcherResult<CommentsConnection>> articleComments(
      @InputArgument("first") Integer first,
      @InputArgument("after") String after,
      @InputArgument("last") Integer last,
      @InputArgument("before") String before,
      DgsDataFetchingEnvironment dfe) {

    if (first == null && last == null) {
      throw new IllegalArgumentException("first and last must have one present");
    }

    User current = SecurityUtil.getCurrentUser().orElse(null);
    Article article = dfe.getSource();
    Map<String, ArticleData> map = dfe.getLocalContext();
    ArticleData articleData = map.get(article.getSlug());

    Mono<CursorPager<CommentData>> commentsMono;
    if (first != null) {
      commentsMono =
          commentQueryService.findByArticleIdWithCursor(
              articleData.getId(),
              current,
              new CursorPageParameter<>(DateTimeCursor.parse(after), first, Direction.NEXT));
    } else {
      commentsMono =
          commentQueryService.findByArticleIdWithCursor(
              articleData.getId(),
              current,
              new CursorPageParameter<>(DateTimeCursor.parse(before), last, Direction.PREV));
    }

    return commentsMono
        .map(
            comments -> {
              graphql.relay.PageInfo pageInfo = buildCommentPageInfo(comments);
              CommentsConnection result =
                  CommentsConnection.newBuilder()
                      .pageInfo(pageInfo)
                      .edges(
                          comments.getData().stream()
                              .map(
                                  a ->
                                      CommentEdge.newBuilder()
                                          .cursor(a.getCursor().toString())
                                          .node(buildCommentResult(a))
                                          .build())
                              .collect(Collectors.toList()))
                      .build();
              return DataFetcherResult.<CommentsConnection>newResult()
                  .data(result)
                  .localContext(
                      comments.getData().stream()
                          .collect(Collectors.toMap(CommentData::getId, c -> c)))
                  .build();
            })
        .toFuture();
  }

  private DefaultPageInfo buildCommentPageInfo(CursorPager<CommentData> comments) {
    return new DefaultPageInfo(
        comments.getStartCursor() == null
            ? null
            : new DefaultConnectionCursor(comments.getStartCursor().toString()),
        comments.getEndCursor() == null
            ? null
            : new DefaultConnectionCursor(comments.getEndCursor().toString()),
        comments.hasPrevious(),
        comments.hasNext());
  }

  private Comment buildCommentResult(CommentData comment) {
    return Comment.newBuilder()
        .id(comment.getId())
        .body(comment.getBody())
        .updatedAt(ISODateTimeFormat.dateTime().withZoneUTC().print(comment.getCreatedAt()))
        .createdAt(ISODateTimeFormat.dateTime().withZoneUTC().print(comment.getCreatedAt()))
        .build();
  }
}
