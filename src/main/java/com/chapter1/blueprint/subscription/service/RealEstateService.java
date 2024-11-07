package com.chapter1.blueprint.subscription.service;

import com.chapter1.blueprint.subscription.domain.RealEstatePrice;
import com.chapter1.blueprint.subscription.domain.Ssgcode;
import com.chapter1.blueprint.subscription.repository.RealEstatePriceRepository;
import com.chapter1.blueprint.subscription.repository.SsgcodeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RealEstateService {
    private final RealEstatePriceRepository realEstatePriceRepository;
    private final SsgcodeRepository ssgcodeRepository;
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    @Value("${public.api.key}")
    private String apiKey;

    @Value("${realestate.api.url}")
    private String realestateUrl;

    private final XmlMapper xmlMapper = new XmlMapper();

    public String getRealEstatePrice() {
        String callDate = "202406";
        // 특정 코드 26110의 Ssgcode를 하나만 가져와 테스트
        Ssgcode ssgcd = ssgcodeRepository.findBySsgCd5("26110");

        if (ssgcd == null) {
            log.error("Ssgcode 26110 not found");
            return "Ssgcode 26110이 존재하지 않습니다";
        }

        try {
            // 단일 요청
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> processSsgcode(ssgcd, callDate), executorService);

            future.join();

            executorService.shutdown();
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }

            return "API 불러오기 및 DB저장 성공";
        } catch (Exception e) {
            log.error("Error processing real estate data: ", e);
            return "API 처리 중 오류 발생: " + e.getMessage();
        }

        //List<Ssgcode> ssgcds = ssgcodeRepository.findAll();

        //try {
        //    List<CompletableFuture<Void>> futures = ssgcds.stream()
        //            .map(ssgcd -> CompletableFuture.runAsync(() -> processSsgcode(ssgcd, callDate), executorService))
        //            .toList();

        //    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        //    executorService.shutdown();
        //    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
        //        executorService.shutdownNow();
        //    }

        //    return "API 불러오기 및 DB저장 성공";
        //} catch (Exception e) {
        //    log.error("Error processing real estate data: ", e);
        //    return "API 처리 중 오류 발생: " + e.getMessage();
        //}
    }

    private void processSsgcode(Ssgcode ssgcd, String callDate) {
        try {
            Thread.sleep(500); // 요청 간격 0.5초 대기
            String response = fetchApiData(ssgcd.getSsgCd5(), callDate);
            log.info("XML Response: {}", response);

            // XML Fault 응답일 경우 로그만 남기고 종료
            if (isXMLFaultResponse(response)) {
                log.error("API returned XML fault for Ssgcode {}: {}", ssgcd.getSsgCd5(), getFaultString(response));
                return;
            }

            processApiResponse(response, ssgcd.getSsgCd5());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread was interrupted for Ssgcode {}", ssgcd.getSsgCd5(), e);
        } catch (Exception e) {
            log.error("Error processing Ssgcode {}: {}", ssgcd.getSsgCd5(), e.getMessage());
        }
    }

    private boolean isXMLFaultResponse(String response) {
        return response != null && response.contains("XMLFault");
    }

    // XML Fault 응답의 상세 내용 로깅
    private String getFaultString(String response) {
        try {
            // 기본 XmlMapper 설정으로 진행
            XmlMapper tempMapper = new XmlMapper();
            JsonNode rootNode = tempMapper.readTree(response);

            // namespace를 포함한 경로와 포함하지 않은 경로 모두 시도
            JsonNode faultString = null;

            // 가능한 모든 경로 시도
            String[] paths = {
                    "/ns1:XMLFault/ns1:faultstring",
                    "/XMLFault/faultstring",
                    "/faultstring"
            };

            for (String path : paths) {
                faultString = rootNode.at(path);
                if (!faultString.isMissingNode()) {
                    return faultString.asText();
                }
            }

            // findValue로 마지막 시도
            faultString = rootNode.findValue("faultstring");
            if (faultString != null) {
                return faultString.asText();
            }

            // 디버깅을 위한 XML 구조 출력
            log.debug("XML Structure: {}", rootNode.toPrettyString());
            log.error("Could not find fault string in response: {}", response);
            return "Error: Could not parse fault string";

        } catch (Exception e) {
            log.error("Error parsing XML fault: ", e);
            return "Error parsing XML fault: " + e.getMessage();
        }
    }

    private String fetchApiData(String ssgCd5, String callDate) throws Exception {
        // URL을 있는 그대로 사용 (이미 인코딩된 serviceKey 사용)
        String requestUrl = String.format("%s?serviceKey=%s&LAWD_CD=%s&DEAL_YMD=%s&numOfRows=50",
                realestateUrl, apiKey, ssgCd5, callDate);

        log.info("Requesting URL: {}", requestUrl);

        HttpURLConnection connection = null;
        try {
            URL url = new URL(requestUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 브라우저와 유사한 헤더 설정
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // 응답 읽기
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
                String responseStr = response.toString();
                log.info("Raw API Response: {}", responseStr);

                // XML 응답 구조 검증
                if (responseStr.contains("<response>")) {
                    return responseStr;
                } else if (responseStr.contains("XMLFault")) {
                    log.error("Received XMLFault response: {}", responseStr);
                    // XMLFault 응답을 처리하되, 실제 오류 내용 확인
                    return responseStr;
                } else {
                    log.error("Unexpected response format: {}", responseStr);
                    throw new RuntimeException("Unexpected API response format");
                }
            }
        } catch (Exception e) {
            log.error("Error fetching API data for Ssgcode {}: {}", ssgCd5, e.getMessage());
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void processApiResponse(String response, String ssgCd5) throws Exception {
        if (response.contains("XMLFault")) {
            log.error("API returned XMLFault for Ssgcode {}: {}", ssgCd5, response);
            return;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(response)));
            XPath xPath = XPathFactory.newInstance().newXPath();

            // 모든 item 노드 가져오기
            String expression = "//item";
            org.w3c.dom.NodeList nodeList = (org.w3c.dom.NodeList) xPath.evaluate(
                    expression, document, XPathConstants.NODESET
            );

            log.info("Found {} items in response", nodeList.getLength());

            for (int i = 0; i < nodeList.getLength(); i++) {
                try {
                    Node itemNode = nodeList.item(i);
                    RealEstatePrice realEstatePrice = new RealEstatePrice();

                    // 각 필드 값 추출
                    realEstatePrice.setSsgCd(getNodeIntValue(itemNode, "sggCd"));
                    realEstatePrice.setUmdNm(getNodeValue(itemNode, "umdNm"));
                    realEstatePrice.setJibun(getNodeValue(itemNode, "jibun"));
                    realEstatePrice.setAptDong(getNodeValue(itemNode, "aptDong"));
                    realEstatePrice.setAptNm(getNodeValue(itemNode, "aptNm"));
                    realEstatePrice.setDealDay(getNodeIntValue(itemNode, "dealDay"));
                    realEstatePrice.setDealMonth(getNodeIntValue(itemNode, "dealMonth"));
                    realEstatePrice.setDealYear(getNodeIntValue(itemNode, "dealYear"));

                    // dealAmount 특별 처리 (쉼표 제거)
                    String dealAmount = getNodeValue(itemNode, "dealAmount");
                    if (dealAmount != null) {
                        dealAmount = dealAmount.replace(",", "");
                        realEstatePrice.setDealAmount(Long.parseLong(dealAmount)*10000);
                    }

                    // dealDate 설정
                    if (realEstatePrice.getDealYear() != null &&
                            realEstatePrice.getDealMonth() != null &&
                            realEstatePrice.getDealDay() != null) {
                        realEstatePrice.setDealDate(formatDate(
                                realEstatePrice.getDealYear(),
                                realEstatePrice.getDealMonth(),
                                realEstatePrice.getDealDay()
                        ));
                    }
                    realEstatePrice.setExcluUseAr(getNodeBigDecimalValue(itemNode, "excluUseAr"));
                    realEstatePriceRepository.save(realEstatePrice);
                    log.debug("Saved real estate price data for item {}/{}: {}",
                            i + 1, nodeList.getLength(), realEstatePrice.getAptNm());
                } catch (Exception e) {
                    log.error("Error processing item {}: {}", i, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error parsing XML response: {}", e.getMessage());
            throw e;
        }
    }

    // Node에서 텍스트 값을 가져오는 헬퍼 메서드
    private String getNodeValue(Node parentNode, String tagName) {
        NodeList nodeList = ((org.w3c.dom.Element) parentNode).getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            if (node.getTextContent() != null && !node.getTextContent().trim().isEmpty()) {
                return node.getTextContent().trim();
            }
        }
        return null;
    }

    // Node에서 Integer 값을 가져오는 헬퍼 메서드
    private Integer getNodeIntValue(Node parentNode, String tagName) {
        String value = getNodeValue(parentNode, tagName);
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                log.warn("Failed to parse integer value for {}: {}", tagName, value);
            }
        }
        return null;
    }
    private BigDecimal getNodeBigDecimalValue(Node parentNode, String tagName) {
        String value = getNodeValue(parentNode, tagName);
        if (value != null) {
            try {
                // 쉼표나 공백 제거
                value = value.replace(",", "").trim();
                return new BigDecimal(value);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse BigDecimal value for {}: {}", tagName, value);
            }
        }
        return null;
    }

    private Date formatDate(int year, int month, int day) {
        return Date.from(LocalDate.of(year, month, day)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());
    }
}
