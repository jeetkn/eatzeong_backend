package com.place;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.File;
import java.util.TimeZone;

@SpringBootApplication
@MapperScan(value={"com.place.service.mapper"})
@EnableAsync
public class MainStartClass {

    @PostConstruct
    void started() { TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul")); }

	public static void main(String[] args) {
//        System.setProperty("reactor.netty.ioWorkerCount", "1");
//        System.setProperty("reactor.ipc.netty.pool.maxConnections", "2000");
//        System.setProperty("server.tomcat.max-threads", "1");
        new File(System.getProperty("user.dir")+"/log").mkdirs();
        new File(System.getProperty("user.dir")+ "/image/review").mkdirs();
        new File(System.getProperty("user.dir")+ "/image/profile").mkdirs();
	    SpringApplication.run(MainStartClass.class, args);
	}

	
	 /*
     * SqlSessionFactory 설정 
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception{
        
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        
        sessionFactory.setDataSource(dataSource);
        return sessionFactory.getObject();
        
    }

//    @Bean
//    public ThreadPoolTaskExecutor myThreadPool() {
//        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
//        te.setCorePoolSize(10);
//        te.setMaxPoolSize(100);
//        te.setQueueCapacity(200);
//        te.initialize();
//        return te;
//    }
}
