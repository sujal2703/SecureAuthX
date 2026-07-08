import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import AdminDashboardPage from "@/app/dashboard/admin/page";
import AuditLogsPage from "@/app/dashboard/admin/audit-logs/page";
import SecurityIncidentsPage from "@/app/dashboard/admin/incidents/page";
import IncidentDetailPage from "@/app/dashboard/admin/incidents/[id]/page";
import AnnouncementsPage from "@/app/dashboard/admin/announcements/page";
import SystemSettingsPage from "@/app/dashboard/admin/settings/page";
import AdminUsersPage from "@/app/dashboard/admin/users/page";
import AdminOrganizationsPage from "@/app/dashboard/admin/organizations/page";
import { AuthProvider } from "@/contexts/auth-context";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

jest.mock("next/navigation", () => ({
  useRouter: jest.fn(() => ({ push: jest.fn() })),
  usePathname: jest.fn(() => "/dashboard/admin"),
  useParams: jest.fn(() => ({ id: "incident-1" })),
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

const mockDashboard = {
  totalUsers: 150,
  activeSessions: 42,
  totalOrganizations: 12,
  totalOAuthClients: 8,
  totalPasskeys: 67,
  totalLoginEvents: 1245,
  developerProjects: 5,
  totalSessions: 300,
  securityIncidents: 3,
};

const mockAuditLogs = {
  content: [
    { id: "log-1", userId: "user-1", organizationId: "org-1", ipAddress: "192.168.1.1", action: "LOGIN", target: "/api/v1/auth/login", success: true, details: "", createdAt: "2026-07-08T10:00:00Z" },
    { id: "log-2", userId: "user-2", organizationId: "org-1", ipAddress: "10.0.0.1", action: "UPDATE", target: "/api/v1/profile", success: true, details: "Updated email", createdAt: "2026-07-08T09:00:00Z" },
    { id: "log-3", userId: "user-3", organizationId: "org-1", ipAddress: "172.16.0.1", action: "DELETE", target: "/api/v1/sessions/sess-1", success: false, details: "Unauthorized", createdAt: "2026-07-07T18:00:00Z" },
  ],
  totalPages: 1,
  totalElements: 3,
};

const mockIncidents = {
  content: [
    { id: "incident-1", userId: "user-1", incidentType: "BRUTE_FORCE", severity: "HIGH", description: "Multiple failed login attempts detected", resolved: false, resolvedBy: null, resolvedAt: null, createdAt: "2026-07-08T10:00:00Z", updatedAt: "2026-07-08T10:00:00Z", ipAddress: "192.168.1.100" },
    { id: "incident-2", userId: "user-2", incidentType: "SUSPICIOUS_ACCESS", severity: "MEDIUM", description: "Unusual access pattern from new location", resolved: true, resolvedBy: "admin@test.com", resolvedAt: "2026-07-08T12:00:00Z", createdAt: "2026-07-07T08:00:00Z", updatedAt: "2026-07-08T12:00:00Z", ipAddress: "10.0.0.50" },
  ],
  totalPages: 1,
  totalElements: 2,
};

const mockSingleIncident = {
  id: "incident-1", userId: "user-1", incidentType: "BRUTE_FORCE", severity: "HIGH", description: "Multiple failed login attempts detected", resolved: false, resolvedBy: null, resolvedAt: null, createdAt: "2026-07-08T10:00:00Z", updatedAt: "2026-07-08T10:00:00Z", ipAddress: "192.168.1.100",
};

const mockAnnouncements = [
  { id: "ann-1", title: "Scheduled Maintenance", message: "System will be down on Sunday", severity: "WARNING", active: true, createdBy: "admin@test.com", createdAt: "2026-07-08T10:00:00Z" },
  { id: "ann-2", title: "New Feature Release", message: "Passkey support is now available", severity: "INFO", active: true, createdBy: "admin@test.com", createdAt: "2026-07-07T08:00:00Z" },
];

const mockSettings = [
  { settingKey: "MAX_LOGIN_ATTEMPTS", settingValue: "5", description: "Maximum failed login attempts before lockout" },
  { settingKey: "SESSION_TIMEOUT_MINUTES", settingValue: "60", description: "Session timeout duration in minutes" },
  { settingKey: "PASSWORD_MIN_LENGTH", settingValue: "8", description: "Minimum password length requirement" },
];

jest.mock("@/services/admin-service", () => ({
  adminService: {
    getDashboard: jest.fn(),
    listAuditLogs: jest.fn(),
    getAuditLog: jest.fn(),
    listAnnouncements: jest.fn(),
    createAnnouncement: jest.fn(),
    updateAnnouncement: jest.fn(),
    deleteAnnouncement: jest.fn(),
    listIncidents: jest.fn(),
    getIncident: jest.fn(),
    resolveIncident: jest.fn(),
    listSettings: jest.fn(),
    updateSetting: jest.fn(),
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

describe("AdminDashboardPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it("renders the page title", async () => {
    renderWithProviders(<AdminDashboardPage />);
    await waitFor(() => {
      expect(screen.getByText("Admin Dashboard")).toBeInTheDocument();
    });
  });

  it("renders stat cards when loaded", async () => {
    const { getDashboard } = await import("@/services/admin-service").then((m) => m.adminService);
    (getDashboard as jest.Mock).mockResolvedValue(mockDashboard);

    renderWithProviders(<AdminDashboardPage />);
    await waitFor(() => {
      expect(screen.getByText("150")).toBeInTheDocument();
      expect(screen.getByText("42")).toBeInTheDocument();
      expect(screen.getByText("Total Users")).toBeInTheDocument();
      expect(screen.getByText("Active Sessions")).toBeInTheDocument();
    });
  });

  it("shows admin gate on 403", async () => {
    const { getDashboard } = await import("@/services/admin-service").then((m) => m.adminService);
    (getDashboard as jest.Mock).mockRejectedValue({ response: { status: 403 } });

    renderWithProviders(<AdminDashboardPage />);
    await waitFor(() => {
      expect(screen.getByText("Admin access required")).toBeInTheDocument();
    });
  });
});

describe("AuditLogsPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it("renders the page title", async () => {
    renderWithProviders(<AuditLogsPage />);
    await waitFor(() => {
      expect(screen.getByText("Audit Logs")).toBeInTheDocument();
    });
  });

  it("renders audit log entries when loaded", async () => {
    const { listAuditLogs } = await import("@/services/admin-service").then((m) => m.adminService);
    (listAuditLogs as jest.Mock).mockResolvedValue(mockAuditLogs);

    renderWithProviders(<AuditLogsPage />);
    await waitFor(() => {
      expect(screen.getByText("LOGIN")).toBeInTheDocument();
      expect(screen.getByText("UPDATE")).toBeInTheDocument();
      expect(screen.getByText("DELETE")).toBeInTheDocument();
    });
  });

  it("shows empty state when no logs", async () => {
    const { listAuditLogs } = await import("@/services/admin-service").then((m) => m.adminService);
    (listAuditLogs as jest.Mock).mockResolvedValue({ content: [], totalPages: 0, totalElements: 0 });

    renderWithProviders(<AuditLogsPage />);
    await waitFor(() => {
      expect(screen.getByText("No audit logs found")).toBeInTheDocument();
    });
  });
});

describe("SecurityIncidentsPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it("renders the page title", async () => {
    renderWithProviders(<SecurityIncidentsPage />);
    await waitFor(() => {
      expect(screen.getByText("Security Incidents")).toBeInTheDocument();
    });
  });

  it("renders incident entries when loaded", async () => {
    const { listIncidents } = await import("@/services/admin-service").then((m) => m.adminService);
    (listIncidents as jest.Mock).mockResolvedValue(mockIncidents);

    renderWithProviders(<SecurityIncidentsPage />);
    await waitFor(() => {
      expect(screen.getByText("BRUTE_FORCE")).toBeInTheDocument();
      expect(screen.getByText("SUSPICIOUS_ACCESS")).toBeInTheDocument();
    });
  });

  it("shows empty state when no incidents", async () => {
    const { listIncidents } = await import("@/services/admin-service").then((m) => m.adminService);
    (listIncidents as jest.Mock).mockResolvedValue({ content: [], totalPages: 0, totalElements: 0 });

    renderWithProviders(<SecurityIncidentsPage />);
    await waitFor(() => {
      expect(screen.getByText("No incidents found")).toBeInTheDocument();
    });
  });
});

describe("IncidentDetailPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it("renders incident details when loaded", async () => {
    const { getIncident } = await import("@/services/admin-service").then((m) => m.adminService);
    (getIncident as jest.Mock).mockResolvedValue(mockSingleIncident);

    renderWithProviders(<IncidentDetailPage />);
    await waitFor(() => {
      expect(screen.getByText("BRUTE_FORCE")).toBeInTheDocument();
      expect(screen.getByText("Multiple failed login attempts detected")).toBeInTheDocument();
    });
  });

  it("renders resolve button for unresolved incident", async () => {
    const { getIncident } = await import("@/services/admin-service").then((m) => m.adminService);
    (getIncident as jest.Mock).mockResolvedValue(mockSingleIncident);

    renderWithProviders(<IncidentDetailPage />);
    await waitFor(() => {
      expect(screen.getByText("Resolve Incident")).toBeInTheDocument();
    });
  });
});

describe("AnnouncementsPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it("renders the page title", async () => {
    renderWithProviders(<AnnouncementsPage />);
    await waitFor(() => {
      expect(screen.getByText("Announcements")).toBeInTheDocument();
    });
  });

  it("renders announcement entries when loaded", async () => {
    const { listAnnouncements } = await import("@/services/admin-service").then((m) => m.adminService);
    (listAnnouncements as jest.Mock).mockResolvedValue(mockAnnouncements);

    renderWithProviders(<AnnouncementsPage />);
    await waitFor(() => {
      expect(screen.getByText("Scheduled Maintenance")).toBeInTheDocument();
      expect(screen.getByText("New Feature Release")).toBeInTheDocument();
    });
  });

  it("shows empty state when no announcements", async () => {
    const { listAnnouncements } = await import("@/services/admin-service").then((m) => m.adminService);
    (listAnnouncements as jest.Mock).mockResolvedValue([]);

    renderWithProviders(<AnnouncementsPage />);
    await waitFor(() => {
      expect(screen.getByText("No announcements")).toBeInTheDocument();
    });
  });

  it("opens create form when clicking New Announcement", async () => {
    const { listAnnouncements } = await import("@/services/admin-service").then((m) => m.adminService);
    (listAnnouncements as jest.Mock).mockResolvedValue(mockAnnouncements);

    renderWithProviders(<AnnouncementsPage />);
    await waitFor(() => {
      expect(screen.getByText("New Announcement")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText("New Announcement"));
    await waitFor(() => {
      expect(screen.getByText("Create")).toBeInTheDocument();
    });
  });
});

describe("SystemSettingsPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it("renders the page title", async () => {
    renderWithProviders(<SystemSettingsPage />);
    await waitFor(() => {
      expect(screen.getByText("System Settings")).toBeInTheDocument();
    });
  });

  it("renders settings when loaded", async () => {
    const { listSettings } = await import("@/services/admin-service").then((m) => m.adminService);
    (listSettings as jest.Mock).mockResolvedValue(mockSettings);

    renderWithProviders(<SystemSettingsPage />);
    await waitFor(() => {
      expect(screen.getByText("MAX_LOGIN_ATTEMPTS")).toBeInTheDocument();
      expect(screen.getByText("SESSION_TIMEOUT_MINUTES")).toBeInTheDocument();
    });
  });

  it("shows empty state when no settings", async () => {
    const { listSettings } = await import("@/services/admin-service").then((m) => m.adminService);
    (listSettings as jest.Mock).mockResolvedValue([]);

    renderWithProviders(<SystemSettingsPage />);
    await waitFor(() => {
      expect(screen.getByText("No settings found")).toBeInTheDocument();
    });
  });
});

describe("AdminUsersPage", () => {
  it("renders the page title", () => {
    renderWithProviders(<AdminUsersPage />);
    expect(screen.getAllByText("User Management").length).toBeGreaterThanOrEqual(1);
  });
});

describe("AdminOrganizationsPage", () => {
  it("renders the page title", () => {
    renderWithProviders(<AdminOrganizationsPage />);
    expect(screen.getByText("Organization Admin")).toBeInTheDocument();
  });
});
