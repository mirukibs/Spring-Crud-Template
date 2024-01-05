package com.spring.SpringCrudTemplate.services;

import com.spring.SpringCrudTemplate.DTOs.RegistrationDTO;
import com.spring.SpringCrudTemplate.exceptions.RoleNotFoundException;
import com.spring.SpringCrudTemplate.exceptions.UserNotFoundException;
import com.spring.SpringCrudTemplate.models.AppUser;
import com.spring.SpringCrudTemplate.models.Role;
import com.spring.SpringCrudTemplate.repositories.AppUserRepository;
import com.spring.SpringCrudTemplate.repositories.RoleRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static com.spring.SpringCrudTemplate.configurations.EmailValidator.validateEmail;

/**
 * Service class responsible for user registration logic.
 */
@Service
@AllArgsConstructor
public class RegistrationService {

    @Autowired
    private final AppUserService appUserService;
    @Autowired
    private final RoleRepository roleRepository;
    @Autowired
    private final AppUserRepository appUserRepository;
    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    /**
     * Registers a new user based on the provided RegistrationDTO.
     *
     * @param request The RegistrationDTO containing user registration information.
     * @return ResponseEntity indicating the success or failure of the registration process.
     */
    public ResponseEntity<String> registerUser(RegistrationDTO request) {
        validateEmail(request.getEmail());

        AppUser appUser = createAppUserFromRequest(request);

        Role role = getRoleByName();

        appUser.setRoles(Collections.singletonList(role));

        return appUserService.signUpUser(appUser);
    }

    /**
     * Retrieves the user ID for a given email after successful registration.
     *
     * @param userEmail The email of the registered user.
     * @return The user ID.
     * @throws UsernameNotFoundException if the user is not found.
     */
    public Long getSavedUserId(String userEmail) {
        try {
            AppUser savedUser = findUserByEmail(userEmail);

            return savedUser.getUserID();
        } catch (UsernameNotFoundException e) {
            handleUserRetrievalError(userEmail, e);
            throw e;
        }
    }


    /**
     * Creates an AppUser entity from the provided RegistrationDTO.
     *
     * @param request The RegistrationDTO containing user registration information.
     * @return The created AppUser entity.
     */
    private AppUser createAppUserFromRequest(RegistrationDTO request) {
        AppUser appUser = new AppUser();
        appUser.setFirstName(request.getFirstname());
        appUser.setLastName(request.getLastname());
        appUser.setEmail(request.getEmail());
        appUser.setPassword(request.getPassword());
        return appUser;
    }

    /**
     * Retrieves the default role ("USER") from the RoleRepository.
     *
     * @return The retrieved Role entity.
     * @throws RoleNotFoundException if the role is not found.
     */
    private Role getRoleByName() {
        return roleRepository.findByName("USER")
                .orElseThrow(() -> new RoleNotFoundException("Role not found: USER"));
    }

    /**
     * Finds a user by email using the AppUserRepository.
     *
     * @param userEmail The email of the user to find.
     * @return The found AppUser entity.
     * @throws UserNotFoundException if the user is not found.
     */
    private AppUser findUserByEmail(String userEmail) {
        return appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found after registration"));
    }

    /**
     * Handles errors during user ID retrieval after registration.
     *
     * @param userEmail The email of the user for whom the error occurred.
     * @param e         The exception that occurred.
     */
    private void handleUserRetrievalError(String userEmail, Exception e) {
        log.error("Error retrieving user ID after registration for email: {}", userEmail, e);
    }
}
