type RuntimeConfig = {
  authApiUrl?: string;
  usersApiUrl?: string;
};

function runtimeConfig(): RuntimeConfig {
  if (typeof window === 'undefined') {
    return {};
  }
  return (window as Window & { __CM_CONFIG__?: RuntimeConfig }).__CM_CONFIG__ ?? {};
}

function normalizeApiUrl(url: string): string {
  return url.trim().replace(/^https:\/\/https:\/\//, 'https://').replace(/\/+$/, '');
}

const defaultAuthApiUrl = 'http://localhost:8081/api';
const defaultUsersApiUrl = 'http://localhost:8082/api';

export const environment = {
  authApiUrl: normalizeApiUrl(runtimeConfig().authApiUrl ?? defaultAuthApiUrl),
  usersApiUrl: normalizeApiUrl(runtimeConfig().usersApiUrl ?? defaultUsersApiUrl),
};
