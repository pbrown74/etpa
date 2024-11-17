package com.etpa.electric.repository;

import com.etpa.electric.entity.Fraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FractionRepository extends JpaRepository<Fraction, String> {

    List<Fraction> findByProfile(String profile);

    Optional<Fraction> findByProfileAndMonthCode(String profile, Integer monthCode);

}