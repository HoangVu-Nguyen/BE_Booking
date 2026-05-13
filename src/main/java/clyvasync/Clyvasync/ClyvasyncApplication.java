package clyvasync.Clyvasync;

import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableMethodCache(basePackages = "clyvasync.Clyvasync") // Thay bằng package của bạn
@EnableCreateCacheAnnotation
@EnableCaching
public class ClyvasyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClyvasyncApplication.class, args);
	}

}
