package zerobase.dividend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@RequiredArgsConstructor
@Configuration
public class CacheConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    // Redis 비밀번호를 설정했을시
//    @Value("${spring.redis.password}")
//    private String password;

    // Redis 연결을 위한 RedisConnectionFactory 생성 -> 단순히 Redis 연결을 위한것
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 1. Redis 연결 설정정보 구성 (호스트, 포트 등)
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        // 싱글 인스턴스 서버 -> RedisStandaloneConfiguration 클래스 사용
        // 클러스터 서버 -> RedisClusterConfiguration 클래스 사용

        configuration.setHostName(this.host);
        configuration.setPort(this.port);
//        configuration.setPassword(this.password);   // Redis 비밀번호를 설정했을시

        // 2. 생성된 configuration 정보로 RedisConnectionFactory 생성
        return new LettuceConnectionFactory(configuration);
        // LettuceConnectionFactory 는 RedisConnectionFactory 인터페이스를 구현한 클래스 중 하나
    }

    /*
    Redis 는 캐시 관리 외에도 다양한 용도로 사용가능 -> 우리는 캐시 관리자로 사용할거야
    redisCacheManager() 메소드는
    redisConnectionFactory() 메소드에서 반환된 RedisConnectionFactory 를 사용하여
    Redis 캐시 관리자를 설정하는 메소드
     */
    @Bean
    public CacheManager redisCacheManger(RedisConnectionFactory redisConnectionFactory) {
        // 1. Redis Cache 설정정보 구성 (직렬화 방법 설정 등) / Redis 는 key - val
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
//                .entryTtl(Duration.ofDays(1));   // Redis 전체 데이터에 대한 TTL 설정

        // 2. 생성된 configuration 정보 + RedisConnectionFactory 로 RedisCacheManager 빌드해서 CacheManager 리턴
        return RedisCacheManager.RedisCacheManagerBuilder
                                .fromConnectionFactory(redisConnectionFactory)
                                .cacheDefaults(configuration)
                                .build();
    }

}

/*
< Redis 운영모드 & 솔루션 >
1. Standalone - 단일 인스턴스로 구성된 운영 모드. 하나의 서버에서 실행되며, 단일 프로세스에서 모든 데이터를 처리.
2. Cluster - 여러 개의 인스턴스로 구성된 분산 운영 모드. 데이터를 분산 저장. 데이터의 가용성과 확장성을 보장.
3. Sentinel - Redis 의 고가용성 솔루션. 마스터-슬레이브 구성. 마스터 장애시 슬레이브를 마스터로 선출하여 고가용성 보장.

< 내부는 오브젝트형 사용 / 외부는 공용형이 바이트 >
직렬화 : (자바) 오브젝트 -> 바이트 형태로
역직렬화 : 바이트 -> (자바) 오브젝트 형태로

Redis 는 자바 시스템 외부에 있는 캐시 서버
캐시데이터 저장하려면 직렬화 거쳐야
캐시 불러올때는 역직렬화 거쳐야
 */