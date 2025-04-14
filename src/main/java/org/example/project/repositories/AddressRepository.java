package org.example.project.repositories;

import org.example.project.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address,Long> {

//      If you want to use direct query from database
//    @Query("SELECT a FROM Address a WHERE a.user.email =?1")
//    List<Address> findAllByEmail(String s);
}
