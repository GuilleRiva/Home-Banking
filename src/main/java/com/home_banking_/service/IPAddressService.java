package com.home_banking_.service;

import com.home_banking_.dto.request.IPAddressRequestDto;
import com.home_banking_.dto.response.IPAddressResponseDto;


import java.util.List;

public interface IPAddressService {

    IPAddressResponseDto registerIP(IPAddressRequestDto dto);

    void makeAsSuspicious (Long ipId);

    boolean isSuspicious(String ip);

    List<IPAddressResponseDto> getIPsByUser(Long userId);

    void deleteIP(Long ipId);

}
