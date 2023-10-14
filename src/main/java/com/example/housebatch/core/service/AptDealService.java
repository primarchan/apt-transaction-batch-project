package com.example.housebatch.core.service;

import com.example.housebatch.core.dto.AptDealDto;
import com.example.housebatch.core.dto.AptDto;
import com.example.housebatch.core.entity.Apt;
import com.example.housebatch.core.entity.AptDeal;
import com.example.housebatch.core.repository.AptDealRepository;
import com.example.housebatch.core.repository.AptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AptDealDto 에 있는 값을 Apt, AptDeal Entity 로 저장한다.
 */
@Service
@RequiredArgsConstructor
public class AptDealService {

    private final AptRepository aptRepository;

    private final AptDealRepository aptDealRepository;

    @Transactional
    public void upsert(AptDealDto dto) {
        Apt apt = getAptOrNew(dto);
        saveAptDeal(dto, apt);
    }

    private Apt getAptOrNew(AptDealDto dto) {
        Apt apt = aptRepository.findAptByAptNameAndJibun(dto.getAptName(), dto.getJibun())
                .orElseGet(() -> Apt.from(dto));

        return aptRepository.save(apt);
    }

    private void saveAptDeal(AptDealDto dto, Apt apt) {
        AptDeal aptDeal = aptDealRepository.findAptDealByAptAndExclusiveAreaAndDealDateAndDealAmountAndFloor(
                apt, dto.getExclusiveArea(), dto.getDealDate(), dto.getDealAmount(), dto.getFloor()
        ).orElseGet(() -> AptDeal.of(dto, apt));
        aptDeal.setDealCanceled(dto.isDealCanceled());
        aptDeal.setDealCanceledDate(dto.getDealCanceledDate());
        aptDealRepository.save(aptDeal);
    }

    public List<AptDto> findByGuLawdCdAndDealDate(String guLawdCd, LocalDate dealDate) {
        return aptDealRepository.findByDealCanceledIsFalseAndDealDateEquals(dealDate)
                .stream()
                .filter(aptDeal -> aptDeal.getApt().getGuLawdCd().equals(guLawdCd))
                .map(aptDeal -> new AptDto(aptDeal.getApt().getAptName(), aptDeal.getDealAmount()))
                .collect(Collectors.toList());
    }

}
