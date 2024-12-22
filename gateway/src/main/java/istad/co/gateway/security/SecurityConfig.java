package istad.co.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                  @Value("${client-security-matchers}") String[] securityMatchers,
                                                  @Value("${client-permit-matchers}") String[] permitMatchers,
                                                  ReactiveClientRegistrationRepository repository) {

        /*System.out.println(Arrays.toString(securityMatchers));
        System.out.println(Arrays.toString(permitMatchers));

        // Apply this filter-chain only to resources needing sessions
        final var clientRoutes = Stream.of(securityMatchers).map(PathPatternParserServerWebExchangeMatcher::new)
                .map(ServerWebExchangeMatcher.class::cast)
                .toList();
        http.securityMatcher(new OrServerWebExchangeMatcher(clientRoutes));*/

        http
                .authorizeExchange(exchange -> exchange
                        //.pathMatchers(permitMatchers).permitAll()
                        .anyExchange().permitAll()
                );

        http
                .oauth2Login(oAuth2LoginSpec -> {
                    oAuth2LoginSpec.authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("/"));
                    oAuth2LoginSpec.authenticationFailureHandler(new RedirectServerAuthenticationFailureHandler("/401"));
                    oAuth2LoginSpec.authorizationRequestResolver(pkceResolver(repository));
                })
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .logout(logoutSpec -> logoutSpec
                        .logoutSuccessHandler(serverLogoutSuccessHandler()));

        return http.build();
    }

    private ServerOAuth2AuthorizationRequestResolver pkceResolver(ReactiveClientRegistrationRepository repository) {
        var resolver = new DefaultServerOAuth2AuthorizationRequestResolver(repository);
        resolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
        return resolver;
    }

    private ServerLogoutSuccessHandler serverLogoutSuccessHandler() {
        RedirectServerLogoutSuccessHandler redirectServerLogoutSuccessHandler = new RedirectServerLogoutSuccessHandler();
//        final String DEFAULT_LOGOUT_SUCCESS_URL = "/";
//        URI logoutSuccessUrl = URI.create(DEFAULT_LOGOUT_SUCCESS_URL);
//        redirectServerLogoutSuccessHandler.setLogoutSuccessUrl(logoutSuccessUrl);
        String authorizationServerLogoutUrl = "http://localhost:8080/logout";
        redirectServerLogoutSuccessHandler.setLogoutSuccessUrl(URI.create(authorizationServerLogoutUrl));


        return redirectServerLogoutSuccessHandler;
    }
}
