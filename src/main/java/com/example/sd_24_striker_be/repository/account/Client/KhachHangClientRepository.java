package com.example.sd_24_striker_be.repository.account.Client;

import com.example.sd_24_striker_be.modal.account.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KhachHangClientRepository extends JpaRepository<KhachHang, Integer> {

}
