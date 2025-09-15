package se.lexicon.springaimyown.service;

import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

public interface OpenAIService {

    String processSimpleChatQuery(String question);

    Flux<String> processSimpleChatQueryWithStream(String question);

    String processSimpleChatQueryWithContext(String question);

    String processImage(MultipartFile file);

    String generateImage(String question);
}
