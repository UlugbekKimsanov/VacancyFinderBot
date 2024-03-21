package com.example.vacancyfinderbot.service;

import com.example.vacancyfinderbot.entity.Channel;
import com.example.vacancyfinderbot.entity.UserEntity;
import com.example.vacancyfinderbot.entity.Vacancy;
import com.example.vacancyfinderbot.repository.ChannelRepository;
import com.example.vacancyfinderbot.repository.UserRepository;
import com.example.vacancyfinderbot.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final VacancyRepository vacancyRepository;

    public UserEntity getUser(Long chatId) {
        return userRepository.findById(chatId).orElse(null);
    }

    public void addUser(UserEntity user) {
        userRepository.save(user);
    }

    public List<Channel> getUserChannels(Long userId) {
        return channelRepository.findAllByUserEntity_UserId(userId);
    }

    public void addChannel(UserEntity user,String postLink) {
        Channel channel = new Channel();
        String[] parts = postLink.split("/");
        channel.setName(parts[3]);
        channel.setLastPost(postLink);
        channel.setUserEntity(user);
        channelRepository.save(channel);

    }
    public void updateChannel(Channel channel){
        channelRepository.save(channel);
    }

    public List<Vacancy> getUserVacancies(UserEntity userEntity) {
        return vacancyRepository.findAllByOwnerIs(userEntity);
    }

    public List<UserEntity> getAllUser() {
        return userRepository.findAll();
    }

    public void addVacancy(Vacancy vacancy) {
        vacancyRepository.save(vacancy);
    }
}
