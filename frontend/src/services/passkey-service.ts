import api from "@/lib/api";
import type { Passkey } from "@/types/api";

export const passkeyService = {
  async list(): Promise<Passkey[]> {
    const response = await api.get<Passkey[]>("/api/v1/passkeys");
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/api/v1/passkeys/${id}`);
  },
};
