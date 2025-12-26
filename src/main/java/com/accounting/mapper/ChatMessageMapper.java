package com.accounting.mapper;

import com.accounting.entity.ChatMessageEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天消息 Mapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessageEntity> {
    
    /**
     * 获取用户最近的聊天记录
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 聊天记录列表
     */
    @Select("SELECT * FROM chat_messages WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<ChatMessageEntity> getRecentMessages(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    /**
     * 删除用户的所有聊天记录
     * 
     * @param userId 用户ID
     * @return 删除的记录数
     */
    @Delete("DELETE FROM chat_messages WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}

