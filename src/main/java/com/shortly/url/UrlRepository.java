package com.shortly.url;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<UrlRecord,Long> {
    Optional<UrlRecord> findByShortCode(String code);
}
