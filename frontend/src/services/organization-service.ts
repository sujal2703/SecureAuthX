import api from "@/lib/api";
import type { Organization, OrganizationCreateRequest, OrganizationUpdateRequest } from "@/types/api";

export const organizationService = {
  async getCurrent(): Promise<Organization> {
    const response = await api.get<Organization>("/api/v1/organizations/current");
    return response.data;
  },

  async list(): Promise<Organization[]> {
    const response = await api.get<Organization[]>("/api/v1/organizations");
    return response.data;
  },

  async get(id: string): Promise<Organization> {
    const response = await api.get<Organization>(`/api/v1/organizations/${id}`);
    return response.data;
  },

  async create(data: OrganizationCreateRequest): Promise<Organization> {
    const response = await api.post<Organization>("/api/v1/organizations", data);
    return response.data;
  },

  async update(id: string, data: OrganizationUpdateRequest): Promise<Organization> {
    const response = await api.patch<Organization>(`/api/v1/organizations/${id}`, data);
    return response.data;
  },
};
