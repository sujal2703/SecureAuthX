import { render, screen, waitFor } from "@testing-library/react";
import SessionsPage from "@/app/dashboard/sessions/page";
import { AuthProvider } from "@/contexts/auth-context";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
  usePathname: jest.fn(() => "/dashboard/sessions"),
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

const mockSessions = [
  {
    id: "1",
    createdAt: "2026-07-08T10:00:00Z",
    lastActivityAt: "2026-07-08T12:00:00Z",
    expiresAt: "2026-07-15T10:00:00Z",
    revoked: false,
    expired: false,
    ipAddress: "192.168.1.1",
    deviceName: "Windows PC",
    operatingSystem: "Windows 10",
    browser: "Chrome 120",
    current: true,
  },
  {
    id: "2",
    createdAt: "2026-07-07T08:00:00Z",
    lastActivityAt: "2026-07-07T20:00:00Z",
    expiresAt: "2026-07-14T08:00:00Z",
    revoked: false,
    expired: false,
    ipAddress: "10.0.0.1",
    deviceName: "MacBook Pro",
    operatingSystem: "macOS",
    browser: "Safari 17",
    current: false,
  },
];

jest.mock("@/services/session-service", () => ({
  sessionService: {
    list: jest.fn(),
    revoke: jest.fn(),
    revokeAll: jest.fn(),
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

describe("SessionsPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it("renders the sessions page title", async () => {
    renderWithProviders(<SessionsPage />);
    await waitFor(() => {
      expect(screen.getByText("Sessions")).toBeInTheDocument();
    });
  });

  it("renders session cards when sessions are loaded", async () => {
    const { list } = await import("@/services/session-service").then(
      (m) => m.sessionService,
    );
    (list as jest.Mock).mockResolvedValue(mockSessions);

    renderWithProviders(<SessionsPage />);
    await waitFor(() => {
      expect(screen.getByText("Windows PC")).toBeInTheDocument();
      expect(screen.getByText("MacBook Pro")).toBeInTheDocument();
    });
  });

  it("shows empty state when no sessions", async () => {
    const { list } = await import("@/services/session-service").then(
      (m) => m.sessionService,
    );
    (list as jest.Mock).mockResolvedValue([]);

    renderWithProviders(<SessionsPage />);
    await waitFor(() => {
      expect(screen.getByText("No active sessions")).toBeInTheDocument();
    });
  });

  it("shows current session badge", async () => {
    const { list } = await import("@/services/session-service").then(
      (m) => m.sessionService,
    );
    (list as jest.Mock).mockResolvedValue(mockSessions);

    renderWithProviders(<SessionsPage />);
    await waitFor(() => {
      expect(screen.getByText("Current")).toBeInTheDocument();
    });
  });
});
