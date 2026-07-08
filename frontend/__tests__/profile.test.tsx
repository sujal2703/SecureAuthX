import { render, screen, waitFor } from "@testing-library/react";
import ProfilePage from "@/app/dashboard/profile/page";
import { AuthProvider } from "@/contexts/auth-context";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
  usePathname: jest.fn(() => "/dashboard/profile"),
}));

jest.mock("@/services/auth-service", () => ({
  authService: {
    refresh: jest.fn(),
    logout: jest.fn(),
  },
}));

jest.mock("@/services/profile-service", () => ({
  profileService: {
    getUserInfo: jest.fn(),
  },
}));

jest.mock("@/services/organization-service", () => ({
  organizationService: {
    getCurrent: jest.fn(),
  },
}));

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false } },
});

function renderWithProviders(ui: React.ReactElement) {
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>{ui}</AuthProvider>
    </QueryClientProvider>,
  );
}

describe("ProfilePage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it("renders the profile page title", async () => {
    renderWithProviders(<ProfilePage />);
    await waitFor(() => {
      expect(screen.getByText("Profile")).toBeInTheDocument();
    });
    expect(
      screen.getByText(/account information/i),
    ).toBeInTheDocument();
  });

  it("renders account details section", async () => {
    renderWithProviders(<ProfilePage />);
    await waitFor(() => {
      expect(screen.getByText("Account Details")).toBeInTheDocument();
    });
  });

  it("renders organization section", async () => {
    renderWithProviders(<ProfilePage />);
    await waitFor(() => {
      expect(screen.getByText("Organization")).toBeInTheDocument();
    });
  });

  it("displays user email when available", async () => {
    localStorage.setItem("accessToken", "test-token");
    localStorage.setItem("refreshToken", "test-refresh");
    const { refresh } = await import("@/services/auth-service").then(
      (m) => m.authService,
    );
    (refresh as jest.Mock).mockResolvedValue({
      accessToken: "new-token",
      refreshToken: "new-refresh",
      expiresIn: 3600,
      tokenType: "Bearer",
    });
    renderWithProviders(<ProfilePage />);
    await waitFor(() => {
      expect(screen.getByText("Email")).toBeInTheDocument();
    });
  });
});
