package com.example.housebatch.core.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.data.util.Pair;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class NotificationDto {

    private String email;  // 회원 이메일

    private String guName;  // 법정 구 명

    private Integer count;  // 거래량

    private List<AptDto> aptDeals;  // 아파트 명, 거래 가격

    public String toMessage() {
        DecimalFormat  decimalFormat = new DecimalFormat();

        return String.format(
                "%s 아파트 실거래가 알림\n" +
                "총 %d개 거래가 발생했습니다.\n", guName, count)
                +
                aptDeals.stream()
                        .map(deal -> String.format("- %s : %s원\n", deal.getName(), decimalFormat.format(deal.getPrice())))
                        .collect(Collectors.joining());
    }

}
