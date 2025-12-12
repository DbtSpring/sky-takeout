package com.sky.controller.user;

import com.sky.context.BaseContext1;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags="用户端-订单接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @ApiOperation("提交订单，并返回信息")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("提交订单，并返回信息,ordersDTO:{}",ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    @ApiOperation("订单支付")
    @PutMapping("/payment")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付，OrdersPaymentDTO:{}",ordersPaymentDTO);

        //以下是正常的逻辑（需要用企业注册小程序）
//        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
//        log.info("返回预支付单子，OrderPaymentVO:{}",orderPaymentVO);
//        return Result.success(orderPaymentVO);  //生成预支付单子，返回给小程序，小程序调起微信支付

        //以下是快速通过的假逻辑
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        return Result.success();
    }

    @ApiOperation("历史订单分页查询")
    @GetMapping("/historyOrders")
    public Result<PageResult> ordersPageQuery(Integer page, Integer pageSize, Integer status){
        log.info("历史订单分页查询,page:{},pageSize:{},status:{}",page,pageSize,status);
        OrdersPageQueryDTO ordersPageQueryDTO = OrdersPageQueryDTO.builder()
                .page(page)
                .pageSize(pageSize)
                .status(status)
                .userId(BaseContext1.getThreadLocal())
                .build();
        PageResult pageResult = orderService.ordersPageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation("根据id查询订单详情")
    public Result<OrderVO> getById(@PathVariable Long id){
        log.info("根据id查询订单详情,id:{}",id);
        OrderVO orderVO = orderService.getById(id);
        return Result.success(orderVO);
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单（把OrderDetailList添加到ShoppingCart）")
    public Result repetition(@PathVariable Long id){
        log.info("再来一单（把OrderDetailList添加到ShoppingCart）,id:{}",id);
        orderService.repetition(id);
        return Result.success();
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id) throws Exception{
        log.info("取消订单,id:{}",id);
        orderService.cancel(id);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    @ApiOperation("用户催单")
    public Result reminder(@PathVariable Long id){
        log.info("用户催单，用户id：{}",id);
        orderService.reminder(id);
        return Result.success();
    }



}
