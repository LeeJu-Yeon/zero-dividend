package zerobase.dividend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data   // Getter, Setter, RequiredArgsConstructor, ToString, EqualsAndHashCode, Value
//@Builder -> 역직렬화 에러 no Creators, like default constructor, exist
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    private String ticker;
    private String name;

}
