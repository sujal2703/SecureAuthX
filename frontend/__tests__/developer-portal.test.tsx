import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import DeveloperDashboardPage from "@/app/dashboard/developer/page";
import ProjectsPage from "@/app/dashboard/developer/projects/page";
import NewProjectPage from "@/app/dashboard/developer/projects/new/page";
import { AuthProvider } from "@/contexts/auth-context";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(() => ({ push: jest.fn() })),
  usePathname: jest.fn(() => "/dashboard/developer"),
  useParams: jest.fn(() => ({ id: "project-1" })),
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

const mockProjects = [
  {
    id: "project-1",
    userId: "user-1",
    name: "My API Project",
    description: "A sample project",
    oauthClientId: null,
    enabled: true,
    createdAt: "2026-07-08T10:00:00Z",
    updatedAt: "2026-07-08T10:00:00Z",
  },
  {
    id: "project-2",
    userId: "user-1",
    name: "Mobile Backend",
    description: "",
    oauthClientId: null,
    enabled: true,
    createdAt: "2026-07-07T08:00:00Z",
    updatedAt: "2026-07-07T08:00:00Z",
  },
];

jest.mock("@/services/developer-service", () => ({
  developerService: {
    listProjects: jest.fn(),
    getProject: jest.fn(),
    createProject: jest.fn(),
    deleteProject: jest.fn(),
    listApiKeys: jest.fn(),
    createApiKey: jest.fn(),
    revokeApiKey: jest.fn(),
    getUsage: jest.fn(),
    getRateLimits: jest.fn(),
    setRateLimits: jest.fn(),
    deleteRateLimits: jest.fn(),
    rotateSecret: jest.fn(),
  },
}));

jest.mock("@/services/oauth-client-service", () => ({
  oauthClientService: {
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

describe("DeveloperDashboardPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it("renders the page title", async () => {
    renderWithProviders(<DeveloperDashboardPage />);
    await waitFor(() => {
      expect(screen.getByText("Developer Portal")).toBeInTheDocument();
    });
  });

  it("renders stat cards when loaded", async () => {
    const { listProjects } = await import("@/services/developer-service").then(
      (m) => m.developerService,
    );
    (listProjects as jest.Mock).mockResolvedValue(mockProjects);

    renderWithProviders(<DeveloperDashboardPage />);
    await waitFor(() => {
      expect(screen.getByText("Total Projects")).toBeInTheDocument();
      expect(screen.getByText("API Keys")).toBeInTheDocument();
      expect(screen.getByText("Rate Limit Status")).toBeInTheDocument();
    });
  });

  it("shows empty state when no projects", async () => {
    const { listProjects } = await import("@/services/developer-service").then(
      (m) => m.developerService,
    );
    (listProjects as jest.Mock).mockResolvedValue([]);

    renderWithProviders(<DeveloperDashboardPage />);
    await waitFor(() => {
      expect(screen.getByText("No projects yet")).toBeInTheDocument();
    });
  });
});

describe("ProjectsPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it("renders the page title", async () => {
    renderWithProviders(<ProjectsPage />);
    await waitFor(() => {
      expect(screen.getByText("Projects")).toBeInTheDocument();
    });
  });

  it("renders project cards when loaded", async () => {
    const { listProjects } = await import("@/services/developer-service").then(
      (m) => m.developerService,
    );
    (listProjects as jest.Mock).mockResolvedValue(mockProjects);

    renderWithProviders(<ProjectsPage />);
    await waitFor(() => {
      expect(screen.getByText("My API Project")).toBeInTheDocument();
      expect(screen.getByText("Mobile Backend")).toBeInTheDocument();
    });
  });

  it("shows empty state when no projects", async () => {
    const { listProjects } = await import("@/services/developer-service").then(
      (m) => m.developerService,
    );
    (listProjects as jest.Mock).mockResolvedValue([]);

    renderWithProviders(<ProjectsPage />);
    await waitFor(() => {
      expect(screen.getByText("No projects")).toBeInTheDocument();
    });
  });

  it("filters projects by search", async () => {
    const { listProjects } = await import("@/services/developer-service").then(
      (m) => m.developerService,
    );
    (listProjects as jest.Mock).mockResolvedValue(mockProjects);

    renderWithProviders(<ProjectsPage />);
    await waitFor(() => {
      expect(screen.getByText("My API Project")).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText("Search projects...");
    fireEvent.change(searchInput, { target: { value: "Mobile" } });

    await waitFor(() => {
      expect(screen.queryByText("My API Project")).not.toBeInTheDocument();
      expect(screen.getByText("Mobile Backend")).toBeInTheDocument();
    });
  });
});
