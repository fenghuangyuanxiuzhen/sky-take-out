package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    WorkspaceService workspaceService;
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

    @Override
    public SalesTop10ReportVO getSaleTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, MIN);
        LocalDateTime endTime = LocalDateTime.of(end, MAX);
        List<GoodsSalesDTO> goodsSalesDTOS = orderMapper.getSalesTop10(beginTime,endTime);
        List<String> name = goodsSalesDTOS.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> number = goodsSalesDTOS.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.join(name,","))
                .numberList(StringUtils.join(number,","))
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        //1.导入表
        InputStream in  = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        //2.查数据写数据
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(begin, MIN), LocalDateTime.of(end, MAX));
        try {
            //基于提供好的模板文件创建一个新的Excel表格对象
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheetAt(0);
            sheet.getRow(1).getCell(1).setCellValue(begin+"至"+end);
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());
            for(int i = 0 ; i < 30 ; i++){
                businessData = workspaceService.getBusinessData(LocalDateTime.of(begin, MIN), LocalDateTime.of(begin, MAX));
                begin = begin.plusDays(1);
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(begin.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());



            }
            //3.输出表 关闭资源
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            out.flush();
            out.close();
            excel.close();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }



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
