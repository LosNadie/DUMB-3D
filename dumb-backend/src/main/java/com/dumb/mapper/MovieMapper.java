package com.dumb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dumb.entity.Movie;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MovieMapper extends BaseMapper<Movie> {
}
