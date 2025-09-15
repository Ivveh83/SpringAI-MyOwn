package se.lexicon.springaimyown.service;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
public class OpenAIServiceImpl implements OpenAIService{

    //It supports both blocking and unblocking process
    private final OpenAiChatModel openAiChatModel;
    private final OpenAiImageModel openAiImageModel;

    @Autowired
    public OpenAIServiceImpl(OpenAiChatModel openAiChatModel, OpenAiImageModel openAiImageModel) {
        this.openAiChatModel = openAiChatModel;
        this.openAiImageModel = openAiImageModel;
    }

    @Override
    public String processSimpleChatQuery(String question) {
        if(question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("Question is null or empty");
        }
        return openAiChatModel.call(question);
        //return "This is a simple response to your question: " + question + "";
    }

    //Non-blocking, asynchronus
    @Override
    public Flux<String> processSimpleChatQueryWithStream(String question) {
        return openAiChatModel.stream(question);
    }

    @Override
    public String processSimpleChatQueryWithContext(String question) {
        if(question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("Question is null or empty");
        }
        //System Message: Sets the behaviour/personality/instruction/tone for the AI Models.
        SystemMessage systemMessage = SystemMessage.builder()
                .text("You are an AI Assistant named LEXBOT.")
                .build();
        //User Message: Represents the actual question on input from the user.
        UserMessage userMessage = UserMessage.builder()
                .text(question)
                .build();
        //Chat Options:
                    /*
            OpenAiChatOptions - kort referens:

            .seed          : Long seed för deterministisk generering; samma seed + samma parametrar → samma output.
            .temperature   : Styr randomness i tokenval; 0 = mest deterministiskt, högre = mer variation, max 2.0.
            .topLogProbs   : Returnerar log-probabilities i response för de N mest troliga tokens vid varje steg; påverkar ej tokenval.
            .metadata      : Map<String,Object> för att bifoga extra info (etiketter, userId, topic) utan att påverka texten. Finns sedan i response.
            .store         : Om true lagras meddelandet i sessionen och kan användas som kontext för framtida anrop. Raderas vid nästa session; false = glöms bort.
            etc...
            */

        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(2.0)
                .build();

        //Prompt:
        Prompt prompt = Prompt.builder()
                .messages(systemMessage, userMessage)
                .chatOptions(openAiChatOptions)
                .build();

        ChatResponse chatResponse = openAiChatModel.call(prompt);

        Generation generation = chatResponse.getResult();

        return generation.getOutput().getText();
    }

    @Override
    public String processImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image can not be null or empty");
        }

        if (file.getContentType() == null) {
            throw new IllegalArgumentException("Invalid file type. File must be an image.");
        }

        SystemMessage systemMessage = SystemMessage.builder()
                .text("You are a helpful assistant that describes the content of an image.") //System message
                .build();
        Media media = Media.builder()
                .data(file.getResource())
                .mimeType(MimeTypeUtils.IMAGE_JPEG)
                .build();
        UserMessage userMessage = UserMessage.builder()
                .text("Please describe this image.") // .text måste finnas med, kan dock vara tom
                .media(media)
                .build();
        Prompt prompt = Prompt.builder()
                .messages(systemMessage, userMessage)
                .build();
        ChatResponse chatResponse = openAiChatModel.call(prompt);

        Generation generation = chatResponse.getResult();

        return generation.getOutput().getText();
    }

    @Override
    public String generateImage(String question) {
        if(question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("Question is null or empty");
        }
        String systemInstruction = String.format("""
                 Create a highly detailed, professional image following these specifications:
                                Subject: %s
                                Technical Guidelines:
                                - Avoid text or writing in the image
                                - Ensure family-friendly content
                                - Focus on clear, sharp details
                                - Use balanced color composition
                """, question);
        OpenAiImageOptions openAiImageOptions = OpenAiImageOptions.builder()
                .model("dall-e-3")
                .quality("hd")
                .N(1)
                .responseFormat("url")
                .build();

        ImagePrompt imagePrompt = new ImagePrompt(systemInstruction, openAiImageOptions);
        ImageResponse imageResponse = openAiImageModel.call(imagePrompt);
        List<ImageGeneration> generations = imageResponse.getResults();
        ImageGeneration firsImage = generations.getFirst();

        String url = firsImage.getOutput().getUrl();

        try (InputStream in = URI.create(url).toURL().openStream()){
            Files.copy(in, Paths.get("generated_image" + System.currentTimeMillis() + ".png"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return url;
    }
}
