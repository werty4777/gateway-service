package com.sulluscocha.gateway.configuration.oauth.dto;

public class User {
    private Profile profile;
    private Store Almacen;
    private Rol rol;

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Store getAlmacen() {
        return Almacen;
    }

    public void setAlmacen(Store almacen) {
        Almacen = almacen;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
