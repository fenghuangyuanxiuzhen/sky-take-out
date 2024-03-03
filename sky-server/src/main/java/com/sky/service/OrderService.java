package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.apache.ibatis.javassist.tools.rmi.ObjectNotFoundException;

/**
 * @author 杨楠
 * @version 1.0
 */
public interface OrderService {
    /**
     * 提交订单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);
    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 查询历史订单
     * @param page
     * @param pageSize
     * @param status
     * @return
     */

    PageResult getHistoryOrders(int page, int pageSize, Integer status);

    /**
     * 查看订单详情
     * @param id
     * @return
     */

    OrderVO getDetails(Long id);

    /**
     * 取消订单
     * @param id
     */

    void cancelOrderById(Long id) throws Exception;

    /**
     * 再来一单
     * @param id
     */

    void repetition(Long id);

    /**
     * 订单按照状态搜索
     * @param ordersPageQueryDTO
     * @return
     */

    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 各个状态的订单数量统计
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 接单
     * @param ordersConfirmDTO
     */

    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒单
     * @param ordersRejectionDTO
     */

    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 取消订单
     * @param ordersCancelDTO
     */

    void cancel(OrdersCancelDTO ordersCancelDTO);

    /**
     * 派送订单
     * @param id
     */

    void delivery(Long id);

    /**
     * 完成订单
     * @param id
     */

    void complete(Long id);

    /**
     * 客户催单
     * @param id
     */

    void reminder(Long id);
}
