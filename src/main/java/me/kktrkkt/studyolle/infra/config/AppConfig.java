package me.kktrkkt.studyolle.infra.config;

import lombok.RequiredArgsConstructor;
import me.kktrkkt.studyolle.modules.notification.NotificationInterceptor;
import me.kktrkkt.studyolle.modules.notification.NotificationRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AppConfig implements WebMvcConfigurer {

    private final NotificationRepository notificationRepository;

    @Bean
    public ModelMapper modelMapper(){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true);
        return modelMapper;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new NotificationInterceptor(notificationRepository))
                .excludePathPatterns("/resources/**");
    }
}
