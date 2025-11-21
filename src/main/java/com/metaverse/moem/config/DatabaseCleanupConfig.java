package com.metaverse.moem.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 데이터베이스 정리 컴포넌트
 * 애플리케이션 시작 시 team_invitations 같은 관리되지 않는 테이블을 정리합니다.
 */
@Slf4j
@Component
@Order(1) // 다른 초기화보다 먼저 실행
public class DatabaseCleanupConfig {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseCleanupConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void cleanupOrphanTables() {
        try {
            log.info("데이터베이스 정리 시작: team_invitations 테이블 확인 중...");
            
            // team_invitations 테이블 존재 여부 확인
            String checkTableSql = """
                SELECT COUNT(*) 
                FROM information_schema.tables 
                WHERE table_schema = DATABASE() 
                AND table_name = 'team_invitations'
                """;
            
            Integer tableExists = jdbcTemplate.queryForObject(checkTableSql, Integer.class);
            
            if (tableExists != null && tableExists > 0) {
                log.warn("team_invitations 테이블이 발견되었습니다. 정리 중...");
                
                // 외래키 제약조건 비활성화
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
                
                try {
                    // team_invitations 테이블 삭제
                    jdbcTemplate.execute("DROP TABLE IF EXISTS team_invitations");
                    log.info("team_invitations 테이블이 성공적으로 삭제되었습니다.");
                } finally {
                    // 외래키 제약조건 다시 활성화
                    jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
                }
            } else {
                log.info("team_invitations 테이블이 존재하지 않습니다. 정리 불필요.");
            }
        } catch (Exception e) {
            // 정리 실패해도 애플리케이션 시작은 계속 진행
            log.warn("데이터베이스 정리 중 오류 발생 (무시됨): {}", e.getMessage());
        }
    }
}

