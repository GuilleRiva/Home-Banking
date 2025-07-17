package com.home_banking_.service;

import com.home_banking_.dto.ResponseDto.IPAddressResponseDto;


import java.util.List;

public interface IPAddressService {

    IPAddressResponseDto registerIP(IPAddressResponseDto dto);

    void makeAsSuspicious (Long ipId);

    boolean isSuspicious(String ip);

    List<IPAddressResponseDto> getIPsByUser(Long userId);

    void deleteIP(Long ipId);

}
