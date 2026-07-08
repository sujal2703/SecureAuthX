import api from "@/lib/api";
import type {
  Passkey,
  RegisterOptionsResponse,
  RegisterVerificationRequest,
  RegisterVerificationResponse,
} from "@/types/api";

export const passkeyService = {
  async list(): Promise<Passkey[]> {
    const response = await api.get<Passkey[]>("/api/v1/passkeys");
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/api/v1/passkeys/${id}`);
  },

  async registerOptions(): Promise<RegisterOptionsResponse> {
    const response = await api.post<RegisterOptionsResponse>(
      "/api/v1/passkeys/register/options",
    );
    return response.data;
  },

  async registerVerify(
    data: RegisterVerificationRequest,
  ): Promise<RegisterVerificationResponse> {
    const response = await api.post<RegisterVerificationResponse>(
      "/api/v1/passkeys/register/verify",
      data,
    );
    return response.data;
  },
};
