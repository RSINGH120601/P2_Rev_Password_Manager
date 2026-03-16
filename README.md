# RevVault - Secure Password Manager

RevVault is a secure, full-stack password management application. It allows users to securely store and manage their passwords using AES-256 for vault encryption, BCrypt for master password hashing, and JWT for secure session management.

## Project Structure

This project follows a decoupled architecture:
*   **Backend:** Java Spring Boot (REST APIs, Security Logic, Database Interaction)
*   **Frontend:** Pure HTML, CSS, JavaScript (Glassmorphism UI, client-side logic)
*   **Database:** Oracle Database (Managed via Spring Data JPA / Hibernate)

---

## 🚀 Setup Instructions

### Prerequisites
Before you begin, ensure you have the following installed on your system:
1.  **Java Development Kit (JDK) 17 or higher**
2.  **Apache Maven** (for building the backend)
3.  **Oracle Database** (or you can switch to another DB like MySQL/PostgreSQL in the config)
4.  A modern Web Browser (Chrome, Firefox, Edge, Safari)

---

### Step 1: Database Setup
The backend is configured to connect to a local Oracle Database. 
1. Make sure your Oracle database instance is running.
2. The application is configured to connect to `jdbc:oracle:thin:@//localhost:1521/orclpdb`.
3. If you need to change the database credentials, open `src/main/resources/application.properties` and update the following lines to match your local Oracle setup:
   ```properties
   spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/orclpdb
   spring.datasource.username=password_manager
   spring.datasource.password=pm123
   ```
*(Note: Spring Boot's 'update' DDL setting will automatically create the necessary tables when the application starts.)*

---

### Step 2: Running the Backend (Spring Boot)
1. Open up a terminal/command prompt.
2. Navigate to the root directory of the project (`passwordmanager` folder containing the `pom.xml`).
3. Clean and build the project using Maven:
   ```bash
   mvn clean install
   ```
4. Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```
5. The backend server will start and run on `http://localhost:8084`.

---

### Step 3: Running the Frontend
The frontend requires no build steps or heavy Node.js frameworks! It uses pure standard web technologies.

1. Ensure the backend Spring Boot server is perfectly running.
2. Open the `Frontend` folder in your file explorer.
3. Verify the API connection URL in `Frontend/js/config.js`:
   ```javascript
   const API_BASE = "http://localhost:8084/auth";
   ```
4. Double-click **`login.html`** or **`register.html`** to open it directly in your web browser.
5. Create an account, set up your security questions, and start securely managing your passwords!

---

## 🛡️ Key Features
*   **Zero-Knowledge Architecture approach:** Vault passwords are AES-encrypted before being stored in the database.
*   **Two-Factor Authentication (2FA):** Optional OTP verification on login.
*   **Master Password Verification:** Required before viewing or editing sensitive stored entries.
*   **Security Audit:** In-built checker to scan for weak, old, or reused passwords.
*   **Custom i18n:** Built-in lightweight multi-language support (English, Hindi, French, Japanese).
*   **Dark/Light Mode:** Remembered automatically via browser LocalStorage.

## 👥 Group Project Division (Suggested)
If working in a team of 4, the recommended division is:
1.  **Security & Identity Architect:** Authentication, JWT, Encryption, BCrypt, 2FA.
2.  **Backend Developer:** Vault CRUD APIs, Security Audit algorithms, Recovery Flows.
3.  **Frontend Developer (UI/UX):** HTML/CSS layout, Glassmorphism, Theme switching, i18n logic.
4.  **Full-Stack Integrator:** Axios API integration, State management, Validations, Testing.
