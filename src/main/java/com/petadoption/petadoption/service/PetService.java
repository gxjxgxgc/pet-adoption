package com.petadoption.petadoption.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.petadoption.petadoption.entity.Pet;

import java.util.List;

public interface PetService extends IService<Pet> {
    IPage<Pet> getPetsByTypeAndStatus(Page<Pet> page, String type, Integer status);

    IPage<Pet> getPetsByStatus(Page<Pet> page, Integer status);

    IPage<Pet> getPetsByFilter(Page<Pet> page, String keyword, String type, String breed, Integer gender, Integer minAge, Integer maxAge, Double minWeight, Double maxWeight, List<String> healthStatusList, String sortBy, String sortOrder, Integer status, List<Integer> statusList);
    IPage<Pet> searchPets(Page<Pet> page, String keyword);
    boolean savePet(Pet pet);
    boolean updatePet(Long id, Pet pet);
    boolean deletePet(Long id);
    boolean updateStatus(Long id, Integer status);
    IPage<Pet> getHotPets(Page<Pet> page);
    void batchImportPets(org.springframework.web.multipart.MultipartFile file);
    void exportPets(jakarta.servlet.http.HttpServletResponse response);
    void incrementViewCount(Long petId);
    void incrementFavoriteCount(Long petId);
    void decrementFavoriteCount(Long petId);
    Pet getPetWithHealthStatuses(Long id);
}
