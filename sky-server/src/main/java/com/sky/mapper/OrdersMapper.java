package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrdersMapper {

    void insert(Orders orders);

    void updateByNumberAndUserId(Orders orders);

    Page<Orders> ordersPageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    void updateById(Orders updateOrders);

    @Select("select count(id) from orders where status=#{status}")
    Integer countByStatus(Integer status);


    @Select("select * from orders where status=#{status} and order_time < #{orderTime}")
    List<Orders> selectByStatusAndOrderTimeLt(Integer status, LocalDateTime orderTime);


    Double getTurnoverByMap(Map map);

    Integer getOrdersByMap(Map map);

}
