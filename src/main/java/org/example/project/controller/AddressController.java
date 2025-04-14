package org.example.project.controller;

import org.apache.coyote.Response;
import org.example.project.payload.AddressDTO;
import org.example.project.service.AddressService;
import org.example.project.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    private AddressService addressService;



    @PostMapping("/addresses")
    public ResponseEntity<?> createAddress(@RequestBody AddressDTO address) {

        AddressDTO createdAddress = addressService.createAddress(address);

        return new ResponseEntity<>(createdAddress, HttpStatus.CREATED);
    }

    @GetMapping("/addresses")
    public ResponseEntity<?> getAllAddresses() {
        List<AddressDTO> addresses = addressService.getAllAddresses();
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/addresses/{addressId}")
    public  ResponseEntity<?> getAddressById(@PathVariable Long addressId) {
        AddressDTO address = addressService.getAddressById(addressId);
        return ResponseEntity.ok(address);
    }

    @GetMapping("/user/addresses")
    public ResponseEntity<?> getUsersAddresses() {
        List<AddressDTO> addresses = addressService.getLoggedInUserAddresses();

        return ResponseEntity.ok(addresses);
    }


    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<?> updateAddress(@PathVariable Long addressId,
                                           @RequestBody AddressDTO address) {

        AddressDTO updatedAddress = addressService.updateAddressById(addressId,address);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long addressId) {
        String status = addressService.deleteAddressById(addressId);

        return ResponseEntity.ok(status);
    }


}
