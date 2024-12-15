package com.project.planpulse.service;

import com.project.planpulse.model.Board;
import com.project.planpulse.model.PasswordResetToken;
import com.project.planpulse.model.Task;
import com.project.planpulse.model.User;
import com.project.planpulse.repository.BoardRepository;
import com.project.planpulse.repository.PasswordResetTokenRepository;
import com.project.planpulse.repository.TaskRepository;
import com.project.planpulse.repository.UserRepository;
import com.project.planpulse.validation.EmailValidator;
import com.project.planpulse.validation.PasswordValidator;
import com.project.planpulse.validation.UsernameValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;


    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String UPLOAD_DIR = "uploads/";


    // user registration with multipart
    public User registerUserWithMultipart(String firstName,
                                          String lastName,
                                          String username,
                                          String email,
                                          String password,
                                          String confirmPassword,
                                          MultipartFile profileImage) throws IOException {
        validateUserRegistrationFields(firstName, lastName, username, email, password, confirmPassword);
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            throw new RuntimeException("Username or Email already exists");
        }
        // if profile image is provided, store it
        String profileImageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            profileImageUrl = storeProfileImage(profileImage);
        }

        User user = new User();
        user.setFirstname(firstName);
        user.setLastname(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setProfileImageUrl(profileImageUrl);
        user.setBoardIds(new ArrayList<>());
        return userRepository.save(user);
    }

    // update user with multipart
    public User updateUserWithMultipart(String userId,
                                        String username,
                                        String email,
                                        String firstname,
                                        String lastname,
                                        MultipartFile profileImage) throws IOException, RuntimeException {
        User existingUser = getUserById(userId);
        // update fields if provided
        if (username != null && !username.isBlank()) {
            if (!existingUser.getUsername().equals(username) && userRepository.existsByUsername(username)) {
                throw new RuntimeException("Username already taken");
            }
            if (!isValidUsername(username)) {
                throw new RuntimeException("Invalid username. It must be 3-30 characters long and can only contain letters, numbers, dots, underscores, and hyphens.");
            }
            existingUser.setUsername(username);
        }
        if (email != null && !email.isBlank()) {
            if (!existingUser.getEmail().equals(email) && userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email already in use");
            }
            if (!isValidEmail(email)) {
                throw new RuntimeException("Invalid email format");
            }
            existingUser.setEmail(email);
        }
        if (firstname != null && !firstname.isBlank()) {
            existingUser.setFirstname(firstname);
        }
        if (lastname != null && !lastname.isBlank()) {
            existingUser.setLastname(lastname);
        }

        // handle profile image update if new image is provided
        if (profileImage != null && !profileImage.isEmpty()) {
            String oldImageUrl = existingUser.getProfileImageUrl();
            // store the new image first
            String newImageUrl = storeProfileImage(profileImage);
            // delete old image if it exists
            if (oldImageUrl != null && !oldImageUrl.isBlank()) {
                deleteProfileImage(oldImageUrl);
            }
            // update user with the new URL
            existingUser.setProfileImageUrl(newImageUrl);
        }

        return userRepository.save(existingUser);
    }

    private void validateUserRegistrationFields(String firstName, String lastName, String username, String email, String password, String confirmPassword) {
        if (firstName == null || firstName.isBlank()) {
            throw new RuntimeException("Firstname is required");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new RuntimeException("Lastname is required");
        }
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Username is required");
        }
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (!isValidEmail(email)) {
            throw new RuntimeException("Invalid email format");
        }
        if (!isValidUsername(username)) {
            throw new RuntimeException("Invalid username. It must be 3-30 characters long and can only contain letters, numbers, dots, underscores, and hyphens.");
        }
        if (password == null || password.isBlank()) {
            throw new RuntimeException("Password is required");
        }
        if (confirmPassword == null || confirmPassword.isBlank()) {
            throw new RuntimeException("Confirm password is required");
        }
        if (!password.equals(confirmPassword)) {
            throw new RuntimeException("Password and confirmation password do not match");
        }
        if (!isValidPassword(password)) {
            throw new RuntimeException("Password does not meet requirements");
        }
    }

    private String storeProfileImage(MultipartFile file) throws IOException, RuntimeException {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty. Please upload a valid image.");
        }
        // validate MIME type
        String contentType = file.getContentType();
        if (contentType == null || !isValidImageMimeType(contentType)) {
            throw new RuntimeException("Invalid file type. Only image files are allowed.");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasValidImageExtension(originalFilename)) {
            throw new RuntimeException("Invalid file extension. Supported formats are: .jpg, .jpeg, .png, .gif.");
        }
        // validate file content (magic number check)
        if (!isValidImageMagicNumber(file)) {
            throw new RuntimeException("Uploaded file is not a valid image.");
        }
        // unique filename generation below
        String extension = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
        String uniqueFilename = UUID.randomUUID() + "-" + System.currentTimeMillis() + extension;

        // create directory if it does not exist
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            boolean uploadResult = uploadDir.mkdirs();
            if (!uploadResult) throw new IOException("Failed to create upload directory: " + UPLOAD_DIR);
        }

        // save file to the uploads directory
        Path filePath = Paths.get(UPLOAD_DIR, uniqueFilename);
        Files.write(filePath, file.getBytes(), StandardOpenOption.CREATE_NEW);
        System.out.println("File saved to: " + filePath.toAbsolutePath());
        // relative URL for storage in the database
        return UPLOAD_DIR + uniqueFilename;
    }

    private void deleteProfileImage(String imageUrl) {
        // uploads/filename.extension - file format processed
        if (imageUrl == null || !imageUrl.startsWith(UPLOAD_DIR)) {
            return;
        }
        String filename = imageUrl.substring(UPLOAD_DIR.length());
        Path filePath = Paths.get(UPLOAD_DIR, filename);
        try {
            boolean isDeleted = Files.deleteIfExists(filePath);
            if (isDeleted) {
                System.out.println("Deleted profile image: " + filePath.toAbsolutePath());
            } else {
                System.err.println("No file found to delete at: " + filePath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Failed to delete profile image: " + filePath + " - " + e.getMessage());
        }
    }

    public ResponseEntity<Resource> loadProfileImage(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // profile image URL
        String profileImageUrl = user.getProfileImageUrl();
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            throw new RuntimeException("No profile image found");
        }

        String filename = profileImageUrl.substring(UPLOAD_DIR.length());

        // ensure the file format is supported
        if (!hasValidImageExtension(filename)) {
            throw new RuntimeException("Unsupported file format: " + filename);
        }

        // load the resource from storage
        Path filePath = Paths.get(UPLOAD_DIR, filename).normalize();
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("File not found or not readable: " + filename);
            }

            // get the mime type of the image
            MediaType contentType = checkContentType(filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .contentType(contentType)
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found or not readable: " + filename);
        }
    }

    public MediaType checkContentType(String fileName) {
        try {
            String mimeType = Files.probeContentType(Paths.get(fileName));
            return mimeType != null ? MediaType.parseMediaType(mimeType) : MediaType.APPLICATION_OCTET_STREAM;
        } catch (IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM; // default to binary stream if MIME type is unknown
        }
    }


    public void deleteUser(String userId) throws RuntimeException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // delete user profile image if exists
        String imageUrl = user.getProfileImageUrl();
        if (imageUrl != null && !imageUrl.isBlank()) {
            deleteProfileImage(imageUrl);
        }

        // handle boards created by the user
        List<Board> createdBoards = boardRepository.findByCreatorId(userId);
        for (Board board : createdBoards) {
            List<String> collaboratorIds = board.getCollaboratorIds();

            // check if the board has no other collaborators and remove the board along with tasks
            if (collaboratorIds == null || collaboratorIds.isEmpty()) {
                List<Task> tasks = taskRepository.findByBoardId(board.getId());
                taskRepository.deleteAll(tasks);
                boardRepository.delete(board);
            }
        }

        // handle boards where the user is a collaborator
        List<Board> collaboratingBoards = boardRepository.findByCollaboratorIdsContaining(userId);
        for (Board board : collaboratingBoards) {
            List<String> collaboratorIds = board.getCollaboratorIds();
            if (collaboratorIds != null) {
                // remove the user from collaborators
                collaboratorIds.remove(userId);
            }

            // if no collaborators and no creator remain then delete the board & tasks
            boolean noRemainingUsers = (collaboratorIds == null || collaboratorIds.isEmpty())
                    && (board.getCreatorId() == null || !userRepository.existsById(board.getCreatorId()));

            if (noRemainingUsers) {
                // delete the board & tasks
                List<Task> tasks = taskRepository.findByBoardId(board.getId());
                taskRepository.deleteAll(tasks);
                boardRepository.delete(board);
            } else {
                // update the board with the remaining collaborators
                board.setCollaboratorIds(collaboratorIds);
                boardRepository.save(board);
            }
        }

        userRepository.deleteById(userId);
    }

    public User getUserById(String userId) throws RuntimeException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Invalid user credentials"));
    }

    private boolean isValidImageMimeType(String mimeType) {
        return mimeType.equals("image/jpeg") ||
                mimeType.equals("image/png") ||
                mimeType.equals("image/gif") ||
                mimeType.equals("image/webp") ||
                mimeType.equals("image/bmp");
    }

    private boolean hasValidImageExtension(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        return lowerCaseFilename.endsWith(".jpg") ||
                lowerCaseFilename.endsWith(".jpeg") ||
                lowerCaseFilename.endsWith(".png") ||
                lowerCaseFilename.endsWith(".gif") ||
                lowerCaseFilename.endsWith(".webp") ||
                lowerCaseFilename.endsWith(".bmp");
    }

    private boolean isValidImageMagicNumber(MultipartFile file) throws IOException {
        byte[] headerBytes = new byte[8];
        try (var inputStream = file.getInputStream()) {
            if (inputStream.read(headerBytes) < 8) {
                return false; // File too small to be valid
            }
        }

        // JPEG: FF D8 FF
        if (headerBytes[0] == (byte) 0xFF && headerBytes[1] == (byte) 0xD8 && headerBytes[2] == (byte) 0xFF) {
            return true;
        }
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (headerBytes[0] == (byte) 0x89 && headerBytes[1] == (byte) 0x50 &&
                headerBytes[2] == (byte) 0x4E && headerBytes[3] == (byte) 0x47 &&
                headerBytes[4] == (byte) 0x0D && headerBytes[5] == (byte) 0x0A &&
                headerBytes[6] == (byte) 0x1A && headerBytes[7] == (byte) 0x0A) {
            return true;
        }
        // GIF: 47 49 46 38
        if (headerBytes[0] == (byte) 0x47 && headerBytes[1] == (byte) 0x49 &&
                headerBytes[2] == (byte) 0x46 && headerBytes[3] == (byte) 0x38) {
            return true;
        }
        // BMP: 42 4D
        if (headerBytes[0] == (byte) 0x42 && headerBytes[1] == (byte) 0x4D) {
            return true;
        }

        // Unsupported file type
        return false;
    }

    public User authenticateByUsername(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Invalid login credentials"));
        if (user != null && passwordEncoder.matches(password, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public User authenticateByEmail(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid login credentials"));
        if (user != null && passwordEncoder.matches(password, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public void initiatePasswordReset(String email) throws RuntimeException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid credentials"));
        PasswordResetToken resetToken = new PasswordResetToken(user.getId(), 15); // 15 mins expiry time
        passwordResetTokenRepository.save(resetToken);
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken());
    }

    public void resetPasswordWithToken(String token, String newPassword) {
        if (!isValidPassword(newPassword)) {
            throw new RuntimeException("Password does not meet requirements");
        }
        Optional<PasswordResetToken> optionalToken = passwordResetTokenRepository.findByToken(token);
        if (optionalToken.isEmpty()) {
            throw new RuntimeException("Invalid token");
        }
        PasswordResetToken resetToken = optionalToken.get();
        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new RuntimeException("Token has expired");
        }
        User user = userRepository.findById(resetToken.getUserId()).orElseThrow(() -> new RuntimeException("Invalid token or credentials"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
    }

    public void resetPassword(User user, String currentPassword, String newPassword, String confirmPassword) throws RuntimeException {
        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("New password cannot be empty");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("New password and confirmation do not match");
        }
        if (!isValidPassword(newPassword)) {
            throw new RuntimeException("Password does not meet requirements");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(hashedPassword);
        userRepository.save(user);
    }

    private boolean isValidPassword(String password) {
        PasswordValidator validator = new PasswordValidator();
        return validator.isValid(password, null);
    }

    private boolean isValidUsername(String username) {
        UsernameValidator validator = new UsernameValidator();
        return validator.isValid(username, null);
    }

    private boolean isValidEmail(String email) {
        EmailValidator validator = new EmailValidator();
        return validator.isValid(email, null);
    }

    public User getUserByEmail(String email, String requesterId) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!user.getId().equals(requesterId)) {
            throw new RuntimeException("Unauthorized access");
        }
        return user;
    }
}
