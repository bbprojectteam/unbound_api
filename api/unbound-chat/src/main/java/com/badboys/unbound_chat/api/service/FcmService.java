package com.badboys.unbound_chat.api.service;

import com.badboys.unbound_chat.api.repository.FcmTokenRepository;
import com.google.firebase.messaging.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Slf4j
@Transactional
@Service
public class FcmService {

    private final FirebaseMessaging firebaseMessaging;
    private final FcmTokenRepository fcmTokenRepository;

    @Autowired
    public FcmService(FirebaseMessaging firebaseMessaging, FcmTokenRepository fcmTokenRepository) {
        this.firebaseMessaging = firebaseMessaging;
        this.fcmTokenRepository = fcmTokenRepository;
    }

    /**
     * 여러 유저에게 한 번에 FCM 푸시 알림 전송
     */
    public void sendNotifications(Set<Long> userIds, String title, String body, Map<String, String> data) {

        Set<String> allTokens = fcmTokenRepository.findTokensByUserIds(userIds);

        // 조회된 모든 FCM 토큰에 대해 알림 전송
        if (!allTokens.isEmpty()) {
            for (String token : allTokens) {
                sendFcmMessage(token, title, body, data);
            }
        } else {
            log.warn("FCM 토큰 없음 - UserIDs: {}", userIds);
        }
    }

    /**
     * 개별 FCM 푸시 메시지 전송
     */
    private void sendFcmMessage(String token, String title, String body, Map<String, String> data) {
        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            // 추가 데이터 (예: roomId 등) 포함 가능
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            firebaseMessaging.send(messageBuilder.build());
            log.info("푸시 알림 성공 - Token: {}", token);

        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                log.warn("앱 삭제됨 - FCM 토큰 제거: {}", token);
                fcmTokenRepository.deleteByToken(token);
            } else {
                log.error("푸시 알림 실패 - Token: {}, Error: {}", token, e.getMessagingErrorCode());
            }
        }
    }
}
