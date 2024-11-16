package com.etpa.electric.repository;

import com.etpa.electric.entity.Fraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FractionRepository extends JpaRepository<Fraction, String> {

    List<Fraction> findByProfile(String profile);

}