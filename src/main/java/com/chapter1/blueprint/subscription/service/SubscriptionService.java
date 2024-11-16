package com.chapter1.blueprint.subscription.service;

import com.chapter1.blueprint.member.domain.Member;
import com.chapter1.blueprint.member.repository.MemberRepository;
import com.chapter1.blueprint.subscription.domain.SubscriptionList;
import com.chapter1.blueprint.subscription.repository.RealEstatePriceRepository;
import com.chapter1.blueprint.subscription.repository.SubscriptionListRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionService {
    private final RealEstatePriceRepository realEstatePriceRepository;
    private final SubscriptionListRepository subscriptionListRepository;
    private final MemberRepository memberRepository;

    @Value("${public.api.key}")
    private String apiKey;

    @Value("${sub.apt.api.url}")
    private String subAptApiUrl;

    @Value("${sub.apt2.api.url}")
    private String subApt2ApiUrl;

    @Value("${sub.other.api.url}")
    private String subOtherApiUrl;

    public String updateSubAPT() {
        String requestUrl = subAptApiUrl + "?page=1&perPage=50&" + "serviceKey=" + apiKey;

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
            log.info("API Response: {}", response.toString());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.toString());
            JsonNode items = rootNode.path("data");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (JsonNode item : items) {
                SubscriptionList subscriptionList = new SubscriptionList();

                //subscriptionList.setRegion(item.path("SUBSCRPT_AREA_CODE_NM").asText());

                String hssplyAdres = item.path("HSSPLY_ADRES").asText();
                String[] addressParts = parseAddress(hssplyAdres);

                if (addressParts != null) {
                    subscriptionList.setRegion(addressParts[0]); // 예: "울산광역시"
                    subscriptionList.setCity(addressParts[1]);   // 예: "중구"
                    subscriptionList.setDistrict(addressParts[2]); // 예: "우정동"
                    subscriptionList.setDetail(addressParts[3]);   // 예: "286-1번지"
                }
                subscriptionList.setName(item.path("HOUSE_NM").asText());
                subscriptionList.setHouseManageNo(item.path("HOUSE_MANAGE_NO").asInt());
                subscriptionList.setHouseDtlSecdNm(item.path("HOUSE_DTL_SECD_NM").asText());
                subscriptionList.setRentSecd(item.path("RENT_SECD_NM").asText());
                subscriptionList.setHouseDtlSecd(item.path("HOUSE_DTL_SECD_NM").asText());
                subscriptionList.setRceptBgnde(parseDate(item.path("RCEPT_BGNDE").asText(), dateFormat));
                subscriptionList.setRceptEndde(parseDate(item.path("RCEPT_ENDDE").asText(), dateFormat));
                subscriptionList.setPblancUrl(item.path("PBLANC_URL").asText());

                subscriptionListRepository.save(subscriptionList);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return "API 불러오기 및 DB저장 성공";
    }

    public String updateSubAPT2() {
        String requestUrl = subApt2ApiUrl + "?page=1&perPage=50&" + "serviceKey=" + apiKey;

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
            log.info("API Response: {}", response.toString());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.toString());
            JsonNode items = rootNode.path("data");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (JsonNode item : items) {
                SubscriptionList subscriptionList = new SubscriptionList();

                //subscriptionList.setRegion(item.path("SUBSCRPT_AREA_CODE_NM").asText());

                String hssplyAdres = item.path("HSSPLY_ADRES").asText();
                String[] addressParts = parseAddress(hssplyAdres);

                if (addressParts != null) {
                    subscriptionList.setRegion(addressParts[0]); // 예: "울산광역시"
                    subscriptionList.setCity(addressParts[1]);   // 예: "중구"
                    subscriptionList.setDistrict(addressParts[2]); // 예: "우정동"
                    subscriptionList.setDetail(addressParts[3]);   // 예: "286-1번지"
                }
                subscriptionList.setName(item.path("HOUSE_NM").asText());
                subscriptionList.setHouseManageNo(item.path("HOUSE_MANAGE_NO").asInt());
                subscriptionList.setHouseDtlSecdNm(item.path("HOUSE_SECD_NM").asText());
                subscriptionList.setRentSecd(item.path("HOUSE_SECD_NM").asText());
                subscriptionList.setHouseDtlSecd(item.path("HOUSE_SECD_NM").asText());
                subscriptionList.setRceptBgnde(parseDate(item.path("SUBSCRPT_RCEPT_BGNDE").asText(), dateFormat));
                subscriptionList.setRceptEndde(parseDate(item.path("SUBSCRPT_RCEPT_ENDDE").asText(), dateFormat));
                subscriptionList.setPblancUrl(item.path("PBLANC_URL").asText());

                subscriptionListRepository.save(subscriptionList);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return "API 불러오기 및 DB저장 성공";
    }

    public String updateSubOther() {
        String requestUrl = subOtherApiUrl + "?page=1&perPage=50&" + "serviceKey=" + apiKey;

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
            log.info("API Response: {}", response.toString());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.toString());
            JsonNode items = rootNode.path("data");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (JsonNode item : items) {
                SubscriptionList subscriptionList = new SubscriptionList();

                //subscriptionList.setRegion(item.path("SUBSCRPT_AREA_CODE_NM").asText());

                String hssplyAdres = item.path("HSSPLY_ADRES").asText();
                String[] addressParts = parseAddress(hssplyAdres);

                if (addressParts != null) {
                    subscriptionList.setRegion(addressParts[0]); // 예: "울산광역시"
                    subscriptionList.setCity(addressParts[1]);   // 예: "중구"
                    subscriptionList.setDistrict(addressParts[2]); // 예: "우정동"
                    subscriptionList.setDetail(addressParts[3]);   // 예: "286-1번지"
                }
                subscriptionList.setName(item.path("HOUSE_NM").asText());
                subscriptionList.setHouseManageNo(item.path("HOUSE_MANAGE_NO").asInt());
                subscriptionList.setHouseDtlSecdNm(item.path("HOUSE_DTL_SECD_NM").asText());
                subscriptionList.setRentSecd(item.path("HOUSE_SECD_NM").asText());
                subscriptionList.setHouseDtlSecd(item.path("HOUSE_DTL_SECD_NM").asText());
                subscriptionList.setRceptBgnde(parseDate(item.path("SUBSCRPT_RCEPT_BGNDE").asText(), dateFormat));
                subscriptionList.setRceptEndde(parseDate(item.path("SUBSCRPT_RCEPT_ENDDE").asText(), dateFormat));
                subscriptionList.setPblancUrl(item.path("PBLANC_URL").asText());

                subscriptionListRepository.save(subscriptionList);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return "API 불러오기 및 DB저장 성공";
    }

    private Date parseDate(String dateStr, SimpleDateFormat dateFormat) {
        try {
            return dateStr.isEmpty() ? null : dateFormat.parse(dateStr);
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", dateStr, e);
            return null;
        }
    }

    private String[] parseAddress(String address) {
        // 시도 (특별시, 광역시, 도) + (구/군/시) + (동/가/도로명 등) + 나머지 주소를 파싱
        Pattern pattern = Pattern.compile(
                "^(\\S+시|\\S+도|\\S+특별자치시)\\s?" + // 시도: 서울특별시, 경기도, 세종특별자치시 등
                        "(\\S+구|\\S+군|\\S+시)?\\s?" +          // 시군구: 영등포구, 아산시 등 (선택적)
                        "((?:\\S+동(?:\\d*가)?|\\S+읍|\\S+면|.+로|.+길)\\s?(?:\\d+번지)?)?\\s?" + // 읍면동/도로명, 번지 포함
                        "(.+)?$"                                // 나머지 주소
        );
        Matcher matcher = pattern.matcher(address);

        if (matcher.find()) {
            // 각 그룹을 확인하며 null인 경우를 빈 문자열로 처리
            String region1 = matcher.group(1) != null ? matcher.group(1) : ""; // 시도
            String region2 = matcher.group(2) != null ? matcher.group(2) : ""; // 시군구
            String region3 = matcher.group(3) != null ? matcher.group(3) : ""; // 읍면동/도로명
            String restAddress = matcher.group(4) != null ? matcher.group(4) : ""; // 나머지 주소

            return new String[]{region1, region2, region3, restAddress};
        }
        return null; // 주소 형식이 맞지 않으면 null 반환
    }

    public Page<SubscriptionList> getAllSubscription(Pageable pageable) {
        return subscriptionListRepository.findAll(pageable);
    }

    public List<SubscriptionList> getAllSubscriptions() {
        return subscriptionListRepository.findAll();
    }

    public List<SubscriptionList> recommendSubscription(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with ID (recommendSubscription): " + memberId));

        return subscriptionListRepository.findByRegionAndCityAndDistrictContaining(member.getRegion(), member.getDistrict(), member.getLocal());
    }
}
