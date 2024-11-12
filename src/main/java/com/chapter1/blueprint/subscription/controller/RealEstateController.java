package com.chapter1.blueprint.subscription.controller;

import com.chapter1.blueprint.exception.dto.SuccessResponse;
import com.chapter1.blueprint.subscription.domain.DTO.RealEstateDTO;
import com.chapter1.blueprint.subscription.domain.DTO.RealEstatePriceSummaryDTO;
import com.chapter1.blueprint.subscription.service.RealEstateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/realestate")
public class RealEstateController {
    private final RealEstateService realEstateService;

    @GetMapping(value = "/get")
    public ResponseEntity<?> getRealEstatePrice() {
        String result = realEstateService.getRealEstatePrice();
        return ResponseEntity.ok(new SuccessResponse(result));
    }

    //@GetMapping(value="/summary")
    //public ResponseEntity<?> getRealEstateSummary(@RequestParam String region,@RequestParam String sggCdNm, @RequestParam String umdNm) {
    //    List<RealEstatePriceSummaryDTO> result = realEstateService.getRealEstateSummary(region, sggCdNm, umdNm);
    //    return ResponseEntity.ok(new SuccessResponse(result));
    //}
    @GetMapping("/summary/{region}/{sggCdNm}/{umdNm}")
    public ResponseEntity<?> getRealEstateSummary(
            @PathVariable String region,
            @PathVariable String sggCdNm,
            @PathVariable String umdNm
    ) {
        try {
            List<RealEstatePriceSummaryDTO> data = realEstateService.getRealEstateSummary(region, sggCdNm, umdNm);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/locations")
    public ResponseEntity<Map<String, List<String>>> getRegions() {
        try {
            List<String> regions = realEstateService.getAllRegions();
            return ResponseEntity.ok(Map.of("regions", regions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/sgg/{region}")
    public ResponseEntity<List<String>> getSggList(@PathVariable String region) {
        try {
            List<String> sggList = realEstateService.getSggList(region);
            return ResponseEntity.ok(sggList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/umd/{region}/{sggCdNm}")
    public ResponseEntity<List<String>> getUmdList(
            @PathVariable String region,
            @PathVariable String sggCdNm
    ) {
        try {
            List<String> umdList = realEstateService.getUmdList(region, sggCdNm);
            return ResponseEntity.ok(umdList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
