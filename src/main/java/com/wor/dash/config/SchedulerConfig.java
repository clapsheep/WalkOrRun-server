package com.wor.dash.config;

import com.wor.dash.challenge.model.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulerConfig {
    private final ChallengeService challengeService;
    // @Scheduled(cron = "초 분 시 일 월 요일")
    // * : 모든 값
    //? : 설정 없음 (일, 요일에서만 사용)
    //- : 범위 지정
    //, : 여러 값 지정
    int num = 1;

    /// : 증분값
    //L : 마지막 (일, 요일에서만 사용)
    //W : 가장 가까운 평일 (일에서만 사용)
    //# : N번째 특정 요일 (요일에서만 사용)
    @Scheduled(cron = "0 0 0 * * *")    // 매일 00시
    public void createDailyChallengeTask() {
        try {
            challengeService.addDailyChallenge(1);
            log.debug("[SchedulerConfig] : createDailyChallengeTask Success");
        } catch (Exception e) {
            log.debug("[SchedulerConfig] : createDailyChallengeTask Exception");
        }
    }

    // TEST
//    @Scheduled(cron = "0 56 13 * * *") //
//    public void aa() {
//        try {
//            challengeService.addWeeklyChallenge(2);
//            log.debug("[SchedulerConfig] : Weekly Success");
//            System.out.println("Weekly 성공");
//            challengeService.addMonthlyChallenge(3);
//            log.debug("[SchedulerConfig] : Monthly Success");
//            System.out.println("Monthly 성공");
//            log.debug("[SchedulerConfig] : aa Success");
//        } catch (Exception e) {
//            log.debug("[SchedulerConfig] : aa Exception");
//            e.printStackTrace();
//        }
//    }

    @Scheduled(cron = "0 0 0 * * MON") // 매주 월요일 00시 정각
    public void createWeeklyChallengeTask() {
        try {
            challengeService.addWeeklyChallenge(2);
            log.debug("[SchedulerConfig] : createWeeklyChallengeTask Success");
        } catch (Exception e) {
            log.debug("[SchedulerConfig] : addWeeklyChallenge Exception");
        }
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public void createMonthlyChallengeTask() {
        try {
            challengeService.addMonthlyChallenge(3);
            log.debug("[SchedulerConfig] : addMonthlyChallenge Success");
        } catch (Exception e) {
            log.debug("[SchedulerConfig] : addMonthlyChallenge Exception");
        }
    }

    @Scheduled(cron = "0 0 */3 * * *")
    public void checkChallengeIsEndedTask() {
        try {
            challengeService.checkIsEndedChallenge();
            log.debug("[SchedulerConfig] : checkChallengeIsEndedTask Success");
        } catch (Exception e) {
            log.debug("[SchedulerConfig] : checkChallengeIsEndedTask Exception");
        }
    }
}