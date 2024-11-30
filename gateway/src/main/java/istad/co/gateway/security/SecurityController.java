package istad.co.gateway.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/security")
public class SecurityController {

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/hello")
    Map<String, String> hello() {
        return Map.of("message", "Hello from Gateway");
    }


}
