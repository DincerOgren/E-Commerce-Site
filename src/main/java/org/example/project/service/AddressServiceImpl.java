package org.example.project.service;

import org.example.project.exceptions.APIException;
import org.example.project.exceptions.ResourceNotFoundException;
import org.example.project.model.Address;
import org.example.project.model.User;
import org.example.project.payload.AddressDTO;
import org.example.project.repositories.AddressRepository;
import org.example.project.repositories.UserRepository;
import org.example.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private UserRepository userRepository;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO) {
        Address addressToSave  = modelMapper.map(addressDTO, Address.class);

        User user= authUtil.loggedInUser();

        addressToSave.setUser(user);
        List<Address> userAddresses = user.getAddresses();
        userAddresses.add(addressToSave);
        user.setAddresses(userAddresses);

        Address savedAddress = addressRepository.save(addressToSave);

        return  modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddresses() {

        if(addressRepository.findAll().isEmpty()) {
            throw new APIException("No address found");
        }

        List<Address> addressList = addressRepository.findAll();

        List<AddressDTO> addressDTOList = addressList.stream()
                .map(address-> modelMapper.map(address,AddressDTO.class)).toList();

        return addressDTOList;

    }

    @Override
    public AddressDTO getAddressById(Long addressId){

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address","addressId",addressId));

        return modelMapper.map(address,AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getLoggedInUserAddresses(){
       // List<Address> userAddresses = addressRepository.findAllByEmail(authUtil.loggedInEmail());

        List<Address> userAddresses = authUtil.loggedInUser().getAddresses();

        List<AddressDTO> addressDTOList = userAddresses.stream()
                .map(address -> modelMapper.map(address,AddressDTO.class)).toList();

        return addressDTOList;

    }

    @Override
    public AddressDTO updateAddressById(Long addressId, AddressDTO addressDTO) {
        Address addressFromDB = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address","addressId",addressId));

        Address updatedAddress = modelMapper.map(addressDTO, Address.class);

        addressFromDB.setCity(addressDTO.getCity());
        addressFromDB.setCountry(addressDTO.getCountry());
        addressFromDB.setState(addressDTO.getState());
        addressFromDB.setStreet(addressDTO.getStreet());

        Address savedAddress = addressRepository.save(addressFromDB);

        // We added this user update because if cascading fails this will save the user
        User user = addressFromDB.getUser();
        user.getAddresses().removeIf(address1 -> address1.getAddressId().equals(addressId));
        user.getAddresses().add(savedAddress);
        userRepository.save(user);




        return  modelMapper.map(savedAddress,AddressDTO.class);
    }

    @Override
    public String deleteAddressById(Long addressId){
        Address addressFromDB = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address","addressId",addressId));

        User user = addressFromDB.getUser();
        user.getAddresses().removeIf(address1 -> address1.getAddressId().equals(addressId));
        userRepository.save(user);

        addressRepository.delete(addressFromDB);

        return "Address successfully deleted with id: " + addressId;
    }

}
