package com.petadoption.petadoption.service.impl;

import com.petadoption.petadoption.service.OcrService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OcrServiceImpl implements OcrService {

    private static final Pattern ID_CARD_PATTERN = Pattern.compile("([1-9]\\d{5}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx])");
    private static final int[] WEIGHT_FACTORS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    @Override
    public Map<String, String> recognizeIdCard(String imageUrl) {
        Map<String, String> result = new HashMap<>();
        result.put("success", "false");
        result.put("message", "请上传身份证图片，系统将自动识别身份证号码");

        if (!StringUtils.hasText(imageUrl)) {
            result.put("message", "图片URL不能为空");
            return result;
        }

        String extractedText = extractTextFromImage(imageUrl);

        if (!StringUtils.hasText(extractedText)) {
            result.put("message", "无法从图片中识别到文字，请确保图片清晰");
            return result;
        }

        String idCardNumber = extractIdCardNumber(extractedText);

        if (idCardNumber == null) {
            result.put("message", "未能在图片中识别到身份证号码，请确保身份证图片清晰完整");
            return result;
        }

        if (!validateIdCardNumber(idCardNumber)) {
            result.put("message", "识别到的身份证号码无效，请重新上传清晰的身份证图片");
            return result;
        }

        result.put("success", "true");
        result.put("idCardNumber", idCardNumber);
        result.put("message", "身份证号码识别成功");
        return result;
    }

    private String extractTextFromImage(String imageUrl) {
        return "";
    }

    private String extractIdCardNumber(String text) {
        Matcher matcher = ID_CARD_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @Override
    public boolean validateIdCardNumber(String idCardNumber) {
        if (!StringUtils.hasText(idCardNumber)) {
            return false;
        }

        idCardNumber = idCardNumber.toUpperCase();

        if (idCardNumber.length() != 18) {
            return false;
        }

        if (!ID_CARD_PATTERN.matcher(idCardNumber).matches()) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 17; i++) {
            char c = idCardNumber.charAt(i);
            sum += Character.getNumericValue(c) * WEIGHT_FACTORS[i];
        }

        char checkCode = CHECK_CODES[sum % 11];

        return checkCode == idCardNumber.charAt(17);
    }
}
