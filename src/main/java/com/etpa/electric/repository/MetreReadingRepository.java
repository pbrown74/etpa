package com.etpa.electric.repository;

import com.etpa.electric.entity.MetreReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetreReadingRepository extends JpaRepository<MetreReading, String> {

}