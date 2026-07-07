package com.secureauthx.server.sessions.service;

import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class UserAgentParser {

    public String parseBrowser(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);
        if (ua.contains("edg/") || ua.contains("edge/")) {
            return "Edge";
        }
        if (ua.contains("opr/") || ua.contains("opera")) {
            return "Opera";
        }
        if (ua.contains("chrome/") && !ua.contains("chromium")) {
            return "Chrome";
        }
        if (ua.contains("firefox/")) {
            return "Firefox";
        }
        if (ua.contains("safari/") && !ua.contains("chrome/")) {
            return "Safari";
        }
        return "Other";
    }

    public String parseOperatingSystem(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ios")) {
            return "iOS";
        }
        if (ua.contains("windows")) {
            return "Windows";
        }
        if (ua.contains("mac os") || ua.contains("macintosh")) {
            return "macOS";
        }
        if (ua.contains("linux") && !ua.contains("android")) {
            return "Linux";
        }
        if (ua.contains("android")) {
            return "Android";
        }
        return "Other";
    }

    public String parseDeviceName(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown Device";
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);
        if (ua.contains("iphone")) {
            return "iPhone";
        }
        if (ua.contains("ipad")) {
            return "iPad";
        }
        if (ua.contains("android")) {
            if (ua.contains("mobile")) {
                return "Android Phone";
            }
            return "Android Device";
        }
        if (ua.contains("windows")) {
            return "Windows PC";
        }
        if (ua.contains("macintosh") || ua.contains("mac os")) {
            return "Mac";
        }
        if (ua.contains("linux")) {
            return "Linux PC";
        }
        return "Unknown Device";
    }
}
