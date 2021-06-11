package com.demo;

import com.poiji.annotation.ExcelCellName;
import com.poiji.annotation.ExcelRow;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class CompanyReport {
    @ExcelRow
    private int row;
    @ExcelCellName("Last check")
    private Date lastCheckDate;
    @ExcelCellName("Code")
    private String code;
    @ExcelCellName("Name")
    private String name;
    @ExcelCellName("Sector")
    private String sector;
    @ExcelCellName("Industry")
    private String industry;
    @ExcelCellName("Status")
    private String status;
    @ExcelCellName("P/E")
    private String pe;
    @ExcelCellName("5 year grow")
    private String fiveYearGrow;
    @ExcelCellName("High stable grow")
    private String highStableGrow;
    @ExcelCellName("Down from 2020")
    private String downFrom2020;
    @ExcelCellName("High volatile")
    private String highVolatile;
}
