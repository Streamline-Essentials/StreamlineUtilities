package tv.quaint.configs.obj;

import lombok.Getter;
import lombok.Setter;

public class PermissionGroup {
    @Getter @Setter
    private String identifier;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String permission;

    public PermissionGroup(String identifier, String name, String permission) {
        setIdentifier(identifier);
        setName(name);
        setPermission(permission);
    }
}
