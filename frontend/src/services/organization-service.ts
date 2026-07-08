import api from "@/lib/api";
import type { Organization } from "@/types/api";

export const organizationService = {
  async getCurrent(): Promise<Organization> {
    const response = await api.get<Organization>("/api/v1/organizations/current");
    return response.data;
  },

  async list(): Promise<Organization[]> {
    const response = await api.get<Organization[]>("/api/v1/organizations");
    return response.data;
  },
};
