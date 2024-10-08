package com.example.vacancyfinderbot;
import com.example.vacancyfinderbot.config.BotConfig;
import com.example.vacancyfinderbot.entity.Channel;
import com.example.vacancyfinderbot.entity.State;
import com.example.vacancyfinderbot.entity.UserEntity;
import com.example.vacancyfinderbot.entity.Vacancy;
import com.example.vacancyfinderbot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class VacancyFinderBot extends TelegramLongPollingBot {
    private final BotConfig config;
    private final UserService userService;
    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasCallbackQuery()){

        } else{
            Message message = update.getMessage();
            String text = message.getText();
            Long userId = message.getChatId();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(userId);
            UserEntity userEntity = userService.getUser(update.getMessage().getChatId());
            if (userEntity == null) {
                sendMessage.setText("Assalomu alaykum" + message.getChat().getFirstName() + " botga xush kelibsiz!");
                userService.addUser(new UserEntity(userId, State.Default,null,message.getChat().getFirstName()));
                sendMessage.setReplyMarkup(getReplyMarkup());
                execute(sendMessage);
            } else {
                switch (text){
                    case "Kanallarim" -> showChannels(userId);
                    case "Kanal qo'shish" -> {
                        userEntity.setState(State.Kanal_Qoshish);
                        userService.addUser(userEntity);
                        sendMessage.setText("Kanaldagi oxirgi postni linkini yuboring:");
                        execute(sendMessage);}
                    case "Kalit so'z" -> {
                        userEntity.setState(State.Kalit_Soz);
                        userService.addUser(userEntity);
                        sendMessage.setText("Vakansiyalarni saralash uchun kalit so'z kiriting(Java):");
                        execute(sendMessage);
                    }case "Mening profilim" ->{
                        String userDetails = "";
                        userDetails += "Name: " + userEntity.getName()
                                + "\n Kalit so'z: " +userEntity.getKalitSoz()
                                + "\n Vakansiyalar: " + userService.getUserVacancies(userEntity).size()
                                + "\n Kanallar: " + userService.getUserChannels(userId).size();
                        sendMessage.setText(userDetails);
                        execute(sendMessage);
                    }
                    case "/start" -> {
                        sendMessage.setText("Assalomu alaykum " + message.getChat().getFirstName() + " qayta tashrifingizdan xursandmiz!");
                        sendMessage.setReplyMarkup(getReplyMarkup());
                        execute(sendMessage);}
                    default ->{
                        if (userEntity.getState() == State.Kanal_Qoshish){
                            addChannel(userEntity,message.getText());
                        } else if (userEntity.getState() == State.Kalit_Soz) {
                            userEntity.setKalitSoz(text);
                            userEntity.setState(State.Default);
                            userService.addUser(userEntity);
                            sendMessage.setText("Kalit so'z qo'shildi!");
                            execute(sendMessage);
                        }
                    }
                }
            }
        }
    }
    @SneakyThrows
    private void showChannels(Long userId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);
        List<Channel> channels = userService.getUserChannels(userId);
        if(channels.isEmpty()){
            sendMessage.setText("Sizda kanallar mavjud emas");
            execute(sendMessage);
        }else {
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < channels.size(); i++) {
                text.append("\n").append(i + 1).append(". ").append(channels.get(i).getName());
            }
            sendMessage.setText(String.valueOf(text));
            execute(sendMessage);
        }
    }
    @SneakyThrows
    private void addChannel(UserEntity user, String postLink) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getUserId());
        boolean checkDuplicate = checkDuplicate(postLink, user);
        if (checkUrl(postLink) && !checkDuplicate){
            String defResponse = getDefData(postLink+"100");
            System.out.println("defResponse = " + defResponse);
            userService.addChannel(user,postLink,defResponse);
            sendMessage.setText("Kanal qo'shildi!");
            execute(sendMessage);
        }else if(!checkDuplicate){
            sendMessage.setText("Post linki yaroqsiz");
            execute(sendMessage);
        }

    }

    @SneakyThrows
    private String getDefData(String postUrl) {
        URL url = new URL(postUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return cutText(response.toString());
    }

    @SneakyThrows
    private boolean checkDuplicate(String postLink, UserEntity user) {
        List<Channel> userChannels = userService.getUserChannels(user.getUserId());
        String[] parts = postLink.split("/");
        if(parts.length >= 3){
            String channelName = parts[3];
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(user.getUserId());
            for (Channel userChannel : userChannels) {
                if(Objects.equals(channelName, userChannel.getName())){
                    sendMessage.setText("Ushbu kanal allaqachon qo'shilgan!");
                    execute(sendMessage);
                    return true;
                }
            }
        }
        return false;
    }
    
    @SneakyThrows
    private boolean checkUrl(String postLink) {
        URL url = null;
        try {
            url = new URL(postLink);
        } catch (MalformedURLException e) {
            return false;
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        return conn.getResponseCode() == 200;
    }
    private ReplyKeyboard getReplyMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        KeyboardRow  keyboardRow = new KeyboardRow();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRow.add("Kanallarim");
        keyboardRow.add("Kanal qo'shish");
        keyboardRows.add(keyboardRow);
        keyboardRow =  new KeyboardRow();
        keyboardRow.add("Kalit so'z");
        keyboardRows.add(keyboardRow);
        keyboardRow.add("Mening profilim");
        keyboardMarkup.setKeyboard(keyboardRows);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;

    }

    @SneakyThrows
    @Scheduled(fixedRate = 600_000)
    public void checkVacancy() {
        System.out.println("Scheduled ishladi!");
        SendMessage sendMessage = new SendMessage();
        List<UserEntity> userEntities = userService.getAllUser();
        for (UserEntity user : userEntities) {
            List<Channel> userChannels = userService.getUserChannels(user.getUserId());
            System.out.println("\nuser.getName() = " + user.getName());
            for (Channel userChannel : userChannels) {
                System.out.println("userChannel = " + userChannel.getName());
                String lastPost = userChannel.getLastPost();
                String baseUrl = lastPost.substring(0, lastPost.lastIndexOf("/") + 1);
                String[] parts = lastPost.split("/");
                int lastPostId = Integer.parseInt(parts[4]);
                String newUrl;
                int j = 1;
                while (true) {
                    newUrl = baseUrl + (lastPostId + j++);
                    System.out.println("\nnewUrl = " + newUrl);
                    if (Objects.equals(getDefData(newUrl),userChannel.getDefResponse())) {
                        System.out.println("Checked to last post");
                        userChannel.setLastPost(baseUrl+ (lastPostId+j-2));
                        userService.updateChannel(userChannel);
                        break;
                    }else if(isTrueVacancy(user, newUrl)){
                        Vacancy vacancy = new Vacancy(newUrl, user);
                        userService.addVacancy(vacancy);
                        userChannel.setLastPost(newUrl);
                        userService.updateChannel(userChannel);
                        sendMessage.setChatId(user.getUserId());
                        sendMessage.setText(newUrl);
                        execute(sendMessage);
                    }
                }
            }
        }
    }
    @SneakyThrows
    private boolean isTrueVacancy(UserEntity user, String postUrl) {
        URL url = new URL(postUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String text = cutText(response.toString());
        String result = (text != null) ? text.toLowerCase() : null;
        System.out.println("result = " + result);

        String keyword = user.getKalitSoz().toLowerCase();
        String pattern = Pattern.quote(keyword) + "(?=[^a-zA-Z])";

        return result != null && (
                result.matches(".*" + pattern + ".*")
        ) &&
                !result.contains("resume") &&
                !result.contains("rezyume") &&
                !result.contains("ish joyi kerak");
    }

    private String cutText(String postUrl) {
        Document doc = Jsoup.parse(postUrl);
        Elements metaTags = doc.select("meta[property=og:description]");
        if (!metaTags.isEmpty()) {
            return metaTags.first().attr("content");
        }
        return null;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }
    @Override
    public String getBotToken(){
        return config.getToken();
    }

}
