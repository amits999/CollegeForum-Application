package com.amitsharma.action.collegeforum;

public class QuestionModel {
    String name;
    String profile_image;
    String comment;
    String date;
    String time;

    public QuestionModel(String name, String profile_image, String comment, String date, String time) {
        this.name = name;
        this.profile_image = profile_image;
        this.comment = comment;
        this.date = date;
        this.time = time;
    }

    public QuestionModel() {
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
