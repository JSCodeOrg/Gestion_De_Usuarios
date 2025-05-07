package com.JSCode.GestionUsuarios.security;

public enum RoleEnum {
    usuario(1), repartidor(2), administrador(3);

    private final int id;

    RoleEnum(int id) {
        this.id = id;
    }

    public static String getRoleNameById(int id) {
        for (RoleEnum role : RoleEnum.values()) {
            if (role.id == id) {
                return "ROLE_" + role.name();
            }
        }
        throw new IllegalArgumentException("Rol inválido: " + id);
    }
}
