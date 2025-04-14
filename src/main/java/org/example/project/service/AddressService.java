package org.example.project.service;

import org.example.project.payload.AddressDTO;

import java.util.List;

public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO);

    List<AddressDTO> getAllAddresses();

    AddressDTO getAddressById(Long addressId);

    List<AddressDTO> getLoggedInUserAddresses();

    AddressDTO updateAddressById(Long addressId,AddressDTO addressDTO);

    String deleteAddressById(Long addressId);
}
