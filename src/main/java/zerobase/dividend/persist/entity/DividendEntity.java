package zerobase.dividend.persist.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import zerobase.dividend.model.Dividend;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "DIVIDEND")
@Getter
@ToString
@NoArgsConstructor
@Table(   // 복합 유니크 키 설정 => 배당금 데이터 중복저장 방지
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"companyId", "date"}
                )
        }
)
public class DividendEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId;

    private LocalDateTime date;

    private String dividend;

    public DividendEntity(Long companyId, Dividend dividend) {
        this.companyId = companyId;
        this.date = dividend.getDate();
        this.dividend = dividend.getDividend();
    }

}

/*
복합 유니크 키 설정 => companyId / date 값이 같은 배당금 데이터는 존재할수 x

유니크 키 중복 발생시 처리방법
1. IGNORE => 해당 레코드는 무시하고 insert 진행
2. ON DUPLICATE KEY UPDATE (업데이트할 컬럼) => 지정한 컬럼값만 업데이트

서비스 코드에 쿼리가 들어가는거 선호 x -> DividendRepository 에 boolean existsByCompanyIdAndDate 생성
데이터 존재하는지 확인하고, 존재하지 않을때만 저장하는 걸로
 */
