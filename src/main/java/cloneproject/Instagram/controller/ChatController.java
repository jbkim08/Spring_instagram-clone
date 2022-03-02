package cloneproject.Instagram.controller;

import cloneproject.Instagram.dto.chat.*;
import cloneproject.Instagram.dto.result.ResultResponse;
import cloneproject.Instagram.service.ChatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.util.List;

import static cloneproject.Instagram.dto.result.ResultCode.*;

@Api(tags = "채팅 API")
@Validated
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @ApiOperation(value = "채팅방 생성")
    @PostMapping("/chat/rooms")
    public ResponseEntity<ResultResponse> createChatRoom(@NotEmpty(message = "상대방 username은 필수입니다.") @RequestParam List<String> usernames) {
        final ChatRoomCreateResponse response = chatService.createRoom(usernames);

        return ResponseEntity.ok(ResultResponse.of(CREATE_CHAT_ROOM_SUCCESS, response));
    }

    @ApiOperation(value = "채팅방 조회")
    @ApiImplicitParam(name = "roomId", value = "채팅방 PK", example = "1", required = true)
    @DeleteMapping("/chat/rooms/{roomId}")
    public ResponseEntity<ResultResponse> inquireChatRoom(@NotNull(message = "채팅방 PK는 필수입니다.") @PathVariable Long roomId) {
        final ChatRoomInquireResponse response = chatService.inquireRoom(roomId);

        return ResponseEntity.ok(ResultResponse.of(INQUIRE_CHAT_ROOM_SUCCESS, response));
    }

    @ApiOperation(value = "채팅방 삭제")
    @ApiImplicitParam(name = "roomId", value = "채팅방 PK", example = "1", required = true)
    @DeleteMapping("/chat/rooms/hide")
    public ResponseEntity<ResultResponse> hideChatRoom(@NotNull(message = "채팅방 PK는 필수입니다.") @RequestParam Long roomId) {
        final JoinRoomDeleteResponse response = chatService.deleteJoinRoom(roomId);

        return ResponseEntity.ok(ResultResponse.of(DELETE_JOIN_ROOM_SUCCESS, response));
    }

    @ApiOperation(value = "채팅방 목록 페이징 조회", notes = "페이지당 10개씩 조회할 수 있습니다.")
    @ApiImplicitParam(name = "page", value = "페이지", example = "1", required = true)
    @GetMapping("/chat/rooms")
    public ResponseEntity<ResultResponse> getJoinRooms(@NotNull(message = "페이지는 필수입니다.") @RequestParam Integer page) {
        final Page<JoinRoomDTO> response = chatService.getJoinRooms(page);

        return ResponseEntity.ok(ResultResponse.of(GET_JOIN_ROOMS_SUCCESS, response));
    }

    @ApiOperation(value = "채팅방 메시지 목록 페이징 조회", notes = "페이지당 10개씩 조회할 수 있습니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roomId", value = "채팅방 PK", example = "1", required = true),
            @ApiImplicitParam(name = "page", value = "페이지", example = "1", required = true)
    })
    @GetMapping("/chat/rooms/{roomId}/messages")
    public ResponseEntity<ResultResponse> getChatMessages(
            @NotNull(message = "채팅방 PK는 필수입니다.") @PathVariable Long roomId,
            @NotNull(message = "페이지는 필수입니다.") @RequestParam Integer page) {
        final Page<MessageDTO> response = chatService.getChatMessages(roomId, page);

        return ResponseEntity.ok(ResultResponse.of(GET_CHAT_MESSAGES_SUCCESS, response));
    }

    @MessageMapping("/messages/like")
    public void likeMessage(
            @NotNull(message = "좋아요할 메시지 PK는 필수입니다.") @RequestParam Long messageId,
            @NotNull(message = "회원 PK는 필수입니다.") @RequestParam Long memberId) {
        chatService.likeMessage(messageId, memberId);
    }

    @MessageMapping("/messages/unlike")
    public void unlikeMessage(
            @NotNull(message = "좋아요 취소할 메시지 PK는 필수입니다.") @RequestParam Long messageId,
            @NotNull(message = "회원 PK는 필수입니다.") @RequestParam Long memberId) {
        chatService.unlikeMessage(messageId, memberId);
    }

    @MessageMapping("/messages/delete")
    public void deleteMessage(
            @NotNull(message = "삭제할 메시지 PK는 필수입니다.") @RequestParam Long messageId,
            @NotNull(message = "회원 PK는 필수입니다.") @RequestParam Long memberId) {
        chatService.deleteMessage(messageId, memberId);
    }

    @MessageMapping("/messages/images")
    public void sendImageMessage(
            @NotNull(message = "채팅방 PK는 필수입니다.") @RequestParam Long roomId,
            @NotNull(message = "메시지를 전송하는 회원 PK는 필수입니다.") @RequestParam Long senderId,
            @RequestParam MultipartFile image) {
        chatService.sendImage(roomId, senderId, image);
    }

    @MessageMapping("/messages")
    public void sendTextMessage(@RequestBody MessageRequest request) {
        chatService.sendMessage(request);
    }

    @MessageMapping("/messages/indicate")
    public void indicate(@RequestBody IndicateRequest request) {
        chatService.indicate(request);
    }
}