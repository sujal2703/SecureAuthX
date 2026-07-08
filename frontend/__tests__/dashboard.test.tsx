import { render, screen, waitFor } from "@testing-library/react";
import DashboardPage from "@/app/dashboard/page";
import { AuthProvider } from "@/contexts/auth-context";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
  usePathname: jest.fn(() => "/dashboard"),
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

jest.mock("@/services/session-service", () => ({
  sessionService: {
    list: jest.fn(),
  },
}));

jest.mock("@/services/passkey-service", () => ({
  passkeyService: {
    list: jest.fn(),
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

describe("DashboardPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it("renders the welcome section", async () => {
    renderWithProviders(<DashboardPage />);
    await waitFor(() => {
      expect(screen.getByText(/welcome/i)).toBeInTheDocument();
    });
    expect(
      screen.getByText(/overview of your account/i),
    ).toBeInTheDocument();
  });

  it("renders stat cards", async () => {
    renderWithProviders(<DashboardPage />);
    await waitFor(() => {
      expect(screen.getByText("Account Status")).toBeInTheDocument();
      expect(screen.getByText("Active Sessions")).toBeInTheDocument();
      expect(screen.getByText("Registered Devices")).toBeInTheDocument();
      expect(screen.getAllByText("Security Status").length).toBeGreaterThanOrEqual(1);
    });
  });

  it("renders quick action links", async () => {
    renderWithProviders(<DashboardPage />);
    await waitFor(() => {
      expect(screen.getByText("View Profile")).toBeInTheDocument();
      expect(screen.getByText("Manage Sessions")).toBeInTheDocument();
      expect(screen.getByText("Manage Devices")).toBeInTheDocument();
    });
  });

  it("renders user information section", async () => {
    renderWithProviders(<DashboardPage />);
    await waitFor(() => {
      expect(screen.getByText("User Information")).toBeInTheDocument();
    });
  });
});
