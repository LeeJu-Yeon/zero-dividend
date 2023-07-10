package zerobase.dividend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        ThreadPoolTaskScheduler threadPool = new ThreadPoolTaskScheduler();   // 스레드 풀 생성

        int n = Runtime.getRuntime().availableProcessors();   // 코어 갯수
        threadPool.setPoolSize(n + 1);   // 스레드 풀 사이즈 지정
        threadPool.initialize();   // 스레드 풀 초기화

        taskRegistrar.setTaskScheduler(threadPool);   // 스레드 풀 사용해라

    }

}

/*
스케쥴러에서 실행되어야 할 기능이 여러개일 경우
Thread Pool 설정해줘야

스프링의 스케쥴러는 기본적으로 한 개의 Thread 로 동작
따라서, 한 작업이 수행되는동안 다른 작업 수행 x => 원하는 스케쥴에 실행 x

Thread Pool 의 적정 사이즈는
서비스가 실행되는 CPU 의 코어 갯수를 N 이라 하면
CPU 를 많이 쓰는 작업 -> N + 1 개의 스레드
I/O 작업이 많은 경우 -> N * 2 개의 스레드
 */