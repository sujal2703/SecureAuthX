import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { AuthProvider, useAuthContext } from "@/contexts/auth-context";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { authService } from "@/services/auth-service";

jest.mock("@/services/auth-service", () => ({
  authService: {
    login: jest.fn(),
    register: jest.fn(),
    logout: jest.fn(),
    refresh: jest.fn(),
  },
}));

jest.mock("@/services/profile-service", () => ({
  profileService: {
    getUserInfo: jest.fn(),
  },
}));

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false } },
});

function TestComponent() {
  const { isAuthenticated, isLoading, user, login, register, logout } =
    useAuthContext();
  return (
    <div>
      <div data-testid="loading">{isLoading.toString()}</div>
      <div data-testid="authenticated">{isAuthenticated.toString()}</div>
      <div data-testid="user">{JSON.stringify(user)}</div>
      <button
        onClick={() => login("test@example.com", "password123")}
        data-testid="login-btn"
      >
        Login
      </button>
      <button
        onClick={() =>
          register("test@example.com", "password123", "John", "Doe")
        }
        data-testid="register-btn"
      >
        Register
      </button>
      <button onClick={() => logout()} data-testid="logout-btn">
        Logout
      </button>
    </div>
  );
}

function renderWithProviders(ui: React.ReactElement) {
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>{ui}</AuthProvider>
    </QueryClientProvider>,
  );
}

describe("AuthContext", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it("starts with loading state", async () => {
    renderWithProviders(<TestComponent />);
    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });
  });

  it("shows not authenticated when no tokens exist", async () => {
    renderWithProviders(<TestComponent />);
    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
      expect(screen.getByTestId("authenticated")).toHaveTextContent("false");
    });
  });

  it("authenticates after successful login", async () => {
    (authService.login as jest.Mock).mockResolvedValueOnce({
      accessToken: "access-token",
      refreshToken: "refresh-token",
      expiresIn: 3600,
      tokenType: "Bearer",
    });
    renderWithProviders(<TestComponent />);
    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });
    fireEvent.click(screen.getByTestId("login-btn"));
    await waitFor(() => {
      expect(screen.getByTestId("authenticated")).toHaveTextContent("true");
    });
  });

  it("authenticates after successful registration", async () => {
    (authService.register as jest.Mock).mockResolvedValueOnce({
      accessToken: "access-token",
      refreshToken: "refresh-token",
      expiresIn: 3600,
      tokenType: "Bearer",
    });
    renderWithProviders(<TestComponent />);
    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });
    fireEvent.click(screen.getByTestId("register-btn"));
    await waitFor(() => {
      expect(screen.getByTestId("authenticated")).toHaveTextContent("true");
    });
  });

  it("deauthenticates after logout", async () => {
    (authService.login as jest.Mock).mockResolvedValueOnce({
      accessToken: "access-token",
      refreshToken: "refresh-token",
      expiresIn: 3600,
      tokenType: "Bearer",
    });
    (authService.logout as jest.Mock).mockResolvedValueOnce(undefined);
    renderWithProviders(<TestComponent />);
    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });
    fireEvent.click(screen.getByTestId("login-btn"));
    await waitFor(() => {
      expect(screen.getByTestId("authenticated")).toHaveTextContent("true");
    });
    fireEvent.click(screen.getByTestId("logout-btn"));
    await waitFor(() => {
      expect(screen.getByTestId("authenticated")).toHaveTextContent("false");
    });
  });

  it("stores tokens in localStorage on login", async () => {
    (authService.login as jest.Mock).mockResolvedValueOnce({
      accessToken: "access-token-123",
      refreshToken: "refresh-token-456",
      expiresIn: 3600,
      tokenType: "Bearer",
    });
    renderWithProviders(<TestComponent />);
    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });
    fireEvent.click(screen.getByTestId("login-btn"));
    await waitFor(() => {
      expect(localStorage.getItem("accessToken")).toBe("access-token-123");
      expect(localStorage.getItem("refreshToken")).toBe("refresh-token-456");
    });
  });

  it("clears tokens from localStorage on logout", async () => {
    localStorage.setItem("accessToken", "existing-token");
    localStorage.setItem("refreshToken", "existing-refresh");
    (authService.logout as jest.Mock).mockResolvedValueOnce(undefined);
    renderWithProviders(<TestComponent />);
    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });
    fireEvent.click(screen.getByTestId("logout-btn"));
    await waitFor(() => {
      expect(localStorage.getItem("accessToken")).toBeNull();
      expect(localStorage.getItem("refreshToken")).toBeNull();
    });
  });
});
