package com.cybermanager.infrastructure.out.entities.session;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cm_session_display_preferences")
public class SessionDisplayPreferenceJpaEntity {

    @Id
    @Column(name = "user_email", nullable = false, length = 255)
    public String userEmail;
    @Column(nullable = false)
    public boolean groupEntries;

}
