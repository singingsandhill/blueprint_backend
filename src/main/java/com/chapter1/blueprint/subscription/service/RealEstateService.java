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
import java.net.SocketTimeoutException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RealEstateService {
    private final RealEstatePriceRepository realEstatePriceRepository;
    private final SsgcodeRepository ssgcodeRepository;
    private final RealEstatePriceSummaryRepository realEstatePriceSummaryRepository;

    // 카운터 추가
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private final AtomicInteger savedCount = new AtomicInteger(0);

    private final Set<String> globalProcessedCodes = createConcurrentSet();

    // 스레드 풀 크기를 시스템 리소스에 맞게 조정
    private final ExecutorService executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 2
    );

    @Value("${public.api.key}")
    private String apiKey;

    @Value("${realestate.api.url}")
    private String realestateUrl;

    public String getRealEstatePrice() {
        String callDate = "202305";
        List<Ssgcode> ssgcds = ssgcodeRepository.findAllSsgcodes();
        Set<String> failedSsgCodes = createConcurrentSet();

        log.info("Starting to process {} ssgcodes for period: {}", ssgcds.size(), callDate);

        // 카운터 초기화
        processedCount.set(0);
        savedCount.set(0);

        try {
            // [1단계] 전체 데이터 처리 시도
            int chunkSize = 10;
            List<List<Ssgcode>> chunks = splitListIntoChunks(ssgcds, chunkSize);

            for (int i = 0; i < chunks.size(); i++) {
                List<Ssgcode> currentChunk = chunks.get(i);
                processChunk(currentChunk, callDate, failedSsgCodes, i + 1, chunks.size());
                Thread.sleep(2000); // 청크 간 딜레이
            }

            // [2단계] 실패한 것들만 재시도
            if (!failedSsgCodes.isEmpty()) {
                log.info("Retrying {} failed ssgcodes", failedSsgCodes.size());
                int maxRetries = 3;

                for (int retryCount = 1; retryCount <= maxRetries && !failedSsgCodes.isEmpty(); retryCount++) {
                    // 현재 실패 목록 복사
                    Set<String> currentFailed = new HashSet<>(failedSsgCodes);
                    failedSsgCodes.clear();

                    // 실패한 코드들만 필터링
                    List<Ssgcode> retryList = ssgcds.stream()
                            .filter(ssgcd -> currentFailed.contains(ssgcd.getSsgCd5()))
                            .collect(Collectors.toList());

                    List<List<Ssgcode>> retryChunks = splitListIntoChunks(retryList, 5); // 더 작은 청크로 재시도

                    log.info("Retry attempt {}/{} - Processing {} ssgcodes",
                            retryCount, maxRetries, retryList.size());

                    for (int i = 0; i < retryChunks.size(); i++) {
                        processChunk(retryChunks.get(i), callDate, failedSsgCodes,
                                i + 1, retryChunks.size());
                        Thread.sleep(5000); // 재시도 시 더 긴 딜레이
                    }

                    if (failedSsgCodes.isEmpty()) {
                        log.info("All failed ssgcodes successfully processed on retry {}", retryCount);
                        break;
                    }

                    // 다음 재시도 전 대기
                    if (retryCount < maxRetries) {
                        Thread.sleep(10000 * retryCount);
                    }
                }
            }

            // 최종 결과 확인
            long uniqueSsgCodes = realEstatePriceRepository.countBySsgCdAndYearMonth(
                    callDate.substring(0, 4),
                    Integer.parseInt(callDate.substring(4))
            );

            String result =  generateResultSummary(ssgcds.size(), processedCount.get(),
                    savedCount.get(), uniqueSsgCodes, failedSsgCodes);
            realEstatePriceRepository.updateRealEstatePriceFromSsgcode();
            realEstatePriceRepository.insertSummary();

            return result;

        } catch (Exception e) {
            log.error("Error in main processing loop: ", e);
            return String.format("API 처리 중 오류 발생: %s (처리된 개수: %d, 실패: %d)",
                    e.getMessage(), processedCount.get(), failedSsgCodes.size());
        } finally {
            shutdownExecutorService();
        }
    }

    // 리스트를 청크 단위로 분할하는 유틸리티 메서드
    private <T> List<List<T>> splitListIntoChunks(List<T> list, int chunkSize) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            chunks.add(list.subList(
                    i,
                    Math.min(i + chunkSize, list.size())
            ));
        }
        return chunks;
    }

    private boolean isXMLFaultResponse(String response) {
        return response != null && response.contains("XMLFault");
    }

    private String fetchApiData(String ssgCd5, String callDate) throws Exception {
        String requestUrl = String.format("%s?serviceKey=%s&LAWD_CD=%s&DEAL_YMD=%s&numOfRows=1000",
                realestateUrl, apiKey, ssgCd5, callDate);

        log.debug("Requesting URL for ssgCd5 {}: {}", ssgCd5, requestUrl);
        HttpURLConnection connection = null;

        try {
            URL url = new URL(requestUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(30000);     // 30초로 증가
            connection.setReadTimeout(100000);        // 60초 유지

            int responseCode = connection.getResponseCode();
            log.info("Response code for ssgCd5 {}: {}", ssgCd5, responseCode);

            String responseStr;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line).append("\n");
                    }
                    responseStr = response.toString();

                    // 기본적인 XML 구조 확인
                    if (!responseStr.contains("<response>")) {
                        log.error("Invalid XML response for ssgCd5 {}: {}", ssgCd5, responseStr);
                        return null;
                    }

                    log.debug("Received valid response for ssgCd5 {} ({} bytes)",
                            ssgCd5, responseStr.length());
                    return responseStr;
                }
            } else {
                try (BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), "UTF-8"))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line).append("\n");
                    }
                    log.error("Error response for ssgCd5 {} (code {}): {}",
                            ssgCd5, responseCode, errorResponse.toString());
                    return null;
                }
            }
        } catch (Exception e) {
            log.error("Error fetching data for ssgCd5 {}: {}", ssgCd5, e.getMessage());
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void processApiResponse(String response, String ssgCd5) throws Exception {
        if (response == null) {
            log.error("Null response received for Ssgcode {}", ssgCd5);
            return;
        }

        if (response.contains("XMLFault")) {
            String faultString = getFaultString(response);
            log.error("API returned XMLFault for Ssgcode {}: {}", ssgCd5, faultString);
            return;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(response)));
            XPath xPath = XPathFactory.newInstance().newXPath();

            // API 응답 검증
            String resultCode = (String) xPath.evaluate("//resultCode", document, XPathConstants.STRING);
            String resultMsg = (String) xPath.evaluate("//resultMsg", document, XPathConstants.STRING);
            String totalCount = (String) xPath.evaluate("//totalCount", document, XPathConstants.STRING);

            log.info("API Response for ssgCd5 {}: resultCode={}, resultMsg={}, totalCount={}",
                    ssgCd5, resultCode, resultMsg, totalCount);

            // resultCode 체크 수정: "000"이 성공 코드
            if (!"000".equals(resultCode)) {  // "00" 에서 "000"으로 수정
                log.error("API error for ssgCd5 {}: {}", ssgCd5, resultMsg);
                return;
            }

            // totalCount가 0인 경우 조기 반환 추가
            if ("0".equals(totalCount)) {
                log.info("No data available for ssgCd5 {}", ssgCd5);
                return;
            }

            // 모든 item 노드 가져오기
            String expression = "//item";
            NodeList nodeList = (NodeList) xPath.evaluate(expression, document, XPathConstants.NODESET);

            if (nodeList.getLength() == 0) {
                log.warn("No items found in response for ssgCd5 {}", ssgCd5);
                return;
            }

            log.info("Processing {} items for ssgCd5 {}", nodeList.getLength(), ssgCd5);

            int savedItems = 0;
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
                    savedItems++;

                    if (i % 10 == 0 || i == nodeList.getLength() - 1) {
                        log.info("Progress for ssgCd5 {}: {}/{} items processed",
                                ssgCd5, i + 1, nodeList.getLength());
                    }
                } catch (Exception e) {
                    log.error("Error processing item {} for ssgCd5 {}: {}", i, ssgCd5, e.getMessage());
                }
            }

            if (savedItems > 0) {
                log.info("Successfully processed ssgCd5 {}: {} out of {} items saved",
                        ssgCd5, savedItems, nodeList.getLength());
            } else {
                log.error("Failed to save any items for ssgCd5 {}", ssgCd5);
                throw new Exception("No items were saved");
            }

        } catch (Exception e) {
            log.error("Error parsing XML response for ssgCd5 {}: {}", ssgCd5, e.getMessage());
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

    public List<RealEstatePriceSummaryDTO> getRealEstateSummary(String region, String sggCdNm, String umdNm) {
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

    private boolean processSsgcode(Ssgcode ssgcd, String callDate) {
        int maxRetries = 3;
        int retryDelayMs = 1000;
        boolean success = false;

        for (int retry = 0; retry < maxRetries; retry++) {
            try {
                if (retry > 0) {
                    Thread.sleep(retryDelayMs * (long) Math.pow(2, retry - 1));
                }

                String response = fetchApiData(ssgcd.getSsgCd5(), callDate);

                if (response == null || isXMLFaultResponse(response)) {
                    log.error("Invalid response for Ssgcode {}", ssgcd.getSsgCd5());
                    continue;
                }

                // 데이터 처리 전 기존 카운트
                long beforeCount = realEstatePriceRepository.countBySsgCd(ssgcd.getSsgCd5());

                // 데이터 처리
                processApiResponse(response, ssgcd.getSsgCd5());

                // 처리 후 카운트 확인
                long afterCount = realEstatePriceRepository.countBySsgCd(ssgcd.getSsgCd5());

                if (afterCount > beforeCount) {
                    success = true;
                    savedCount.incrementAndGet();
                    log.info("Successfully saved data for ssgCd5 {}: {} records",
                            ssgcd.getSsgCd5(), (afterCount - beforeCount));
                    break;
                }

            } catch (Exception e) {
                if (retry == maxRetries - 1) {
                    log.error("Final retry failed for Ssgcode {}", ssgcd.getSsgCd5(), e);
                } else {
                    log.warn("Retry {}/{} failed for Ssgcode {}",
                            retry + 1, maxRetries, ssgcd.getSsgCd5());
                }
            }
        }
        return success;
    }

    private String getFaultString(String response) {
        try {
            // 기본 XmlMapper 설정으로 진행
            XmlMapper tempMapper = new XmlMapper();
            JsonNode rootNode = tempMapper.readTree(response);

            // namespace를 포함한 경로와 포함하지 않은 경로 모두 시도
            JsonNode faultString = null;

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

            return "Could not parse fault string";
        } catch (Exception e) {
            return "Error parsing XML fault: " + e.getMessage();
        }
    }
    private void processAllSsgcodes(List<Ssgcode> ssgcds, String callDate,
                                    Set<String> failedSsgCodes) throws InterruptedException {
        int chunkSize = 10;
        List<List<Ssgcode>> chunks = splitListIntoChunks(ssgcds, chunkSize);

        for (int i = 0; i < chunks.size(); i++) {
            processChunk(chunks.get(i), callDate, failedSsgCodes, i + 1, chunks.size());
            Thread.sleep(2000); // 청크 간 딜레이
        }
    }

    private void retryFailedSsgcodes(List<Ssgcode> allSsgcds, String callDate,
                                     Set<String> failedSsgCodes) throws InterruptedException {
        int maxRetries = 3;

        for (int retryCount = 1; retryCount <= maxRetries && !failedSsgCodes.isEmpty(); retryCount++) {
            log.info("Retry attempt {}/{} for {} failed ssgcodes",
                    retryCount, maxRetries, failedSsgCodes.size());

            // 현재 실패 목록의 스냅샷 생성
            Set<String> currentFailedCodes = new HashSet<>(failedSsgCodes);

            // 실패한 ssg 코드에 해당하는 Ssgcode 객체들을 찾아서 재시도
            List<Ssgcode> retryList = allSsgcds.stream()
                    .filter(ssgcd -> currentFailedCodes.contains(ssgcd.getSsgCd5()))
                    .distinct() // 중복 제거
                    .collect(Collectors.toList());

            log.info("Attempting to retry {} unique ssgcodes", retryList.size());

            Set<String> stillFailed = createConcurrentSet();

            // 더 작은 청크 사이즈로 재처리
            List<List<Ssgcode>> retryChunks = splitListIntoChunks(retryList, 3); // 청크 크기를 더 작게 조정

            for (int i = 0; i < retryChunks.size(); i++) {
                List<Ssgcode> uniqueChunk = retryChunks.get(i).stream()
                        .distinct()
                        .collect(Collectors.toList());

                processChunk(uniqueChunk, callDate, stillFailed,
                        i + 1, retryChunks.size());
                Thread.sleep(5000); // 재시도 간격 증가
            }

            // 실패 목록 업데이트
            failedSsgCodes.clear();
            failedSsgCodes.addAll(stillFailed);

            if (failedSsgCodes.isEmpty()) {
                log.info("All failed ssgcodes successfully processed on retry {}", retryCount);
                break;
            }

            // 다음 재시도 전 대기 시간 증가
            if (retryCount < maxRetries) {
                long waitTime = 10000L * retryCount; // 10초씩 증가
                log.info("Waiting {} seconds before next retry...", waitTime/1000);
                Thread.sleep(waitTime);
            }
        }

        if (!failedSsgCodes.isEmpty()) {
            log.error("Still failed after all retries: {} ssgcodes", failedSsgCodes.size());
            log.error("Failed ssgcodes: {}", failedSsgCodes);
        }
    }

    private void processChunk(List<Ssgcode> chunk, String callDate,
                              Set<String> failedSsgCodes, int currentChunk, int totalChunks) {
        List<CompletableFuture<Boolean>> futures = chunk.stream()
                .map(ssgcd -> CompletableFuture.supplyAsync(() -> {
                    try {
                        boolean result = processSsgcode(ssgcd, callDate);
                        int completed = processedCount.incrementAndGet();

                        if (result) {
                            log.info("Successfully processed ssgcode: {}, Progress: {}/{}",
                                    ssgcd.getSsgCd5(), currentChunk, totalChunks);
                            failedSsgCodes.remove(ssgcd.getSsgCd5());
                        } else {
                            log.error("Failed to process ssgcode: {}", ssgcd.getSsgCd5());
                            failedSsgCodes.add(ssgcd.getSsgCd5());
                        }
                        return result;
                    } catch (Exception e) {
                        log.error("Error processing ssgcode {}: {}",
                                ssgcd.getSsgCd5(), e.getMessage());
                        failedSsgCodes.add(ssgcd.getSsgCd5());
                        return false;
                    }
                }, executorService))
                .collect(Collectors.toList());

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(15, TimeUnit.MINUTES);

            long successCount = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(success -> success)
                    .count();

            log.info("Chunk {}/{} completed: {} successful out of {} attempts",
                    currentChunk, totalChunks, successCount, chunk.size());
        } catch (Exception e) {
            log.error("Error processing chunk {}/{}: {}",
                    currentChunk, totalChunks, e.getMessage());
            chunk.stream()
                    .map(Ssgcode::getSsgCd5)
                    .forEach(failedSsgCodes::add);
        }
    }

    private String generateResultSummary(int totalSsgcodes, int processed,
                                         int saved, long uniqueSsgCodes, Set<String> failedSsgCodes) {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format(
                "API 처리 완료.\n전체: %d개\n처리됨: %d개\n저장 성공: %d개\nDB 저장 수: %d개",
                totalSsgcodes, processed, saved, uniqueSsgCodes
        ));

        if (!failedSsgCodes.isEmpty()) {
            summary.append(String.format("\n실패: %d개", failedSsgCodes.size()));
            summary.append("\n실패한 코드: ").append(String.join(", ", failedSsgCodes));
        }

        double successRate = ((double) uniqueSsgCodes / totalSsgcodes) * 100;
        if (successRate < 90) {
            summary.append(String.format("\n경고: 전체 중 %.2f%%만 성공적으로 처리됨", successRate));
        }

        return summary.toString();
    }

    private void shutdownExecutorService() {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(30, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private Set<String> createConcurrentSet() {
        return Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
