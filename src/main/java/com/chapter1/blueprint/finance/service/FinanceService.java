package com.chapter1.blueprint.finance.service;

import com.chapter1.blueprint.finance.domain.LoanList;
import com.chapter1.blueprint.finance.domain.SavingsList;
import com.chapter1.blueprint.finance.repository.LoanListRepository;
import com.chapter1.blueprint.finance.repository.SavingsListRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FinanceService {
    private final SavingsListRepository savingsListRepository;
    private final LoanListRepository loanListRepository;
    private final RestTemplate restTemplate;

    @Value("${fss.api.key}")
    private String fssApiKey;

    @Value("${deposite.api.url}")
    private String depositeApiUrl;

    @Value("${saving.api.url}")
    private String savingApiUrl;

    @Value("${mortgageLoan.api.url}")
    private String mortgageLoanApiUrl;

    @Value("${rentHouseLoan.api.url}")
    private String rentHouseLoanApiUrl;

    public String updateDeposit() {
        try {
            String requestUrl = depositeApiUrl + "?auth=" + fssApiKey + "&topFinGrpNo=020000&pageNo=1";
            log.info("requestUrl : {}", requestUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));

            ResponseEntity<String> response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("API 호출 실패: {}", response.getStatusCode());
                return "API 호출 실패: " + response.getStatusCode();
            }

            String responseBody = response.getBody();
            if (responseBody == null) {
                log.error("API 응답이 비어있습니다");
                return "API 응답이 비어있습니다";
            }

            log.info("API Response: {}", responseBody);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode result = rootNode.path("result");
            JsonNode baseList = result.path("baseList");
            JsonNode optionList = result.path("optionList");

            for (JsonNode baseItem : baseList) {
                String finPrdtCd = baseItem.path("fin_prdt_cd").asText();

                for (JsonNode optionItem : optionList) {
                    if (finPrdtCd.equals(optionItem.path("fin_prdt_cd").asText())) {
                        SavingsList savingsList = new SavingsList();

                        savingsList.setFinPrdtCd(finPrdtCd);
                        savingsList.setKorCoNm(baseItem.path("kor_co_nm").asText());
                        savingsList.setDclsMonth(baseItem.path("dcls_month").asText());
                        savingsList.setFinPrdtNm(baseItem.path("fin_prdt_nm").asText());
                        savingsList.setJoinWay(baseItem.path("join_way").asText());
                        savingsList.setJoinMember(baseItem.path("join_member").asText());

                        savingsList.setIntrRateNm(optionItem.path("intr_rate_type_nm").asText());

                        if (!optionItem.path("save_trm").isMissingNode()) {
                            savingsList.setSaveTrm(optionItem.path("save_trm").asInt());
                        }
                        if (!optionItem.path("intr_rate").isMissingNode() && !optionItem.path("intr_rate").isNull()) {
                            savingsList.setIntrRate(BigDecimal.valueOf(optionItem.path("intr_rate").asDouble()));
                        }
                        if (!optionItem.path("intr_rate2").isMissingNode() && !optionItem.path("intr_rate2").isNull()) {
                            savingsList.setIntrRate2(BigDecimal.valueOf(optionItem.path("intr_rate2").asDouble()));
                        }
                        savingsList.setPrdCategory("deposit");

                        savingsListRepository.save(savingsList);
                    }
                }
            }
            return "API 불러오기 및 DB저장 성공";

        } catch (Exception e) {
            log.error("Error updating deposit data", e);
            return "API 데이터 처리 중 오류 발생: " + e.getMessage();
        }
    }

    public String updateSaving() {
        try {
            String requestUrl = savingApiUrl + "?auth=" + fssApiKey + "&topFinGrpNo=020000&pageNo=1";
            log.info("requestUrl : {}", requestUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));

            ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            String responseBody = response.getBody();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode result = rootNode.path("result");
            JsonNode baseList = result.path("baseList");
            JsonNode optionList = result.path("optionList");

            for (JsonNode baseItem : baseList) {
                String finPrdtCd = baseItem.path("fin_prdt_cd").asText();

                for (JsonNode optionItem : optionList) {
                    if (finPrdtCd.equals(optionItem.path("fin_prdt_cd").asText())) {
                        SavingsList savingsList = new SavingsList();

                        savingsList.setFinPrdtCd(finPrdtCd);
                        savingsList.setKorCoNm(baseItem.path("kor_co_nm").asText());
                        savingsList.setDclsMonth(baseItem.path("dcls_month").asText());
                        savingsList.setFinPrdtNm(baseItem.path("fin_prdt_nm").asText());
                        savingsList.setJoinWay(baseItem.path("join_way").asText());
                        savingsList.setJoinMember(baseItem.path("join_member").asText());
                        savingsList.setIntrRateNm(optionItem.path("intr_rate_type_nm").asText());
                        savingsList.setSaveTrm(optionItem.path("save_trm").asInt());
                        savingsList.setIntrRate(BigDecimal.valueOf(optionItem.path("intr_rate").asDouble()));
                        savingsList.setIntrRate2(BigDecimal.valueOf(optionItem.path("intr_rate2").asDouble()));

                        savingsList.setPrdCategory("saving");

                        savingsListRepository.save(savingsList);
                    }
                }
            }
            return "API 불러오기 및 DB저장 성공";

        } catch (Exception e) {
            log.error("Error updating saving data", e);
            return "Error updating saving data : " + e.getMessage();
        }
    }

    public String updateMortgageLoan() {
        try {
            String requestUrl = mortgageLoanApiUrl + "?auth=" + fssApiKey + "&topFinGrpNo=050000&pageNo=1";
            log.info("requestUrl : {}", requestUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));

            ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            String responseBody = response.getBody();
            log.info("API Response: {}", responseBody);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode result = rootNode.path("result");
            JsonNode baseList = result.path("baseList");
            JsonNode optionList = result.path("optionList");

            for (JsonNode baseItem : baseList) {
                String finPrdtCd = baseItem.path("fin_prdt_cd").asText();

                for (JsonNode optionItem : optionList) {
                    if (finPrdtCd.equals(optionItem.path("fin_prdt_cd").asText())) {
                        LoanList loanList = new LoanList();

                        loanList.setFinPrdtCd(finPrdtCd);
                        loanList.setKorCoNm(baseItem.path("kor_co_nm").asText());
                        loanList.setDclsMonth(baseItem.path("dcls_month").asText());
                        loanList.setFinPrdtNm(baseItem.path("fin_prdt_nm").asText());
                        loanList.setJoinWay(baseItem.path("join_way").asText());
                        loanList.setLoanLmt(baseItem.path("loan_lmt").asText());
                        loanList.setMrtgTypeNm(optionItem.path("mrtg_type_nm").asText());
                        loanList.setLendRateTypeNm(optionItem.path("lend_rate_type_nm").asText());
                        loanList.setRpayTypeNm(optionItem.path("rpay_type_nm").asText());
                        loanList.setLendRateMin(BigDecimal.valueOf(optionItem.path("lend_rate_min").asDouble()));
                        loanList.setLendRateMax(BigDecimal.valueOf(optionItem.path("lend_rate_max").asDouble()));
                        loanList.setLendRateAvg(BigDecimal.valueOf(optionItem.path("lend_rate_avg").asDouble()));
                        loanList.setPrdCategory("mortgage");

                        loanListRepository.save(loanList);
                    }
                }
            }
            return "API 불러오기 및 DB저장 성공";

        } catch (Exception e) {
            log.error("Error updating deposit data", e);
            return "API 데이터 처리 중 오류 발생: " + e.getMessage();
        }
    }

    public String updateRenthouse() {
        try {
            String requestUrl = rentHouseLoanApiUrl + "?auth=" + fssApiKey + "&topFinGrpNo=050000&pageNo=1";
            log.info("requestUrl : {}", requestUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));

            ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            String responseBody = response.getBody();
            log.info("API Response: {}", responseBody);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode result = rootNode.path("result");
            JsonNode baseList = result.path("baseList");
            JsonNode optionList = result.path("optionList");

            for (JsonNode baseItem : baseList) {
                String finPrdtCd = baseItem.path("fin_prdt_cd").asText();

                for (JsonNode optionItem : optionList) {
                    if (finPrdtCd.equals(optionItem.path("fin_prdt_cd").asText())) {
                        LoanList loanList = new LoanList();

                        loanList.setFinPrdtCd(finPrdtCd);
                        loanList.setKorCoNm(baseItem.path("kor_co_nm").asText());
                        loanList.setDclsMonth(baseItem.path("dcls_month").asText());
                        loanList.setFinPrdtNm(baseItem.path("fin_prdt_nm").asText());
                        loanList.setJoinWay(baseItem.path("join_way").asText());
                        loanList.setLoanLmt(baseItem.path("loan_lmt").asText());
                        //loanList.setMrtgTypeNm(baseItem.path("mrtg_type_nm").asText());
                        loanList.setLendRateTypeNm(optionItem.path("lend_rate_type_nm").asText());
                        loanList.setRpayTypeNm(optionItem.path("rpay_type_nm").asText());
                        loanList.setLendRateMin(BigDecimal.valueOf(optionItem.path("lend_rate_min").asDouble()));
                        loanList.setLendRateMax(BigDecimal.valueOf(optionItem.path("lend_rate_max").asDouble()));
                        loanList.setLendRateAvg(BigDecimal.valueOf(optionItem.path("lend_rate_avg").asDouble()));
                        loanList.setPrdCategory("rentHouse");

                        loanListRepository.save(loanList);
                    }
                }
            }
            return "API 불러오기 및 DB저장 성공";

        } catch (Exception e) {
            log.error("Error updating deposit data", e);
            return "API 데이터 처리 중 오류 발생: " + e.getMessage();
        }
    }

    public SavingsList getSavingsFilter() {
        return savingsListRepository.getSavingsFilter();
    }

    public LoanList getLoanFilter() {
        return loanListRepository.getLoanFilter();
    }

    public Page<LoanList> getFilteredLoans(Pageable pageable, String mrtgTypeNm, String lendRateTypeNm) {
        return loanListRepository.findLoansWithFilters(mrtgTypeNm, lendRateTypeNm, pageable);
    }
}