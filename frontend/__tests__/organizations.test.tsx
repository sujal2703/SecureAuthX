import { render, screen, waitFor } from "@testing-library/react";
import OrganizationsPage from "@/app/dashboard/organizations/page";
import { AuthProvider } from "@/contexts/auth-context";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
  usePathname: jest.fn(() => "/dashboard/organizations"),
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

const mockOrgs = [
  {
    id: "1",
    name: "My Org",
    slug: "my-org",
    personal: false,
    role: "OWNER",
    createdAt: "2026-07-08T10:00:00Z",
  },
  {
    id: "2",
    name: "Personal",
    slug: "personal",
    personal: true,
    role: "MEMBER",
    createdAt: "2026-07-01T08:00:00Z",
  },
];

jest.mock("@/services/organization-service", () => ({
  organizationService: {
    list: jest.fn(),
    create: jest.fn(),
    update: jest.fn(),
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

describe("OrganizationsPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it("renders the page title", async () => {
    renderWithProviders(<OrganizationsPage />);
    await waitFor(() => {
      expect(screen.getByText("Organizations")).toBeInTheDocument();
    });
  });

  it("renders organization cards when loaded", async () => {
    const { list } = await import("@/services/organization-service").then(
      (m) => m.organizationService,
    );
    (list as jest.Mock).mockResolvedValue(mockOrgs);

    renderWithProviders(<OrganizationsPage />);
    await waitFor(() => {
      expect(screen.getByText("My Org")).toBeInTheDocument();
      expect(screen.getAllByText("Personal").length).toBeGreaterThanOrEqual(1);
    });
  });

  it("shows empty state when no organizations", async () => {
    const { list } = await import("@/services/organization-service").then(
      (m) => m.organizationService,
    );
    (list as jest.Mock).mockResolvedValue([]);

    renderWithProviders(<OrganizationsPage />);
    await waitFor(() => {
      expect(screen.getByText("No organizations")).toBeInTheDocument();
    });
  });

  it("shows error state on failure", async () => {
    const { list } = await import("@/services/organization-service").then(
      (m) => m.organizationService,
    );
    (list as jest.Mock).mockRejectedValue(new Error("API error"));

    renderWithProviders(<OrganizationsPage />);
    await waitFor(() => {
      expect(screen.getByText("Failed to load organizations")).toBeInTheDocument();
    });
  });

  it("shows personal badge for personal orgs", async () => {
    const { list } = await import("@/services/organization-service").then(
      (m) => m.organizationService,
    );
    (list as jest.Mock).mockResolvedValue(mockOrgs);

    renderWithProviders(<OrganizationsPage />);
    await waitFor(() => {
      expect(screen.getAllByText("Personal").length).toBe(2);
    });
  });
});
