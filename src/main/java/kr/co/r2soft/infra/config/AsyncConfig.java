package kr.co.r2soft.infra.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;

@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        int processor = Runtime.getRuntime().availableProcessors();
        log.info("available processor is {}", processor);
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(1024);
        return new ThreadPoolExecutor(processor, processor * 2, 60, TimeUnit.SECONDS, workQueue);
    }
}
