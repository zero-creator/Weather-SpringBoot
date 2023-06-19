package com.app.services;

import com.app.entities.UserModel;

import java.util.Map;

public interface IUserServices {
    public boolean addUser(UserModel obj) throws RuntimeException;
}
