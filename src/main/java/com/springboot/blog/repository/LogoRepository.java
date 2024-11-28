package com.springboot.blog.repository;

import com.springboot.blog.entity.Logo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogoRepository extends JpaRepository<Logo, Long> {
}
