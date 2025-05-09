package com.green.jobdone.config.webSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.jobdone.business.BusinessRepository;
import com.green.jobdone.common.exception.ChatErrorCode;
import com.green.jobdone.common.exception.CustomException;
import com.green.jobdone.config.firebase.FcmService;
import com.green.jobdone.config.jwt.JwtUser;
import com.green.jobdone.config.jwt.TokenProvider;
import com.green.jobdone.config.security.AuthenticationFacade;
import com.green.jobdone.entity.Room;
import com.green.jobdone.room.RoomRepository;
import com.green.jobdone.room.chat.ChatService;
import com.green.jobdone.room.chat.model.ChatPostReq;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final AuthenticationFacade authenticationFacade;
    private final BusinessRepository businessRepository;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final RoomRepository roomRepository;
    private final ChatService chatService;
    private final Set<WebSocketSession> sessions = new HashSet<>();
    private final Map<WebSocketSession, Long> sessionRoomMap = new HashMap<>();
    private final Map<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final TokenProvider tokenProvider;
    private final FcmService fcmService;
    public ChatWebSocketHandler(ChatService chatService, AuthenticationFacade authenticationFacade, RoomRepository roomRepository, TokenProvider tokenProvider, BusinessRepository businessRepository, FcmService fcmService) {
        this.chatService = chatService;
        this.authenticationFacade = authenticationFacade;
        this.roomRepository = roomRepository;
        this.tokenProvider = tokenProvider;
        this.businessRepository = businessRepository;
        this.fcmService = fcmService;
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        long roomId = getRoomIdByUri(session.getUri().toString());
        log.info("현재 방 {}에 연결된 세션 목록: {}", roomId, roomSessions.get(roomId));
        Map<String,Object> attributes = session.getAttributes();
        String token = (String) attributes.get("JWTToken");
        Long signedUserId = null;
        if(token!=null){
            JwtUser jwtUser = tokenProvider.getJwtUserFromToken(token);
            signedUserId = jwtUser.getSignedUserId();
        } else {
            throw new CustomException(ChatErrorCode.FAIL_TO_CONNECT);
        }
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new CustomException(ChatErrorCode.MISSING_ROOM));
        Long userId = room.getUser().getUserId();
        Long busiUserId = businessRepository.findUserIdByBusinessId(room.getBusiness().getBusinessId());
        // 아이디가 둘중 하나라도 맞다면 false>둘다 해당되지 않으면 true> a!=b and c!=b
//        if(!userId.equals(signedUserId) && !busiUserId.equals(signedUserId)){
//            throw new CustomException(ChatErrorCode.FAIL_TO_CONNECT);
//        }
        Integer flag = null;
        if (userId.equals(signedUserId)) {
            flag = 1;
        } else if (busiUserId.equals(signedUserId)) {
            flag = 0;
        } else {
            throw new CustomException(ChatErrorCode.FAIL_TO_CONNECT);
        }
        session.getAttributes().put("flag", flag);
        session.getAttributes().put("token", token);


//        if (sessionRoomMap.containsKey(session)) {
//            long currentRoomId = sessionRoomMap.get(session);
//            if (currentRoomId != roomId) {
//                log.info("세션 {}는 이미 방 {}에 연결되어 있음, 방 {}에 연결할 수 없습니다.", session.getId(), currentRoomId, roomId);
//                return; // 다른 방으로 재연결을 차단
//            }
//        }

//        boolean isAlreadyConnected = false;
//        for (Map.Entry<Long, Set<WebSocketSession>> entry : roomSessions.entrySet()) {
//            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
//                log.info("세션 {}는 이미 다른 방에 연결되어 있음. 방: {}", session.getId(), entry.getKey());
//                log.info("모든방 {} ",roomSessions);
//                isAlreadyConnected = true;
//                break; // for문 바로 탈출
//            }
//        }
//        if (isAlreadyConnected) {
//            return; // 위에서 방이 있으면 true로 변했을거니 중복방 생성 x 될듯?
//        }


        session.setBinaryMessageSizeLimit(10*1024*1024);
        Set<WebSocketSession> sessionSet = roomSessions.computeIfAbsent(roomId, k -> new HashSet<>());
        if (!sessionSet.contains(session)) {
            sessionSet.add(session);  // 중복 세션을 방지
            log.info("세션 {}가 방 {}에 추가됨", session.getId(), roomId);
        }

        log.info("Room ID: " + roomId);

    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        long roomId2 = getRoomIdByUri(session.getUri().toString());
        // 해당 roomId에 대한 세션 목록에서 제거

        roomSessions.forEach((roomId, sessionSet) -> {
            if (sessionSet != null) {
                sessionSet.remove(session);
                log.info("남은 세션: {}", roomSessions);

                // 세션 목록이 비면 방 삭제
                if (sessionSet.isEmpty()) {
                    roomSessions.remove(roomId);
                    log.info("방 {}가 비어 삭제됨", roomId);
                }
            }
        });

        log.info("모든 방의 현재 세션 상태: {}", roomSessions);
    }
//        Set<WebSocketSession> sessionSet = roomSessions.get(roomId);
//        if (sessionSet != null) {
//            boolean aa = sessionSet.remove(session);
//            log.info("삭제되는거 맞음?? : {}, {}",aa, roomId);
//            if (sessionSet.isEmpty()) {
//                roomSessions.remove(roomId); // 세션이 모두 끊어지면 방을 삭제할 수 있음
//            }
//        }
//        log.info("현재 방 {}에 연결된 세션 목록: {}", roomId, roomSessions.get(roomId));
//    }

    private long getRoomIdByUri(String uri){
        String[] uriParts = uri.split("/");
        return Long.parseLong(uriParts[uriParts.length - 1]);
    }

//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) {
//        sessions.add(session);
//        log.info("WebSocket connection established: " + session.getId());
//    } 공통방

//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
//        logger.info("메세지전용: " + message.getPayload());
//        long roomId = Long.parseLong(message.getPayload());
//        for (WebSocketSession webSocketSession : sessions) {
//            if (webSocketSession.isOpen()) {
//                try {
//                    webSocketSession.sendMessage(new TextMessage("새 메세지: " + message.getPayload()));
//                } catch (IOException e) {
//                    logger.error("웹소켓 메시지 전송 중 오류 발생", e);
//                }
//            }
//        }
//    }
@Override
protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("메세지전용: " + message.getPayload());
        // JSON 형식으로 파싱
        String jsonString = message.getPayload();
        log.info("jsonString: " + jsonString);
        ChatPostReq req = new ChatPostReq();
        try {
            log.info("try문 잘 들어오나 확인용");
            JsonNode jsonNode = objectMapper.readTree(jsonString);

            long roomId = jsonNode.get("roomId").asLong();
            log.info("roomId: " + roomId);
            int flag = jsonNode.get("flag").asInt();
            log.info("flag: " + flag);
            String contents = jsonNode.has("message") ? jsonNode.get("message").asText().trim() : "";
            // 필요한 데이터 처리
            log.info("message: " + contents);

            req.setRoomId(roomId);
            req.setFlag(flag);
            req.setContents(contents);

            String uri = session.getUri().toString();
            String[] uriParts = uri.split("/");
            long roomId2 = Long.parseLong(uriParts[uriParts.length - 1]);

            if(roomId != roomId2) {
                throw new CustomException(ChatErrorCode.FAIL_TO_CONNECT);
            }

            // 해당 roomId의 모든 세션에 메시지 전송
            Set<WebSocketSession> sessionSet = roomSessions.get(roomId);
            log.info("sessionSet: " + sessionSet);
            if (sessionSet != null) {
                log.info("if문 정상 진입1");
                for (WebSocketSession webSocketSession : sessionSet) {
                    log.info("채팅 보내는 for문 정상 진입");
                    if (webSocketSession.isOpen()) {
                    log.info("채팅 보내는 if문 정상 진입");
//                        webSocketSession.sendMessage(new TextMessage("새 메시지: " + message.getPayload()));
                    }
                }
                        log.info("req확인: {}", req);
                        chatService.insChat(null,req);
            }
        } catch (Exception e) {
            throw new CustomException(ChatErrorCode.FAIL_TO_CONNECT);
        }
}



    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        log.info("사진 보내는곳으로 정상접속");

        try {
            byte[] payload = message.getPayload().array();
            String jsonString = new String(payload, StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            if (!jsonNode.has("roomId") || !jsonNode.has("flag")) {
                throw new RuntimeException("JSON 데이터에 roomId 또는 flag가 없습니다.");
            }
            long roomId = jsonNode.get("roomId").asLong();
//            int flag = jsonNode.get("flag").asInt();
            String flag1 = jsonNode.get("flag").asText();
            int flag = Integer.parseInt(flag1);
//            String token = jsonNode.has("token") ? jsonNode.get("token").asText().trim() : null; 토큰 인증은 첨 접속시!
            String textMessage = jsonNode.has("message") ? jsonNode.get("message").asText().trim() : "";
            log.info("textMessage 확인: {}",textMessage);

            // 파일 처리
            JsonNode fileNode = jsonNode.get("file"); // 배열이 아닌 하나의 객체
            log.info("fileNode: {}", fileNode);
            MultipartFile pic = null;

            if (fileNode != null) {
                String base64File = fileNode.get("data").asText().trim(); // Base64 인코딩된 파일 데이터
                log.info("base64File: " + base64File);
                // Base64 디코딩 전에 파일이 비어있지 않은지 체크
                byte[] fileData = Base64.getDecoder().decode(base64File);
                log.info("파일 디코딩하러 들어왔나 확인용");
                pic = convertByteArrayToMultipartFile(fileData, fileNode.get("name").asText());

            }
            log.info("기존에 걸렸던곳 바로앞");
            log.info("textMessage: " + textMessage);
            log.info("pic: " + pic);

            if (textMessage.isEmpty() && pic==null) {
                throw new RuntimeException("메시지와 파일이 모두 비어 있습니다.");
            }

            log.info("if문 잘 넘어갔나?");

            ChatPostReq chatPostReq = new ChatPostReq();
            chatPostReq.setRoomId(roomId);
            chatPostReq.setContents(textMessage.isEmpty() ? null : textMessage);
            chatPostReq.setFlag(flag);
            log.info("roomId {}, flag {}, message: {}", roomId, flag, textMessage);
//            log.info("토큰: " + token);
            String jsonData = chatService.insChat(pic, chatPostReq);
            // jsondata가 아니라 사진 url으로 내용 변경
            log.info("jsonData 어케 나옴: "+jsonData);
            // string 으로넘어옴
//            Map<String ,String> map = new HashMap<>();
//            if(jsonData != null) {
//                map.put("pic",jsonData);
//            }
//            map.put("message",textMessage);
//            ObjectMapper mapper = new ObjectMapper();
//            String json = objectMapper.writeValueAsString(map);
//            log.info("제발 찍혀주세요 {} ",json);

            Set<WebSocketSession> sessionSet = roomSessions.get(roomId);
            if (sessionSet != null) {
                log.info("세션점검 if문 정상 진입");
                for (WebSocketSession webSocketSession : sessionSet) {
                        log.info("채팅 보내는 for문 정상 진입");
                    if (webSocketSession.isOpen()) {
                        log.info("채팅 보내는 if문 정상 진입");
//                        webSocketSession.sendMessage(new TextMessage(jsonData));
//                        webSocketSession.sendMessage(new TextMessage(textMessage));
                            webSocketSession.sendMessage(new TextMessage(jsonData));
                    }
                }
            }


//            session.sendMessage(new TextMessage("파일 업로드 및 메시지 저장 완료"));

        } catch (Exception e) {
            log.error("파일 업로드 및 메시지 처리 중 오류 발생", e);
//            try {
//                session.sendMessage(new TextMessage("파일 업로드 및 메시지 처리 실패: " + e.getMessage()));
//            } catch (IOException ex) {
//                log.error("웹소켓 응답 전송 중 오류 발생", ex);
//            } 설마?
        }
    }

    private MultipartFile convertByteArrayToMultipartFile(byte[] payload, String fileName) {
        return new CustomMultipartFile(payload, fileName, "application/octet-stream");
    }

}

