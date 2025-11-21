package com.metaverse.moem.contest.service;

import com.metaverse.moem.contest.domain.Contest;
import com.metaverse.moem.contest.repository.ContestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContestScrapingService {

    private static final String TARGET_URL = "https://www.wevity.com/?c=find&s=1&gub=1&cidx=20";
    private static final Pattern D_DAY_PATTERN = Pattern.compile("D([\\-\\+])(\\d+)");

    private final ContestRepository contestRepository;

    /**
     * 애플리케이션 기동 5초 후 최초 1회 실행,
     * 이후 12시간 간격(기본값)으로 자동 갱신.
     */
    @Scheduled(
            initialDelayString = "${contest.scrape.initial-delay-ms:5000}",
            fixedDelayString = "${contest.scrape.interval-ms:43200000}"
    )
    @Transactional
    public void scrapeContestData() {
        int savedCount = 0;

        try {
            log.info("📢 공모전 데이터 크롤링 시작: {}", TARGET_URL);

            Document doc = Jsoup.connect(TARGET_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();

            Elements contestItems = doc.select(".ms-list > .list > li:not(.top)");
            log.info("🔎 {}개의 공모전 항목 감지, 파싱을 진행합니다.", contestItems.size());

            for (Element item : contestItems) {
                String title = item.selectFirst(".tit a") != null ? item.selectFirst(".tit a").text().trim() : "";
                String host = item.selectFirst(".organ") != null ? item.selectFirst(".organ").text().trim() : "";
                Element dayElement = item.selectFirst(".day");

                if (dayElement == null) {
                    log.debug("⏭️ D-Day 정보가 없어 건너뜀: {}", title);
                    continue;
                }

                String dDayText = dayElement.text().trim();
                LocalDate deadline = parseDeadline(dDayText);
                if (deadline == null) {
                    log.debug("⏭️ 마감된 공모전 혹은 날짜 파싱 실패, 건너뜀: {}", title);
                    continue;
                }

                String sourceUrl = item.selectFirst(".tit a") != null
                        ? item.selectFirst(".tit a").attr("abs:href")
                        : "";
                String category = item.selectFirst(".sub-tit") != null
                        ? item.selectFirst(".sub-tit").text().replace("분야 :", "").trim()
                        : "기타";

                if (title.isEmpty() || host.isEmpty() || sourceUrl.isEmpty()) {
                    log.debug("⏭️ 필수 데이터 누락으로 저장하지 않음: {}", title);
                    continue;
                }

                boolean exists = contestRepository
                        .findByTitleAndHostAndDeadline(title, host, deadline)
                        .isPresent();

                if (exists) {
                    continue;
                }

                contestRepository.save(
                        Contest.builder()
                                .title(title)
                                .host(host)
                                .deadline(deadline)
                                .sourceUrl(sourceUrl)
                                .category(category.isBlank() ? "기타" : category)
                                .build()
                );
                savedCount++;
            }

            log.info("✅ 공모전 크롤링 완료. 신규 저장 건수: {}", savedCount);
        } catch (IOException e) {
            log.error("❌ 공모전 페이지 연결 실패 (네트워크/URL 문제) : {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ 공모전 데이터 파싱 중 예외 발생", e);
        }
    }

    private LocalDate parseDeadline(String dDayText) {
        Matcher matcher = D_DAY_PATTERN.matcher(dDayText);

        if (matcher.find()) {
            String sign = matcher.group(1); // '-' 또는 '+'
            int days = Integer.parseInt(matcher.group(2));

            if ("-".equals(sign)) {
                return LocalDate.now().plusDays(days);
            }
            return null; // D+ 는 이미 마감
        }

        if (dDayText.contains("D-0")) {
            return LocalDate.now();
        }

        return null;
    }
}