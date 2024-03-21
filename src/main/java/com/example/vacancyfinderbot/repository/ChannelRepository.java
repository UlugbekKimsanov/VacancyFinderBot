package com.example.vacancyfinderbot.repository;

import com.example.vacancyfinderbot.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelRepository extends JpaRepository<Channel,String> {
    List<Channel> findAllByUserEntity_UserId(Long userId);

}
