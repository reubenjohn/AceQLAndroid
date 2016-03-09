package org.kawanfw.sql.api.client.android;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by Reuben John on 3/9/2016.
 */
public interface OnInsertListener<T> {
    void onInsertRow(PreparedStatement preparedStatement, T item);

    void onResult(SQLException e);
}
