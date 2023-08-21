package ru.czl.Iridium24Bot.service;

import com.screenshotone.jsdk.Client;
import com.screenshotone.jsdk.ResponseException;
import com.screenshotone.jsdk.TakeOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.asynchttpclient.request.body.multipart.FilePart;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.czl.Iridium24Bot.config.BotConfig;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    static final String HELP_TEXT = "Бот создан для проверки баланса сервисов Iridium.\n\n" +
            "Введите /balance для проверки баланса";

    static final String ERROR_TEXT= "Error occurred: ";

    public TelegramBot(BotConfig config) {
        this.config=config;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/balance", "Показать баланс всех карт"));
        listofCommands.add(new BotCommand("/help", "Помощь"));
        try{
            this.execute(new SetMyCommands(listofCommands,new BotCommandScopeDefault(),null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }
    @Override
    public String getBotUsername() {
        return config.getBotname();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/balance":
                    //registerUser(update.getMessage());
                    starCommandReceived(chatId, update.getMessage().getChat().getFirstName());

                    break;

                case "/help":

                    prepareAndSendMessage(chatId, HELP_TEXT);
                    break;

                default:

                    prepareAndSendMessage(chatId, "Команда не поддерживается, введите: /help");
            }
        }
    }

    private void starCommandReceived(long chatId, String name) { // Ответ на старт
        String answer= "Добро пожаловать, "+name+". Я бот который поможет узнать баланс сервисов Iridium";
        log.info("Replied to user " + name);
        sendMessage(chatId, answer);

//        screenshot(chatId,"332211");
//        sendScreen(chatId);

        try {
            screenShot();
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sendImageUploadingAFile(chatId);


    }

    private void sendMessage(long chatId, String textToSend) { // ответ на закрепленные комады
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        }
        catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());

        }
    }

    public void sendImageUploadingAFile(long chatId) {
        String img = "C:\\jdk-15.0.2\\IdeaProjects\\Iridium24Bot\\111.png";
        // Create send method
        SendPhoto sendPhotoRequest = new SendPhoto();
        // Set destination chat id
        sendPhotoRequest.setChatId(String.valueOf(chatId));
        // Set the photo file as a new photo (You can also use InputStream with a constructor overload)
        sendPhotoRequest.setPhoto(new InputFile(new File(img)));
        try {
            // Execute the method
            execute(sendPhotoRequest);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void screenShot () throws ResponseException, IOException { // работатет скрин
            final Client client = Client.withKeys("generated key", "generated key 2");
    TakeOptions takeOptions = TakeOptions.url("https://stat.steccom.ru/cgi-bin/clients/services/list?status=on&session_id=ec5835166d4d4092dedfc0d51c7dd16f&account_id=269694&type_id=9001")
            .fullPage(true)
            .deviceScaleFactor(1)
            .viewportHeight(800)
            .viewportWidth(800)
            .format("png")
            .omitBackground(true);
    final byte[] image = client.take(takeOptions);
        // System.out.println("Скрин сделан");

        try {
            Files.write(new File("C:\\jdk-15.0.2\\IdeaProjects\\Iridium24Bot\\111.png").toPath(), image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Скрин сделан");
    }

    private void prepareAndSendMessage(long chatId, String textToSend) { // вынесен ответ на команду
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try {
            execute(message);
        }
        catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());

        }
    }


}
