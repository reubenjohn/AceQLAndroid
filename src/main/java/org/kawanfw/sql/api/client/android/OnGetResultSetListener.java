package org.kawanfw.sql.api.client.android;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface OnGetResultSetListener {
    void onGetResultSets(ResultSet[] rs, SQLException e);
}
