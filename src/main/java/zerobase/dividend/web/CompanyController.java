package zerobase.dividend.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.constants.CacheKey;
import zerobase.dividend.persist.entity.CompanyEntity;
import zerobase.dividend.service.CompanyService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/company")   // 경로의 공통되는 부분
@AllArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final CacheManager redisCacheManager;

    /*
    ResponseEntity 를 사용하면 상태 코드, 헤더, 본문을 포함한 완전한 HTTP 응답을 유연하게 구성할 수 있다
    <?> 는 와일드카드 제네릭을 의미하며, 어떤 타입의 응답 본문도 수용할 수 있다
    ResponseEntity.ok(응답) 메서드는 상태 코드 200과 함께 응답 본문을 생성한다
     */

    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@RequestParam String keyword) {
//        List<String> result = this.companyService.autocomplete(keyword);   // Trie 로 자동완성
        List<String> result = this.companyService.getCompanyNamesByKeyword(keyword);   // SQL Like 로 자동완성

        return ResponseEntity.ok(result);
    }

    @GetMapping
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<?> searchCompany(final Pageable pageable) {   // 클라이언트에서 페이지 옵션을 추가해서 호출가능
        Page<CompanyEntity> companies = this.companyService.getAllCompany(pageable);
        return ResponseEntity.ok(companies);
    }

    @PostMapping
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<?> addCompany(@RequestBody Company request) {
        String ticker = request.getTicker().trim();
        if (ObjectUtils.isEmpty(ticker)) {
            throw new RuntimeException("ticker is empty");
        }

        Company company = this.companyService.save(ticker);
        this.companyService.addAutocompleteKeyword(company.getName());

        return ResponseEntity.ok(company);
    }

    @DeleteMapping("/{ticker}")
    @PreAuthorize("hasRole('WRITE')")   // 관리권한 가진 사람만
    public ResponseEntity<?> deleteCompany(@PathVariable String ticker) {
        String companyName = this.companyService.deleteCompany(ticker);
        this.clearFinanceCache(companyName);   // 잊지말고 캐시도 삭제

        log.info("delete company -> " + companyName);

        return ResponseEntity.ok(companyName);
    }

    public void clearFinanceCache(String companyName) {
        // @CacheEvict 어노테이션 방식이 아닌, 코드 작성하여 캐시 삭제하기
        this.redisCacheManager.getCache(CacheKey.KEY_FINANCE).evict(companyName);
    }

}
