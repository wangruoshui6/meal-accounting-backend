package com.accounting.mapper;

import com.accounting.entity.Diary;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 日记Mapper接口
 */
@Mapper
public interface DiaryMapper extends BaseMapper<Diary> {
}
