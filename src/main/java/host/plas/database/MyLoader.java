package host.plas.database;

import host.plas.StreamlineUtilities;
import host.plas.essentials.users.UtilitiesUser;
import lombok.Getter;
import lombok.Setter;
import net.streamline.api.database.modules.DBKeeper;
import net.streamline.api.loading.Loader;

public class MyLoader extends Loader<UtilitiesUser> {
    private static MyLoader instance;

    public static MyLoader getInstance() {
        if (instance == null) instance = new MyLoader();

        return instance;
    }

    @Override
    public DBKeeper<UtilitiesUser> getKeeper() {
        return StreamlineUtilities.getKeeper();
    }

    @Override
    public UtilitiesUser getConsole() {
        return null;
    }

    @Override
    public void fireLoadEvents(UtilitiesUser user) {

    }

    @Override
    public UtilitiesUser instantiate(String s) {
        return new UtilitiesUser(s);
    }

    @Override
    public void fireCreateEvents(UtilitiesUser user) {

    }

    @Override
    public UtilitiesUser augment(UtilitiesUser user) {
        return user;
    }
}
