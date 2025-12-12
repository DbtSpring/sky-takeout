package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

import java.time.LocalDateTime;

public interface WorkspaceService {


    BusinessDataVO businessData(LocalDateTime beginTime, LocalDateTime endTime);

    OrderOverViewVO overviewOrders();

    DishOverViewVO overviewDishes();

    SetmealOverViewVO overviewSetmeals();
}
