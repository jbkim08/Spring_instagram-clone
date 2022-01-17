package cloneproject.Instagram.repository;

import cloneproject.Instagram.dto.comment.CommentDTO;
import cloneproject.Instagram.dto.comment.QCommentDTO;
import cloneproject.Instagram.dto.post.*;
import cloneproject.Instagram.entity.member.Member;
import cloneproject.Instagram.entity.member.QMember;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static cloneproject.Instagram.entity.comment.QComment.comment;
import static cloneproject.Instagram.entity.comment.QCommentLike.commentLike;
import static cloneproject.Instagram.entity.member.QFollow.follow;
import static cloneproject.Instagram.entity.post.QBookmark.bookmark;
import static cloneproject.Instagram.entity.post.QPost.post;
import static cloneproject.Instagram.entity.post.QPostImage.postImage;
import static cloneproject.Instagram.entity.post.QPostLike.postLike;
import static cloneproject.Instagram.entity.post.QPostTag.postTag;

@Slf4j
@RequiredArgsConstructor
public class PostRepositoryQuerydslImpl implements PostRepositoryQuerydsl {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PostDTO> findPostDtoPage(Member member, Pageable pageable) {
        if (member.getFollowings().isEmpty())
            return null;

        // TODO: 최신 댓글 2개 추가 -> 댓글 API 구현할 때 업데이트
        final List<PostDTO> postDTOs = queryFactory
                .select(new QPostDTO(
                        post.id,
                        post.content,
                        post.uploadDate,
                        post.member.username,
                        post.member.name,
                        post.member.image.imageUrl,
                        post.comments.size(),
                        post.postLikes.size(),
                        JPAExpressions
                                .selectFrom(bookmark)
                                .where(bookmark.post.eq(post).and(bookmark.member.eq(member)))
                                .exists(),
                        JPAExpressions
                                .selectFrom(postLike)
                                .where(postLike.post.eq(post).and(postLike.member.eq(member)))
                                .exists(),
                        JPAExpressions
                                .select(postLike.member.username)
                                .from(postLike)
                                .where(postLike.post.eq(post)
                                        .and(postLike.member
                                                .in(JPAExpressions
                                                        .select(follow.followMember)
                                                        .from(follow)
                                                        .where(follow.member.eq(member)))))
                                .limit(1)
                ))
                .from(post)
                .join(QMember.member)
                .on(post.member.username.in(
                        JPAExpressions
                                .select(follow.followMember.username)
                                .from(follow)
                                .where(follow.member.eq(member))
                ))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(post.id.desc())
                .distinct()
                .fetch();

        final List<Long> postIds = postDTOs.stream()
                .map(PostDTO::getPostId)
                .collect(Collectors.toList());

        final List<PostImageDTO> postImageDTOs = queryFactory
                .select(new QPostImageDTO(
                        postImage.post.id,
                        postImage.id,
                        postImage.image.imageUrl
                ))
                .from(postImage)
                .where(postImage.post.id.in(postIds))
                .fetch();

        final List<Long> postImageIds = postImageDTOs.stream()
                .map(PostImageDTO::getId)
                .collect(Collectors.toList());

        final List<PostTagDTO> postTagDTOs = queryFactory
                .select(new QPostTagDTO(
                        postTag.postImage.id,
                        postTag.id,
                        postTag.tag
                ))
                .from(postTag)
                .where(postTag.postImage.id.in(postImageIds))
                .fetch();

        final Map<Long, List<PostTagDTO>> postImageDTOMap = postTagDTOs.stream()
                .collect(Collectors.groupingBy(PostTagDTO::getPostImageId));
        postImageDTOs.forEach(i -> i.setPostTagDTOs(postImageDTOMap.get(i.getId())));

        final Map<Long, List<PostImageDTO>> postDTOMap = postImageDTOs.stream()
                .collect(Collectors.groupingBy(PostImageDTO::getPostId));
        postDTOs.forEach(p -> p.setPostImageDTOs(postDTOMap.get(p.getPostId())));

        return new PageImpl<>(postDTOs, pageable, postDTOs.size());
    }

    @Override
    public List<PostDTO> findRecent10PostDTOs(Long memberId) {
        final List<PostDTO> postDTOs = queryFactory
                .select(new QPostDTO(
                        post.id,
                        post.content,
                        post.uploadDate,
                        post.member.username,
                        post.member.name,
                        post.member.image.imageUrl,
                        post.comments.size(),
                        post.postLikes.size(),
                        JPAExpressions
                                .selectFrom(bookmark)
                                .where(bookmark.post.eq(post).and(bookmark.member.id.eq(memberId)))
                                .exists(),
                        JPAExpressions
                                .selectFrom(postLike)
                                .where(postLike.post.eq(post).and(postLike.member.id.eq(memberId)))
                                .exists(),
                        JPAExpressions
                                .select(postLike.member.username)
                                .from(postLike)
                                .where(postLike.post.eq(post)
                                        .and(postLike.member
                                                .in(JPAExpressions
                                                        .select(follow.followMember)
                                                        .from(follow)
                                                        .where(follow.member.id.eq(memberId)))))
                                .limit(1)
                ))
                .from(post)
                .join(post.member, QMember.member)
                .on(post.member.username.in(
                        JPAExpressions
                                .select(follow.followMember.username)
                                .from(follow)
                                .where(follow.member.id.eq(memberId))
                ))
                .limit(10)
                .orderBy(post.id.desc())
                .fetch();

        final List<Long> postIds = postDTOs.stream()
                .map(PostDTO::getPostId)
                .collect(Collectors.toList());

        final List<PostImageDTO> postImageDTOs = queryFactory
                .select(new QPostImageDTO(
                        postImage.post.id,
                        postImage.id,
                        postImage.image.imageUrl
                ))
                .from(postImage)
                .where(postImage.post.id.in(postIds))
                .fetch();

        final List<Long> postImageIds = postImageDTOs.stream()
                .map(PostImageDTO::getId)
                .collect(Collectors.toList());

        final List<PostTagDTO> postTagDTOs = queryFactory
                .select(new QPostTagDTO(
                        postTag.postImage.id,
                        postTag.id,
                        postTag.tag
                ))
                .from(postTag)
                .where(postTag.postImage.id.in(postImageIds))
                .fetch();

        final Map<Long, List<PostTagDTO>> postImageDTOMap = postTagDTOs.stream()
                .collect(Collectors.groupingBy(PostTagDTO::getPostImageId));
        postImageDTOs.forEach(i -> i.setPostTagDTOs(postImageDTOMap.get(i.getId())));

        final Map<Long, List<PostImageDTO>> postDTOMap = postImageDTOs.stream()
                .collect(Collectors.groupingBy(PostImageDTO::getPostId));
        postDTOs.forEach(p -> p.setPostImageDTOs(postDTOMap.get(p.getPostId())));

        return postDTOs;
    }

    @Override
    public Optional<PostResponse> findPostResponse(Long postId, Long memberId) {
        final Optional<PostResponse> response = Optional.ofNullable(queryFactory
                .select(new QPostResponse(
                        post.id,
                        post.content,
                        post.uploadDate,
                        post.member.username,
                        post.member.name,
                        post.member.image.imageUrl,
                        post.postLikes.size(),
                        JPAExpressions
                                .selectFrom(bookmark)
                                .where(bookmark.post.eq(post).and(bookmark.member.id.eq(memberId)))
                                .exists(),
                        JPAExpressions
                                .selectFrom(postLike)
                                .where(postLike.post.eq(post).and(postLike.member.id.eq(memberId)))
                                .exists(),
                        JPAExpressions
                                .select(postLike.member.username)
                                .from(postLike)
                                .where(postLike.post.eq(post)
                                        .and(postLike.member
                                                .in(JPAExpressions
                                                        .select(follow.followMember)
                                                        .from(follow)
                                                        .where(follow.member.id.eq(memberId)))))
                                .limit(1)
                ))
                .from(post)
                .where(post.id.eq(postId))
                .fetchOne());

        if (response.isEmpty())
            return Optional.empty();

        final List<PostImageDTO> postImageDTOs = queryFactory
                .select(new QPostImageDTO(
                        postImage.post.id,
                        postImage.id,
                        postImage.image.imageUrl
                ))
                .from(postImage)
                .where(postImage.post.id.eq(postId))
                .fetch();

        final List<Long> postImageIds = postImageDTOs.stream()
                .map(PostImageDTO::getId)
                .collect(Collectors.toList());

        final List<PostTagDTO> postTagDTOs = queryFactory
                .select(new QPostTagDTO(
                        postTag.postImage.id,
                        postTag.id,
                        postTag.tag
                ))
                .from(postTag)
                .where(postTag.postImage.id.in(postImageIds))
                .fetch();

        final Map<Long, List<PostTagDTO>> postImageDTOMap = postTagDTOs.stream()
                .collect(Collectors.groupingBy(PostTagDTO::getPostImageId));
        postImageDTOs.forEach(i -> i.setPostTagDTOs(postImageDTOMap.get(i.getId())));

        response.get().setPostImageDTOs(postImageDTOs);

        final List<CommentDTO> commentDTOs = queryFactory
                .select(new QCommentDTO(
                        comment.post.id,
                        comment.id,
                        comment.member.username,
                        comment.content,
                        comment.uploadDate,
                        comment.commentLikes.size(),
                        JPAExpressions
                                .selectFrom(commentLike)
                                .where(commentLike.comment.eq(comment).and(commentLike.member.id.eq(memberId)))
                                .exists(),
                        comment.children.size()
                ))
                .from(comment)
                .where(comment.post.id.eq(postId))
                .orderBy(comment.id.desc())
                .limit(10)
                .fetch();

        response.get().setCommentDTOs(commentDTOs);

        return response;
    }

}