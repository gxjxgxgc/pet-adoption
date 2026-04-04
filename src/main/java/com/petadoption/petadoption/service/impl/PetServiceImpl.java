package com.petadoption.petadoption.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petadoption.petadoption.entity.HealthStatus;
import com.petadoption.petadoption.entity.Pet;
import com.petadoption.petadoption.entity.PetHealthStatus;
import com.petadoption.petadoption.exception.BusinessException;
import com.petadoption.petadoption.mapper.HealthStatusMapper;
import com.petadoption.petadoption.mapper.PetHealthStatusMapper;
import com.petadoption.petadoption.mapper.PetMapper;
import com.petadoption.petadoption.service.PetService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetServiceImpl extends ServiceImpl<PetMapper, Pet> implements PetService {

    private final PetMapper petMapper;
    private final PetHealthStatusMapper petHealthStatusMapper;
    private final HealthStatusMapper healthStatusMapper;

    public PetServiceImpl(PetMapper petMapper, PetHealthStatusMapper petHealthStatusMapper, HealthStatusMapper healthStatusMapper) {
        this.petMapper = petMapper;
        this.petHealthStatusMapper = petHealthStatusMapper;
        this.healthStatusMapper = healthStatusMapper;
    }

    @Override
    public IPage<Pet> getPetsByStatus(Page<Pet> page, Integer status) {
        return petMapper.selectPetsByStatus(page, status);
    }

    @Override
    public IPage<Pet> getPetsByFilter(Page<Pet> page, String keyword, String type, String breed, Integer gender, Integer minAge, Integer maxAge, Double minWeight, Double maxWeight, List<String> healthStatusList, String sortBy, String sortOrder, Integer status, List<Integer> statusList) {
        return petMapper.selectPetsByFilter(page, keyword, type, breed, gender, minAge, maxAge, minWeight, maxWeight, healthStatusList, sortBy, sortOrder, status, statusList);
    }

    @Override
    public IPage<Pet> getPetsByTypeAndStatus(Page<Pet> page, String type, Integer status) {
        return petMapper.selectPetsByTypeAndStatus(page, type, status);
    }

    @Override
    public IPage<Pet> searchPets(Page<Pet> page, String keyword) {
        return petMapper.searchPets(page, keyword);
    }

    @Override
    @Transactional
    public boolean savePet(Pet pet) {
        validatePetData(pet);
        
        List<String> healthStatusCodes = null;
        if (pet.getHealthStatuses() != null && !pet.getHealthStatuses().isEmpty()) {
            if (pet.getHealthStatuses().get(0).getId() == null && pet.getHealthStatuses().get(0).getCode() != null) {
                healthStatusCodes = pet.getHealthStatuses().stream()
                        .map(HealthStatus::getCode)
                        .collect(Collectors.toList());
            }
        }
        
        pet.setStatus(1);
        pet.setPublishTime(LocalDateTime.now());
        
        if (petMapper.insert(pet) == 0) {
            return false;
        }
        
        if (healthStatusCodes != null && !healthStatusCodes.isEmpty()) {
            for (String code : healthStatusCodes) {
                HealthStatus healthStatus = healthStatusMapper.selectByCode(code);
                if (healthStatus != null) {
                    PetHealthStatus petHealthStatus = new PetHealthStatus();
                    petHealthStatus.setPetId(pet.getId());
                    petHealthStatus.setHealthStatusId(healthStatus.getId());
                    petHealthStatusMapper.insert(petHealthStatus);
                }
            }
        } else if (pet.getHealthStatuses() != null && !pet.getHealthStatuses().isEmpty()) {
            for (HealthStatus healthStatus : pet.getHealthStatuses()) {
                if (healthStatus.getId() != null) {
                    PetHealthStatus petHealthStatus = new PetHealthStatus();
                    petHealthStatus.setPetId(pet.getId());
                    petHealthStatus.setHealthStatusId(healthStatus.getId());
                    petHealthStatusMapper.insert(petHealthStatus);
                }
            }
        }
        
        return true;
    }

    @Override
    public boolean updatePet(Long id, Pet pet) {
        // 数据验证
        validatePetData(pet);
        
        // 检查宠物是否存在
        Pet existingPet = petMapper.selectById(id);
        if (existingPet == null) {
            throw new BusinessException("宠物不存在");
        }
        
        // 处理健康状况
        List<String> healthStatusCodes = null;
        if (pet.getHealthStatuses() != null && !pet.getHealthStatuses().isEmpty()) {
            if (pet.getHealthStatuses().get(0).getId() == null && pet.getHealthStatuses().get(0).getCode() != null) {
                healthStatusCodes = pet.getHealthStatuses().stream()
                        .map(HealthStatus::getCode)
                        .collect(Collectors.toList());
            }
        }
        
        pet.setId(id);
        
        if (petMapper.updateById(pet) == 0) {
            return false;
        }
        
        // 更新健康状况关联 - 先删除旧的关联
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PetHealthStatus> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("pet_id", id);
        petHealthStatusMapper.delete(queryWrapper);
        
        if (healthStatusCodes != null && !healthStatusCodes.isEmpty()) {
            for (String code : healthStatusCodes) {
                HealthStatus healthStatus = healthStatusMapper.selectByCode(code);
                if (healthStatus != null) {
                    PetHealthStatus petHealthStatus = new PetHealthStatus();
                    petHealthStatus.setPetId(id);
                    petHealthStatus.setHealthStatusId(healthStatus.getId());
                    petHealthStatusMapper.insert(petHealthStatus);
                }
            }
        } else if (pet.getHealthStatuses() != null && !pet.getHealthStatuses().isEmpty()) {
            for (HealthStatus healthStatus : pet.getHealthStatuses()) {
                if (healthStatus.getId() != null) {
                    PetHealthStatus petHealthStatus = new PetHealthStatus();
                    petHealthStatus.setPetId(id);
                    petHealthStatus.setHealthStatusId(healthStatus.getId());
                    petHealthStatusMapper.insert(petHealthStatus);
                }
            }
        }
        
        return true;
    }

    @Override
    public boolean deletePet(Long id) {
        // 检查宠物是否存在
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            throw new BusinessException("宠物不存在");
        }
        
        // 权限校验（这里简化处理，实际应该检查当前用户是否有权限删除）
        // if (!hasPermission(pet, currentUser)) {
        //     throw new BusinessException("无权限删除此宠物信息");
        // }
        
        return petMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        // 检查宠物是否存在
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            throw new BusinessException("宠物不存在");
        }
        
        // 状态流转控制
        validateStatusTransition(pet.getStatus(), status);
        
        pet.setStatus(status);
        return petMapper.updateById(pet) > 0;
    }

    @Override
    public IPage<Pet> getHotPets(Page<Pet> page) {
        // 基于浏览量和收藏数的推荐算法
        return petMapper.selectHotPets(page);
    }

    /**
     * 验证宠物数据
     */
    private void validatePetData(Pet pet) {
        if (pet.getName() == null || pet.getName().trim().isEmpty()) {
            throw new BusinessException("宠物名称不能为空");
        }
        
        if (pet.getType() == null || pet.getType().trim().isEmpty()) {
            throw new BusinessException("宠物类型不能为空");
        }
        
        if (pet.getAge() != null && pet.getAge() < 0) {
            throw new BusinessException("宠物年龄不能为负数");
        }
        
        if (pet.getGender() != null && (pet.getGender() < 0 || pet.getGender() > 2)) {
            throw new BusinessException("宠物性别值无效");
        }
        
        if (pet.getWeight() != null && pet.getWeight().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("宠物体重不能为负数");
        }
    }

    /**
     * 验证状态流转
     */
    private void validateStatusTransition(Integer currentStatus, Integer newStatus) {
        switch (currentStatus) {
            case 0:
                if (newStatus != 1 && newStatus != 3) {
                    throw new BusinessException("隐藏状态只能转换为待领养或已领养");
                }
                break;
            case 1:
                if (newStatus != 2 && newStatus != 3 && newStatus != 0) {
                    throw new BusinessException("待领养状态只能转换为领养中、已领养或隐藏");
                }
                break;
            case 2:
                if (newStatus != 3 && newStatus != 1) {
                    throw new BusinessException("领养中状态只能转换为已领养或待领养");
                }
                break;
            case 3:
                if (newStatus != 1 && newStatus != 0) {
                    throw new BusinessException("已领养状态只能转换为待领养或隐藏");
                }
                break;
            default:
                throw new BusinessException("无效的当前状态");
        }
    }

    /**
     * 检查用户是否有权限操作宠物
     */
    // private boolean hasPermission(Pet pet, User currentUser) {
    //     // 管理员可以操作所有宠物
    //     if (currentUser.isAdmin()) {
    //         return true;
    //     }
    //     // 发布者可以操作自己的宠物
    //     return pet.getOwnerId() != null && pet.getOwnerId().equals(currentUser.getId());
    // }

    @Override
    public void batchImportPets(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new BusinessException("只支持Excel文件格式");
        }

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            List<Pet> pets = new ArrayList<>();
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                Pet pet = new Pet();
                
                pet.setName(getCellValueAsString(row.getCell(0)));
                pet.setType(getCellValueAsString(row.getCell(1)));
                pet.setBreed(getCellValueAsString(row.getCell(2)));
                
                String ageStr = getCellValueAsString(row.getCell(3));
                if (ageStr != null && !ageStr.isEmpty()) {
                    pet.setAge(Integer.parseInt(ageStr));
                }
                
                String genderStr = getCellValueAsString(row.getCell(4));
                if (genderStr != null) {
                    if ("公".equals(genderStr)) {
                        pet.setGender(1);
                    } else if ("母".equals(genderStr)) {
                        pet.setGender(2);
                    } else {
                        pet.setGender(0);
                    }
                }
                
                String weightStr = getCellValueAsString(row.getCell(5));
                if (weightStr != null && !weightStr.isEmpty()) {
                    pet.setWeight(new BigDecimal(weightStr));
                }
                
                pet.setColor(getCellValueAsString(row.getCell(6)));
                
                // 健康状况处理（暂时跳过，需要从Excel中读取并转换为健康状况关联）
                // String healthStatusStr = getCellValueAsString(row.getCell(7));
                // if (healthStatusStr != null && !healthStatusStr.isEmpty()) {
                //     // 这里需要根据健康状况字符串转换为健康状况关联
                // }
                
                String sterilizedStr = getCellValueAsString(row.getCell(8));
                if (sterilizedStr != null) {
                    if ("是".equals(sterilizedStr) || "true".equalsIgnoreCase(sterilizedStr)) {
                        pet.setSterilized(1);
                    } else {
                        pet.setSterilized(0);
                    }
                }
                
                pet.setDescription(getCellValueAsString(row.getCell(9)));
                
                pet.setStatus(1);
                pet.setPublishTime(LocalDateTime.now());
                
                pets.add(pet);
            }
            
            saveBatch(pets);
        } catch (IOException e) {
            throw new BusinessException("解析Excel文件失败");
        } catch (NumberFormatException e) {
            throw new BusinessException("Excel文件格式错误，请检查数字格式");
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    @Override
    public void exportPets(jakarta.servlet.http.HttpServletResponse response) {
        List<Pet> pets = list();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("宠物数据");
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "宠物名称", "类型", "品种", "年龄(月)", "性别", "颜色", "体重(kg)", "描述", "状态", "健康状况", "是否绝育", "发布时间", "浏览量", "收藏数"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            for (int i = 0; i < pets.size(); i++) {
                Pet pet = pets.get(i);
                Row row = sheet.createRow(i + 1);
                
                row.createCell(0).setCellValue(pet.getId() != null ? pet.getId() : 0);
                row.createCell(1).setCellValue(pet.getName() != null ? pet.getName() : "");
                row.createCell(2).setCellValue(pet.getType() != null ? pet.getType() : "");
                row.createCell(3).setCellValue(pet.getBreed() != null ? pet.getBreed() : "");
                row.createCell(4).setCellValue(pet.getAge() != null ? pet.getAge() : 0);
                
                String genderText = "";
                if (pet.getGender() != null) {
                    switch (pet.getGender()) {
                        case 1: genderText = "公"; break;
                        case 2: genderText = "母"; break;
                        default: genderText = "未知";
                    }
                }
                row.createCell(5).setCellValue(genderText);
                
                row.createCell(6).setCellValue(pet.getColor() != null ? pet.getColor() : "");
                row.createCell(7).setCellValue(pet.getWeight() != null ? pet.getWeight().doubleValue() : 0);
                row.createCell(8).setCellValue(pet.getDescription() != null ? pet.getDescription() : "");
                
                String statusText = "";
                if (pet.getStatus() != null) {
                    switch (pet.getStatus()) {
                        case 0: statusText = "隐藏"; break;
                        case 1: statusText = "待领养"; break;
                        case 2: statusText = "领养中"; break;
                        case 3: statusText = "已领养"; break;
                        default: statusText = "未知";
                    }
                }
                row.createCell(9).setCellValue(statusText);
                
                // 健康状况处理
                String healthStatusText = "";
                if (pet.getHealthStatuses() != null && !pet.getHealthStatuses().isEmpty()) {
                    healthStatusText = pet.getHealthStatuses().stream()
                            .map(HealthStatus::getName)
                            .collect(java.util.stream.Collectors.joining(","));
                }
                row.createCell(10).setCellValue(healthStatusText);
                
                String sterilizedText = pet.getSterilized() != null && pet.getSterilized() == 1 ? "是" : "否";
                row.createCell(11).setCellValue(sterilizedText);
                
                row.createCell(12).setCellValue(pet.getPublishTime() != null ? pet.getPublishTime().toString() : "");
                row.createCell(13).setCellValue(pet.getViewCount() != null ? pet.getViewCount() : 0);
                row.createCell(14).setCellValue(pet.getFavoriteCount() != null ? pet.getFavoriteCount() : 0);
            }
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=pets_" + java.time.LocalDate.now() + ".xlsx");
            
            workbook.write(response.getOutputStream());
        } catch (java.io.IOException e) {
            throw new BusinessException("导出Excel文件失败");
        }
    }

    @Override
    public void incrementViewCount(Long petId) {
        Pet pet = getById(petId);
        if (pet != null) {
            pet.setViewCount(pet.getViewCount() != null ? pet.getViewCount() + 1 : 1);
            updateById(pet);
        }
    }

    @Override
    public void incrementFavoriteCount(Long petId) {
        Pet pet = getById(petId);
        if (pet != null) {
            pet.setFavoriteCount(pet.getFavoriteCount() != null ? pet.getFavoriteCount() + 1 : 1);
            updateById(pet);
        }
    }

    @Override
    public void decrementFavoriteCount(Long petId) {
        Pet pet = getById(petId);
        if (pet != null && pet.getFavoriteCount() != null && pet.getFavoriteCount() > 0) {
            pet.setFavoriteCount(pet.getFavoriteCount() - 1);
            updateById(pet);
        }
    }

    @Override
    public Pet getPetWithHealthStatuses(Long id) {
        return petMapper.selectPetWithHealthStatuses(id);
    }
}

