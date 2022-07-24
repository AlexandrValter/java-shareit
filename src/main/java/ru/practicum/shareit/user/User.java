package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    @NotNull
    @Email
    private String email;

    public User() {
    }
}