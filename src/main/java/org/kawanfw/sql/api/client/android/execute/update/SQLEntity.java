package org.kawanfw.sql.api.client.android.execute.update;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by Reuben John on 3/9/2016.
 */
public interface SQLEntity {
    String getEntityName();

    String[] getAttributeNames();

    int onPrepareStatement(PreparedStatement preparedStatement, int i) throws SQLException;
}
