package com.bian.niosocket.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@Log4j2
public class AsyncConfig extends AsyncConfigurerSupport {

    @Value("${thread-pool.core-size}")
    public int corePoolSize = 8;
    @Value("${thread-pool.max-size}")
    public int maxPoolSize = 12;
    @Value("${thread-pool.queue-capacity}")
    public int queueCapacity = 999999;
    @Value("${thread-pool.keep-alive-seconds}")
    public int keepAliveSeconds = 60;
    @Value("${thread-pool.thread-name-prefix}")
    public String threadNamePrefix = "async-service-";
    @Value("${thread-pool.await-termination-seconds}")
    public int awaitTerminationSeconds = 20;
    @Value("${thread-pool.wait-for-jobs-to-complete-on-shutdown}")
    public boolean waitForJobsToCompleteOnShutdown = true;

    @Override
    public Executor getAsyncExecutor() {
        return asyncExecutor();
    }

    @Bean
    public ThreadPoolTaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        //许的空闲时间,当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix(threadNamePrefix);
        //线程池的关闭策略
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        executor.setWaitForTasksToCompleteOnShutdown(waitForJobsToCompleteOnShutdown);
        /*
         * 当线程池的任务缓存队列已满并且线程池中的线程数目达到maximumPoolSize，如果还有任务到来就会采取任务拒绝策略
         * 通常有以下四种策略：
         * ThreadPoolExecutor.AbortPolicy:丢弃任务并抛出RejectedExecutionException异常。
         * ThreadPoolExecutor.DiscardPolicy：也是丢弃任务，但是不抛出异常。
         * ThreadPoolExecutor.DiscardOldestPolicy：丢弃队列最前面的任务，然后重新尝试执行任务（重复此过程）
         * ThreadPoolExecutor.CallerRunsPolicy：重试添加当前的任务，自动重复调用 execute() 方法，直到成功
         */
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> log.warn(String.format("异步方法[%s]错误, params: %s", method, Arrays.toString(params)), ex);
    }
}

