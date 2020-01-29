package com.place;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@MapperScan(value={"com.place.service.mapper"})
@EnableAsync
public class MainStartClass {

	public static void main(String[] args) {
        System.setProperty("reactor.netty.ioWorkerCount", "1");
        System.setProperty("reactor.ipc.netty.pool.maxConnections", "2000");
        System.setProperty("server.port", "8081");
        System.setProperty("server.tomcat.max-threads", "1");
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

    @Bean
    public ThreadPoolTaskExecutor myThreadPool() {
        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
        te.setCorePoolSize(10);
        te.setMaxPoolSize(100);
        te.initialize();
        return te;
    }
}
