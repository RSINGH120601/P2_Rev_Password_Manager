package com.rev.passwordmanager;

import com.rev.passwordmanager.dto.*;
import com.rev.passwordmanager.entity.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive Pure JUnit 5 Test Suite for RevVault.
 * Total Test Cases: 18
 * Coverage: All Entities and All major DTOs (Request/Response objects).
 */
public class UserServiceTest {

    // --- ENTITY TESTS ---

    @Test
    void testUserEntityFull() {
        User user = new User();
        // user.setId(101L); // setId is not available in User entity
        user.setName("Ayush");
        user.setPhone("9876543210");
        user.setUsername("ayush829");
        user.setEmail("ayush@rev.com");
        user.setMasterPassword("hashed_mp");
        user.setTwoFactorEnabled(true);
        user.setOtpCode("123456");
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        // assertEquals(101L, user.getId());
        assertEquals("Ayush", user.getName());
        assertEquals("ayush829", user.getUsername());
        assertTrue(user.getTwoFactorEnabled());
        assertNotNull(user.getOtpExpiry());
    }

    @Test
    void testPasswordEntryFull() {
        PasswordEntry entry = new PasswordEntry();
        // entry.setId(501L); // setId is not available in PasswordEntry entity
        entry.setAccountName("Bank Account");
        entry.setWebsite("hdfc.com");
        entry.setUsername("ayush_hdfc");
        entry.setEncryptedPassword("aes_encrypted_data");
        entry.setCategory("Finance");
        entry.setFavorite(true);

        // assertEquals(501L, entry.getId());
        assertEquals("Bank Account", entry.getAccountName());
        assertEquals("Finance", entry.getCategory());
        assertTrue(entry.getFavorite());
    }

    @Test
    void testSecurityQuestionEntity() {
        SecurityQuestion sq = new SecurityQuestion();
        sq.setId(1L);
        sq.setQuestionText("What is your pet's name?");

        assertEquals(1L, sq.getId());
        assertEquals("What is your pet's name?", sq.getQuestionText());
    }

    @Test
    void testUserSecurityAnswerEntity() {
        User user = new User();
        SecurityQuestion sq = new SecurityQuestion();
        UserSecurityAnswer answer = new UserSecurityAnswer();

        answer.setId(1L);
        answer.setUser(user);
        answer.setSecurityQuestion(sq);
        answer.setAnswerHash("bcrypt_hash");

        assertEquals(1L, answer.getId());
        assertNotNull(answer.getUser());
        assertEquals("bcrypt_hash", answer.getAnswerHash());
    }

    // --- AUTHENTICATION DTO TESTS ---

    @Test
    void testLoginRequest() {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setMasterPassword("secret");

        assertEquals("admin", req.getUsername());
        assertEquals("secret", req.getMasterPassword());
    }

    @Test
    void testRegisterRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("tester");
        req.setEmail("test@rev.com");
        req.setSecurityAnswers(Collections.emptyList());

        assertEquals("tester", req.getUsername());
        assertTrue(req.getSecurityAnswers().isEmpty());
    }

    @Test
    void testVerify2FARequest() {
        Verify2FARequest req = new Verify2FARequest();
        req.setUsername("ayush");
        req.setOtp("654321");

        assertEquals("ayush", req.getUsername());
        assertEquals("654321", req.getOtp());
    }

    @Test
    void testChangePasswordRequest() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("old123");
        req.setNewPassword("new123");

        assertEquals("old123", req.getCurrentPassword());
        assertEquals("new123", req.getNewPassword());
    }

    @Test
    void testRecoverPasswordRequest() {
        RecoverPasswordRequest req = new RecoverPasswordRequest();
        req.setUsername("user_recover");
        req.setNewPassword("recovered_pass");

        assertEquals("user_recover", req.getUsername());
        assertEquals("recovered_pass", req.getNewPassword());
    }

    // --- PASSWORD MANAGEMENT DTO TESTS ---

    @Test
    void testAddPasswordRequest() {
        AddPasswordRequest req = new AddPasswordRequest();
        req.setAccountName("Netflix");
        req.setPassword("netflix_pass");

        assertEquals("Netflix", req.getAccountName());
        assertEquals("netflix_pass", req.getPassword());
    }

    @Test
    void testUpdatePasswordRequest() {
        UpdatePasswordRequest req = new UpdatePasswordRequest();
        req.setAccountName("Updated Netflix");
        req.setCategory("Entertainment");

        assertEquals("Updated Netflix", req.getAccountName());
        assertEquals("Entertainment", req.getCategory());
    }

    @Test
    void testPasswordResponse() {
        PasswordResponse res = new PasswordResponse(1L, "Spotify", "spot.com", "user", "pass", "Music", "Notes", true);

        assertEquals(1L, res.getId());
        assertEquals("Spotify", res.getAccountName());
        assertTrue(res.getFavorite());
    }

    // --- PROFILE & SYSTEM DTO TESTS ---

    @Test
    void testProfileResponse() {
        ProfileResponse res = new ProfileResponse("Ayush", "ayush@rev.com", "9876", "ayush829", true);

        assertEquals("ayush829", res.getUsername());
        res.setUsername("ayush_profile");
        assertEquals("ayush_profile", res.getUsername());
        assertTrue(res.isTwoFactorEnabled());
    }

    @Test
    void testUpdateProfileRequest() {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setName("Ayush Updated");
        req.setPhone("0000000000");

        assertEquals("Ayush Updated", req.getName());
        assertEquals("0000000000", req.getPhone());
    }

    @Test
    void testApiResponse() {
        ApiResponse<String> res = new ApiResponse<>(true, "Success", "Payload");

        assertTrue(res.isSuccess());
        assertEquals("Success", res.getMessage());
        assertEquals("Payload", res.getData());
    }

    @Test
    void testSecurityAuditResponse() {
        SecurityAuditResponse res = new SecurityAuditResponse();
        res.setSecurityScore(88);
        res.setWeakPasswords(Collections.emptyList());

        assertEquals(88, res.getSecurityScore());
        assertNotNull(res.getWeakPasswords());
    }

    @Test
    void testPaginationResponse() {
        PaginationResponse<String> res = new PaginationResponse<>(Collections.emptyList(), 1, 10, 50L, 5);

        assertEquals(5, res.getTotalPages());
        assertEquals(1, res.getCurrentPage());
        assertEquals(50L, res.getTotalElements());
    }

    @Test
    void testSecurityAnswerDTO() {
        SecurityAnswerDTO dto = new SecurityAnswerDTO();
        dto.setQuestionId(1L);
        dto.setAnswer("Tiger");

        assertEquals(1L, dto.getQuestionId());
        assertEquals("Tiger", dto.getAnswer());
    }
}
