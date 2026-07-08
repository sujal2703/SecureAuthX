import api from "@/lib/api";
import type {
  DashboardResponse,
  AuditLogResponse,
  SecurityIncidentResponse,
  IncidentResolveRequest,
  AnnouncementRequest,
  AnnouncementResponse,
  SystemSettingRequest,
  SystemSettingResponse,
} from "@/types/api";

export const adminService = {
  async getDashboard(): Promise<DashboardResponse> {
    const response = await api.get<DashboardResponse>("/api/v1/admin/dashboard");
    return response.data;
  },

  async listAuditLogs(params?: {
    userId?: string;
    action?: string;
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
  }): Promise<{ content: AuditLogResponse[]; totalElements: number; totalPages: number; number: number; size: number }> {
    const response = await api.get("/api/v1/admin/audit", { params });
    return response.data;
  },

  async getAuditLog(id: string): Promise<AuditLogResponse> {
    const response = await api.get<AuditLogResponse>(`/api/v1/admin/audit/${id}`);
    return response.data;
  },

  async listAnnouncements(): Promise<AnnouncementResponse[]> {
    const response = await api.get<AnnouncementResponse[]>("/api/v1/admin/announcements");
    return response.data;
  },

  async createAnnouncement(data: AnnouncementRequest): Promise<AnnouncementResponse> {
    const response = await api.post<AnnouncementResponse>("/api/v1/admin/announcements", data);
    return response.data;
  },

  async updateAnnouncement(id: string, data: AnnouncementRequest): Promise<AnnouncementResponse> {
    const response = await api.patch<AnnouncementResponse>(`/api/v1/admin/announcements/${id}`, data);
    return response.data;
  },

  async deleteAnnouncement(id: string): Promise<void> {
    await api.delete(`/api/v1/admin/announcements/${id}`);
  },

  async listIncidents(params?: {
    resolved?: boolean;
    page?: number;
    size?: number;
  }): Promise<{ content: SecurityIncidentResponse[]; totalElements: number; totalPages: number; number: number; size: number }> {
    const response = await api.get("/api/v1/admin/incidents", { params });
    return response.data;
  },

  async getIncident(id: string): Promise<SecurityIncidentResponse> {
    const response = await api.get<SecurityIncidentResponse>(`/api/v1/admin/incidents/${id}`);
    return response.data;
  },

  async resolveIncident(id: string, data: IncidentResolveRequest): Promise<SecurityIncidentResponse> {
    const response = await api.patch<SecurityIncidentResponse>(`/api/v1/admin/incidents/${id}`, data);
    return response.data;
  },

  async listSettings(): Promise<SystemSettingResponse[]> {
    const response = await api.get<SystemSettingResponse[]>("/api/v1/admin/settings");
    return response.data;
  },

  async updateSetting(key: string, data: SystemSettingRequest): Promise<SystemSettingResponse> {
    const response = await api.put<SystemSettingResponse>(`/api/v1/admin/settings/${key}`, data);
    return response.data;
  },
};
