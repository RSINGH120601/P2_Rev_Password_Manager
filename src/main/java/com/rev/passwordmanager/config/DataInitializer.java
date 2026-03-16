package com.rev.passwordmanager.config;

import com.rev.passwordmanager.entity.SecurityQuestion;
import com.rev.passwordmanager.repository.SecurityQuestionRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer {

    @Autowired
    private SecurityQuestionRepository securityQuestionRepository;

    @PostConstruct
    public void loadSecurityQuestions() {

        // If questions already exist, do nothing
        if (securityQuestionRepository.count() > 0) {
            return;
        }

        List<String> defaultQuestions = List.of(
                "What is your Favourite Game?",
                "What was your first school?",
                "What is your favorite player?",
                "What is your birth city?",
                "What was your childhood nickname?"
        );

        for (String questionText : defaultQuestions) {
            SecurityQuestion question = new SecurityQuestion();
            question.setQuestionText(questionText);
            securityQuestionRepository.save(question);
        }

        System.out.println("Default security questions inserted.");
    }
}