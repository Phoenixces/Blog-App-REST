package com.springboot.blog.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is mandatory")
    private String usernameOrEmail;

    @NotBlank(message = "Password is mandatory")
    private String password;
}
