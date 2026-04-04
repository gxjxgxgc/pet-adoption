package com.petadoption.petadoption.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.petadoption.entity.Pet;
import com.petadoption.petadoption.exception.BusinessException;
import com.petadoption.petadoption.mapper.PetMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PetServiceImplTest {

    @Mock
    private PetMapper petMapper;

    @InjectMocks
    private PetServiceImpl petService;

    private Pet testPet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testPet = new Pet();
        testPet.setId(1L);
        testPet.setName("小白");
        testPet.setType("cat");
        testPet.setBreed("英国短毛猫");
        testPet.setAge(12);
        testPet.setGender(2);
        testPet.setColor("白色");
        testPet.setWeight(new BigDecimal(3.5));
        testPet.setDescription("性格温顺，喜欢与人互动");
        testPet.setStatus(1);
        // 健康状况处理（暂时跳过，需要使用健康状况关联）
        // List<HealthStatus> healthStatuses = new ArrayList<>();
        // HealthStatus healthy = new HealthStatus();
        // healthy.setId(1L);
        // healthy.setCode("healthy");
        // healthy.setName("健康");
        // healthStatuses.add(healthy);
        // testPet.setHealthStatuses(healthStatuses);
        testPet.setSterilized(1);
        testPet.setShelterId(4L);
        testPet.setPublishTime(LocalDateTime.now());
        testPet.setCreateTime(LocalDateTime.now());
        testPet.setUpdateTime(LocalDateTime.now());
        testPet.setDeleted(0);
    }

    @Test
    void testSavePet_Success() {
        // 模拟保存操作
        when(petMapper.insert(any(Pet.class))).thenReturn(1);

        boolean result = petService.savePet(testPet);

        assertTrue(result);
        assertEquals(0, testPet.getStatus()); // 验证状态被设置为待审核
        assertNotNull(testPet.getPublishTime()); // 验证发布时间被设置
        verify(petMapper, times(1)).insert(testPet);
    }

    @Test
    void testSavePet_InvalidData() {
        // 测试空名称
        Pet invalidPet = new Pet();
        invalidPet.setType("cat");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            petService.savePet(invalidPet);
        });

        assertEquals("宠物名称不能为空", exception.getMessage());
    }

    @Test
    void testUpdatePet_Success() {
        // 模拟宠物存在
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(petMapper.updateById(any(Pet.class))).thenReturn(1);

        testPet.setName("大白");
        boolean result = petService.updatePet(1L, testPet);

        assertTrue(result);
        verify(petMapper, times(1)).selectById(1L);
        verify(petMapper, times(1)).updateById(testPet);
    }

    @Test
    void testUpdatePet_PetNotFound() {
        // 模拟宠物不存在
        when(petMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            petService.updatePet(1L, testPet);
        });

        assertEquals("宠物不存在", exception.getMessage());
    }

    @Test
    void testDeletePet_Success() {
        // 模拟宠物存在
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(petMapper.deleteById(1L)).thenReturn(1);

        boolean result = petService.deletePet(1L);

        assertTrue(result);
        verify(petMapper, times(1)).selectById(1L);
        verify(petMapper, times(1)).deleteById(1L);
    }

    @Test
    void testDeletePet_PetNotFound() {
        // 模拟宠物不存在
        when(petMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            petService.deletePet(1L);
        });

        assertEquals("宠物不存在", exception.getMessage());
    }

    @Test
    void testUpdateStatus_Success() {
        // 模拟宠物存在
        when(petMapper.selectById(1L)).thenReturn(testPet);
        when(petMapper.updateById(any(Pet.class))).thenReturn(1);

        boolean result = petService.updateStatus(1L, 2); // 从待领养改为领养中

        assertTrue(result);
        assertEquals(2, testPet.getStatus());
        verify(petMapper, times(1)).selectById(1L);
        verify(petMapper, times(1)).updateById(testPet);
    }

    @Test
    void testUpdateStatus_InvalidTransition() {
        // 模拟宠物存在
        when(petMapper.selectById(1L)).thenReturn(testPet);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            petService.updateStatus(1L, 0); // 从待领养改为待审核，无效的状态转换
        });

        assertEquals("待领养状态只能转换为领养中或已领养", exception.getMessage());
    }

    @Test
    void testUpdateStatus_PetNotFound() {
        // 模拟宠物不存在
        when(petMapper.selectById(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            petService.updateStatus(1L, 2);
        });

        assertEquals("宠物不存在", exception.getMessage());
    }

    @Test
    void testGetHotPets() {
        // 模拟分页查询
        Page<Pet> page = new Page<>(1, 10);
        IPage<Pet> expectedPage = new Page<>(1, 10);
        when(petMapper.selectPetsByStatus(page, 1)).thenReturn(expectedPage);

        IPage<Pet> result = petService.getHotPets(page);

        assertNotNull(result);
        verify(petMapper, times(1)).selectPetsByStatus(page, 1);
    }
}
