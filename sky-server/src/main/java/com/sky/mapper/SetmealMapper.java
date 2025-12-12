package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.anno.AutoFill;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SetmealMapper {


    Page<SetmealVO> setmealPageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    @AutoFill(OperationType.INSERT)
    void add(Setmeal setmeal);

    @Select("select status from setmeal where id = #{id}")
    Integer getStatusById(Long id);

    @Delete("delete from setmeal where id = #{id}")
    void deleteById(Long id);

    @Select("select * from setmeal where id = #{id}")
    SetmealVO getById(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);


    void setStatusByIds(Integer status, List<Long> ids);

    List<Setmeal> getList(Setmeal setmeal);

    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd " +
            "left outer join dish d " +
            "on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishesBySetmealId(Long setmealId);

    @Select("select count(id) from setmeal where status = #{status}")
    Integer countByStatus(Integer status);
}
