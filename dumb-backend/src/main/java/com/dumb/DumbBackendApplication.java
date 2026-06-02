package com.dumb;

import com.dumb.config.AiProperties;
import com.dumb.config.LastFmProperties;
import com.dumb.config.TmdbProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({AiProperties.class, LastFmProperties.class, TmdbProperties.class})
public class DumbBackendApplication {

    public static void main(String[] args) {
        loadDotEnvLocal();
        SpringApplication.run(DumbBackendApplication.class, args);
    }

    private static void loadDotEnvLocal() {
        Path dotenv = Paths.get(System.getProperty("user.dir"), ".env.local");
        if (!Files.exists(dotenv)) {
            log.debug("未找到 .env.local 文件: {}", dotenv.toAbsolutePath());
            return;
        }
        try {
            int count = 0;
            for (String line : Files.readAllLines(dotenv)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                // 去掉可能的引号
                if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                // 仅在系统属性未设置时才注入（避免覆盖真正的环境变量）
                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                    count++;
                }
            }
            log.info("已从 .env.local 加载 {} 个配置项", count);
        } catch (IOException e) {
            log.warn("读取 .env.local 失败: {}", e.getMessage());
        }
    }
}
