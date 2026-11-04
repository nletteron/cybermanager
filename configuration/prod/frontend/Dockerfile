# syntax=docker/dockerfile:1
FROM node:24-alpine AS build
RUN npm install --global npm@11.9.0
WORKDIR /workspace/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci
COPY frontend/ ./
RUN npm run build

FROM nginx:stable-alpine
RUN apk add --no-cache jq
COPY --from=build /workspace/dist/cybermanager-frontend/browser/ /usr/share/nginx/html/
COPY configuration/prod/frontend/default.conf /etc/nginx/conf.d/default.conf
COPY configuration/prod/frontend/40-runtime-config.sh /docker-entrypoint.d/40-runtime-config.sh
RUN sed -i 's/\r$//' /docker-entrypoint.d/40-runtime-config.sh && \
    chmod +x /docker-entrypoint.d/40-runtime-config.sh
ENV CM_AUTH_API_URL=/auth-api \
    CM_API_URL=/api
EXPOSE 80
HEALTHCHECK --interval=10s --timeout=5s --start-period=10s --retries=3 CMD wget -q -O /dev/null http://127.0.0.1/health
