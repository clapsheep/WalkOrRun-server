package com.wor.dash.password.model.service;

import com.wor.dash.password.model.PasswordAnswer;
import com.wor.dash.password.model.PasswordFindQnA;
import com.wor.dash.password.model.PasswordQuestion;

import java.util.List;
import java.util.Optional;


public
interface PasswordService {

    Optional<List<PasswordQuestion>> allQuestions();
    Optional<PasswordQuestion> getQuestion(int questionId);
    Optional<PasswordFindQnA> getQnA(int userId);
    Optional<Integer> addAnswer(PasswordAnswer passwordAnswer);

}
