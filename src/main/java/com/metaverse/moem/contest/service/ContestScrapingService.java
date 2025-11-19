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

    private final ContestRepository contestRepository;

    @Scheduled(initialDelay = 5000, fixedDelay = Long.MAX_VALUE)
    @Transactional
    public void scrapeContestData() {
        final String URL = "https://www.wevity.com/?c=find&s=1&gub=1&cidx=20";
        int savedCount = 0;

        try {
            log.info("📢 크롤링 시작: 대상 URL: {}", URL);

            Document doc = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();

            Elements contestItems = doc.select(".ms-list > .list > li:not(.top)");
            log.info("📢 총 {}개의 공모전 항목을 찾았습니다. 파싱을 시작합니다.", contestItems.size());

            // D-Day 패턴을 정규식으로 정의: "D-" 또는 "D+" 뒤에 오는 숫자
            Pattern pattern = Pattern.compile("D[\\-\\+](\\d+)");


            for (Element item : contestItems) {
                String title = item.select(".tit a").text().trim();
                String host = item.select(".organ").text().trim();

                // .day 요소에서 D-day 텍스트 추출 (예: "D-11 접수중")
                String dDayTextFull = item.select(".day").first().text().trim();
                String sourceUrl = item.select(".tit a").attr("abs:href");
                String category = item.select(".sub-tit").text().replace("분야 :", "").trim();

                LocalDate deadline = null;

                Matcher matcher = pattern.matcher(dDayTextFull);

                if (matcher.find()) {
                    // D-Day 기호와 숫자 분리
                    String sign = dDayTextFull.substring(matcher.start(), matcher.start() + 2); // "D-" 또는 "D+"
                    String daysString = matcher.group(1); // 숫자 부분만 추출

                    int days;
                    try {
                        days = Integer.parseInt(daysString);
                    } catch (NumberFormatException e) {
                        log.warn("파싱 오류: 추출된 숫자 변환 실패. 텍스트: {}", daysString);
                        continue;
                    }

                    if (sign.equals("D-")) {
                        // 접수중 (D-N)
                        deadline = LocalDate.now().plusDays(days);
                    } else if (sign.equals("D+")) {
                        // 마감 (D+N)
                        continue;
                    }

                } else if (dDayTextFull.contains("D-0")) {
                    // D-0인 경우 (오늘 마감)
                    deadline = LocalDate.now();
                } else {
                    // D-Day 정보가 없는 항목이거나 마감된 항목은 건너뜁니다.
                    log.warn("날짜 정보를 찾을 수 없는 항목 건너뜀 (텍스트: {}): {}", dDayTextFull, title);
                    continue;
                }

                if (title.isEmpty() || host.isEmpty() || deadline == null) {
                    log.warn("필수 데이터 누락으로 항목 건너뜀: {}", title);
                    continue;
                }

                // 중복 검사 및 저장 로직
                if (contestRepository.findByTitleAndHostAndDeadline(title, host, deadline).isEmpty()) {
                    Contest newContest = Contest.builder()
                            .title(title)
                            .host(host)
                            .deadline(deadline)
                            .sourceUrl(sourceUrl)
                            .category(category)
                            .build();
                    contestRepository.save(newContest);
                    savedCount++;
                }
            }
            log.info("✅ 공모전 크롤링 완료. 신규 데이터 {}건 저장됨. (URL: {})", savedCount, URL);

        } catch (IOException e) {
            log.error("❌ 공모전 크롤링 중 IO 오류 발생 (네트워크/URL 문제): {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ 데이터 파싱 중 예상치 못한 오류 발생", e);
        }
    }
}