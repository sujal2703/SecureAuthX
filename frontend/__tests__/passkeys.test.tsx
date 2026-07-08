import { render, screen, waitFor } from "@testing-library/react";
import PasskeysPage from "@/app/dashboard/passkeys/page";
import { AuthProvider } from "@/contexts/auth-context";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(() => ({ push: jest.fn() })),
  usePathname: jest.fn(() => "/dashboard/passkeys"),
  useParams: jest.fn(() => ({})),
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
    credentialId: "cred-abc-123",
    deviceName: "Windows Hello",
    aaguid: "00000000-0000-0000-0000-000000000000",
    credentialType: "platform",
    backedUp: true,
    createdAt: "2026-07-08T10:00:00Z",
    updatedAt: "2026-07-08T10:00:00Z",
  },
  {
    id: "pk-2",
    credentialId: "cred-def-456",
    deviceName: "iPhone",
    aaguid: "00000000-0000-0000-0000-000000000001",
    credentialType: "cross-platform",
    backedUp: true,
    createdAt: "2026-07-07T08:00:00Z",
    updatedAt: "2026-07-07T08:00:00Z",
  },
];

jest.mock("@/services/passkey-service", () => ({
  passkeyService: {
    list: jest.fn(),
    delete: jest.fn(),
    registerOptions: jest.fn(),
    registerVerify: jest.fn(),
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

describe("PasskeysPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it("renders the page title", async () => {
    renderWithProviders(<PasskeysPage />);
    await waitFor(() => {
      expect(screen.getByText("Passkeys")).toBeInTheDocument();
    });
  });

  it("renders passkey cards when loaded", async () => {
    const { list } = await import("@/services/passkey-service").then(
      (m) => m.passkeyService,
    );
    (list as jest.Mock).mockResolvedValue(mockPasskeys);

    renderWithProviders(<PasskeysPage />);
    await waitFor(() => {
      expect(screen.getByText("Windows Hello")).toBeInTheDocument();
      expect(screen.getByText("iPhone")).toBeInTheDocument();
    });
  });

  it("shows total passkey count", async () => {
    const { list } = await import("@/services/passkey-service").then(
      (m) => m.passkeyService,
    );
    (list as jest.Mock).mockResolvedValue(mockPasskeys);

    renderWithProviders(<PasskeysPage />);
    await waitFor(() => {
      expect(screen.getByText("2")).toBeInTheDocument();
    });
  });

  it("shows empty state when no passkeys", async () => {
    const { list } = await import("@/services/passkey-service").then(
      (m) => m.passkeyService,
    );
    (list as jest.Mock).mockResolvedValue([]);

    renderWithProviders(<PasskeysPage />);
    await waitFor(() => {
      expect(screen.getByText("No passkeys registered")).toBeInTheDocument();
    });
  });

  it("shows synced badge for backed up passkeys", async () => {
    const { list } = await import("@/services/passkey-service").then(
      (m) => m.passkeyService,
    );
    (list as jest.Mock).mockResolvedValue(mockPasskeys);

    renderWithProviders(<PasskeysPage />);
    await waitFor(() => {
      expect(screen.getAllByText("Synced").length).toBeGreaterThanOrEqual(1);
    });
  });
});
