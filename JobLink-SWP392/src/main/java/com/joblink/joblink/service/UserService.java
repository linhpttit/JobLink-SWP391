package com.joblink.joblink.service;

import com.joblink.joblink.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public long getTotalUsers() {
        return userRepository.countTotalUsers();
    }

}
