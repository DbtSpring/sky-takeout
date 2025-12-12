package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext1;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.apache.http.client.utils.HttpClientUtils;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl1 implements OrderService {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    @Value("${sky.shop.address}")
    private String shopAddress;
    @Value("${sky.baidu.ak}")
    private String ak;


    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //1. 异常情况的处理（收货地址为空、超出配送范围、购物车为空）
        Long userId = BaseContext1.getThreadLocal();
        //异常情况的处理:收货地址为空

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new OrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //异常情况的处理:购物车为空
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext1.getThreadLocal())
                .build();
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        if (shoppingCarts == null || shoppingCarts.isEmpty()) {
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        checkOutOfRange(addressBook.getProvinceName()
                + addressBook.getCityName()
                + addressBook.getDistrictName()
                + addressBook.getDetail());

        //2. 添加orders表
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setUserId(userId);

        orders.setPhone(addressBook.getPhone());
        orders.setAddressBookId(addressBook.getId());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());

        orders.setTablewareNumber(ordersSubmitDTO.getTablewareNumber());
        orders.setTablewareStatus(ordersSubmitDTO.getTablewareStatus());
        orders.setOrderTime(LocalDateTime.now());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));

        ordersMapper.insert(orders);

        //3. 添加orders——detail表(order和shoppingCart的关系）
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart s : shoppingCarts) {
            OrderDetail orderDetail = OrderDetail.builder()
                    .orderId(orders.getId())
                    .build();
            BeanUtils.copyProperties(s, orderDetail);
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);


        //4.删除shopping carts
        shoppingCartMapper.cleanByUserId(userId);
        //5. 返回数据

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
        return orderSubmitVO;

    }

    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        Long userId = BaseContext1.getThreadLocal();
        User user = userMapper.getById(userId);
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(),
                new BigDecimal(0.01),
                "苍穹外卖支付",
                user.getOpenid()
        );
        if (jsonObject.getString("code") != null
                && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException(MessageConstant.ORDER_ALREADY_PAID);
        }
        OrderPaymentVO orderPaymentVO = jsonObject.toJavaObject(OrderPaymentVO.class);
        orderPaymentVO.setPackageStr(jsonObject.getString("package"));
        return orderPaymentVO;
    }

    @Override
    public void paySuccess(String outTradeNo) {
        Long userId = BaseContext1.getThreadLocal();
        Orders orders = Orders.builder()
                .number(outTradeNo)
                .userId(userId)
                .payStatus(Orders.PAID)
                .status(Orders.TO_BE_CONFIRMED)
                .checkoutTime(LocalDateTime.now())
                .build();
        ordersMapper.updateByNumberAndUserId(orders);

        //通过WebSocket，向客户端-管理端发送订单消息
        Map map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", orders.getId());
        map.put("content", "订单号" + outTradeNo);
        webSocketServer.sendToAllClient(JSON.toJSONString(map));

    }

    @Override
    public PageResult ordersPageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> pages = ordersMapper.ordersPageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();
        if (pages != null && pages.getPages() != 0) {
            for (Orders orders : pages.getResult()) {
                OrderVO orderVO = new OrderVO();
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());
                orderVO.setOrderDetailList(orderDetails);
                BeanUtils.copyProperties(orders, orderVO);
                list.add(orderVO);
            }
        }
        return new PageResult(pages.getTotal(), list);

        //Page对象包含的关键信息：
        //getTotal()：总记录数
        //getResult()：当前页的数据列表

        //getPages()：总页数
        //getPageNum()：当前页码
        //getPageSize()：每页记录数
    }

    @Override
    public OrderVO getById(Long id) {
        Orders orders = ordersMapper.getById(id);
        List<OrderDetail> list = orderDetailMapper.getByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(list);
        return orderVO;
    }

    @Override
    public void repetition(Long id) {
        Long userId = BaseContext1.getThreadLocal();
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        List<ShoppingCart> shoppingCartList = orderDetailList.stream()
                .map(o -> {
                    ShoppingCart shoppingCart = new ShoppingCart();
                    BeanUtils.copyProperties(o, shoppingCart, "id");
                    shoppingCart.setCreateTime(LocalDateTime.now());
                    shoppingCart.setUserId(userId);
                    return shoppingCart;
                })
                .collect(Collectors.toList());
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    @Override
    public void cancel(Long id) throws Exception {
        Orders orders = ordersMapper.getById(id);
        Orders updateOrders = new Orders();
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (orders.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if (orders.getStatus() == 2) {
            //退款(微信退款先注释掉）
//            weChatPayUtil.refund(orders.getNumber(),orders.getNumber() ,new BigDecimal(0.01), new BigDecimal(0.01));
            updateOrders.setPayStatus(Orders.REFUND);
        }
        updateOrders.setStatus(Orders.CANCELLED);
        updateOrders.setId(orders.getId());
        updateOrders.setCancelReason("用户取消订单");
        updateOrders.setCancelTime(LocalDateTime.now());
        ordersMapper.updateById(updateOrders);
    }

    @Override
    public OrderStatisticsVO getStatistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(ordersMapper.countByStatus(2));
        orderStatisticsVO.setConfirmed(ordersMapper.countByStatus(3));
        orderStatisticsVO.setDeliveryInProgress(ordersMapper.countByStatus(4));

        return orderStatisticsVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(3)
                .build();
        ordersMapper.updateById(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        Orders orders = ordersMapper.getById(ordersRejectionDTO.getId());
        if (orders.getStatus() != 2) {
            throw new OrderBusinessException(MessageConstant.WRONG_ORDER_STATUS);
        }
        Orders updateOrders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(6)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .build();
        ordersMapper.updateById(updateOrders);

//        退款：
//        weChatPayUtil.refund(orders.getNumber(), orders.getNumber(), new BigDecimal(0.01), new BigDecimal(0.01));

    }

    @Override
    public void cancelByAdmin(OrdersCancelDTO ordersCancelDTO) {
        Orders updateOrders = Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(6)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .build();
        ordersMapper.updateById(updateOrders);

        Orders orders = ordersMapper.getById(ordersCancelDTO.getId());
        if (orders.getStatus() != 1) {//已付款
            //        退款：
//        weChatPayUtil.refund(orders.getNumber(), orders.getNumber(), new BigDecimal(0.01), new BigDecimal(0.01));
        }

    }

    @Override
    public void delivery(Long id) {
        Orders orders = ordersMapper.getById(id);
        if (orders.getStatus() != 3) {
            throw new OrderBusinessException(MessageConstant.WRONG_ORDER_STATUS);
        }
        Orders updateOrders = Orders.builder()
                .id(id)
                .status(4)
                .build();
        ordersMapper.updateById(updateOrders);

    }

    @Override
    public void complete(Long id) {
        Orders orders = ordersMapper.getById(id);
        if (orders.getStatus() != 4) {
            throw new OrderBusinessException(MessageConstant.WRONG_ORDER_STATUS);
        }
        Orders updateOrders = Orders.builder()
                .id(id)
                .status(5)
                .build();
        ordersMapper.updateById(updateOrders);
    }

    @Override
    public void reminder(Long id) {
        Orders orders = ordersMapper.getById(id);
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Map map = new HashMap();
        map.put("type", 1);
        map.put("orderId", id);
        map.put("content", "订单号:"+orders.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    /**
     * 检查配送地址是否超出范围
     */
    private void checkOutOfRange(String address) {
        Map<String, String> map = new HashMap<>();
        map.put("address", shopAddress);
        map.put("ak", ak);
        map.put("output", "json");

        //获取商家坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);
        JSONObject shopCoordinateJo = JSON.parseObject(shopCoordinate);
        if (!shopCoordinateJo.getString("status").equals("0")) {
            throw new OrderBusinessException(MessageConstant.GET_SHOP_COORDINATE_FAILED);
        }
        JSONObject shopLocationJo = shopCoordinateJo.getJSONObject("result").getJSONObject("location");
        String shopLatitude = shopLocationJo.getString("lat");
        String shopLongitude = shopLocationJo.getString("lng");
        String shopLocation = shopLatitude + "," + shopLongitude;


        //获取用户地址坐标
        map.put("address", address);
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);
        JSONObject userCoordinateJo = JSON.parseObject(userCoordinate);
        if (!shopCoordinateJo.getString("status").equals("0")) {
            throw new OrderBusinessException(MessageConstant.GET_USER_COORDINATE_FAILED);
        }
        JSONObject userLocationJo = userCoordinateJo.getJSONObject("result").getJSONObject("location");
        String userLatitude = userLocationJo.getString("lat");
        String userLongitude = userLocationJo.getString("lng");
        String userLocation = userLatitude + "," + userLongitude;


        //解析路径
        map.put("origin", shopLocation);
        map.put("destination", userLocation);
        map.put("steps_info", "0");
        map.put("riding_type", "1");

        String routes = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/riding", map);
        JSONObject routesJo = JSON.parseObject(routes);
        if (!routesJo.getString("status").equals("0")) {
            throw new OrderBusinessException(MessageConstant.PARSE_PATH_FAILED);
        }
        JSONArray routesJa = routesJo.getJSONObject("result").getJSONArray("routes");
        Integer distance = (Integer) ((JSONObject) routesJa.get(0)).get("distance");
        if (distance > 5000) {
            throw new OrderBusinessException(MessageConstant.USER_OUT_OF_RANGE);
        }

    }

}

