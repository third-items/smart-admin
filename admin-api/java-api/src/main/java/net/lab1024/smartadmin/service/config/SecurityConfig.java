package net.lab1024.smartadmin.service.config;

import net.lab1024.smartadmin.service.filter.SmartSecurityTokenFilter;
import net.lab1024.smartadmin.service.common.security.SmartSecurityUrlMatchers;
import net.lab1024.smartadmin.service.handler.AuthenticationFailHandler;
import net.lab1024.smartadmin.service.module.system.login.EmployeeLoginTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Spring Security
 *
 * @author 罗伊
 * @date 2021/8/3 17:50
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${access-control-allow-origin}")
    private String accessControlAllowOrigin;
    /**
     * 认证失败处理类
     */
    @Autowired
    private AuthenticationFailHandler authenticationFailHandler;
    /**
     * url
     */
    @Autowired
    private SmartSecurityUrlMatchers smartSecurityUrlMatchers;

    /**
     * 获取TOKEN 解析类
     */
    @Autowired
    private EmployeeLoginTokenService loginTokenService;
    /**
     * 跨域配置
     *
     * @return
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // 设置访问源地址
        config.addAllowedOriginPattern(accessControlAllowOrigin);
        // 设置访问源请求头
        config.addAllowedHeader("*");
        // 设置访问源请求方法
        config.addAllowedMethod("*");
        // 对接口配置跨域设置
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry interceptUrlRegistry = httpSecurity
                // CSRF禁用，因为不使用session
                .csrf().disable()
                // 认证失败处理类
                .exceptionHandling().authenticationEntryPoint(authenticationFailHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                // 过滤请求
                .authorizeRequests();
        //可以匿名登录的URL
        String [] anonymousUrlArray = smartSecurityUrlMatchers.getAnonymousUrlArray();
        interceptUrlRegistry.antMatchers(anonymousUrlArray).permitAll();

        //登录的URL
        String [] authenticatedUrlArray = smartSecurityUrlMatchers.getAuthenticatedUrlArray();
        interceptUrlRegistry.antMatchers(authenticatedUrlArray).authenticated();

        httpSecurity.addFilterBefore(new SmartSecurityTokenFilter(loginTokenService), UsernamePasswordAuthenticationFilter.class);
        httpSecurity.addFilterBefore(corsFilter(), SmartSecurityTokenFilter.class);
    }

    @Override
    public void configure(WebSecurity web) {
        // 忽略url
        WebSecurity.IgnoredRequestConfigurer ignoring = web.ignoring();
        List<String> ignoreUrlListList = smartSecurityUrlMatchers.getIgnoreUrlList();
        for (String url : ignoreUrlListList) {
            ignoring.antMatchers(url);
        }
    }


}
