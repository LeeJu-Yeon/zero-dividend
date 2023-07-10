package zerobase.dividend.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.ScrapedResult;
import zerobase.dividend.model.constants.CacheKey;
import zerobase.dividend.persist.CompanyRepository;
import zerobase.dividend.persist.DividendRepository;
import zerobase.dividend.persist.entity.CompanyEntity;
import zerobase.dividend.persist.entity.DividendEntity;
import zerobase.dividend.scraper.Scraper;

import java.util.List;

@Slf4j   // 스케쥴러가 샐행된걸 알수있게 로깅을 남겨주자
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    private final Scraper yahooFinanceScraper;

    /*
    cron 에 직접 주기를 입력하면
    주기 변경시마다 코드 빌드 -> 재배포 해야함 => 비효율적
    application.yml 같은 설정 파일에 주기를 입력하면
    다시 빌드하거나 배포하지 않아도 되기 때문에 유연성이 높아진다
     */
    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)   // 스케쥴러 동작시 캐시 비우기
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {

        log.info("scraping scheduler is started");

        // 1. 저장된 회사 목록을 조회
        List<CompanyEntity> companies = this.companyRepository.findAll();

        // 2. 회사마다 배당금 정보를 새로 스크래핑
        for (CompanyEntity companyEntity : companies) {

            // 어느 회사를 스크래핑 하고 있는지 알고싶을때
            log.info("scraping scheduler is started -> " + companyEntity.getName());

            ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(
                    new Company(companyEntity.getTicker(), companyEntity.getName()));

            // 3. 스크래핑한 배당금 정보 중 데이터베이스에 없는 값만 저장
            scrapedResult.getDividends().stream()
                    // 디비든 모델을 디비든 엔티티로 매핑
                    .map(e -> new DividendEntity(companyEntity.getId(), e))
                    // 엘리먼트를 하나씩 존재유뮤 검사 & 없는 값만 디비든 레파지토리에 저장
                    .forEach(e -> {
                        boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if (!exists) {
                            this.dividendRepository.save(e);
                            log.info("insert new dividend -> " + e.toString());
                        }
                    });

            // 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000);   // 3 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }

    }

}

/*
< 추가로 알아보면 좋은 내용 >
회사가 많을때는 회사 리스트를 가져오는것도 고민해봐야 함
또한, 스크래핑 시 Thread.sleep 텀이 있는데, 회사가 많을수록 시간 많이 소요

=> 스프링 배치 Spring Batch : 효울적으로 대용량 데이터 처리 가능
 */
