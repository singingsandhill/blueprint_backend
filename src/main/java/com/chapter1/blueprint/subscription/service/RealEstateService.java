package com.chapter1.blueprint.subscription.service;

import com.chapter1.blueprint.exception.codes.ErrorCode;
import com.chapter1.blueprint.exception.codes.ErrorCodeException;
import com.chapter1.blueprint.subscription.domain.DTO.RealEstatePriceSummaryDTO;
import com.chapter1.blueprint.subscription.domain.RealEstatePrice;
import com.chapter1.blueprint.subscription.domain.Ssgcode;
import com.chapter1.blueprint.subscription.repository.RealEstatePriceRepository;
import com.chapter1.blueprint.subscription.repository.RealEstatePriceSummaryRepository;
import com.chapter1.blueprint.subscription.repository.SsgcodeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RealEstateService {
    private final RealEstatePriceRepository realEstatePriceRepository;
    private final SsgcodeRepository ssgcodeRepository;
    private final RealEstatePriceSummaryRepository realEstatePriceSummaryRepository;
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    @Value("${public.api.key}")
    private String apiKey;

    @Value("${realestate.api.url}")
    private String realestateUrl;

    private final XmlMapper xmlMapper = new XmlMapper();

    public String getRealEstatePrice() {
        String callDate = "202407";

        List<Ssgcode> ssgcds = ssgcodeRepository.findAll();
        List<Ssgcode> distinctSsgcds = ssgcds.stream()
                .collect(Collectors.toMap(
                        Ssgcode::getSsgCd5, // 중복 기준 필드
                        Function.identity(),
                        (existing, replacement) -> existing // 중복 시 기존 값 유지
                ))
                .values()
                .stream()
                .collect(Collectors.toList());

        try {
            List<CompletableFuture<Void>> futures = distinctSsgcds.stream()
                    .map(ssgcd -> CompletableFuture.runAsync(() -> processSsgcode(ssgcd, callDate), executorService))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executorService.shutdown();
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            realEstatePriceRepository.updateRealEstatePriceFromSsgcode();
            realEstatePriceRepository.insertSummary();
            return "API 불러오기 및 DB저장 성공";
        } catch (Exception e) {
            log.error("Error processing real estate data: ", e);
            return "API 처리 중 오류 발생: " + e.getMessage();
        }
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
                    realEstatePrice.setSsgCd(String.valueOf(getNodeIntValue(itemNode, "sggCd")));
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
                        realEstatePrice.setDealAmount(Long.parseLong(dealAmount) * 10000);
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

    public List<RealEstatePriceSummaryDTO> getRealEstateSummary(String region, String sggCdNm, String umdNm){
        validateInput(region, sggCdNm, umdNm);
        try {
            List<Object[]> results = realEstatePriceSummaryRepository
                    .findByRegionAndSggCdNmAndUmdNm(region, sggCdNm, umdNm);

            if (results.isEmpty()) {
                throw new ErrorCodeException(ErrorCode.REAL_ESTATE_NOT_FOUND);
            }

            return results.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (ErrorCodeException e) {
            throw e;
        } catch (Exception e) {
            log.error("부동산 정보 조회 실패 - region: {}, sggCdNm: {}, umdNm: {}", region, sggCdNm, umdNm, e);
            throw new ErrorCodeException(ErrorCode.REAL_ESTATE_SERVER_ERROR);
        }
    }

    private RealEstatePriceSummaryDTO convertToDTO(Object[] result) {
        return new RealEstatePriceSummaryDTO(
                (String) result[0],     // region
                (String) result[1],     // sggCdNm
                (String) result[2],     // umdNm
                ((Number) result[3]).intValue(),    // dealYear
                ((Number) result[4]).intValue(),    // dealMonth
                ((Number) result[5]).intValue(),    // dealCount
                ((Number) result[6]).doubleValue()  // pricePerAr
        );
    }

    private void validateInput(String region, String sggCdNm, String umdNm) {
        if (StringUtils.isEmpty(region) || StringUtils.isEmpty(sggCdNm) || StringUtils.isEmpty(umdNm)) {
            throw new ErrorCodeException(ErrorCode.INVALID_REGION_PARAMETER);
        }
    }

    public List<String> getAllRegions() {
        try {
            return realEstatePriceSummaryRepository.findDistinctRegions();
        } catch (Exception e) {
            log.error("지역 목록 조회 실패", e);
            throw new ErrorCodeException(ErrorCode.REAL_ESTATE_SERVER_ERROR);
        }
    }

    public List<String> getSggList(String region) {
        try {
            if (!StringUtils.hasText(region)) {
                throw new ErrorCodeException(ErrorCode.INVALID_REGION_PARAMETER);
            }

            List<String> sggList = realEstatePriceSummaryRepository.findDistinctSggCdNmByRegion(region);

            if (sggList.isEmpty()) {
                throw new ErrorCodeException(ErrorCode.REAL_ESTATE_NOT_FOUND);
            }

            return sggList;
        } catch (ErrorCodeException e) {
            throw e;
        } catch (Exception e) {
            log.error("시군구 목록 조회 실패 - region: {}", region, e);
            throw new ErrorCodeException(ErrorCode.REAL_ESTATE_SERVER_ERROR);
        }
    }

    public List<String> getUmdList(String region, String sggCdNm) {
        try {
            if (!StringUtils.hasText(region) || !StringUtils.hasText(sggCdNm)) {
                throw new ErrorCodeException(ErrorCode.INVALID_REGION_PARAMETER);
            }

            List<String> umdList = realEstatePriceSummaryRepository.findDistinctUmdNmByRegionAndSggCdNm(region, sggCdNm);

            if (umdList.isEmpty()) {
                throw new ErrorCodeException(ErrorCode.REAL_ESTATE_NOT_FOUND);
            }

            return umdList;
        } catch (ErrorCodeException e) {
            throw e;
        } catch (Exception e) {
            log.error("읍면동 목록 조회 실패 - region: {}, sggCdNm: {}", region, sggCdNm, e);
            throw new ErrorCodeException(ErrorCode.REAL_ESTATE_SERVER_ERROR);
        }
    }
}
