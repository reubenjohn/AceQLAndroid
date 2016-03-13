package org.kawanfw.sql.api.client.android.execute.query;


import java.sql.SQLException;
import java.util.List;

public interface OnQueriesComplete<T> {
    void onQueryComplete(List<List<T>> list, SQLException e1);
}
