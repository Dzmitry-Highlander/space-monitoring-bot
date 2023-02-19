package io.proj3ct.space_monitoring_bot.service;

import com.vdurmont.emoji.EmojiParser;
import io.proj3ct.space_monitoring_bot.config.BotConfig;
import io.proj3ct.space_monitoring_bot.model.User;
import io.proj3ct.space_monitoring_bot.dao.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Objects;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;
    private static final String HELP_TEXT = "This bot can show you next rocket launches info." +
            " You can use Menu button to choose command or type some of these commands: " +
            "\n\n /start - start bot\n" +
            "/showlaunches - shows next 5 rocket launches\n" +
            "/shownasaphoto - shows NASA photo of the day\n" +
            "/mydata - get your data stored\n" +
            "/deletemydata - delete your data\n" +
            "/help - how to use this bot";
    private final BotConfig config;

    public TelegramBot(BotConfig config) {
        super(config.getBotToken());
        this.config = config;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String message = null;
        String firstName = null;
        long chatId = 0;

        if (update.hasMessage() && update.getMessage().hasText()) {
            message = update.getMessage().getText();
            firstName = update.getMessage().getChat().getFirstName();
            chatId = update.getMessage().getChatId();
        }
        switch (Objects.requireNonNull(message)) {
            case "/start":
                registerUser(update.getMessage());
                startCommandReceived(chatId, firstName);
                break;
            case "/mydata":
                sendMessage(chatId, registeredUserData(update.getMessage()));
                log.info("Data showed: " + firstName);
                break;
            case "/deletemydata":
                deleteUserData(update.getMessage());
                log.info("Data deleted for - " + firstName);
                break;
            case "/help":
                sendMessage(chatId, HELP_TEXT);
                log.info("Help showed: " + firstName);
                break;
            case "/showlaunches":
                try {
                    sendMessage(chatId ,showLaunches());
                    log.info("Launches showed: " + firstName);
                } catch (IOException exception) {
                    log.error("Error: " + exception.getMessage());
                }
                break;
            case "/shownasaphoto":
                try {
                    showPhotoOfTheDay(chatId);
                    log.info("NASA photo of the day showed: " + firstName);
                } catch (IOException | TelegramApiException exception) {
                    log.error("Error: " + exception.getMessage());
                }
                break;
            default:
                sendMessage(chatId, "Command not recognized.");
                log.info("Command not recognized: " + firstName);
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    private String showLaunches() throws IOException {
        ParserService spaceParse = new ParserService();

        return spaceParse.parser();
    }

    private void showPhotoOfTheDay(Long chatId) throws IOException, TelegramApiException {
        NasaParserService nasaParserService = new NasaParserService();
        InputFile photo = new InputFile();
        photo.setMedia(nasaParserService.photoURL());
        SendPhoto sendPhoto = new SendPhoto(String.valueOf(chatId), photo);

        sendMessage(chatId, nasaParserService.parser());
        execute(sendPhoto);
    }

    private void deleteUserData(Message message) {
        if (userRepository.findById(message.getChatId()).isPresent()) {
            userRepository.deleteById(message.getChatId());

            sendMessage(message.getChatId(), "Data has been deleted.");
        } else {
            sendMessage(message.getChatId(), "There is no your data in my base.");
        }
    }

    private void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            User user = new User();

            user.setChatId(message.getChatId());
            user.setUserName(message.getChat().getUserName());
            user.setLastName(message.getChat().getLastName());
            user.setFirstName(message.getChat().getFirstName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);

            log.info("User saved: " + user);
        }
    }

    private String registeredUserData(Message message){
        String userData = "No data.";

        if (userRepository.findById(message.getChatId()).isPresent()) {
            userData = "Your data:\n\n" + userRepository.findById(message.getChatId());
        }

        return userData;
    }

    private void startCommandReceived(long chatId, String firstName) {
        String answer = EmojiParser.parseToUnicode("Hi, " + firstName + " :man_astronaut:");

        log.info("Replied to user " + firstName);

        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage(String.valueOf(chatId), textToSend);

        try {
            execute(message);
        } catch (TelegramApiException exception) {
            log.error("Error: " + exception.getMessage());
        }
    }
}
