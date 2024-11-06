package com.chapter1.blueprint.subscription.service;

import com.chapter1.blueprint.subscription.domain.SubscriptionList;
import com.chapter1.blueprint.subscription.repository.RealEstatePriceRepository;
import com.chapter1.blueprint.subscription.repository.SubscriptionListRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionService {
    private final RealEstatePriceRepository realEstatePriceRepository;
    private final SubscriptionListRepository subscriptionListRepository;

    @Value("${public.api.key}")
    private String apiKey;

    @Value("${sub.api.url}")
    private String subApiUrl;

    public String updateSub() {
        String requestUrl = subApiUrl +"?"+"serviceKey="+apiKey;

        try{
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
                subscriptionList.setRentSecd(item.path("RENT_SECD_NM").asText());
                subscriptionList.setHouseDtlSecd(item.path("HOUSE_DTL_SECD_NM").asText());
                subscriptionList.setRceptBgnde(parseDate(item.path("RCEPT_BGNDE").asText(),dateFormat));
                subscriptionList.setRceptEndde(parseDate(item.path("RCEPT_ENDDE").asText(),dateFormat));
                subscriptionList.setPblancUrl(item.path("PBLANC_URL").asText());

                subscriptionListRepository.save(subscriptionList);
            }
        } catch (Exception e){
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
        // 주소를 "시도 시군구 읍면동 나머지주소" 형태로 파싱하기 위한 정규식 사용
        Pattern pattern = Pattern.compile("^(\\S+시|\\S+도)\\s(\\S+구|\\S+군|\\S+시)\\s(\\S+동|\\S+읍|\\S+면)\\s(.+)$");
        Matcher matcher = pattern.matcher(address);

        if (matcher.find()) {
            return new String[]{matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4)};
        }
        return null; // 주소 형식이 맞지 않으면 null 반환
    }
}
