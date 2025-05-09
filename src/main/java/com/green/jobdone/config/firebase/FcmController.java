package com.green.jobdone.config.firebase;

import com.green.jobdone.config.firebase.model.FcmTokenReq;
import com.green.jobdone.config.jwt.JwtUser;
import com.green.jobdone.config.jwt.TokenProvider;
import com.green.jobdone.config.security.AuthenticationFacade;
import com.green.jobdone.entity.User;
import com.green.jobdone.user.UserRepository;
import io.jsonwebtoken.Jwt;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("savePushToken")
@RequiredArgsConstructor
@Slf4j
public class FcmController {
    private final AuthenticationFacade authenticationFacade;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;



    @PostMapping
    public String saveFcmToken(@RequestBody FcmTokenReq token, HttpServletRequest request) {
        log.info("Token 토큰 저장하는곳 잘 들어왔냐??: " + token);
        String accessToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
        }
//        log.info(authenticationFacade.getToken());
//        Long userId = authenticationFacade.getSignedUserId();
        log.info("쿠키는 잘됨?: ", accessToken);
        JwtUser jwtUser = tokenProvider.getJwtUserFromToken(accessToken);
        Long userId = jwtUser.getSignedUserId();
        log.info("userId: " + userId);
        User user = userRepository.findById(userId).orElse(null);
        log.info("user: " + user);
        user.setFCMToken(token.getToken());
        userRepository.save(user);
        log.info("저장 잘됬음?? 어케됨??");
        return "저장 완료";
    }
}
