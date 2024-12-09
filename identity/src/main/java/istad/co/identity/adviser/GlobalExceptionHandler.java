package istad.co.identity.adviser;

import istad.co.identity.base.BasedError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public BasedError<String> handleRuntimeException(RuntimeException ex) {
        return BasedError.<String>builder()
                .code("500")
                .description(ex.getMessage())
                .build();
    }


    @ExceptionHandler(NullPointerException.class)
    public BasedError<String> handleNullPointerException(NullPointerException ex) {
        return BasedError.<String>builder()
                .code("400")
                .description(ex.getMessage())
                .build();
    }

}