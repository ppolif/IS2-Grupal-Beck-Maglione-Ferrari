package com.example.testSpringAI.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class IaService {
    private final ChatClient chatClient;

    public IaService(ChatClient.Builder builder) {

        //construir el cliente desde el builder con la configuracion del application properties (variables de entorno)
        this.chatClient = builder.build();
    }

    public String preguntar(String pregunta){
        return chatClient
                .prompt()
                .system("Sos un geek especializado en Bakugan." +
                        "Responde siempre en espaniol.")
                .user(pregunta)
                .call()
                .content();
    }
}
