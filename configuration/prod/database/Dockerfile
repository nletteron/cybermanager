FROM postgres:16-alpine

ENV TZ=Europe/Paris

RUN apk add --no-cache tzdata bash && \
    cp /usr/share/zoneinfo/${TZ} /etc/localtime && \
    echo "${TZ}" > /etc/timezone

COPY postgresql.conf /etc/postgresql/postgresql.conf
COPY pg_hba.conf /etc/postgresql/pg_hba.conf
COPY initdb/ /docker-entrypoint-initdb.d/

RUN sed -i 's/\r$//' /docker-entrypoint-initdb.d/*.sh && \
    chmod +x /docker-entrypoint-initdb.d/*.sh && \
    mkdir -p /var/lib/postgresql/dumps && \
    chown postgres:postgres /var/lib/postgresql/dumps

VOLUME ["/var/lib/postgresql/data", "/var/lib/postgresql/dumps"]

EXPOSE 5432

HEALTHCHECK --interval=10s --timeout=5s --start-period=15s --retries=10 CMD pg_isready -h 127.0.0.1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"

CMD ["postgres", "-c", "config_file=/etc/postgresql/postgresql.conf"]
