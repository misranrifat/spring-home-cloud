package com.example.springhomecloud.repository;

import com.example.springhomecloud.model.FileEntity;
import com.example.springhomecloud.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByUser(User user);

    Optional<FileEntity> findByIdAndUser(Long id, User user);

    List<FileEntity> findByIdInAndUser(List<Long> ids, User user);

    Page<FileEntity> findByUser(User user, Pageable pageable);
}