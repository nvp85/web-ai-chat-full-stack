package com.example.backend.repositories;

import com.example.backend.models.LLModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LLModelRepository extends JpaRepository<LLModel, Integer> {
}
