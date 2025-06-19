package com.home_banking_.service;

import com.home_banking_.model.IPAddress;

import java.util.List;

public interface IPAddressService {
    IPAddress registerIP(Long userId, String ip);
    void makeAsSuspicious (Long ipId);
    boolean isSuspicious(String ip);
    List<IPAddress> getIPsByUser(Long userID);
    void deleteIP(Long ipId);
}
