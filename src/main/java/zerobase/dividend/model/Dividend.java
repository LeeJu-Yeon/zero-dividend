package zerobase.dividend.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data   // Getter, Setter, RequiredArgsConstructor, ToString, EqualsAndHashCode, Value
//@Builder -> 역직렬화 에러 no Creators, like default constructor, exist
@NoArgsConstructor
@AllArgsConstructor
public class Dividend {

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDateTime date;

    private String dividend;

}

/*
FinanceController 의 searchFinance 메소드 호출시
getDividendByCompanyName 메소드 실행 결과인 ScrapedResult 를 캐싱

ScrapedResult 안에 Dividend 안에 LocalDateTime date 를 직렬화하는데 에러발생
Serializer, Deserializer 따로 지정해줘야
 */
