package com.petadoption.petadoption.service;

import java.util.Map;

public interface OcrService {
    Map<String, String> recognizeIdCard(String imageUrl);
    boolean validateIdCardNumber(String idCardNumber);
}
