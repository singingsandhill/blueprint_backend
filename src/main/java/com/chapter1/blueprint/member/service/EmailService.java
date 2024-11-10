package com.chapter1.blueprint.member.service;

import com.chapter1.blueprint.exception.codes.ErrorCode;
import com.chapter1.blueprint.exception.codes.ErrorCodeException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final Map<String, VerificationData> verificationCodes = new ConcurrentHashMap<>();

    private static final long CODE_EXPIRATION_MINUTES = 5;

    public void sendVerificationEmail(String email) {
        String code = generateVerificationCode();
        try {
            sendEmail(email, code);
            verificationCodes.put(email, new VerificationData(code, LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES)));
        } catch (MailException e) {
            throw new ErrorCodeException(ErrorCode.EMAIL_SENDING_FAILED);
        }
    }

    public boolean verifyEmailCode(String email, String code) {
        VerificationData verificationData = verificationCodes.get(email);

        if (verificationData == null) {
            throw new ErrorCodeException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        if (verificationData.getExpirationTime().isBefore(LocalDateTime.now())) {
            verificationCodes.remove(email);
            throw new ErrorCodeException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }

        if (!verificationData.getCode().equals(code)) {
            throw new ErrorCodeException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        verificationCodes.remove(email);
        return true;
    }

    private void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[BluePrint] 이메일 인증 코드 안내");
        message.setText(
                "안녕하세요,\n\n" +
                        "BluePrint 서비스를 이용해주셔서 감사합니다!\n" +
                        "아래 인증 코드를 입력하여 이메일 인증을 완료해 주세요.\n\n" +
                        "인증 코드: " + code + "\n\n" +
                        "해당 인증 코드는 5분 동안 유효합니다.\n\n" +
                        "감사합니다.\n" +
                        "BluePrint 팀 드림"
        );
        mailSender.send(message);
    }

    private String generateVerificationCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    private static class VerificationData {
        private final String code;
        private final LocalDateTime expirationTime;

        public VerificationData(String code, LocalDateTime expirationTime) {
            this.code = code;
            this.expirationTime = expirationTime;
        }

        public String getCode() {
            return code;
        }

        public LocalDateTime getExpirationTime() {
            return expirationTime;
        }
    }

    public void sendTemporaryPassword(String email, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[BluePrint] 임시 비밀번호 발급 안내");
        message.setText(
                "안녕하세요, \n\n" +
                        "사용자의 보안을 위해 빠른 시일 내에 비밀번호를 변경하시기를 권장드립니다.\n" +
                        "발급된 임시 비밀번호: " + temporaryPassword + "\n\n" +
                        "비밀번호 변경 안내: \n" +
                        "1. 홈페이지 상단의 메뉴에서 본인의 이름을 클릭하여 마이페이지로 이동합니다.\n" +
                        "2. '비밀번호 변경' 옵션을 선택하여 새 비밀번호를 설정합니다.\n\n" +
                        "비밀번호 변경 절차에 대한 추가적인 안내가 필요하시면 언제든지 저희 고객 지원팀에 문의해 주시기 바랍니다. \n\n" +
                        "항상 저희 서비스를 이용해 주셔서 진심으로 감사드립니다. \n\n" +
                        "감사합니다."
        );
        mailSender.send(message);
    }
}
