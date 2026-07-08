import { render, screen, waitFor } from "@testing-library/react";
import SecurityPage from "@/app/dashboard/security/page";
import { AuthProvider } from "@/contexts/auth-context";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
  usePathname: jest.fn(() => "/dashboard/security"),
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

const mockPasskeys = [
  {
    id: "pk-1",
    credentialId: "cred-abc",
    deviceName: "Windows Hello",
    aaguid: "00000000-0000-0000-0000-000000000000",
    credentialType: "platform",
    backedUp: true,
    createdAt: "2026-07-08T10:00:00Z",
    updatedAt: "2026-07-08T10:00:00Z",
  },
];

const mockSessions = [
  {
    id: "sess-1",
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
];

jest.mock("@/services/passkey-service", () => ({
  passkeyService: {
    list: jest.fn(),
  },
}));

jest.mock("@/services/session-service", () => ({
  sessionService: {
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

describe("SecurityPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it("renders the page title", async () => {
    renderWithProviders(<SecurityPage />);
    await waitFor(() => {
      expect(screen.getByText("Security")).toBeInTheDocument();
    });
  });

  it("renders security stat cards", async () => {
    const { list: listPasskeys } = await import(
      "@/services/passkey-service"
    ).then((m) => m.passkeyService);
    const { list: listSessions } = await import(
      "@/services/session-service"
    ).then((m) => m.sessionService);

    (listPasskeys as jest.Mock).mockResolvedValue(mockPasskeys);
    (listSessions as jest.Mock).mockResolvedValue(mockSessions);

    renderWithProviders(<SecurityPage />);
    await waitFor(() => {
      expect(screen.getByText("Total Passkeys")).toBeInTheDocument();
      expect(screen.getByText("Password Status")).toBeInTheDocument();
      expect(screen.getAllByText("Active Sessions").length).toBeGreaterThanOrEqual(1);
      expect(screen.getByText("Security Status")).toBeInTheDocument();
    });
  });

  it("shows recommendations when no passkeys", async () => {
    const { list: listPasskeys } = await import(
      "@/services/passkey-service"
    ).then((m) => m.passkeyService);
    const { list: listSessions } = await import(
      "@/services/session-service"
    ).then((m) => m.sessionService);

    (listPasskeys as jest.Mock).mockResolvedValue([]);
    (listSessions as jest.Mock).mockResolvedValue(mockSessions);

    renderWithProviders(<SecurityPage />);
    await waitFor(() => {
      expect(screen.getByText("Security Recommendations")).toBeInTheDocument();
    });
  });

  it("shows Good security status when passkeys exist", async () => {
    const { list: listPasskeys } = await import(
      "@/services/passkey-service"
    ).then((m) => m.passkeyService);
    const { list: listSessions } = await import(
      "@/services/session-service"
    ).then((m) => m.sessionService);

    (listPasskeys as jest.Mock).mockResolvedValue(mockPasskeys);
    (listSessions as jest.Mock).mockResolvedValue(mockSessions);

    renderWithProviders(<SecurityPage />);
    await waitFor(() => {
      expect(screen.getByText("Good")).toBeInTheDocument();
    });
  });
});
