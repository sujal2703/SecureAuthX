import api from "@/lib/api";
import type { Session } from "@/types/api";

export const sessionService = {
  async list(): Promise<Session[]> {
    const response = await api.get<Session[]>("/api/v1/sessions");
    return response.data;
  },

  async getCurrent(): Promise<Session> {
    const response = await api.get<Session>("/api/v1/sessions/current");
    return response.data;
  },

  async revoke(sessionId: string): Promise<void> {
    await api.delete(`/api/v1/sessions/${sessionId}`);
  },

  async revokeCurrent(): Promise<void> {
    await api.delete("/api/v1/sessions/current");
  },

  async revokeAll(): Promise<void> {
    await api.delete("/api/v1/sessions/all");
  },
};
