package com.chapter1.blueprint.subscription.domain.DTO;

import jakarta.persistence.Column;
import lombok.*;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter @Setter
public class RealEstatePriceSummaryDTO {

    private Long idx;

    private String region;

    private String sggCdNm;

    private String umdNm;

    private Integer dealYear;

    private Integer dealMonth;

    private Integer dealCount;

    private Long pricePerAr;

    public RealEstatePriceSummaryDTO(String s, String s1, String s2, int i, int i1, int i2, double v) {
        this.region = s;
        this.sggCdNm = s1;
        this.umdNm = s2;
        this.dealYear = i;
        this.dealMonth = i1;
        this.dealCount = i2;
        this.pricePerAr = (long) v;
    }
}
