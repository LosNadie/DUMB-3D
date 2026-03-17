package com.dumb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dumb.entity.Anime;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnimeMapper extends BaseMapper<Anime> {
}
