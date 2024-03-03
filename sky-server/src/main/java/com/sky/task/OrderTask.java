package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 杨楠
 * @version 1.0
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private  OrderMapper orderMapper;

    //@Scheduled(cron = "0/5 * * * * ?")//test
    @Scheduled(cron = "0 * * * * ?")//每分钟执行一次
    public void processOrderTimeout() {
        log.info("处理超时订单：{}",LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.getByStatusAndCreateTimeTl(Orders.PENDING_PAYMENT, time);
        if (ordersList != null && ordersList.size() > 0) {
            for(Orders orders : ordersList){
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("订单超时，自动取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }

    }
    @Scheduled(cron = "0 0 1 * * ?")//每天一点执行一次
    public void processOrderDeliveryInProgress(){
        log.info("处理前一天正在配送的订单：{}",LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.getByStatusAndCreateTimeTl(Orders.DELIVERY_IN_PROGRESS, time);
        if (ordersList != null && ordersList.size() > 0) {
            for(Orders orders : ordersList){
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
