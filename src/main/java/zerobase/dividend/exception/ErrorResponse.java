package zerobase.dividend.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {   // ExceptionHandler 에서 반환할 모델
    private int code;
    private String message;
}
