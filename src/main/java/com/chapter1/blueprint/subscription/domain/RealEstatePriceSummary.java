package com.chapter1.blueprint.subscription.domain;

import com.chapter1.blueprint.subscription.domain.DTO.RealEstatePriceSummaryDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "real_estate_price_summary",catalog = "subscription")
@SqlResultSetMapping(
        name = "RealEstatePriceSummaryDTOMapping",
        classes = @ConstructorResult(
                targetClass = RealEstatePriceSummaryDTO.class,
                columns = {
                        @ColumnResult(name = "region", type = String.class),
                        @ColumnResult(name = "sgg_cd_nm", type = String.class),
                        @ColumnResult(name = "umd_nm", type = String.class),
                        @ColumnResult(name = "deal_year", type = Integer.class),
                        @ColumnResult(name = "deal_month", type = Integer.class),
                        @ColumnResult(name = "deal_count", type = Integer.class),
                        @ColumnResult(name = "price_per_ar", type = Double.class)
                }
        )
)
public class RealEstatePriceSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private Long idx;

    @Column(name = "region")
    private String region;

    @Column(name = "sgg_cd_nm")
    private String sggCdNm;

    @Column(name = "umd_nm")
    private String umdNm;

    @Column(name = "deal_year")
    private Integer dealYear;

    @Column(name = "deal_month")
    private Integer dealMonth;

    @Column(name = "deal_count")
    private Integer dealCount;

    @Column(name = "price_per_ar")
    private Long pricePerAr;
}
