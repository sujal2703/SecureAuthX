import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import OAuthClientsPage from "@/app/dashboard/oauth-clients/page";
import OAuthClientDetailPage from "@/app/dashboard/oauth-clients/[id]/page";
import NewOAuthClientPage from "@/app/dashboard/oauth-clients/new/page";
import { AuthProvider } from "@/contexts/auth-context";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

const mockRouterPush = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(() => ({ push: mockRouterPush })),
  usePathname: jest.fn(() => "/dashboard/oauth-clients"),
  useParams: jest.fn(() => ({ id: "client-1" })),
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

const mockClients = [
  {
    id: "client-1",
    clientId: "my-app",
    clientName: "My Application",
    confidential: true,
    enabled: true,
    redirectUris: ["https://example.com/callback"],
    createdAt: "2026-07-08T10:00:00Z",
  },
  {
    id: "client-2",
    clientId: "public-app",
    clientName: "Public App",
    confidential: false,
    enabled: false,
    redirectUris: [],
    createdAt: "2026-07-07T08:00:00Z",
  },
];

jest.mock("@/services/oauth-client-service", () => ({
  oauthClientService: {
    list: jest.fn(),
    get: jest.fn(),
    create: jest.fn(),
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

describe("OAuthClientsPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it("renders the page title", async () => {
    renderWithProviders(<OAuthClientsPage />);
    await waitFor(() => {
      expect(screen.getByText("OAuth Clients")).toBeInTheDocument();
    });
  });

  it("renders client cards when loaded", async () => {
    const { list } = await import("@/services/oauth-client-service").then(
      (m) => m.oauthClientService,
    );
    (list as jest.Mock).mockResolvedValue(mockClients);

    renderWithProviders(<OAuthClientsPage />);
    await waitFor(() => {
      expect(screen.getByText("My Application")).toBeInTheDocument();
      expect(screen.getByText("Public App")).toBeInTheDocument();
    });
  });

  it("shows empty state when no clients", async () => {
    const { list } = await import("@/services/oauth-client-service").then(
      (m) => m.oauthClientService,
    );
    (list as jest.Mock).mockResolvedValue([]);

    renderWithProviders(<OAuthClientsPage />);
    await waitFor(() => {
      expect(screen.getByText("No OAuth clients")).toBeInTheDocument();
    });
  });

  it("shows admin gate when 403", async () => {
    const { list } = await import("@/services/oauth-client-service").then(
      (m) => m.oauthClientService,
    );
    (list as jest.Mock).mockRejectedValue({ response: { status: 403 } });

    renderWithProviders(<OAuthClientsPage />);
    await waitFor(() => {
      expect(screen.getByText("Admin access required")).toBeInTheDocument();
    });
  });

  it("filters clients by search", async () => {
    const { list } = await import("@/services/oauth-client-service").then(
      (m) => m.oauthClientService,
    );
    (list as jest.Mock).mockResolvedValue(mockClients);

    renderWithProviders(<OAuthClientsPage />);
    await waitFor(() => {
      expect(screen.getByText("My Application")).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText("Search clients...");
    fireEvent.change(searchInput, { target: { value: "Public" } });

    await waitFor(() => {
      expect(screen.queryByText("My Application")).not.toBeInTheDocument();
      expect(screen.getByText("Public App")).toBeInTheDocument();
    });
  });
});

describe("OAuthClientDetailPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it("renders client detail when loaded", async () => {
    const { get } = await import("@/services/oauth-client-service").then(
      (m) => m.oauthClientService,
    );
    (get as jest.Mock).mockResolvedValue(mockClients[0]);

    renderWithProviders(<OAuthClientDetailPage />);
    await waitFor(() => {
      expect(screen.getAllByText("My Application").length).toBeGreaterThanOrEqual(1);
      expect(screen.getAllByText("my-app").length).toBeGreaterThanOrEqual(1);
    });
  });

  it("shows error state on failure", async () => {
    const { get } = await import("@/services/oauth-client-service").then(
      (m) => m.oauthClientService,
    );
    (get as jest.Mock).mockRejectedValue(new Error("API error"));

    renderWithProviders(<OAuthClientDetailPage />);
    await waitFor(() => {
      expect(screen.getByText("Failed to load OAuth client")).toBeInTheDocument();
    });
  });

  it("shows redirect URIs for confidential clients", async () => {
    const { get } = await import("@/services/oauth-client-service").then(
      (m) => m.oauthClientService,
    );
    (get as jest.Mock).mockResolvedValue(mockClients[0]);

    renderWithProviders(<OAuthClientDetailPage />);
    await waitFor(() => {
      expect(screen.getByText("Redirect URIs")).toBeInTheDocument();
      expect(
        screen.getByText("https://example.com/callback"),
      ).toBeInTheDocument();
    });
  });
});

describe("NewOAuthClientPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it("renders the create form", async () => {
    renderWithProviders(<NewOAuthClientPage />);
    await waitFor(() => {
      expect(screen.getByText("New OAuth Client")).toBeInTheDocument();
      expect(screen.getByLabelText("Client Name")).toBeInTheDocument();
      expect(screen.getByLabelText("Client ID")).toBeInTheDocument();
    });
  });
});
