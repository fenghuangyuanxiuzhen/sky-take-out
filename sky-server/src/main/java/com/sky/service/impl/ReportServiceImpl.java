package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.LocalTime.MAX;
import static java.time.LocalTime.MIN;

/**
 * @author 杨楠
 * @version 1.0
 */
@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    UserMapper userMapper;
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //1封装日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //2封装营业额通过map查询
        List<Double> turnoverList = new ArrayList<>();

        for (LocalDate localDate:dateList){
            LocalDateTime beginTime = LocalDateTime.of(localDate, MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, MAX);
            Map map = new HashMap(){{
                put("begin",beginTime);
                put("end",endTime);
                put("status", Orders.COMPLETED);
            }};
             Double turnover= orderMapper.sumByMap(map);
             turnover = turnover == null ? 0.0 : turnover;
             turnoverList.add(turnover);
        }
        //2封装对象然后返回

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        //1封装日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //统计总的用户
        List<Integer> totalUsers = new ArrayList<>();
        //统计当天的新增用户
        List<Integer> newUsers = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, MAX);
            HashMap map = new HashMap<>();
            map.put("end",endTime);
            Integer totalUser =  userMapper.getCountUserByMap(map);
            totalUsers.add(totalUser);
            map.put("begin",beginTime);
            Integer newUser = userMapper.getCountUserByMap(map);
            newUsers.add(newUser);
        }

        return UserReportVO
                .builder().dateList(StringUtils.join(dateList,","))
                .totalUserList(StringUtils.join(totalUsers,","))
                .newUserList(StringUtils.join(newUsers,","))
                .build();
    }

    @Override
    public OrderReportVO OrderStatistics(LocalDate begin, LocalDate end) {
        //1封装日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Integer> countOrderList = new ArrayList<>();
        List<Integer> countValidOrderList = new ArrayList<>();

        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, MAX);
            Integer countOrder = getOrdersByMap(beginTime,endTime,null);
            Integer countValidOrder = getOrdersByMap(beginTime,endTime,Orders.COMPLETED);
            countOrderList.add(countOrder);
            countValidOrderList.add(countValidOrder);
        }
        Integer totalOrderCount = countOrderList.stream().reduce(Integer::sum).get();
        Integer validOrderCount = countValidOrderList.stream().reduce(Integer::sum).get();
        Double orderCompletedRate = 0.0;
        if(totalOrderCount != null){
            orderCompletedRate = validOrderCount.doubleValue() / totalOrderCount;

        }

        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(countOrderList,","))
                .validOrderCountList(StringUtils.join(countValidOrderList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletedRate)
                .build();
    }
    private Integer getOrdersByMap(LocalDateTime begin,LocalDateTime end ,Integer status){
        Map map = new HashMap();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status",status);
        Integer count = orderMapper.countOrderByMap(map);
        return count;
    }
}
