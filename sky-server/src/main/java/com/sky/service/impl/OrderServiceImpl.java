package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.constant.WebSocketConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
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
import org.apache.ibatis.javassist.tools.rmi.ObjectNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 杨楠
 * @version 1.0
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    AddressBookMapper addressBookMapper;
    @Autowired
    ShoppingCartMapper shoppingCartMapper;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
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
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //1.判断地址 和 购物车是否空
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        Long currentId = BaseContext.getCurrentId();
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        if(addressBook == null){
            throw  new  AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //检查用户收获地址是否超过配送范围
        checkOutOfRange(addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail());
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(currentId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list == null || list.size() == 0){
            throw  new  ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);

        }
        //2.创建orders对象拷贝数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(currentId);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        //3.插入orders 返回id和 order_detail
        orderMapper.insert(orders);

        List<OrderDetail> orderDetails = new ArrayList();
        for(ShoppingCart sc:list){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(sc,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.batchInsert(orderDetails);

        //4.清空购物车
        shoppingCartMapper.deleteByUserId(currentId);
        //5.返回OrderVO对象
        OrderSubmitVO orderSubmitVO = new OrderSubmitVO();
        orderSubmitVO.setOrderTime(orders.getOrderTime());
        orderSubmitVO.setOrderAmount(orders.getAmount());
        orderSubmitVO.setOrderNumber(orders.getNumber());
        orderSubmitVO.setId(orders.getId());

        return orderSubmitVO;

    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
        JSONObject jsonObject = new JSONObject();//由于个人微信支付接口未开通，所以直接跳过微信支付接口
        paySuccess(ordersPaymentDTO.getOrderNumber());

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
        // TODO: 2024/1/1 暂时跳过微信支付接口，直接修改订单状态的代码
        //通过webSocket向客户端推送接单消息
        Map map = new HashMap<>();
        map.put("type", WebSocketConstant.NEW_ORDER_TYPE);
        map.put("userId",orders.getId());
        map.put("content","订单号："+outTradeNo);
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    @Override
    public PageResult getHistoryOrders(int pageNum, int pageSize, Integer status) {
        PageHelper.startPage(pageNum,pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setStatus(status);
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());

        //1.开启一个分页查询把这个 userId 的订单都查出来  利用status进行一个动态查询
         Page<Orders> pages = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> list = new ArrayList();

        // 查询出订单明细，并封装入OrderVO进行响应
        if (pages != null && pages.getTotal() > 0) {
            for (Orders orders : pages) {
                Long orderId = orders.getId();// 订单id

                // 查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                list.add(orderVO);
            }
        }
        return new PageResult(pages.getTotal(), list);
    }

    @Override
    public OrderVO getDetails(Long id) {
        Orders orders = orderMapper.getById(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Override
    public void cancelOrderById(Long id) throws Exception {
        Orders ordersDB = orderMapper.getById(id);
        if(ordersDB == null){
            throw new ObjectNotFoundException(MessageConstant.ORDER_NOT_FOUND);
        }
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if(ordersDB.getStatus() > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        //如果有商户这里需要退款
        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);

    }

    @Override
    public void repetition(Long id) {
        //1.把订单细节里头的信息在放入购物车中
        Long userId = BaseContext.getCurrentId();
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        //将orderDetailList转换为shoppingCartList
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x->{
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(x,shoppingCart,"id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        shoppingCartMapper.BatchInsert(shoppingCartList);
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //1.启动分页查询
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        //2.将orders转化为orderVO
        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(),orderVOList);
    }

    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(orderMapper.countStatus(Orders.TO_BE_CONFIRMED));
        orderStatisticsVO.setConfirmed(orderMapper.countStatus(Orders.CONFIRMED));
        orderStatisticsVO.setDeliveryInProgress(orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS));
        return orderStatisticsVO;
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        List<Orders> ordersList = page.getResult();
        List<OrderVO> orderVOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders order : ordersList) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                String orderDishesStr = getOrderDishesStr(order);
                orderVO.setOrderDishes(orderDishesStr);
                orderVOList.add(orderVO);
            }

        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息的字符串
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders){
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        List<String> orderDishList = orderDetailList.stream().map(x->{
        String orderDish = x.getName()+"*"+x.getNumber()+";";
        return orderDish;

        }).collect(Collectors.toList());
        //将订单对应的所有菜品信息拼接到 一起
        return String.join("",orderDishList);

    }
    /**
     * 接单
     *
     * @param ordersConfirmDTO
     */
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();

        orderMapper.update(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //1只有订单处于待接单才可以拒单
        Long id = ordersRejectionDTO.getId();
        Orders ordersDB = orderMapper.getById(id);

        if (ordersDB == null ||!ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED) ){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //拒单需要退款这里我们没有真的支付所以注释掉
        //支付状态
//        Integer payStatus = ordersDB.getPayStatus();
//        if (payStatus == Orders.PAID) {
//            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//            log.info("申请退款：{}", refund);
//        }



        // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .cancelReason(ordersRejectionDTO.getRejectionReason())
                .build();
        orderMapper.update(orders);

    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Long id = ordersCancelDTO.getId();

//        // 根据id查询订单
//        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());
//
//        //支付状态
//        Integer payStatus = ordersDB.getPayStatus();
//        if (payStatus == 1) {
//            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//            log.info("申请退款：{}", refund);
//        }



        Orders  orders =  new  Orders();
        orders.setId(id);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为3
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    @Override
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    @Override
    public void reminder(Long id) {
        Orders orderDB = orderMapper.getById(id);
        if(orderDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

          Map map = new HashMap(){{
              put("type",WebSocketConstant.REMINDER_ORDER_TYPE);
              put("orderId",orderDB.getId());
              put("content","订单号："+orderDB.getNumber());
          }};
        webSocketServer.sendToAllClient(JSON.toJSONString(map));

    }

    /**
     * 检查客户的收货地址是否超出配送范围
     * @param address
     */
    private void checkOutOfRange(String address) {
        Map map = new HashMap();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("ak",ak);

        //获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }

        //数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //店铺经纬度坐标
        String shopLngLat = lat + "," + lng;

        map.put("address",address);
        //获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("收货地址解析失败");
        }

        //数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        //用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        map.put("origin",shopLngLat);
        map.put("destination",userLngLat);
        map.put("steps_info","0");

        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

        jsonObject = JSON.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }

        //数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if(distance > 5000){
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }

}
