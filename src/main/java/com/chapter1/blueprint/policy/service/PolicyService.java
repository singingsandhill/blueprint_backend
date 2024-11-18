package com.chapter1.blueprint.policy.service;

import com.chapter1.blueprint.member.repository.PolicyAlarmRepository;
import com.chapter1.blueprint.policy.domain.PolicyDetail;
import com.chapter1.blueprint.policy.domain.PolicyList;
import com.chapter1.blueprint.policy.domain.dto.PolicyListDTO;
import com.chapter1.blueprint.policy.repository.PolicyDetailRepository;
import com.chapter1.blueprint.policy.repository.PolicyListRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PolicyService {
    private final PolicyListRepository policyListRepository;
    private final PolicyDetailRepository policyDetailRepositpry;
    private final PolicyAlarmRepository policyAlarmRepository;

    @Value("${tk.policy.api.url}")
    private String policyApiUrlTK;

    @Value("${tk.policy.api.key}")
    private String policyApiKeyTK;

    @Value("${company.policy.api.key}")
    private String companyPolicyApiKey;

    @Value("${company.policy.api.url}")
    private String companyPolicyApiUrl;

    @Value("${company.policy.api.hahtag}")
    private String companyPolicyApiHahtag;

    public String updatePolicyTK() {
        String requestUrl = policyApiUrlTK + "?apiKey=" + policyApiKeyTK + "&searchYear=" + 2024+"&recordCount="+500;
        Integer numOfPolicy = 0;

        try {
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            log.error("API Response: {}", response.toString());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.toString());
            JsonNode items = rootNode.path("resultList");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


            numOfPolicy = items.size();
            for (JsonNode item : items) {
                PolicyList policyList = new PolicyList();
                policyList.setDistrict(item.path("rgnSeNm").asText());
                policyList.setType(item.path("policyTypeNm").asText());
                policyList.setName(item.path("policyNm").asText());
                policyList.setOfferInst(item.path("operInstNm").asText());
                policyList.setManageInst(item.path("sprvsnInstNm").asText());
                policyList.setStartDate(parseDate(item.path("policyBgngYmd").asText(), dateFormat));
                policyList.setEndDate(parseDate(item.path("policyEndYmd").asText(), dateFormat));
                policyList.setApplyStartDate(parseDate(item.path("aplyBgngDt").asText(), dateFormat));
                policyList.setApplyEndDate(parseDate(item.path("aplyEndDt").asText(), dateFormat));

                PolicyDetail policyDetail = new PolicyDetail();
                policyDetail.setSubject(item.path("policyCnDtl").asText()); //대상
                policyDetail.setCondition(item.path("policyCn").asText()); //조건
                policyDetail.setContent(item.path("policyCn").asText()); //내용
                policyDetail.setScale(item.path("policyScl").asText()); //규모
                policyDetail.setEnquiry(item.path("policyEnq").asText()); //문의처
                policyDetail.setWay(item.path("policyEnq").asText()); //지원방법
                policyDetail.setDocument(item.path("dtlLinkUrl").asText()); // 문서
                policyDetail.setUrl(item.path(" ").asText()); //url

                policyListRepository.save(policyList);  // 데이터 저장
                policyDetailRepositpry.save(policyDetail);
                numOfPolicy++;
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
        String result = "성공, 불러온 정책 수는 "+numOfPolicy + " 개";
        return result;
    }

    private Date parseDate(String dateStr, SimpleDateFormat dateFormat) {
        try {
            return dateStr.isEmpty() ? null : dateFormat.parse(dateStr);
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", dateStr, e);
            return null;
        }
    }

    public List<PolicyListDTO> findPoliciesWithApproachingDeadline() {
        return policyListRepository.findPoliciesWithApproachingDeadline();
    }

    @Transactional
    public void deletePolicy(Long policyIdx) {
        policyListRepository.deleteById(policyIdx);

        policyAlarmRepository.deleteByPolicyIdx(policyIdx);
    }

    public String updatePolicyCompany() {
        String requestUrl = companyPolicyApiUrl + "?crtfcKey=" + companyPolicyApiKey +"&dataType=json&hashtags="+ companyPolicyApiHahtag +"&searchCnt=100";
        Integer numOfPolicy = 0;

        try {
            URL url = new URL(requestUrl);
            log.error("check url : "+ requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            log.error("API Response: {}", response.toString());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.toString());
            JsonNode items = rootNode.path("jsonArray");

            SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            numOfPolicy = items.size();
            for (JsonNode item : items) {
                PolicyList policyList = new PolicyList();
                policyList.setDistrict(item.path("pblancNm").asText());
                policyList.setType(item.path("pldirSportRealmLclasCodeNm").asText());
                //policyList.setName(item.path("policyNm").asText());
                policyList.setOfferInst(item.path("excInsttNm").asText());
                policyList.setManageInst(item.path("jrsdInsttNm").asText());

                // 날짜정보는 파싱
                processDateRange(item.path("reqstBeginEndDe").asText(), policyList);

                PolicyDetail policyDetail = new PolicyDetail();
                policyDetail.setSubject(item.path("trgetNm").asText()); //대상
                policyDetail.setCondition(item.path("bsnsSumryCn").asText()); //조건
                policyDetail.setContent(item.path("bsnsSumryCn").asText()); //내용
                //policyDetail.setScale(item.path("policyScl").asText()); //규모
                policyDetail.setEnquiry(item.path("refrncNm").asText()); //문의처
                policyDetail.setWay(item.path("rceptEngnHmpgUrl").asText()); //지원방법

                // 문서 정보 (파일명)
                String fileNm = item.path("fileNm").asText();
                if (fileNm != null && !fileNm.isEmpty()) {
                    policyDetail.setDocument(fileNm);
                }

                // URL 정보
                String pblancUrl = item.path("pblancUrl").asText();
                if (pblancUrl != null && !pblancUrl.isEmpty()) {
                    policyDetail.setUrl(pblancUrl);
                }

                policyListRepository.save(policyList);  // 데이터 저장
                policyDetailRepositpry.save(policyDetail);
                numOfPolicy++;
            }

        } catch (Exception e) {
            log.error("정책 정보 업데이트 중 오류 발생: ", e.getMessage());
            throw new RuntimeException("정책 정보 업데이트 실패", e);
        }
        String result = "성공, 불러온 정책 수는 "+numOfPolicy + " 개";
        return result;
    }

    private void processDateRange(String dateRange, PolicyList policyList) {
        try {
            if (dateRange == null || dateRange.isEmpty()) {
                return;
            }

            // 특수 케이스 처리: "예산 소진시까지"
            if (dateRange.contains("예산 소진시까지")) {
                Date endDate = createDateForYear(2099, 12, 31);
                Date startDate = new Date(); // 현재 날짜를 시작일로 설정
                policyList.setApplyStartDate(startDate);
                policyList.setApplyEndDate(endDate);
                return;
            }

            // 일반적인 날짜 범위 처리 ("20241113 ~ 20241120" 형식)
            String[] dates = dateRange.split("~");
            if (dates.length == 2) {
                String startDateStr = dates[0].trim();
                String endDateStr = dates[1].trim();

                SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyyMMdd");
                policyList.setApplyStartDate(apiDateFormat.parse(startDateStr));
                policyList.setApplyEndDate(apiDateFormat.parse(endDateStr));
            }
        } catch (ParseException e) {
            log.error("날짜 파싱 중 오류 발생: {}", dateRange, e);
            // 파싱 실패 시 기본값 설정
            setDefaultDates(policyList);
        }
    }
    private Date createDateForYear(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, 23, 59, 59);
        return calendar.getTime();
    }

    private void setDefaultDates(PolicyList policyList) {
        // 파싱 실패 시 현재 날짜를 시작일로, 2099-12-31을 종료일로 설정
        policyList.setApplyStartDate(new Date());
        policyList.setApplyEndDate(createDateForYear(2099, 12, 31));
        policyList.setStartDate(new Date());
        policyList.setEndDate(createDateForYear(2099, 12, 31));
    }


}
