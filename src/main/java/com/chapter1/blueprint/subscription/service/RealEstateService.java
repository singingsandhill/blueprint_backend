package com.chapter1.blueprint.subscription.service;

import com.chapter1.blueprint.subscription.repository.RealEstatePriceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RealEstateService {
    private final RealEstatePriceRepository realEstatePriceRepository;

    @Value("${public.api.key}")
    private String apiKey;

    @Value("${realestate.api.url}")
    private String realestateUrl;

    public String getRealEstatePrice() {
        String requestUrl = realestateUrl + "?api_key=" + apiKey;
        // https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade/getRTMSDataSvcAptTrade
        // ?serviceKey=h%2F59h8yR81rQmBElTC6qkCxg%2Bb9EvFngUKCA0YFLWzYxOZYuLUO023e2v9VxqYKdO7UGP9KO45gp%2BxcNtacCLg%3D%3D
        // &LAWD_CD=11110
        // &DEAL_YMD=202406
        // &numOfRows=60

        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/xml; charset=UTF-8");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            log.info("API Response: {}",response.toString());

            ObjectMapper objectMapper = new XmlMapper();
            JsonNode rootNode = objectMapper.readTree(response.toString());
            JsonNode items = rootNode.path("body").path("items");

            if (items.isArray()) {
                for (JsonNode item : items) {
                    // 필요한 필드를 추출하여 사용할 수 있습니다.
                    String aptName = item.path("aptNm").asText();
                    String dealAmount = item.path("dealAmount").asText();
                    String dealYear = item.path("dealYear").asText();
                    String dealMonth = item.path("dealMonth").asText();
                    String dealDay = item.path("dealDay").asText();

                    // 로그 출력 예시
                    log.info("Apartment: {}, Deal Amount: {}, Deal Date: {}-{}-{}", aptName, dealAmount, dealYear, dealMonth, dealDay);

                    // 필요한 경우 DB에 저장하는 로직을 추가할 수 있습니다.
                    // 예: realEstatePriceRepository.save(...);
                }
            }

        } catch (Exception e) {
            return e.getMessage();
        }

        return "successful";
    }


}
