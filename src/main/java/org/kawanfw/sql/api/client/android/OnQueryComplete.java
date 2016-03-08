package org.kawanfw.sql.api.client.android;


import java.sql.SQLException;
import java.util.List;

public interface OnQueryComplete<T> {
    void onQueryComplete(List<T> list, SQLException e1);
}
