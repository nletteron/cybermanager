CREATE TABLE cybermanager.cm_session_display_preferences (
    user_email VARCHAR(255) NOT NULL,
    group_entries BOOLEAN NOT NULL,
    CONSTRAINT pk_cm_session_display_preferences PRIMARY KEY (user_email)
);
