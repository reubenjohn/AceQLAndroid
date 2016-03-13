package org.kawanfw.sql.api.client.android.execute.query;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface OnGetResultSetListener {
    void onQueryComplete(ResultSet[] rs, SQLException e);
}
