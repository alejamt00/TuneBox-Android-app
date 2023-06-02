package com.osaki.tuneboxreborn;

/**
 * Clase que representa un mensaje o "tune" en la aplicación TuneBox.
 */
public class TuneMsg {

    private String authorId;
    private String publicName;
    private String userName;
    private String avatar;
    private String msg;
    private String date;
    private String musicTL;

    /**
     * Constructor parametrizado de la clase TuneMsg
     *
     * @param authorId
     * @param publicName
     * @param userName
     * @param avatar
     * @param msg
     * @param date
     * @param musicTL
     */
    public TuneMsg(String authorId, String publicName, String userName, String avatar, String msg, String date, String musicTL) {
        this.authorId = authorId;
        this.publicName = publicName;
        this.userName = userName;
        this.avatar = avatar;
        this.msg = msg;
        this.date = date;
        this.musicTL = musicTL;
    }


    /**
     * Métodos getter y setter de los datos del mensaje "tune"
     *
     */
    public String getMusicTL() {
        return musicTL;
    }

    public void setMusicTL(String musicTL) {
        this.musicTL = musicTL;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getPublicName() {
        return publicName;
    }

    public void setPublicName(String publicName) {
        this.publicName = publicName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}