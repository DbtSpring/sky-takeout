package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl1 implements ReportService {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO getTurnover(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        while(!begin.equals(end)){
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        dateList.add(end);

        List<Double> turnoverList = new ArrayList<>();
        for(LocalDate date: dateList){
            LocalDateTime beginOfDay = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("beginTime", beginOfDay);
            map.put("endTime", endOfDay);
            map.put("status", Orders.COMPLETED);
            Double turnover = ordersMapper.getTurnoverByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        String dateListStr = listToStr(dateList);
        String turnoverListStr = listToStr(turnoverList);

        return TurnoverReportVO.builder()
                .dateList(dateListStr)
                .turnoverList(turnoverListStr)
                .build();

    }


    @Override
    public UserReportVO getUser(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        while(!begin.equals(end)){
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        dateList.add(end);


        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        for(LocalDate date: dateList){
            LocalDateTime beginOfDay = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
            Integer totalUser = countUser(null, endOfDay);
            Integer newUser = countUser(beginOfDay, endOfDay);
            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }
        System.out.println("totalUserList:" + totalUserList);
        System.out.println("newUserList:" + newUserList);

        return new UserReportVO(
                listToStr(dateList),
                listToStr(totalUserList),
                listToStr(newUserList)
        );

    }

    @Override
    public OrderReportVO getOrders(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        while(!begin.equals(end)){
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        dateList.add(end);

        //每日订单数，以逗号分隔，例如：260,210,215
        List<Integer> orderCountList = new ArrayList<>();
        //每日有效订单数，以逗号分隔，例如：20,21,10
        List<Integer> validOrderCountList = new ArrayList<>();

        //订单总数
        Integer totalOrderCount = 0;
        //有效订单总数
        Integer validOrderCount = 0;
        //订单完成率
        Double orderCompletionRate = 1.0;

        for(LocalDate date: dateList){
            LocalDateTime beginOfDay = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("beginTime", beginOfDay);
            map.put("endTime", endOfDay);
            Integer orderCount = ordersMapper.getOrdersByMap(map);
            orderCountList.add(orderCount);
            totalOrderCount += orderCount;

            map.put("status", Orders.COMPLETED);
            Integer validOrderCount1 = ordersMapper.getOrdersByMap(map);
            validOrderCountList.add(validOrderCount1);
            validOrderCount += validOrderCount1;
        }
        orderCompletionRate = 1.0 * validOrderCount/totalOrderCount;
        return OrderReportVO.builder()
                .dateList(listToStr(dateList))
                .orderCountList(listToStr(orderCountList))
                .validOrderCountList(listToStr(validOrderCountList))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();

    }

    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        Map map = new HashMap<>();
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        map.put("status", Orders.COMPLETED);
        List<GoodsSalesDTO> list = orderDetailMapper.getTop10ByMap(map);

        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();
        for(GoodsSalesDTO g: list){
            nameList.add(g.getName());
            numberList.add(g.getNumber());
        }
        return new SalesTop10ReportVO(listToStr(nameList), listToStr(numberList));

    }

    /**
     * list转字符串（中间用，分隔)
     * @param list
     * @return
     */
    private String listToStr(List list){
        if(list == null || list.isEmpty()){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<list.size(); i++){
            sb.append(list.get(i));
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * 根据时间段来统计用户数量
     * @param begin
     * @param end
     * @return
     */
    private Integer countUser(LocalDateTime begin, LocalDateTime end){
        Map<String, LocalDateTime> map = new HashMap<>();
        map.put("beginTime", begin);
        map.put("endTime", end);
        return userMapper.getUserByMap(map);
    }

    @Override
    public void export(HttpServletResponse response){
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        BusinessDataVO businessDataVO = workspaceService.businessData(
                LocalDateTime.of(begin, LocalTime.MIN),
                LocalDateTime.of(end, LocalTime.MAX));
        InputStream input = this.getClass().getClassLoader()
                .getResourceAsStream("template/reportExportTemplate.xlsx");
        try{
            XSSFWorkbook excel = new XSSFWorkbook(input);
            XSSFSheet sheet = excel.getSheetAt(0);
            //写入时间
            sheet.getRow(1).getCell(1).setCellValue("时间:" + begin + "至" +end);

            //写入概览数据
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //写入明细数据
            for(int i=0; i<30; i++){
                LocalDate thisDay = begin.plusDays(i);
                businessDataVO = workspaceService.businessData(
                        LocalDateTime.of(thisDay, LocalTime.MIN),
                        LocalDateTime.of(thisDay, LocalTime.MAX));
                row = sheet.getRow(7+i);
                row.getCell(1).setCellValue(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(thisDay));
                row.getCell(2).setCellValue(businessDataVO.getTurnover());
                row.getCell(3).setCellValue(businessDataVO.getValidOrderCount());
                row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessDataVO.getUnitPrice());
                row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            }
            //下载文件
            ServletOutputStream output = response.getOutputStream();
            excel.write(output);

            output.flush();
            output.close();
            excel.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
