package zxf.trace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * Spring Boot应用程序入口类
 * 
 * 这个类是Spring Boot应用程序的主入口点，负责启动整个应用程序。
 * 该应用程序实现了HTTP请求和响应的跟踪日志功能，包括入站和出站请求的日志记录。
 */
@SpringBootApplication
public class TraceApplication {

    /**
     * 应用程序主方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(TraceApplication.class, args);
    }
}
