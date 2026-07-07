package com.secureauthx.server.sessions.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserAgentParserTests {

    private final UserAgentParser parser = new UserAgentParser();

    @Test
    void parsesChromeOnWindows() {
        String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        assertThat(parser.parseBrowser(ua)).isEqualTo("Chrome");
        assertThat(parser.parseOperatingSystem(ua)).isEqualTo("Windows");
        assertThat(parser.parseDeviceName(ua)).isEqualTo("Windows PC");
    }

    @Test
    void parsesFirefoxOnMacOS() {
        String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 14.2; rv:121.0) Gecko/20100101 Firefox/121.0";
        assertThat(parser.parseBrowser(ua)).isEqualTo("Firefox");
        assertThat(parser.parseOperatingSystem(ua)).isEqualTo("macOS");
        assertThat(parser.parseDeviceName(ua)).isEqualTo("Mac");
    }

    @Test
    void parsesSafariOnIOS() {
        String ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1";
        assertThat(parser.parseBrowser(ua)).isEqualTo("Safari");
        assertThat(parser.parseOperatingSystem(ua)).isEqualTo("iOS");
        assertThat(parser.parseDeviceName(ua)).isEqualTo("iPhone");
    }

    @Test
    void parsesEdgeOnAndroid() {
        String ua = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.144 Mobile Safari/537.36 Edg/120.0.0.0";
        assertThat(parser.parseBrowser(ua)).isEqualTo("Edge");
        assertThat(parser.parseOperatingSystem(ua)).isEqualTo("Android");
        assertThat(parser.parseDeviceName(ua)).isEqualTo("Android Phone");
    }

    @Test
    void handlesNullUserAgent() {
        assertThat(parser.parseBrowser(null)).isEqualTo("Unknown");
        assertThat(parser.parseOperatingSystem(null)).isEqualTo("Unknown");
        assertThat(parser.parseDeviceName(null)).isEqualTo("Unknown Device");
    }

    @Test
    void handlesEmptyUserAgent() {
        assertThat(parser.parseBrowser("")).isEqualTo("Unknown");
        assertThat(parser.parseOperatingSystem("")).isEqualTo("Unknown");
        assertThat(parser.parseDeviceName("")).isEqualTo("Unknown Device");
    }
}
