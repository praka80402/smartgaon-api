//package com.smartgaon.ai.smartgaon_api.auth.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import com.smartgaon.ai.smartgaon_api.model.User;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface UserRepository extends JpaRepository<User, Long> {
//    Optional<User> findByEmail(String email);
//    Optional<User> findByResetToken(String token);
//    Optional<User> findByPhone(String phone);
//    List<User> findByPinCode(String pincode);
//}

package com.smartgaon.ai.smartgaon_api.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.smartgaon.ai.smartgaon_api.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByResetToken(String token);
    Optional<User> findByPhone(String phone);

    // ✅ Correct method – matches entity field "pincode"
    List<User> findByPincode(String pincode);
}
