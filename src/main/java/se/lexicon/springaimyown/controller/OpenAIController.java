package se.lexicon.springaimyown.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import se.lexicon.springaimyown.service.OpenAIService;

@RestController
@RequestMapping("/api/chat")
public class OpenAIController {

   private final OpenAIService openAIService;

    @Autowired
    public OpenAIController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @GetMapping
    public String welcome() {
        return "Welcome to the OpenAI Chat API!";
    }

    @GetMapping("/messages")
    //value är som standard namnet på parametern, och required är som standard true, så behöver eg. inte dem.
    public String ask(
            @RequestParam(value = "question", required = true)
            @NotNull(message = "Can not be null")
            @NotBlank(message = "Can not be blank")
            @Size(max = 200, message = "Question can not exceed 200 chars")
            String question) {
        System.out.println("question: " + question);
        return openAIService.processSimpleChatQuery(question);
    }

    @GetMapping("/messages/context")
    //value är som standard namnet på parametern, och required är som standard true, så behöver eg. inte dem.
    public String askWithContext(
            @RequestParam(value = "question", required = true)
            @NotNull(message = "Can not be null")
            @NotBlank(message = "Can not be blank")
            @Size(max = 200, message = "Question can not exceed 200 chars")
            String question) {
        System.out.println("question: " + question);
        return openAIService.processSimpleChatQueryWithContext(question);
    }

    //Asynchronus, non-blocking
    @GetMapping(value = "/messages/stream", produces = MediaType.TEXT_MARKDOWN_VALUE) //Response type
    //value är som standard namnet på parametern, och required är som standard true, så behöver eg. inte dem.
    public Flux<String> askWithStream(
            @RequestParam(value = "question", required = true)
            @NotNull(message = "Can not be null")
            @NotBlank(message = "Can not be blank")
            @Size(max = 200, message = "Question can not exceed 200 chars")
            String question) {
        System.out.println("question: " + question);
        return openAIService.processSimpleChatQueryWithStream(question);
    }

    @PostMapping("/images/describe")
    public String askToProcessImage(
            @RequestParam
            @NotNull (message = "File can not be null")
            MultipartFile file) {
        return openAIService.processImage(file);
    }

    @GetMapping("/messages/generate")
    public String generateImageUrl(
            @RequestParam(value = "question", required = true)
            @NotNull(message = "Can not be null")
            @NotBlank(message = "Can not be blank")
            @Size(max = 1000, message = "Question can not exceed 1000 chars")
            String question) {
        System.out.println("question: " + question);
        return openAIService.generateImage(question);
    }
}
