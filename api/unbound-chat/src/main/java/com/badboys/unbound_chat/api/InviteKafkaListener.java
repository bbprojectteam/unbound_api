package com.badboys.unbound_chat.api;

import com.badboys.unbound_chat.api.model.ChatMessage;
import com.badboys.unbound_chat.api.model.RequestInviteDto;
import com.badboys.unbound_chat.api.service.ChatService;
import com.badboys.unbound_chat.api.service.FcmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InviteKafkaListener {

    private final FcmService fcmService;

    @Autowired
    public InviteKafkaListener(FcmService fcmService) {
        this.fcmService = fcmService;
    }

    @KafkaListener(
            topics = "invite-request-topic",
            groupId = "${spring.kafka.consumer.chat.group-id}",
            containerFactory = "inviteKafkaListenerContainerFactory"
    )
    public void inviteListen(RequestInviteDto requestInviteDto) {
        log.info("Kafka에서 초대 메시지 수신: {}", requestInviteDto);
        try {
            fcmService.sendInvite(requestInviteDto);
        } catch (Exception e) {
            log.error("초대 메시지 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
