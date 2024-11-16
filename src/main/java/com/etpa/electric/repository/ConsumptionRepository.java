package com.etpa.electric.repository;

import com.etpa.electric.entity.Consumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsumptionRepository extends JpaRepository<Consumption, String> {

    Optional<Consumption> findByMonthCodeAndMetreId(Integer monthCode, String metreId);

}