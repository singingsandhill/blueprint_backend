package com.chapter1.blueprint.policy.service;

import com.chapter1.blueprint.policy.domain.PolicyDetail;
import com.chapter1.blueprint.policy.domain.PolicyList;
import com.chapter1.blueprint.policy.repository.PolicyDetailRepositpry;
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
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PolicyService {
    private final PolicyListRepository policyListRepository;
    private final PolicyDetailRepositpry policyDetailRepositpry;

    @Value("${tk.policy.api.url}")
    private String policyApiUrlTK;

    @Value("${tk.policy.api.key}")
    private String policyApiKeyTK;


    public String updatePolicyTK() {
        String requestUrl = policyApiUrlTK + "?apiKey=" + policyApiKeyTK + "&searchYear" + 2024;
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
}
