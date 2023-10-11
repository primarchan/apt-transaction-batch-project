package com.example.housebatch.core.service;

import com.example.housebatch.core.entity.Lawd;
import com.example.housebatch.core.repository.LawdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LawdService {

    private final LawdRepository lawdRepository;

    @Transactional
    public void upsert(Lawd lawd) {
        /**
         * 데이터가 존재하지 않으면 Insert, 존재하면 Update
         */
        Lawd saved = lawdRepository.findByLawdCd(lawd.getLawdCd())
                .orElseGet(Lawd::new);
        saved.setLawdCd(lawd.getLawdCd());
        saved.setLawdDong(lawd.getLawdDong());
        saved.setExist(lawd.getExist());

        lawdRepository.save(saved);
    }

}
