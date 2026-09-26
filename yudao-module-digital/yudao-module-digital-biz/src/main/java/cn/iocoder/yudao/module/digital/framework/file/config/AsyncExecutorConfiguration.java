package cn.iocoder.yudao.module.digital.framework.file.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Copyright: Copyright (c) 2020
 *
 * @ClassName: SpringAsyncConfiguration
 * @Despriction: 线程以以及异步初始化
 * @author: yangyj3
 * @date: 2021/9/7 17:44
 */
@Data
@Configuration
@EnableAsync
@Slf4j
public class AsyncExecutorConfiguration {
    private static final int corePoolSize = 6;   // 核心线程数（默认线程数）
    private static final int maxPoolSize = 100;   // 最大线程数
    private static final int keepAliveTime = 60;  // 允许线程空闲时间（单位：默认为秒）
    private static final int queueCapacity = 500; // 缓冲队列数




    @Bean({"callPythonInterFaceExecutor"})
    public TaskExecutor callPythonInterFace() {
        log.debug("初始化callPythonInterFace异步线程池");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 设置核心线程数
        executor.setCorePoolSize(10);
        // 设置最大线程数
        executor.setMaxPoolSize(20);
        //线程池的队列容量 线程不足直接扩 否则需要等待 知道 队列满了才会加线程
        executor.setQueueCapacity(12);
        // 设置默认线程名称
        executor.setThreadNamePrefix("callPythonAsync");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //初始化
        executor.initialize();

        return executor;
    }
}
