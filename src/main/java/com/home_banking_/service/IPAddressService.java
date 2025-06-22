package com.home_banking_.service;

import com.home_banking_.model.IPAddress;
import com.home_banking_.model.Users;

import java.util.List;

public interface IPAddressService {

    IPAddress registerIP(Long user_id, String ip);

    void makeAsSuspicious (Long ipId);

    boolean isSuspicious(String ip);

    List<IPAddress> getIPsByUser(Users users);

    void deleteIP(Long ipId);
}
