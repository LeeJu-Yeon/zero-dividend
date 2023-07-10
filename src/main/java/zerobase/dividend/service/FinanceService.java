package zerobase.dividend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import zerobase.dividend.exception.impl.NoCompanyException;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.Dividend;
import zerobase.dividend.model.ScrapedResult;
import zerobase.dividend.model.constants.CacheKey;
import zerobase.dividend.persist.CompanyRepository;
import zerobase.dividend.persist.DividendRepository;
import zerobase.dividend.persist.entity.CompanyEntity;
import zerobase.dividend.persist.entity.DividendEntity;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j   // 캐싱 기능 돌아가나 로그로 확인
@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    /*
    Redis 의 key value 와는 다른 의미!!
    캐시의 key, 캐시의 이름 = value = Redis key 의 prefix
    Redis 에
    key = "finance::3M Company"  value = "ScrapedResult 가 직렬화된 데이터"
    형태로 저장된다
     */
    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName) {

        // 해당 회사 캐시 정보가 없을시, 메소드 내부 로직이 실행되기 때문에 로그가 찍힌다
        log.info("search company -> " + companyName);

        // 1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity company = this.companyRepository.findByName(companyName)
                .orElseThrow(() -> new NoCompanyException());

        // 2. 조회된 회사 ID 로 배당금 정보 조회
        List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(company.getId());

        // 3. 결과 조합 후 반환
        // 방법 1 : for 문
//        List<Dividend> dividends = new ArrayList<>();
//        for (var entity : dividendEntities) {
//            dividends.add(new Dividend(entity.getDate(), entity.getDividend());
//        }

        // 방법 2 : 스트림
        List<Dividend> dividends = dividendEntities.stream()
                .map(e -> new Dividend(e.getDate(), e.getDividend()))
                .collect(Collectors.toList());

        return new ScrapedResult(new Company(company.getTicker(), company.getName()), dividends);
    }

}

/*
캐싱에 적합한 데이터인가?
기준 1 : 요청이 자주 들어오는가? => 유명한 회사일수록 조회 빈번 => 동일한 데이터에 대한 요청이 빈번 => 캐싱 해놓으면 웅답 빨리 가능
기준 2 : 자주 변경되는 데이터인가? 배당금은 많아봐야 한달에 한번 => 업데이트 빈번 x => 캐싱 적합
 */
