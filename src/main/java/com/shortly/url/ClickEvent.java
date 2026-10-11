package com.shortly.url;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="click_events", indexes=@Index(name="idx_click_short_code", columnList="short_code"))
public class ClickEvent {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="short_code", nullable=false, length=32) private String shortCode;
    @Column(name="clicked_at", nullable=false) private Instant clickedAt;
    protected ClickEvent() {}
    public ClickEvent(String code, Instant at){shortCode=code;clickedAt=at;}
}
