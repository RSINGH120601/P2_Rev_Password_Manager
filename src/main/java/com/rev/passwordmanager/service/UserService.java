package com.rev.passwordmanager.service;

import com.rev.passwordmanager.dto.*;
import com.rev.passwordmanager.entity.*;
import com.rev.passwordmanager.repository.*;
import com.rev.passwordmanager.util.AESUtil;
import com.rev.passwordmanager.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class UserService {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityQuestionRepository securityQuestionRepository;

    @Autowired
    private UserSecurityAnswerRepository userSecurityAnswerRepository;

    @Autowired
    private PasswordEntryRepository passwordEntryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Registers a new user with hashed passwords and defined security questions.
     * Why: Enforces uniqueness on username and email and securely hashes credentials
     * to prevent plain-text breaches.
     */
    @org.springframework.transaction.annotation.Transactional
    public ApiResponse<?> registerUser(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return new ApiResponse<>(false, "Username already exists!", null);
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return new ApiResponse<>(false, "Email already exists!", null);
        }

        if (request.getSecurityAnswers() == null || request.getSecurityAnswers().size() < 3) {
            return new ApiResponse<>(false, "Minimum 3 security questions required!", null);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setMasterPassword(passwordEncoder.encode(request.getMasterPassword()));
        user.setTwoFactorEnabled(false);

        user = userRepository.save(user);

        for (SecurityAnswerDTO dto : request.getSecurityAnswers()) {

            SecurityQuestion question = securityQuestionRepository
                    .findById(dto.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Invalid question ID"));

            UserSecurityAnswer answer = new UserSecurityAnswer();
            answer.setUser(user);
            answer.setSecurityQuestion(question);
            answer.setAnswerHash(passwordEncoder.encode(dto.getAnswer()));

            userSecurityAnswerRepository.save(answer);
        }

        return new ApiResponse<>(true, "User registered successfully!", null);
    }

    /**
     * Authenticates a user by comparing the incoming master password with the stored hash.
     * Why: Protects the application via JWTs so that APIs can confidently verify
     * the caller's identity on subsequent requests.
     */
    public ApiResponse<?> loginUser(LoginRequest request) {

        var optionalUser = userRepository.findByUsername(request.getUsername());

        if (optionalUser.isEmpty()) {
            return new ApiResponse<>(false, "User not found!", null);
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(
                request.getMasterPassword(),
                user.getMasterPassword())) {

            return new ApiResponse<>(false, "Invalid password!", null);
        }

        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            return new ApiResponse<>(true, "2FA_REQUIRED", null);
        }

        String token = jwtUtil.generateToken(user.getUsername());

        return new ApiResponse<>(true, "Login successful", token);
    }

    /**
     * Adds an encrypted password entry into the specified user's vault.
     * Why: Isolates private credentials per user and protects sensitive data using AES encryption
     * so that even a database leak doesn't reveal passwords.
     */
    @org.springframework.transaction.annotation.Transactional
    public ApiResponse<?> addPassword(AddPasswordRequest request, String usernameFromToken) {


        User user = userRepository.findByUsername(usernameFromToken).orElse(null);

        if (user == null) {
            return new ApiResponse<>(false, "User not found", null);
        }

        PasswordEntry entry = new PasswordEntry();
        entry.setUser(user);
        entry.setAccountName(request.getAccountName());
        entry.setWebsite(request.getWebsite());
        entry.setUsername(request.getUsername());
        entry.setEncryptedPassword(AESUtil.encrypt(request.getPassword()));
        entry.setCategory(request.getCategory());
        entry.setNotes(request.getNotes());

        passwordEntryRepository.save(entry);

        return new ApiResponse<>(true, "Password saved successfully!", null);
    }

    /**
     * Fetches a paginated slice of the user's decrypted passwords.
     * Why: Paginates data to keep the frontend speedy, and decrypts credentials right before
     * sending so the UI can display them correctly.
     */
    public ApiResponse<?> getPasswordsByUser(String usernameFromToken,int page,int size,String sortBy) {

        User user = userRepository.findByUsername(usernameFromToken).orElse(null);

        if (user == null) {
            return new ApiResponse<>(false, "User not found", null);
        }

        Pageable pageable = PageRequest.of(page,size,Sort.by(sortBy).descending());

        Page<PasswordEntry> pageResult =
                passwordEntryRepository.findByUserId(user.getId(), pageable);

        var responseList = pageResult.getContent()
                .stream()
                .map(entry ->
                        new PasswordResponse(
                                entry.getId(),
                                entry.getAccountName(),
                                entry.getWebsite(),
                                entry.getUsername(),
                                AESUtil.decrypt(entry.getEncryptedPassword()),
                                entry.getCategory(),
                                entry.getNotes(),
                                entry.getFavorite()
                        )
                ).toList();

        var pageData = new PaginationResponse<>(
                responseList,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );

        return new ApiResponse<>(true,"Passwords fetched successfully",pageData);
    }

    /**
     * Searches for passwords by keyword (e.g., accountName, username, website).
     * Why: Improves user experience by allowing them to quickly find specific credentials
     * in large vaults.
     */
    public ApiResponse<?> searchPasswords(String keyword, String usernameFromToken) {

        User user = userRepository.findByUsername(usernameFromToken).orElse(null);

        if (user == null) {
            return new ApiResponse<>(false, "User not found", null);
        }

        var results = passwordEntryRepository.searchPasswords(user.getId(), keyword);

        var response = results.stream()
                .map(entry ->
                        new PasswordResponse(
                                entry.getId(),
                                entry.getAccountName(),
                                entry.getWebsite(),
                                entry.getUsername(),
                                AESUtil.decrypt(entry.getEncryptedPassword()),
                                entry.getCategory(),
                                entry.getNotes(),
                                entry.getFavorite()
                        )
                ).toList();

        return new ApiResponse<>(true, "Search results", response);
    }

    /**
     * Gets a single password entry for the edit screen.
     * Why: Necessary for populating individual edit pages reliably.
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ApiResponse<?> getPasswordById(Long id, String usernameFromToken) {
        User user = userRepository.findByUsername(usernameFromToken).orElse(null);
        if (user == null) {
            return new ApiResponse<>(false, "User not found", null);
        }

        PasswordEntry entry = passwordEntryRepository.findById(id).orElse(null);
        if (entry == null || !entry.getUser().getId().equals(user.getId())) {
            return new ApiResponse<>(false, "Password entry not found", null);
        }

        PasswordResponse response = new PasswordResponse(
                entry.getId(),
                entry.getAccountName(),
                entry.getWebsite(),
                entry.getUsername(),
                AESUtil.decrypt(entry.getEncryptedPassword()),
                entry.getCategory(),
                entry.getNotes(),
                entry.getFavorite()
        );

        return new ApiResponse<>(true, "Password fetched successfully", response);
    }

    /**
     * Filters a user's passwords by a specific category.
     * Why: Helps organize vaults (e.g., "Work", "Social", "Finance") for easier navigation.
     */
    public ApiResponse<?> getPasswordsByCategory(String category, String usernameFromToken) {

        User user = userRepository.findByUsername(usernameFromToken).orElse(null);

        if (user == null) {
            return new ApiResponse<>(false, "User not found", null);
        }

        var entries = passwordEntryRepository
                .findByUserIdAndCategory(user.getId(), category);

        var response = entries.stream()
                .map(entry ->
                        new PasswordResponse(
                                entry.getId(),
                                entry.getAccountName(),
                                entry.getWebsite(),
                                entry.getUsername(),
                                AESUtil.decrypt(entry.getEncryptedPassword()),
                                entry.getCategory(),
                                entry.getNotes(),
                                entry.getFavorite()
                        )
                ).toList();

        return new ApiResponse<>(true, "Category passwords", response);
    }

    /**
     * Deletes a specified password entry, verifying ownership first.
     * Why: Crucial for data lifecycle management. The ownership check prevents
     * broken object level authorization (BOLA) attacks.
     */
    public ApiResponse<?> deletePassword(Long passwordId, String usernameFromToken) {

        User user = userRepository.findByUsername(usernameFromToken).orElse(null);

        if (user == null) {
            return new ApiResponse<>(false, "User not found", null);
        }

        PasswordEntry entry = passwordEntryRepository.findById(passwordId).orElse(null);

        if (entry == null) {
            return new ApiResponse<>(false, "Password entry not found", null);
        }

        if (!entry.getUser().getId().equals(user.getId())) {
            return new ApiResponse<>(false, "Unauthorized to delete this password", null);
        }

        passwordEntryRepository.delete(entry);

        return new ApiResponse<>(true, "Password deleted successfully", null);
    }

    /**
     * Updates an existing password entry.
     * Why: Supports keeping credentials up to date. Also verifies ownership
     * to prevent unauthorized modifications.
     */
    public ApiResponse<?> updatePassword(UpdatePasswordRequest request,String usernameFromToken) {

        User user = userRepository.findByUsername(usernameFromToken).orElse(null);

        if (user == null) {
            return new ApiResponse<>(false, "User not found", null);
        }

        PasswordEntry entry = passwordEntryRepository.findById(request.getId()).orElse(null);

        if (entry == null) {
            return new ApiResponse<>(false, "Password entry not found", null);
        }

        if (!entry.getUser().getId().equals(user.getId())) {
            return new ApiResponse<>(false, "Unauthorized to update this password", null);
        }

        entry.setAccountName(request.getAccountName());
        entry.setWebsite(request.getWebsite());
        entry.setUsername(request.getUsername());
        entry.setEncryptedPassword(AESUtil.encrypt(request.getPassword()));
        entry.setCategory(request.getCategory());
        entry.setNotes(request.getNotes());

        passwordEntryRepository.save(entry);

        return new ApiResponse<>(true, "Password updated successfully", null);
    }

    /**
     * Toggles the "favorite" flag on a stored password.
     * Why: Enables users to pin heavily used items for quick access on the frontend.
     */
    public ApiResponse<?> toggleFavorite(Long passwordId,String usernameFromToken) {

        User user = userRepository.findByUsername(usernameFromToken).orElse(null);

        if (user == null) {
            return new ApiResponse<>(false, "User not found", null);
        }

        PasswordEntry entry = passwordEntryRepository.findById(passwordId).orElse(null);

        if (entry == null) {
            return new ApiResponse<>(false, "Password entry not found", null);
        }

        if (!entry.getUser().getId().equals(user.getId())) {
            return new ApiResponse<>(false, "Unauthorized", null);
        }

        entry.setFavorite(!entry.getFavorite());

        passwordEntryRepository.save(entry);

        return new ApiResponse<>(true,"Favorite status updated",entry.getFavorite());
    }

    /**
     * Generates a short-lived 6-digit OTP for Two-Factor Authentication.
     * Why: Replaces static security questions for 2FA with a time-sensitive, dynamic code 
     * that can be sent via email or SMS, significantly increasing login security.
     */
    public ApiResponse<?> generateOtp(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return new ApiResponse<>(false, "User not found", null);
        }

        if (!user.isTwoFactorEnabled()) {
            return new ApiResponse<>(false, "2FA is not enabled for this account", null);
        }

        // Generate a 6-digit random OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        
        // Expiry in 2 minutes
        user.setOtpCode(otp);
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(2));
        userRepository.save(user);

        // Return the OTP so the frontend can display it in the simulated popup
        return new ApiResponse<>(true, "OTP generated successfully", otp);
    }

    /**
     * Validates the dynamically generated OTP against the database and expiration limits.
     * Why: Finalizes the 2FA process. Ensuring the code hasn't expired mitigates replay 
     * attacks and brute forcing.
     */
    public ApiResponse<?> verify2FA(Verify2FARequest request) {

        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (user == null) {
            return new ApiResponse<>(false, "User not found", null);
        }

        if (user.getOtpCode() == null || user.getOtpExpiry() == null) {
            return new ApiResponse<>(false, "No OTP found. Please generate an OTP first.", null);
        }

        if (java.time.LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            return new ApiResponse<>(false, "OTP has expired. Please generate a new one.", null);
        }

        if (!user.getOtpCode().equals(request.getOtp())) {
            return new ApiResponse<>(false, "Invalid OTP!", null);
        }

        // OTP is correct - Clear it
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());

        return new ApiResponse<>(true, "2FA verification successful", token);
    }


    /**
     * Allows authenticated users to change their Master Password.
     * Why: Necessary for recovering from potential breaches or maintaining good password hygiene.
     */
    public ApiResponse<?> changeMasterPassword(
            String username,
            ChangePasswordRequest request) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // check current password
        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getMasterPassword())) {

            return new ApiResponse<>(
                    false,
                    "Current password incorrect",
                    null
            );
        }

        // update password
        user.setMasterPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);

        return new ApiResponse<>(
                true,
                "Master password updated successfully",
                null
        );
    }


    /**
     * Updates user profile information (Name, Phone, Email).
     * Why: Allows users to manage their contact and recovery information. Also ensures
     * email uniqueness across the system.
     */
    public ApiResponse<?> updateProfile(String username, UpdateProfileRequest request) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // CHECK IF EMAIL ALREADY USED BY ANOTHER USER
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());

        if(existingUser.isPresent() && existingUser.get().getId() != user.getId()){
            return new ApiResponse<>(false, "Email already used by another account", null);
        }

        // UPDATE PROFILE
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        userRepository.save(user);

        return new ApiResponse<>(
                true,
                "Profile updated successfully",
                user
        );
    }

    /**
     * Fetches current profile data for an authenticated user.
     * Why: Populates the profile settings view in the frontend.
     */
    public ApiResponse<?> getProfile(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return new ApiResponse<>(false, "User not found", null);
        }

        ProfileResponse profile = new ProfileResponse(
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getUsername(),
                user.isTwoFactorEnabled()
        );

        return new ApiResponse<>(true, "Profile retrieved successfully", profile);
    }


    /**
     * Retrieves the list of security questions the user selected during registration.
     * Why: Needed to prompt the user during the password recovery flow without revealing answers.
     */
    public ApiResponse<?> getSecurityQuestions(String username){

        User user = userRepository.findByUsername(username).orElse(null);

        if(user == null){
            return new ApiResponse<>(false,"User not found",null);
        }

        List<UserSecurityAnswer> answers =
                userSecurityAnswerRepository.findByUser_IdOrderByIdAsc(user.getId());

        List<SecurityQuestion> questions = answers.stream()
                .map(UserSecurityAnswer::getSecurityQuestion)
                .toList();

        return new ApiResponse<>(true,"Questions fetched",questions);
    }


    /**
     * Resets the user's master password if they correctly answer their security questions.
     * Why: A vital fallback mechanism to prevent users from permanently losing access
     * to their vault if they forget their master password.
     */
    @org.springframework.transaction.annotation.Transactional
    public ApiResponse<?> recoverPassword(RecoverPasswordRequest request){

        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if(user == null){
            return new ApiResponse<>(false,"User not found",null);
        }

        if (request.getAnswers() == null || request.getAnswers().size() != 3) {
            return new ApiResponse<>(false, "You must provide exactly 3 security answers.", null);
        }

        List<UserSecurityAnswer> storedAnswers =
                userSecurityAnswerRepository.findByUser_IdOrderByIdAsc(user.getId());

        for(int i=0;i<storedAnswers.size();i++){

            boolean match = passwordEncoder.matches(
                    request.getAnswers().get(i),
                    storedAnswers.get(i).getAnswerHash()
            );

            if(!match){
                return new ApiResponse<>(false,"Security answers incorrect",null);
            }
        }

        user.setMasterPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);

        return new ApiResponse<>(true,"Password reset successful",null);
    }


    /**
     * Toggles whether the account requires 2FA on login.
     * Why: Gives the user control over their own security posture.
     */
    public ApiResponse<?> toggle2FA(String token){

        String username = jwtUtil.extractUsername(token.replace("Bearer ",""));

        User user = userRepository.findByUsername(username).orElse(null);

        if(user == null){
            return new ApiResponse<>(false,"User not found",null);
        }

        user.setTwoFactorEnabled(!user.isTwoFactorEnabled());

        userRepository.save(user);

        return new ApiResponse<>(true,"2FA status updated",user.isTwoFactorEnabled());

    }


    /**
     * Calculates a security score by checking for weak, old, and reused passwords across the vault.
     * Why: Encourages users to adopt better security practices by penalizing passwords
     * that are short, reused across multiple websites, or haven't been rotated in 6+ months.
     */
    public ApiResponse<?> getSecurityAudit(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return new ApiResponse<>(false, "User not found", null);
        }

        List<PasswordEntry> entries = passwordEntryRepository.findByUserId(user.getId(), PageRequest.of(0, 10000)).getContent();
        
        List<PasswordResponse> allPasswords = entries.stream()
            .map(entry -> new PasswordResponse(
                entry.getId(), entry.getAccountName(), entry.getWebsite(), entry.getUsername(),
                AESUtil.decrypt(entry.getEncryptedPassword()), entry.getCategory(), entry.getNotes(), entry.getFavorite()
            )).collect(Collectors.toList());

        List<PasswordResponse> weakPasswords = allPasswords.stream()
            .filter(p -> p.getPassword() == null || p.getPassword().length() < 8)
            .collect(Collectors.toList());

        Map<String, List<PasswordResponse>> passwordGroups = new HashMap<>();
        for (PasswordResponse p : allPasswords) {
            passwordGroups.computeIfAbsent(p.getPassword(), k -> new java.util.ArrayList<>()).add(p);
        }

        List<PasswordResponse> reusedPasswords = new java.util.ArrayList<>();
        for (List<PasswordResponse> group : passwordGroups.values()) {
            if (group.size() > 1) {
                reusedPasswords.addAll(group);
            }
        }

        List<PasswordResponse> oldPasswords = entries.stream()
            .filter(e -> e.getCreatedAt() != null && java.time.LocalDateTime.now().minusMonths(6).isAfter(e.getCreatedAt()))
            .map(entry -> new PasswordResponse(
                entry.getId(), entry.getAccountName(), entry.getWebsite(), entry.getUsername(),
                AESUtil.decrypt(entry.getEncryptedPassword()), entry.getCategory(), entry.getNotes(), entry.getFavorite()
            )).collect(Collectors.toList());

        int score = 100;
        score -= weakPasswords.size() * 10;
        score -= (reusedPasswords.size() / 2) * 5;
        score -= oldPasswords.size() * 2;
        if (score < 0) score = 0;

        SecurityAuditResponse audit = new SecurityAuditResponse(weakPasswords, reusedPasswords, oldPasswords, score);
        return new ApiResponse<>(true, "Security audit completed", audit);
    }

    /**
     * Exports all user passwords by serializing them to JSON and encrypting the payload.
     * Why: Critical feature for avoiding vendor lock-in and allowing users to safely 
     * back up their sensitive data.
     */
    public ApiResponse<?> exportVault(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return new ApiResponse<>(false, "User not found", null);
        }

        List<PasswordEntry> entries = passwordEntryRepository.findByUserId(user.getId(), PageRequest.of(0, 10000)).getContent();
        List<PasswordResponse> allPasswords = entries.stream()
            .map(entry -> new PasswordResponse(
                entry.getId(), entry.getAccountName(), entry.getWebsite(), entry.getUsername(),
                AESUtil.decrypt(entry.getEncryptedPassword()), entry.getCategory(), entry.getNotes(), entry.getFavorite()
            )).collect(Collectors.toList());

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(allPasswords);
            String encryptedBackup = AESUtil.encrypt(json);
            return new ApiResponse<>(true, "Vault exported successfully", encryptedBackup);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to export vault", null);
        }
    }

    /**
     * Decrypts and imports an external JSON vault backup payload.
     * Why: Allows users to seamlessly restore their vault from a previously generated backup file.
     */
    public ApiResponse<?> importVault(String username, String encryptedBackup) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return new ApiResponse<>(false, "User not found", null);
        }

        try {
            String json = AESUtil.decrypt(encryptedBackup);
            ObjectMapper mapper = new ObjectMapper();
            List<PasswordResponse> importedPasswords = mapper.readValue(json, new TypeReference<List<PasswordResponse>>(){});
            
            for (PasswordResponse p : importedPasswords) {
                PasswordEntry entry = new PasswordEntry();
                entry.setUser(user);
                entry.setAccountName(p.getAccountName());
                entry.setWebsite(p.getWebsite());
                entry.setUsername(p.getUsername());
                entry.setEncryptedPassword(AESUtil.encrypt(p.getPassword()));
                entry.setCategory(p.getCategory());
                entry.setNotes(p.getNotes());
                entry.setFavorite(p.getFavorite() != null ? p.getFavorite() : false);
                passwordEntryRepository.save(entry);
            }
            return new ApiResponse<>(true, "Vault imported successfully", null);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Failed to import vault. Invalid backup file.", null);
        }
    }
}
