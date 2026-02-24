package com.ihsan.aidatflow.repository;

import com.ihsan.aidatflow.entity.Due;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DueRepository extends JpaRepository<Due, Long> {
    List<Due> findByApartmentIdOrderByDueDateDesc(Long apartmentId);
}
