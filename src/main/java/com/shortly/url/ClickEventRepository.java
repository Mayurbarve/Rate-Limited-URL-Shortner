package com.shortly.url;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClickEventRepository extends JpaRepository<ClickEvent,Long> {
    @Query("select count(c) from ClickEvent c where c.shortCode = :code") long countByCode(@Param("code") String code);
}
