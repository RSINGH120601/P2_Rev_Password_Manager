package com.rev.passwordmanager.controller;

import com.rev.passwordmanager.dto.*;
import com.rev.passwordmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // ================= REGISTER =================
    /**
     * Registers a new user in the system.
     * Why: Necessary to allow new users to create accounts, set up their master password,
     * and define security questions for future account recovery.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(
            @Valid @RequestBody RegisterRequest request) {

        ApiResponse<?> response = userService.registerUser(request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ================= LOGIN =================
    /**
     * Authenticates a user and issues a JWT token.
     * Why: Essential for securing the application. Only authenticated users with a valid
     * JWT token can access their encrypted vaults and private data.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(
            @Valid @RequestBody LoginRequest request) {

        ApiResponse<?> response = userService.loginUser(request);

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }

    // ================= ADD PASSWORD =================
    /**
     * Adds a new encrypted password entry to the user's vault.
     * Why: This is the core feature of the password manager, allowing users to securely
     * store credentials for various platforms.
     */
    @PostMapping("/add-password")
    public ResponseEntity<ApiResponse<?>> addPassword(
            @Valid @RequestBody AddPasswordRequest request,
            Authentication authentication) {

        String username = authentication.getName();

        ApiResponse<?> response =
                userService.addPassword(request, username);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ================= GET PASSWORDS =================
    /**
     * Retrieves a paginated list of the user's saved passwords.
     * Why: Required to populate the frontend dashboard table securely over the network.
     */
    @GetMapping("/passwords")
    public ResponseEntity<ApiResponse<?>> getPasswords(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            Authentication authentication) {

        String username = authentication.getName();

        ApiResponse<?> response =
                userService.getPasswordsByUser(username, page, size, sortBy);

        return ResponseEntity.ok(response);
    }

    // ================= DELETE PASSWORD =================
    /**
     * Deletes a specific password entry from the user's vault.
     * Why: Allows users to manage their data and remove outdated or compromised credentials.
     */
    @DeleteMapping("/delete-password/{id}")
    public ResponseEntity<ApiResponse<?>> deletePassword(
            @PathVariable("id") Long id,
            Authentication authentication) {

        String username = authentication.getName();

        ApiResponse<?> response =
                userService.deletePassword(id, username);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    // ================= UPDATE PASSWORD =================
    /**
     * Updates an existing password entry.
     * Why: Lets users keep their credentials current without having to delete and re-add them.
     */
    @PutMapping("/update-password")
    public ResponseEntity<ApiResponse<?>> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request,
            Authentication authentication) {

        String username = authentication.getName();

        ApiResponse<?> response =
                userService.updatePassword(request, username);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    // ================= GET PASSWORD BY ID =================
    /**
     * Gets a single password entry by ID.
     * Why: Used by the Edit Password page to prepopulate data securely.
     */
    @GetMapping("/passwords/{id}")
    public ResponseEntity<ApiResponse<?>> getPasswordById(
            @PathVariable("id") Long id,
            Authentication authentication) {

        String username = authentication.getName();

        ApiResponse<?> response =
                userService.getPasswordById(id, username);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    // ================= TOGGLE FAVORITE =================
    /**
     * Toggles the favorite status of a password entry.
     * Why: Enhances UX by allowing users to pin or highlight their most frequently used passwords.
     */
    @PutMapping("/toggle-favorite/{id}")
    public ResponseEntity<ApiResponse<?>> toggleFavorite(
            @PathVariable("id") Long id,
            Authentication authentication) {

        String username = authentication.getName();

        ApiResponse<?> response =
                userService.toggleFavorite(id, username);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }


    // ================= VERIFY 2FA =================
    /**
     * Verifies the time-based OTP for Two-Factor Authentication.
     * Why: Adds a mandatory secondary layer of security before issuing the primary JWT token,
     * protecting against compromised master passwords.
     */
    @PostMapping("/verify-2fa")
    public ResponseEntity<ApiResponse<?>> verify2FA(
            @Valid @RequestBody Verify2FARequest request) {

        ApiResponse<?> response = userService.verify2FA(request);

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }

    // ================= GENERATE OTP FOR 2FA =================
    /**
     * Generates a 6-digit OTP and associates it with the user account.
     * Why: Required for the 2FA flow. Simulates triggering an email/SMS containing the challenge code.
     */
    @GetMapping("/generate-otp/{username}")
    public ResponseEntity<ApiResponse<?>> generateOtp(@PathVariable("username") String username) {
        ApiResponse<?> response = userService.generateOtp(username);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ==================MASTER PASSWORD CHANGE ============
    /**
     * Changes the user's master password.
     * Why: A fundamental security requirement allowing users to rotate their central key periodically.
     */
    @PutMapping("/change-master-password")
    public ResponseEntity<ApiResponse<?>> changeMasterPassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        String username = authentication.getName();

        ApiResponse<?> response =
                userService.changeMasterPassword(username, request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    // ==========UPDATE PROFILE ===================
    @PutMapping("/update-profile")
    public ResponseEntity<ApiResponse<?>> updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication) {

        String username = authentication.getName();

        ApiResponse<?> response =
                userService.updateProfile(username, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<?>> getProfile(Authentication authentication) {
        String username = authentication.getName();
        ApiResponse<?> response = userService.getProfile(username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/security-questions/{username}")
    public ApiResponse<?> getSecurityQuestions(@PathVariable("username") String username){

        return userService.getSecurityQuestions(username);

    }

    @PostMapping("/recover-password")
    public ResponseEntity<ApiResponse<?>> recoverPassword(@RequestBody RecoverPasswordRequest request){

        ApiResponse<?> response = userService.recoverPassword(request);
        
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/toggle-2fa")
    public ApiResponse<?> toggle2FA(@RequestHeader("Authorization") String token){

        return userService.toggle2FA(token);

    }

    // ================= SEARCH PASSWORDS =================
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchPasswords(
            @RequestParam("keyword") String keyword,
            Authentication authentication) {
        
        String username = authentication.getName();
        ApiResponse<?> response = userService.searchPasswords(keyword, username);
        return ResponseEntity.ok(response);
    }

    // ================= FILTER BY CATEGORY =================
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<?>> getPasswordsByCategory(
            @PathVariable("category") String category,
            Authentication authentication) {
        
        String username = authentication.getName();
        ApiResponse<?> response = userService.getPasswordsByCategory(category, username);
        return ResponseEntity.ok(response);
    }

    // ================= SECURITY AUDIT =================
    /**
     * Computes a security score based on weak, old, and reused passwords.
     * Why: Provides actionable security insights to the user so they can improve their password habits.
     */
    @GetMapping("/audit")
    public ResponseEntity<ApiResponse<?>> getSecurityAudit(
            Authentication authentication) {
        
        String username = authentication.getName();
        ApiResponse<?> response = userService.getSecurityAudit(username);
        return ResponseEntity.ok(response);
    }

    // ================= EXPORT VAULT =================
    /**
     * Exports all user passwords as an AES-encrypted JSON string.
     * Why: Ensures data portability and allows users to create offline backups of their vaults.
     */
    @GetMapping("/export")
    public ResponseEntity<ApiResponse<?>> exportVault(
            Authentication authentication) {
        
        String username = authentication.getName();
        ApiResponse<?> response = userService.exportVault(username);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    // ================= IMPORT VAULT =================
    /**
     * Imports an AES-encrypted JSON backup into the user's vault.
     * Why: Allows users to restore their data seamlessly if they change devices or lose their account.
     */
    @PostMapping("/import")
    public ResponseEntity<ApiResponse<?>> importVault(
            @RequestBody java.util.Map<String, String> payload,
            Authentication authentication) {
        
        String username = authentication.getName();
        String backupData = payload.get("data");
        ApiResponse<?> response = userService.importVault(username, backupData);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}