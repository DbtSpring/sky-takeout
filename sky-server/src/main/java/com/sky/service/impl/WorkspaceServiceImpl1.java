package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.mapper.*;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkspaceServiceImpl1 implements WorkspaceService {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;


    @Override
    public BusinessDataVO businessData(LocalDateTime beginTime, LocalDateTime endTime) {
        Double turnover = 0.0;//营业额
        Integer validOrderCount = 0;//有效订单数
        Integer allOrderCount = 0; //所有订单数（为了计算完成率的，不用传给前端）
        Double orderCompletionRate = 0.0;//订单完成率
        Double unitPrice = 0.0;//平均客单价
        Integer newUsers = 0;//新增用户数

        Map map = new HashMap();
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        allOrderCount = ordersMapper.getOrdersByMap(map);
        map.put("status", Orders.COMPLETED);

        validOrderCount = ordersMapper.getOrdersByMap(map);
        if(allOrderCount != null && allOrderCount != 0 && validOrderCount != null){
            orderCompletionRate = validOrderCount * 1.0 / allOrderCount;
        }
        turnover = ordersMapper.getTurnoverByMap(map);

        newUsers = userMapper.getUserByMap(map);

        if(validOrderCount != null && validOrderCount != 0 && turnover != null){
            unitPrice = turnover/ validOrderCount;
        }

        //前端展示格式统一
        if(turnover == null){
            turnover = 0.0;
        }

        return BusinessDataVO.builder()
                .validOrderCount(validOrderCount)
                .newUsers(newUsers)
                .orderCompletionRate(orderCompletionRate)
                .turnover(turnover)
                .unitPrice(unitPrice)
                .build();

    }

    @Override
    public OrderOverViewVO overviewOrders() {
        //待接单数量
        Integer waitingOrders = 0;
        //待派送数量
        Integer deliveredOrders = 0;
        //已完成数量
        Integer completedOrders = 0;
        //已取消数量
        Integer cancelledOrders = 0;
        //全部订单
        Integer allOrders = 0;

        Map map = new HashMap();
        map.put("beginTime", LocalDateTime.now().with(LocalTime.MIN));
        allOrders = ordersMapper.getOrdersByMap(map);

        map.put("status", Orders.TO_BE_CONFIRMED);
        waitingOrders = ordersMapper.getOrdersByMap(map);
        map.put("status", Orders.CONFIRMED);
        deliveredOrders = ordersMapper.getOrdersByMap(map);
        map.put("status", Orders.COMPLETED);
        completedOrders = ordersMapper.getOrdersByMap(map);
        map.put("status", Orders.CANCELLED);
        cancelledOrders = ordersMapper.getOrdersByMap(map);

        return OrderOverViewVO.builder()
                .allOrders(allOrders)
                .completedOrders(completedOrders)
                .deliveredOrders(deliveredOrders)
                .waitingOrders(waitingOrders)
                .cancelledOrders(cancelledOrders)
                .build();
    }

    @Override
    public DishOverViewVO overviewDishes() {
        // 已启售数量
        Integer sold = 0;
        // 已停售数量
        Integer discontinued = 0;

        sold = dishMapper.countByStatus(StatusConstant.ENABLE);
        discontinued = dishMapper.countByStatus(StatusConstant.DISABLE);

        return new DishOverViewVO(sold, discontinued);
    }

    @Override
    public SetmealOverViewVO overviewSetmeals() {
        // 已启售数量
        Integer sold = 0;
        // 已停售数量
        Integer discontinued = 0;

        sold = setmealMapper.countByStatus(StatusConstant.ENABLE);
        discontinued = setmealMapper.countByStatus(StatusConstant.DISABLE);

        return new SetmealOverViewVO(sold, discontinued);
    }
}
