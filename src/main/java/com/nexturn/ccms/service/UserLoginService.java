package com.nexturn.ccms.service;

import com.nexturn.ccms.dto.MessageResponse;
import com.nexturn.ccms.dto.UserLoginRequest;
import com.nexturn.ccms.dto.UserLoginResponse;
import com.nexturn.ccms.dto.UserRegisterRequest;

public interface UserLoginService {

    MessageResponse register(
            UserRegisterRequest request);

    UserLoginResponse login(
            UserLoginRequest request);
}