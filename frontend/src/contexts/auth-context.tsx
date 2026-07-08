"use client";

import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  type ReactNode,
} from "react";
import { authService } from "@/services/auth-service";
import { profileService } from "@/services/profile-service";
import { clearTokens, setTokens } from "@/lib/api";
import type { User } from "@/types/auth";

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

interface AuthContextType extends AuthState {
  login: (email: string, password: string) => Promise<void>;
  register: (
    email: string,
    password: string,
    firstName: string,
    lastName: string,
  ) => Promise<void>;
  logout: () => Promise<void>;
  fetchProfile: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

function userFromInfo(info: { sub: string; email: string; given_name?: string; family_name?: string }): User {
  return {
    id: info.sub,
    email: info.email,
    firstName: info.given_name || "",
    lastName: info.family_name || "",
    roles: [],
  };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({
    user: null,
    isAuthenticated: false,
    isLoading: true,
  });

  const fetchProfile = useCallback(async () => {
    try {
      const info = await profileService.getUserInfo();
      if (info) {
        setState((prev) => ({
          ...prev,
          user: userFromInfo(info),
        }));
      }
    } catch {
      // profile fetch is non-critical
    }
  }, []);

  const checkAuth = useCallback(async () => {
    const storedAccess = localStorage.getItem("accessToken");
    const storedRefresh = localStorage.getItem("refreshToken");

    if (!storedAccess || !storedRefresh) {
      setState({ user: null, isAuthenticated: false, isLoading: false });
      return;
    }

    setTokens(storedAccess, storedRefresh);

    try {
      const authData = await authService.refresh();
      localStorage.setItem("accessToken", authData.accessToken);
      localStorage.setItem("refreshToken", authData.refreshToken);
      setState({
        user: { id: "", email: "", firstName: "", lastName: "", roles: [] },
        isAuthenticated: true,
        isLoading: false,
      });
      fetchProfile();
    } catch {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      clearTokens();
      setState({ user: null, isAuthenticated: false, isLoading: false });
    }
  }, [fetchProfile]);

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  const login = useCallback(
    async (email: string, password: string) => {
      const authData = await authService.login({ email, password });
      localStorage.setItem("accessToken", authData.accessToken);
      localStorage.setItem("refreshToken", authData.refreshToken);
      setState({
        user: { id: "", email, firstName: "", lastName: "", roles: [] },
        isAuthenticated: true,
        isLoading: false,
      });
      fetchProfile();
    },
    [fetchProfile],
  );

  const register = useCallback(
    async (
      email: string,
      password: string,
      firstName: string,
      lastName: string,
    ) => {
      const authData = await authService.register({
        email,
        password,
        firstName,
        lastName,
      });
      localStorage.setItem("accessToken", authData.accessToken);
      localStorage.setItem("refreshToken", authData.refreshToken);
      setState({
        user: {
          id: "",
          email,
          firstName,
          lastName,
          roles: [],
        },
        isAuthenticated: true,
        isLoading: false,
      });
      fetchProfile();
    },
    [fetchProfile],
  );

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } finally {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      clearTokens();
      setState({ user: null, isAuthenticated: false, isLoading: false });
    }
  }, []);

  return (
    <AuthContext.Provider
      value={{ ...state, login, register, logout, fetchProfile }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuthContext() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuthContext must be used within an AuthProvider");
  }
  return context;
}
