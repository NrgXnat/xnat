package org.nrg.xapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@Configuration
@EnableWebMvc
@ComponentScan("org.nrg.xapi.rest")
public class WebConfig extends WebMvcConfigurerAdapter implements ApplicationContextAware, InitializingBean {
    @Bean
    public ViewResolver viewResolver() {
        return new InternalResourceViewResolver() {{
                setViewClass(JstlView.class);
                setPrefix("/WEB-INF/views/");
                setSuffix(".jsp");
            }};
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        return messageSource;
    }

    // Added to support Swagger requirements.
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void setApplicationContext(final ApplicationContext context) throws BeansException {
        _context = context;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        _log.debug(_context.getApplicationName());
    }

    private static final Logger _log = LoggerFactory.getLogger(WebConfig.class);
    private ApplicationContext _context;
}