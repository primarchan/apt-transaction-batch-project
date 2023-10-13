package com.example.housebatch.core.entity;

import com.example.housebatch.core.dto.AptDealDto;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table(name = "apt_deal")
@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
public class AptDeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aptDealId;

    @ManyToOne
    @JoinColumn(name = "apt_id")
    private Apt apt;

    @Column(nullable = false)
    private Double exclusiveArea;

    @Column(nullable = false)
    private LocalDate dealDate;

    @Column(nullable = false)
    private Long dealAmount;

    @Column(nullable = false)
    private Integer floor;

    @Column(nullable = false)
    private boolean dealCanceled;

    @Column
    private LocalDate dealCanceledDate;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static AptDeal from(AptDealDto dto) {
        AptDeal deal = new AptDeal();
        deal.setExclusiveArea(dto.getExclusiveArea());
        deal.setDealDate(dto.getDealDate());
        deal.setDealAmount(dto.getDealAmount());
        deal.setFloor(dto.getFloor());
        deal.setDealCanceled(dto.isDealCanceled());
        deal.setDealCanceledDate(dto.getDealCanceledDate());

        return deal;
    }

}
