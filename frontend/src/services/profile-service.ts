import api from "@/lib/api";
import type { UserInfoResponse } from "@/types/api";

export const profileService = {
  async getUserInfo(): Promise<UserInfoResponse> {
    const response = await api.get<UserInfoResponse>("/connect/userinfo");
    return response.data;
  },
};
