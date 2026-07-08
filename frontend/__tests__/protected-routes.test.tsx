import { render, screen, waitFor } from "@testing-library/react";
import { AuthProvider, useAuthContext } from "@/contexts/auth-context";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { authService } from "@/services/auth-service";

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
}));

jest.mock("@/services/auth-service", () => ({
  authService: {
    refresh: jest.fn(),
    logout: jest.fn(),
  },
}));

const mockPush = jest.fn();

function ProtectedComponent() {
  const { isAuthenticated, isLoading } = useAuthContext();
  if (isLoading) return <div data-testid="loading">Loading...</div>;
  if (!isAuthenticated) return <div data-testid="redirect">Redirecting...</div>;
  return <div data-testid="protected">Protected Content</div>;
}

function renderWithProviders(ui: React.ReactElement) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>{ui}</AuthProvider>
    </QueryClientProvider>,
  );
}

describe("Protected Routes", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
    (useRouter as jest.Mock).mockReturnValue({ push: mockPush });
  });



  it("redirects when user is not authenticated", async () => {
    renderWithProviders(<ProtectedComponent />);
    await waitFor(() => {
      expect(screen.getByTestId("redirect")).toBeInTheDocument();
    });
  });

  it("shows protected content when user has valid tokens", async () => {
    localStorage.setItem("accessToken", "valid-access-token");
    localStorage.setItem("refreshToken", "valid-refresh-token");
    (authService.refresh as jest.Mock).mockResolvedValueOnce({
      accessToken: "new-access-token",
      refreshToken: "new-refresh-token",
      expiresIn: 3600,
      tokenType: "Bearer",
    });
    renderWithProviders(<ProtectedComponent />);
    await waitFor(() => {
      expect(screen.getByTestId("protected")).toBeInTheDocument();
    });
  });

  it("redirects when token refresh fails", async () => {
    localStorage.setItem("accessToken", "expired-access-token");
    localStorage.setItem("refreshToken", "expired-refresh-token");
    (authService.refresh as jest.Mock).mockRejectedValueOnce(
      new Error("Refresh failed"),
    );
    renderWithProviders(<ProtectedComponent />);
    await waitFor(() => {
      expect(screen.getByTestId("redirect")).toBeInTheDocument();
    });
  });

  it("clears expired tokens and redirects on refresh failure", async () => {
    localStorage.setItem("accessToken", "expired-access-token");
    localStorage.setItem("refreshToken", "expired-refresh-token");
    (authService.refresh as jest.Mock).mockRejectedValueOnce(
      new Error("Refresh failed"),
    );
    renderWithProviders(<ProtectedComponent />);
    await waitFor(() => {
      expect(screen.getByTestId("redirect")).toBeInTheDocument();
      expect(localStorage.getItem("accessToken")).toBeNull();
      expect(localStorage.getItem("refreshToken")).toBeNull();
    });
  });
});
