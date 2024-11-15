package com.chapter1.blueprint.finance.controller;

import com.chapter1.blueprint.exception.dto.SuccessResponse;
import com.chapter1.blueprint.finance.domain.LoanList;
import com.chapter1.blueprint.finance.domain.SavingsList;
import com.chapter1.blueprint.finance.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/finance")
@Tag(name = "Finance", description = "금융 상품 관리 API")
public class FinanceController {
    private final FinanceService financeService;

    @Operation(summary = "예금 상품 업데이트", description = "예금 상품 정보를 최신 데이터로 업데이트합니다.")
    @ApiResponse(responseCode = "200", description = "업데이트 성공")
    @GetMapping(value = "/update/deposit")
    public ResponseEntity<?> updateDeposit() {
        String result = financeService.updateDeposit();
        return ResponseEntity.ok(new SuccessResponse(result));
    }

    @Operation(summary = "적금 상품 업데이트", description = "적금 상품 정보를 최신 데이터로 업데이트합니다.")
    @ApiResponse(responseCode = "200", description = "업데이트 성공")
    @GetMapping(value = "/update/saving")
    public ResponseEntity<?> updateSaving() {
        String result = financeService.updateSaving();
        return ResponseEntity.ok(new SuccessResponse(result));
    }

    @Operation(summary = "주택담보대출 상품 업데이트", description = "주택담보대출 상품 정보를 최신 데이터로 업데이트합니다.")
    @ApiResponse(responseCode = "200", description = "업데이트 성공")
    @GetMapping(value = "/update/mortgage")
    public ResponseEntity<?> updateMortgage() {
        String result = financeService.updateMortgageLoan();
        return ResponseEntity.ok(new SuccessResponse(result));
    }

    @Operation(summary = "전세자금대출 상품 업데이트", description = "전세자금대출 상품 정보를 최신 데이터로 업데이트합니다.")
    @ApiResponse(responseCode = "200", description = "업데이트 성공")
    @GetMapping(value = "/update/rentHouse")
    public ResponseEntity<?> updateRentHouse() {
        String result = financeService.updateRenthouse();
        return ResponseEntity.ok(new SuccessResponse(result));
    }

    @Operation(summary = "적금 상품 필터 조회", description = "적금 상품 필터 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = SavingsList.class)))
    @GetMapping("/filter/savings")
    public ResponseEntity<SuccessResponse> getSavingsFilter() {

        SavingsList savingsList = financeService.getSavingsFilter();
        return ResponseEntity.ok(new SuccessResponse(savingsList));
    }

    @Operation(summary = "대출 상품 필터 조회", description = "대출 상품 필터 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = LoanList.class)))
    @GetMapping("/filter/loan")
    public ResponseEntity<SuccessResponse> getLoanFilter() {

        LoanList loanList = financeService.getLoanFilter();
        return ResponseEntity.ok(new SuccessResponse(loanList));
    }

    @Operation(summary = "대출 상품 목록 조회", description = "페이지네이션과 필터를 적용하여 대출 상품 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/loans")
    public ResponseEntity<?> getLoans(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false, defaultValue = "") String mrtgTypeNm,
            @RequestParam(required = false, defaultValue = "") String lendRateTypeNm,
            @RequestParam(required = false, defaultValue = "lendRateMin") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction
    ) {
        // Sort 객체 생성
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 서비스 호출
        Page<LoanList> result = financeService.getFilteredLoans(pageable,
                mrtgTypeNm.isEmpty() ? null : mrtgTypeNm,
                lendRateTypeNm.isEmpty() ? null : lendRateTypeNm);

        return ResponseEntity.ok(new SuccessResponse(result));
    }
}
