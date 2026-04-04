package com.petadoption.petadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petadoption.petadoption.entity.PetComment;
import com.petadoption.petadoption.entity.Pet;
import com.petadoption.petadoption.exception.BusinessException;
import com.petadoption.petadoption.mapper.PetCommentMapper;
import com.petadoption.petadoption.response.ResponseCode;
import com.petadoption.petadoption.service.ActivityLogService;
import com.petadoption.petadoption.service.CommentService;
import com.petadoption.petadoption.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<PetCommentMapper, PetComment> implements CommentService {

    private final PetService petService;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional
    public PetComment addComment(Long userId, Long petId, String content, Long parentId) {
        PetComment comment = new PetComment();
        comment.setUserId(userId);
        comment.setPetId(petId);
        comment.setContent(content);
        comment.setParentId(parentId);
        save(comment);
        
        // 记录活动日志
        Pet pet = petService.getById(petId);
        String petName = pet != null ? pet.getName() : "宠物" + petId;
        activityLogService.logActivity(userId, "comment", "pet", petId, "评论了宠物：" + petName);
        
        return comment;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        PetComment comment = getById(commentId);
        if (comment == null) {
            throw new BusinessException(ResponseCode.COMMENT_NOT_FOUND);
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCode.COMMENT_PERMISSION_DENIED);
        }
        removeById(commentId);
    }

    @Override
    public List<PetComment> getPetComments(Long petId) {
        LambdaQueryWrapper<PetComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetComment::getPetId, petId)
               .eq(PetComment::getStatus, 1)
               .orderByDesc(PetComment::getCreateTime);
        return list(wrapper);
    }
}
