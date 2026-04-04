package com.petadoption.petadoption.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petadoption.petadoption.entity.PetComment;

import java.util.List;

public interface CommentService extends IService<PetComment> {

    /**
     * 发表评论
     */
    PetComment addComment(Long userId, Long petId, String content, Long parentId);

    /**
     * 删除评论
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * 获取宠物的评论列表
     */
    List<PetComment> getPetComments(Long petId);
}
