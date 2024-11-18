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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            log.info("API 요청 URL: {}", requestUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            int responseCode = conn.getResponseCode();
            log.info("API 응답 코드: {}", responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();
                log.error("API 오류 응답: {}", errorResponse.toString());
                throw new RuntimeException("API 호출 실패. 응답 코드: " + responseCode);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            log.info("API 응답 데이터: {}", response.toString());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.toString());
            JsonNode items = rootNode.path("jsonArray");

            if (items.isMissingNode() || items.isEmpty()) {
                log.warn("API 응답에 정책 데이터가 없습니다.");
                return "API 응답에 정책 데이터가 없습니다.";
            }

            numOfPolicy = items.size();
            log.info("처리할 정책 수: {}", numOfPolicy);

            for (JsonNode item : items) {
                try {
                    PolicyList policyList = new PolicyList();
                    String policyName = item.path("pblancNm").asText();

                    policyList.setName(policyName);
                    policyList.setCity(extractAndMapCity(policyName));
                    policyList.setDistrict(extractAndMapCity(policyName));
                    policyList.setType(item.path("pldirSportRealmLclasCodeNm").asText());
                    policyList.setOfferInst(item.path("excInsttNm").asText());
                    policyList.setManageInst(item.path("jrsdInsttNm").asText());

                    processDateRange(item.path("reqstBeginEndDe").asText(), policyList);

                    PolicyDetail policyDetail = new PolicyDetail();
                    policyDetail.setSubject(item.path("trgetNm").asText());
                    policyDetail.setCondition(item.path("bsnsSumryCn").asText());
                    policyDetail.setContent(item.path("bsnsSumryCn").asText());
                    policyDetail.setEnquiry(item.path("refrncNm").asText());
                    policyDetail.setWay(item.path("reqstMthPapersCn").asText());

                    String fileNm = item.path("fileNm").asText();
                    if (!fileNm.equals("null") && !fileNm.isEmpty()) {
                        policyDetail.setDocument(fileNm);
                    }

                    String pblancUrl = item.path("pblancUrl").asText();
                    if (!pblancUrl.equals("null") && !pblancUrl.isEmpty()) {
                        policyDetail.setUrl(pblancUrl);
                    }

                    // 각 엔티티 독립적으로 저장
                    policyListRepository.save(policyList);
                    policyDetailRepositpry.save(policyDetail);

                    numOfPolicy++;

                } catch (Exception e) {
                    log.error("정책 데이터 처리 중 오류 발생. 정책명: {}, 오류: {}",
                            item.path("pblancNm").asText(), e.getMessage());
                    // 개별 정책 처리 실패 시 다음 정책 처리 계속
                    continue;
                }
            }

        } catch (Exception e) {
            log.error("정책 정보 업데이트 중 오류 발생: ", e);
            throw new RuntimeException("정책 정보 업데이트 실패: " + e.getMessage());
        }

        String result = String.format("성공, 불러온 정책 수는 %d개", numOfPolicy);
        log.info(result);
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
                policyList.setStartDate(startDate);
                policyList.setEndDate(endDate);
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
                policyList.setStartDate(apiDateFormat.parse(startDateStr));
                policyList.setEndDate(apiDateFormat.parse(endDateStr));
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

    /**
     * 정책명에서 지역 정보를 추출하고 매핑된 도시명을 반환
     */
    private String extractAndMapCity(String policyName) {
        try {
            // 정책명에서 [] 안의 내용 추출
            Pattern pattern = Pattern.compile("\\[(.*?)\\]");
            Matcher matcher = pattern.matcher(policyName);

            if (matcher.find()) {
                String cityCode = matcher.group(1);
                // 매핑된 도시명 반환, 매핑이 없는 경우 원본 값 사용
                return CITY_MAPPING.getOrDefault(cityCode, cityCode);
            }
        } catch (Exception e) {
            log.error("지역 정보 추출 중 오류 발생: {}", policyName, e);
        }

        // [] 안에 지역 정보가 없는 경우 "전국" 반환
        return "전국";
    }

    // 지역 매핑을 위한 Map 선언
    private static final Map<String, String> CITY_MAPPING = new HashMap<>() {{
        put("경기", "경기도");
        put("서울", "서울특별시");
        put("세종", "세종특별자치시");
        put("전북", "전북특별자치도");
        put("울산", "울산광역시");
        put("전남", "전라남도");
    }};
}
