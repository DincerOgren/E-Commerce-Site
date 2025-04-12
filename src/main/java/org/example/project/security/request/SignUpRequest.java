package org.example.project.security.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Data
public class SignUpRequest {
    @NotBlank
    @Size(min=3,max=20)
    private String username;

    @NotBlank
    @Size(min=6,max=20)
    private String password;

    @NotBlank
    @Email
    private String email;

    @Getter
    @Setter
    private Set<String> role;
}
