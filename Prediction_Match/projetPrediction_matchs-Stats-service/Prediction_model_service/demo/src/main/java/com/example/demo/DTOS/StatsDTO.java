package com.example.demo.DTOS;


import lombok.Data;

@Data
public class StatsDTO {
    private String teamName;
    private int goalsScored;
    private int goalsConceded;
    private String result;
    private boolean isHome;
}