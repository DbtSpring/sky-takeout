package com.sky.task;


import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderTask {
    @Autowired
    private OrdersMapper ordersMapper;

    @Scheduled(cron = "0 * * * * *")
    public void processTimeOutOrder(){
        log.info("处理超时未付款订单：{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> list = ordersMapper.selectByStatusAndOrderTimeLt(Orders.PENDING_PAYMENT, time);
        if(list != null && !list.isEmpty()){//存在超时未支付订单
            list.forEach(orders -> {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason(MessageConstant.CUSTOMER_PAY_TIME_OUT);
                ordersMapper.updateById(orders);
            });
        }
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void processDeliveryOrder(){
        log.info("处理一直未点送达的订单:{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusHours(-1);
        List<Orders> list = ordersMapper.selectByStatusAndOrderTimeLt(Orders.DELIVERY_IN_PROGRESS, time);
        if(list != null && !list.isEmpty()){
            list.forEach(orders ->{
                orders.setStatus(Orders.COMPLETED);
                ordersMapper.updateById(orders);
            });
        }
    }




}
