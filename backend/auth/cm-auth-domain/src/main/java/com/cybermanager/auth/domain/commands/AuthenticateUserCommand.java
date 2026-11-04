package com.cybermanager.auth.domain.commands;

public record AuthenticateUserCommand(String email, String password) {

}

