package com.example.Backend.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenHasherTest {

    @Test
    void hashIsDeterministic() {
        String a = RefreshTokenHasher.hash("some.refresh.jwt");
        String b = RefreshTokenHasher.hash("some.refresh.jwt");
        assertThat(a).isEqualTo(b);
    }

    @Test
    void differentTokensHashDifferently() {
        assertThat(RefreshTokenHasher.hash("token.one")).isNotEqualTo(RefreshTokenHasher.hash("token.two"));
    }

    @Test
    void hashIsSha256Hex() {
        String hash = RefreshTokenHasher.hash("abc");
        assertThat(hash).matches("[0-9a-f]{64}");
    }

    @Test
    void hashNeverLeaksTheRawToken() {
        String raw = "the.actual.refresh.token";
        assertThat(RefreshTokenHasher.hash(raw)).isNotEqualTo(raw);
    }
}