package org.kawanfw.sql.api.client.android.execute.query;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface OnGetResultSetsListener {
    void onGetResultSets(ResultSet[] rs, SQLException e);
}
