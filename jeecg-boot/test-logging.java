import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLogging {
    private static final Logger log = LoggerFactory.getLogger(TestLogging.class);
    
    public static void main(String[] args) {
        System.out.println("测试日志配置...");
        
        log.info("这是一条INFO级别的日志");
        log.warn("这是一条WARN级别的日志");
        log.error("这是一条ERROR级别的日志");
        
        System.out.println("日志测试完成，请检查logs目录");
    }
}
