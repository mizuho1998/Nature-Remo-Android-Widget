package com.example.mizuho.natureremowidget;

public class SettingsInfo {

    String mode;
    String temp;
    String dir;
    String vol;
    String power;

    SettingsInfo(String mode, String temp, String dir, String vol, String power) {
        this.mode  = mode;
        this.temp  = temp;
        this.dir   = dir;
        this.vol   = vol;
        this.power = power;
    }

    SettingsInfo() {
        this.mode  = "";
        this.temp  = "";
        this.dir   = "";
        this.vol   = "";
        this.power = "";
    }

}