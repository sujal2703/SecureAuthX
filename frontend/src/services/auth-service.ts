import api, { setTokens, clearTokens } from "@/lib/api";
import type {
  RegisterRequest,
  LoginRequest,
  AuthResponse,
} from "@/types/auth";

export const authService = {
  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>(
      "/api/v1/auth/register",
      data,
    );
    const authData = response.data;
    setTokens(authData.accessToken, authData.refreshToken);
    return authData;
  },

  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>(
      "/api/v1/auth/login",
      data,
    );
    const authData = response.data;
    setTokens(authData.accessToken, authData.refreshToken);
    return authData;
  },

  async logout(): Promise<void> {
    try {
      await api.post("/api/v1/auth/logout");
    } finally {
      clearTokens();
    }
  },

  async refresh(): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>(
      "/api/v1/auth/refresh",
    );
    const authData = response.data;
    setTokens(authData.accessToken, authData.refreshToken);
    return authData;
  },
};
