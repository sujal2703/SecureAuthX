import api from "@/lib/api";
import type {
  ProjectResponse,
  ProjectCreateRequest,
  ProjectUpdateRequest,
  ApiKeyResponse,
  CreateApiKeyRequest,
  CreateApiKeyResponse,
  RateLimitRequest,
  RateLimitResponse,
  UsageAnalyticsResponse,
  RotateSecretResponse,
} from "@/types/api";

export const developerService = {
  // Projects
  async listProjects(): Promise<ProjectResponse[]> {
    const response = await api.get<ProjectResponse[]>("/api/v1/developer/projects");
    return response.data;
  },

  async getProject(id: string): Promise<ProjectResponse> {
    const response = await api.get<ProjectResponse>(`/api/v1/developer/projects/${id}`);
    return response.data;
  },

  async createProject(data: ProjectCreateRequest): Promise<ProjectResponse> {
    const response = await api.post<ProjectResponse>("/api/v1/developer/projects", data);
    return response.data;
  },

  async updateProject(id: string, data: ProjectUpdateRequest): Promise<ProjectResponse> {
    const response = await api.put<ProjectResponse>(`/api/v1/developer/projects/${id}`, data);
    return response.data;
  },

  async deleteProject(id: string): Promise<void> {
    await api.delete(`/api/v1/developer/projects/${id}`);
  },

  // API Keys
  async listApiKeys(projectId: string): Promise<ApiKeyResponse[]> {
    const response = await api.get<ApiKeyResponse[]>(
      `/api/v1/developer/projects/${projectId}/api-keys`,
    );
    return response.data;
  },

  async createApiKey(projectId: string, data: CreateApiKeyRequest): Promise<CreateApiKeyResponse> {
    const response = await api.post<CreateApiKeyResponse>(
      `/api/v1/developer/projects/${projectId}/api-keys`,
      data,
    );
    return response.data;
  },

  async revokeApiKey(projectId: string, keyId: string): Promise<void> {
    await api.delete(`/api/v1/developer/projects/${projectId}/api-keys/${keyId}`);
  },

  // Usage
  async getUsage(
    projectId: string,
    startDate?: string,
    endDate?: string,
  ): Promise<UsageAnalyticsResponse[]> {
    const params: Record<string, string> = {};
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    const response = await api.get<UsageAnalyticsResponse[]>(
      `/api/v1/developer/projects/${projectId}/usage`,
      { params },
    );
    return response.data;
  },

  // Rate Limits
  async getRateLimits(projectId: string): Promise<RateLimitResponse> {
    const response = await api.get<RateLimitResponse>(
      `/api/v1/developer/projects/${projectId}/rate-limits`,
    );
    return response.data;
  },

  async setRateLimits(projectId: string, data: RateLimitRequest): Promise<RateLimitResponse> {
    const response = await api.put<RateLimitResponse>(
      `/api/v1/developer/projects/${projectId}/rate-limits`,
      data,
    );
    return response.data;
  },

  async deleteRateLimits(projectId: string): Promise<void> {
    await api.delete(`/api/v1/developer/projects/${projectId}/rate-limits`);
  },

  // Secret Rotation
  async rotateSecret(projectId: string): Promise<RotateSecretResponse> {
    const response = await api.post<RotateSecretResponse>(
      `/api/v1/developer/projects/${projectId}/rotate-secret`,
    );
    return response.data;
  },
};
