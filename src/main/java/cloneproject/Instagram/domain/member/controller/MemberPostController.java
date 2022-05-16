package cloneproject.Instagram.domain.member.controller;

import java.util.List;

import javax.validation.constraints.Min;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cloneproject.Instagram.domain.feed.dto.MemberPostDTO;
import cloneproject.Instagram.domain.member.service.MemberPostService;
import cloneproject.Instagram.global.result.ResultCode;
import cloneproject.Instagram.global.result.ResultResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

@Api(tags = "멤버 게시물 API")
@RestController
@RequiredArgsConstructor
@Validated
public class MemberPostController {

	private final MemberPostService memberPostService;

	@ApiOperation(value = "멤버 게시물 15개 조회")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "Authorization", value = "있어도 되고 없어도됨", example = "Bearer AAA.BBB.CCC"),
		@ApiImplicitParam(name = "username", value = "유저네임", required = true, example = "dlwlrma")
	})
	@GetMapping("/accounts/{username}/posts/recent")
	public ResponseEntity<ResultResponse> getRecent10Posts(@PathVariable("username") String username) {
		final List<MemberPostDTO> postList = memberPostService.getRecent15PostDTOs(username);

		return ResponseEntity.ok(ResultResponse.of(ResultCode.FIND_RECENT15_MEMBER_POSTS_SUCCESS, postList));
	}

	@ApiOperation(value = "멤버 게시물 페이징 조회(무한스크롤)")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "Authorization", value = "있어도 되고 없어도됨", example = "Bearer AAA.BBB.CCC"),
		@ApiImplicitParam(name = "username", value = "유저네임", required = true, example = "dlwlrma"),
		@ApiImplicitParam(name = "page", value = "페이지", required = true, example = "1")
	})
	@GetMapping("/accounts/{username}/posts")
	public ResponseEntity<ResultResponse> getPostPage(@PathVariable("username") String username,
		@Min(1) @RequestParam int page) {
		final Page<MemberPostDTO> postPage = memberPostService.getMemberPostDTOs(username, 3, page);

		return ResponseEntity.ok(ResultResponse.of(ResultCode.FIND_MEMBER_POSTS_SUCCESS, postPage));
	}

	// ============== 저장 ================
	@ApiOperation(value = "멤버 저장한 게시물 15개 조회")
	@GetMapping("/accounts/posts/saved/recent")
	public ResponseEntity<ResultResponse> getRecent1SavedPosts() {
		final List<MemberPostDTO> postList = memberPostService.getRecent15SavedPostDTOs();

		return ResponseEntity.ok(ResultResponse.of(ResultCode.FIND_RECENT15_MEMBER_SAVED_POSTS_SUCCESS, postList));
	}

	@ApiOperation(value = "멤버 저장한 게시물 페이징 조회(무한스크롤)")
	@GetMapping("/accounts/posts/saved")
	@ApiImplicitParam(name = "page", value = "페이지", required = true, example = "1")
	public ResponseEntity<ResultResponse> getSavedPostPage(@Min(1) @RequestParam int page) {
		final Page<MemberPostDTO> postPage = memberPostService.getMemberSavedPostDTOs(3, page);

		return ResponseEntity.ok(ResultResponse.of(ResultCode.FIND_MEMBER_SAVED_POSTS_SUCCESS, postPage));
	}

	// ============== 태그 ================
	@ApiOperation(value = "멤버 태그된 게시물 15개 조회")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "Authorization", value = "있어도 되고 없어도됨", example = "Bearer AAA.BBB.CCC"),
		@ApiImplicitParam(name = "username", value = "유저네임", required = true, example = "dlwlrma")
	})
	@GetMapping("/accounts/{username}/posts/tagged/recent")
	public ResponseEntity<ResultResponse> getRecent10TaggedPosts(@PathVariable("username") String username) {
		final List<MemberPostDTO> postList = memberPostService.getRecent15TaggedPostDTOs(username);

		return ResponseEntity.ok(ResultResponse.of(ResultCode.FIND_RECENT15_MEMBER_TAGGED_POSTS_SUCCESS, postList));
	}

	@ApiOperation(value = "멤버 태그된 게시물 페이징 조회(무한스크롤)")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "Authorization", value = "있어도 되고 없어도됨", example = "Bearer AAA.BBB.CCC"),
		@ApiImplicitParam(name = "username", value = "유저네임", required = true, example = "dlwlrma"),
		@ApiImplicitParam(name = "page", value = "페이지", required = true, example = "1")
	})
	@GetMapping("/accounts/{username}/posts/tagged")
	public ResponseEntity<ResultResponse> getTaggedPostPage(@PathVariable("username") String username,
		@Min(1) @RequestParam int page) {
		final Page<MemberPostDTO> postPage = memberPostService.getMemberTaggedPostDTOs(username, 3, page);

		return ResponseEntity.ok(ResultResponse.of(ResultCode.FIND_MEMBER_TAGGED_POSTS_SUCCESS, postPage));
	}

}