package com.example.housebatch.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * TODO : 실제 이메일 전송 로직 구현 필요
 */
@Slf4j
@Service
public class SendServiceImpl implements SendService {

    @Override
    public void send(String email, String message) {
        System.out.println("email : " + email + "\n message : " + message);
    }

}
