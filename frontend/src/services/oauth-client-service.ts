import api from "@/lib/api";
import type { OAuthClient, OAuthClientCreateRequest, OAuthClientCreateResponse } from "@/types/api";

export const oauthClientService = {
  async list(): Promise<OAuthClient[]> {
    const response = await api.get<OAuthClient[]>("/api/v1/oauth/clients");
    return response.data;
  },

  async get(id: string): Promise<OAuthClient> {
    const response = await api.get<OAuthClient>(`/api/v1/oauth/clients/${id}`);
    return response.data;
  },

  async create(data: OAuthClientCreateRequest): Promise<OAuthClientCreateResponse> {
    const response = await api.post<OAuthClientCreateResponse>("/api/v1/oauth/clients", data);
    return response.data;
  },
};
