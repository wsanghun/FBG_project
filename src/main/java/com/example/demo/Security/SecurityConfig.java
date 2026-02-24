package com.example.demo.Security;

import com.example.demo.Handler.CustomHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/main/**",
                                "/css/**", "/js/**", "/json/**", "/image/**",
                                "/static/**",
                                "/upload/**", "/upload/image",
                                "/user/login", "/user/join", "/user/**",
                                "/joinProcess",
                                "/board/**",
                                "/boardgame/**",
                                "/games/**",
                                "/search", "/search/**",
                                "/favicon.ico",
                                "mapage/articles"
                        ).permitAll()

                        // 댓글 API
                        .requestMatchers("/api/coments/**").permitAll()

                        // 👍 게시글 좋아요/싫어요
                        .requestMatchers("/api/like/**").permitAll()

                        // 💬 댓글 좋아요/싫어요
                        .requestMatchers("/api/comment-like/**").permitAll()

                        // 🔥 댓글 API 실제 경로 허용
                        .requestMatchers("/api/coments/**").permitAll()

                        // 🔥 전체 API 허용 (fetch가 로그인 페이지로 안 튀게)
                        .requestMatchers("/api/**").permitAll()

                        .anyRequest().authenticated()
                )

                .formLogin(login -> login
                        .loginPage("/user/login")
                        .loginProcessingUrl("/user/login_proc")
                        .usernameParameter("userid")
                        .passwordParameter("userpwd")
                        .defaultSuccessUrl("/", true)
                        .failureHandler(new CustomHandler())
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )

                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
